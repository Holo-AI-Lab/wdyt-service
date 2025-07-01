package ai.holo.wdyt.user.controller;

import ai.holo.wdyt.user.model.dto.ChangeRobotNameDto;
import ai.holo.wdyt.user.model.dto.RobotResponsePayload;
import ai.holo.wdyt.user.service.RobotService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/robot")
public class RobotController {
    RobotService robotService;
    public RobotController(RobotService robotService) {
        this.robotService = robotService;
    }

    @PostMapping("/update-name")
    public RobotResponsePayload updateRobotName(@RequestBody ChangeRobotNameDto changeRobotNameDto) {
        return robotService.updateRobotName(changeRobotNameDto);
    }
}
