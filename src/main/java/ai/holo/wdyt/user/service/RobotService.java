package ai.holo.wdyt.user.service;

import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.user.model.dto.ChangeRobotNameDto;
import ai.holo.wdyt.user.model.dto.CreateRobotRequestPayload;
import ai.holo.wdyt.user.model.dto.RobotDto;
import ai.holo.wdyt.user.model.dto.RobotResponsePayload;
import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.Robot;
import ai.holo.wdyt.user.repository.RobotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
@Slf4j
public class RobotService {

    private final String createRobotUrl;
    private final RobotRepository robotRepository;
    private final String createRobotSecret;

    public RobotService(@Value("${integrations.create-robot.url}") String createRobotUrl,
                        Map<String, String> secretProperties,
                        RobotRepository robotRepository) {
        this.createRobotUrl = createRobotUrl;
        this.robotRepository = robotRepository;
        this.createRobotSecret = secretProperties.get("createRobotSecret");
    }

    public Robot createRobot(Gender gender) {
        RobotResponsePayload responsePayload = callCreateRobotApi(gender);
        LocalDateTime birthday = Instant.ofEpochMilli(responsePayload.birthday())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        Robot robot = new Robot(responsePayload.id(), responsePayload.name(), gender, birthday,
                responsePayload.headImageUrl(), responsePayload.avatarUrl());
        return robotRepository.save(robot);
    }

    private RobotResponsePayload callCreateRobotApi(Gender gender) {
        WebClient webClient = WebClient.builder()
                .baseUrl(createRobotUrl)
                .build();

        return webClient
                .post()
                .header("createRobotEndpointToken", createRobotSecret)
                .bodyValue(new CreateRobotRequestPayload(gender))
                .retrieve()
                .bodyToMono(RobotResponsePayload.class)
                .block();
    }

    @Transactional
    public void deleteRobot(Long id) {
        Robot robot = robotRepository.findById(id).orElseThrow(NotFoundException::new);
        Long robotSourceId = robot.getRobot_source_id();
        robotRepository.deleteById(id);
        try {
            deleteRobotOnSecond(robotSourceId);
        } catch (Exception e) {
            log.error("Failed to delete robot on second service for robot ID: {}", robotSourceId, e);
        }
    }

    private void deleteRobotOnSecond(Long robotSourceId) {
        String url = createRobotUrl + "/mark-as-unused/" + robotSourceId;
        WebClient webClient = WebClient.builder()
                .baseUrl(url)
                .build();

        webClient
                .post()
                .header("createRobotEndpointToken", createRobotSecret)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Transactional
    public RobotDto updateRobotName(ChangeRobotNameDto changeRobotNameDto) {
        Robot robot = robotRepository.findById(changeRobotNameDto.robotId()).orElseThrow(NotFoundException::new);
        robot.setName(changeRobotNameDto.newName());
        robotRepository.save(robot);
        Long robotSourceId = robot.getRobot_source_id();
        try {
            updateRobotNameOnSecond(robotSourceId, changeRobotNameDto.newName());
        } catch (Exception e) {
            log.error("Failed to update robot name on second service for robot ID: {}", robotSourceId, e);
        }
        return new RobotDto(robot);
    }

    private void updateRobotNameOnSecond(Long robotSourceId, String newName) {
        String url = createRobotUrl + "/update-name";
        WebClient webClient = WebClient.builder()
                .baseUrl(url)
                .build();
        webClient
                .post()
                .header("createRobotEndpointToken", createRobotSecret)
                .bodyValue(new ChangeRobotNameDto(robotSourceId, newName))
                .retrieve()
                .bodyToMono(RobotResponsePayload.class)
                .block();
    }
}
