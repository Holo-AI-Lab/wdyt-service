package ai.holo.wdyt.user.service;

import ai.holo.wdyt.askai.service.AiFeedbackDeleteService;
import ai.holo.wdyt.subscription.repository.AppleTransactionRepository;
import ai.holo.wdyt.subscription.repository.UserCreditRepository;
import ai.holo.wdyt.subscription.repository.UserSubscriptionRepository;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.repository.UserFeedbackRepository;
import ai.holo.wdyt.user.repository.UserRepository;
import ai.holo.wdyt.wardrobe.service.WardrobeService;
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
    private final UserCreditRepository userCreditRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final AppleTransactionRepository appleTransactionRepository;
    private final WardrobeService wardrobeService;

    public UserDeleteAccountService(UserRepository userRepository,
                                    RobotService robotService,
                                    UserFeedbackRepository userFeedbackRepository,
                                    AiFeedbackDeleteService aiFeedbackDeleteService,
                                    FriendService friendService,
                                    UserService userService,
                                    UserCreditRepository userCreditRepository,
                                    UserSubscriptionRepository userSubscriptionRepository,
                                    AppleTransactionRepository appleTransactionRepository,
                                    WardrobeService wardrobeService) {
        this.userRepository = userRepository;
        this.robotService = robotService;
        this.userFeedbackRepository = userFeedbackRepository;
        this.aiFeedbackDeleteService = aiFeedbackDeleteService;
        this.friendService = friendService;
        this.userService = userService;
        this.userCreditRepository = userCreditRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.appleTransactionRepository = appleTransactionRepository;
        this.wardrobeService = wardrobeService;
    }

    public void deleteAccount() {
        User user = userService.getUser();
        wardrobeService.deleteWardrobeForUser(user.getId());
        robotService.deleteRobot(user.getRobot().getId());
        friendService.deleteUser(user.getId());
        userCreditRepository.deleteAllByUserId(user.getId());
        appleTransactionRepository.deleteAllByUserId(user.getId());
        userSubscriptionRepository.deleteAllByUserId(user.getId());
        userFeedbackRepository.deleteAllByUserId(user.getId());
        aiFeedbackDeleteService.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }
}
