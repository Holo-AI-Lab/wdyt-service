package ai.holo.wdyt.user.service;

import ai.holo.wdyt.askai.service.AiFeedbackDeleteService;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.repository.UserFeedbackRepository;
import ai.holo.wdyt.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserDeleteAccountService {

    private final UserRepository userRepository;
    private final RobotService robotService;
    private final UserFeedbackRepository userFeedbackRepository;
    private final AiFeedbackDeleteService aiFeedbackDeleteService;
    private final FriendService friendService;
    private final UserService userService;

    public UserDeleteAccountService(UserRepository userRepository,
                                    RobotService robotService,
                                    UserFeedbackRepository userFeedbackRepository,
                                    AiFeedbackDeleteService aiFeedbackDeleteService,
                                    FriendService friendService, UserService userService) {
        this.userRepository = userRepository;
        this.robotService = robotService;
        this.userFeedbackRepository = userFeedbackRepository;
        this.aiFeedbackDeleteService = aiFeedbackDeleteService;
        this.friendService = friendService;
        this.userService = userService;
    }

    public void deleteAccount() {
        User user = userService.getUser();
        robotService.deleteRobot(user.getRobot().getId());
        friendService.deleteUser(user.getId());
        userFeedbackRepository.deleteAllByUserId(user.getId());
        aiFeedbackDeleteService.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }
}
