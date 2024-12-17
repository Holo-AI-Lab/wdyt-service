package ai.holo.wdyt.user.service;

import ai.holo.wdyt.user.model.dto.CreateRobotRequestPayload;
import ai.holo.wdyt.user.model.dto.CreateRobotResponsePayload;
import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.Robot;
import ai.holo.wdyt.user.repository.RobotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

    public Robot createRobot(Long userId, Gender gender) {
        CreateRobotResponsePayload responsePayload = callCreateRobotApi(userId, gender);
        LocalDateTime birthday = Instant.ofEpochMilli(responsePayload.birthday())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        Robot robot = new Robot(responsePayload.name(), gender, birthday,
                responsePayload.headImageUrl(), responsePayload.avatarUrl());
        return robotRepository.save(robot);
    }

    private CreateRobotResponsePayload callCreateRobotApi(Long userId, Gender gender) {
        WebClient webClient = WebClient.builder()
                .baseUrl(createRobotUrl)
                .build();

        CreateRobotResponse response = webClient
                .post()
                .header(HttpHeaders.AUTHORIZATION, createRobotSecret)
                .bodyValue(new CreateRobotRequestPayload(gender.getGenderCode(), userId))
                .retrieve()
                .bodyToMono(CreateRobotResponse.class)
                .block();
        return response.data;
    }

    @Transactional
    public void deleteRobot(Long id) {
        robotRepository.deleteById(id);
    }

    public record CreateRobotResponse(boolean success, CreateRobotResponsePayload data){}
}
