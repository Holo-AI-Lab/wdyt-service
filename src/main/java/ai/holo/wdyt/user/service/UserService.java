package ai.holo.wdyt.user.service;

import ai.holo.wdyt.askai.service.AiFeedbackDeleteService;
import ai.holo.wdyt.askai.service.AiFeedbackService;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.user.model.dto.AddUserFeedbackDto;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.model.dto.UserFeedbackDto;
import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.Robot;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.model.entity.UserFeedback;
import ai.holo.wdyt.user.repository.UserFeedbackRepository;
import ai.holo.wdyt.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RobotService robotService;
    private final UserFeedbackRepository userFeedbackRepository;
    private final AiFeedbackDeleteService aiFeedbackDeleteService;

    public UserService(UserRepository userRepository,
                       RobotService robotService,
                       UserFeedbackRepository userFeedbackRepository,
                       AiFeedbackDeleteService aiFeedbackDeleteService) {
        this.userRepository = userRepository;
        this.robotService = robotService;
        this.userFeedbackRepository = userFeedbackRepository;
        this.aiFeedbackDeleteService = aiFeedbackDeleteService;
    }

    public User createOrRetrieveUser(String email, String name, String appleId) {
        // Check if the user already exists (by email or unique identifier)
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // User already exists, return JWT for the existing user
            return existingUser.get();
        } else {
            // User does not exist, create a new user
            User newUser = new User(email, name, appleId);
            User savedUser = userRepository.save(newUser);
            // We're generating gender randomly for now..
            Robot robot = robotService.createRobot(savedUser.getId(), getGenderRandomly());
            savedUser.setRobot(robot);
            return savedUser;
        }
    }

    private Gender getGenderRandomly() {
        Gender[] genders = Gender.values();
        Random random = new Random();
        return genders[random.nextInt(genders.length)];
    }

    public UserDto getUserInfo() {
        User user = getUser();
        return new UserDto(user);
    }

    public User getUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public UserFeedbackDto addFeedback(AddUserFeedbackDto addUserFeedbackDto) {
        UserFeedback userFeedback = new UserFeedback(getUser().getId(), addUserFeedbackDto.feedback());
        userFeedbackRepository.save(userFeedback);
        return new UserFeedbackDto(userFeedback.getUserId(), userFeedback.getFeedback());
    }

    @Transactional
    public void deleteAccount() {
        User user = getUser();
        robotService.deleteRobot(user.getRobot().getId());
        userFeedbackRepository.deleteAllByUserId(user.getId());
        aiFeedbackDeleteService.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }
}
