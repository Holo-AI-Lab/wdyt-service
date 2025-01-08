package ai.holo.wdyt.user.service;

import ai.holo.wdyt.askai.service.AiFeedbackDeleteService;
import ai.holo.wdyt.askai.service.AiFeedbackService;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.common.exception.UsernameAlreadyExistingException;
import ai.holo.wdyt.user.model.dto.*;
import ai.holo.wdyt.user.model.entity.*;
import ai.holo.wdyt.user.repository.StyleRepository;
import ai.holo.wdyt.user.repository.UserFeedbackRepository;
import ai.holo.wdyt.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RobotService robotService;
    private final UserFeedbackRepository userFeedbackRepository;
    private final AiFeedbackDeleteService aiFeedbackDeleteService;
    private final S3Service s3Service;
    private final StyleRepository styleRepository;

    public UserService(UserRepository userRepository,
                       RobotService robotService,
                       UserFeedbackRepository userFeedbackRepository,
                       AiFeedbackDeleteService aiFeedbackDeleteService, S3Service s3Service, StyleRepository styleRepository) {
        this.userRepository = userRepository;
        this.robotService = robotService;
        this.userFeedbackRepository = userFeedbackRepository;
        this.aiFeedbackDeleteService = aiFeedbackDeleteService;
        this.s3Service = s3Service;
        this.styleRepository = styleRepository;
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
            newUser.setUsername(generateUniqueUsername(email));
            User savedUser = userRepository.save(newUser);
            // We're generating gender randomly for now..
            Robot robot = robotService.createRobot(savedUser.getId(), getGenderRandomly());
            savedUser.setRobot(robot);
            return savedUser;
        }
    }

    public String generateUniqueUsername(String email) {
        // Extract a base username
        String baseUsername = extractBaseUsername(email);
        String uniqueUsername = baseUsername;

        // Check for uniqueness and append suffix if needed
        int suffix = 1;
        while (isUsernameTaken(uniqueUsername)) {
            uniqueUsername = baseUsername + suffix;
            suffix++;
        }

        return uniqueUsername;
    }

    private String extractBaseUsername(String email) {
        // Extract the part before "@" and sanitize it
        String localPart = email.split("@")[0];
        String sanitized = localPart.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        // Fallback for simulated/randomized emails
        if (sanitized.isEmpty() || sanitized.matches("\\d+")) {
            sanitized = "user" + System.currentTimeMillis();
        }

        // Limit the username length to 15 characters for readability
        return sanitized.length() > 15 ? sanitized.substring(0, 15) : sanitized;
    }

    private boolean isUsernameTaken(String username) {
        // Query the repository to check if the username exists
        return userRepository.existsByUsername(username);
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

    @Transactional
    public UserDto uploadProfilePicture(byte[] image) {
        long currentTimeMillis = System.currentTimeMillis();
        User user = getUser();
        String path = saveProfileImageOnS3(new ByteArrayInputStream(image), user, currentTimeMillis);

        user.setProfilePicture(path);
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    private String saveProfileImageOnS3(InputStream image, User user, long currentTimeMillis) {
        String path = String.format("%d/profile_%d.png", user.getId(),currentTimeMillis);
        s3Service.saveImage(image, path);
        return path;
    }

    @Transactional
    public UserDto changeUsername(ChangeUsernameDto changeUsernameDto) {
        boolean alreadyExisting = userRepository.existsByUsername(changeUsernameDto.username());
        if (alreadyExisting) {
            throw new UsernameAlreadyExistingException();
        }
        User user = getUser();
        user.setUsername(changeUsernameDto.username());
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    @Transactional
    public UserDto updateUserSelectedStyles(UpdateUserSelectedStyle updateUserSelectedStyle) {
        User user = getUser();
        user.setSelectedStyle(new UserSelectedStyle(updateUserSelectedStyle.styles()));
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    @Transactional
    public UserDto updateAdaptedStyleMode(UpdateAdaptedStyleModeDto updateAdaptedStyleModeDto) {
        User user = getUser();
        user.setStyleAdapted(updateAdaptedStyleModeDto.isStyleAdapted());
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    public List<String> getStyles() {
        return styleRepository.findAll().stream().map(Style::getName).toList();
    }
}
