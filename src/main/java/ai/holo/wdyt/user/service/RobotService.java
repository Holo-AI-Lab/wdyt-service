package ai.holo.wdyt.user.service;

import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.user.model.dto.ChangeRobotNameDto;
import ai.holo.wdyt.user.model.dto.CreateRobotRequestPayload;
import ai.holo.wdyt.user.model.dto.RobotResponsePayload;
import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.Robot;
import ai.holo.wdyt.user.repository.RobotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
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
        robotRepository.deleteById(id);
        deleteRobotOnSecond(id);
    }

    private void deleteRobotOnSecond(Long id) {
        String url = createRobotUrl + "/delete/" + id;
        WebClient webClient = WebClient.builder()
                .baseUrl(url)
                .build();

        webClient
                .delete()
                .header("createRobotEndpointToken", createRobotSecret)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Transactional
    public RobotResponsePayload updateRobotName(ChangeRobotNameDto changeRobotNameDto) {
        Robot robot = robotRepository.findById(changeRobotNameDto.robotId()).orElseThrow(NotFoundException::new);
        robot.setName(changeRobotNameDto.newName());
        robotRepository.save(robot);
        return updateRobotNameOnSecond(changeRobotNameDto);
    }

    private RobotResponsePayload updateRobotNameOnSecond(ChangeRobotNameDto changeRobotNameDto) {
        String url = createRobotUrl + "/update-name";
        WebClient webClient = WebClient.builder()
                .baseUrl(url)
                .build();

        return webClient
                .post()
                .header("createRobotEndpointToken", createRobotSecret)
                .bodyValue(changeRobotNameDto)
                .retrieve()
                .bodyToMono(RobotResponsePayload.class)
                .block();
    }
}
