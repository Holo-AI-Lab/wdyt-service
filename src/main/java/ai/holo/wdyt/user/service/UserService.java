package ai.holo.wdyt.user.service;

import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.event.service.EventPublisher;
import ai.holo.wdyt.common.exception.AuthenticationException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.common.exception.ParameterValidationException;
import ai.holo.wdyt.common.exception.UsernameAlreadyExistingException;
import ai.holo.wdyt.common.notification.service.PushNotificationService;
import ai.holo.wdyt.user.model.dto.*;
import ai.holo.wdyt.user.model.entity.*;
import ai.holo.wdyt.user.model.event.NewUserRegisteredEvent;
import ai.holo.wdyt.user.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RobotService robotService;
    private final UserFeedbackRepository userFeedbackRepository;
    private final S3Service s3Service;
    private final StyleRepository styleRepository;
    private final String s3Endpoint;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final EventPublisher eventPublisher;
    private final PushNotificationService pushNotificationService;

    public UserService(UserRepository userRepository,
                       RobotService robotService,
                       @Value("${aws.s3.endpoint}") String s3Endpoint,
                       UserFeedbackRepository userFeedbackRepository,
                       S3Service s3Service,
                       StyleRepository styleRepository, FriendRequestRepository friendRequestRepository, FriendRepository friendRepository, EventPublisher eventPublisher, PushNotificationService pushNotificationService) {
        this.userRepository = userRepository;
        this.robotService = robotService;
        this.userFeedbackRepository = userFeedbackRepository;
        this.s3Service = s3Service;
        this.styleRepository = styleRepository;
        this.s3Endpoint = s3Endpoint;
        this.friendRequestRepository = friendRequestRepository;
        this.friendRepository = friendRepository;
        this.eventPublisher = eventPublisher;
        this.pushNotificationService = pushNotificationService;
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
            eventPublisher.publishEvent(new NewUserRegisteredEvent(newUser.getId()));
            // We're generating gender randomly for now..
            Robot robot = robotService.createRobot(savedUser.getId(), getGenderRandomly());
            savedUser.setRobot(robot);
            return savedUser;
        }
    }

    public String generateUniqueUsername(String email) {
        // Extract a base username
        String baseUsername = extractBaseUsername(email);

        // Ensure the base username meets the minimum length requirement
        if (baseUsername.length() < 6) {
            baseUsername = padToMinLength(baseUsername, 6);
        }

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
        sanitized = sanitized.length() > 15 ? sanitized.substring(0, 15) : sanitized;

        return sanitized;
    }

    private String padToMinLength(String username, int minLength) {
        StringBuilder padded = new StringBuilder(username);
        while (padded.length() < minLength) {
            padded.append("0"); // Pad with zeros to reach the minimum length
        }
        return padded.toString();
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
        return userRepository.findByEmail(email).orElseThrow(() ->
                new AuthenticationException(String.format("User with %s email is not found", email)));
    }

    @Transactional
    public UserFeedbackDto addFeedback(AddUserFeedbackDto addUserFeedbackDto) {
        UserFeedback userFeedback = new UserFeedback(getUser().getId(), addUserFeedbackDto.feedback());
        userFeedbackRepository.save(userFeedback);
        return new UserFeedbackDto(userFeedback.getUserId(), userFeedback.getFeedback());
    }

    @Transactional
    public UserDto uploadProfilePicture(byte[] image) {
        long currentTimeMillis = System.currentTimeMillis();
        User user = getUser();
        String path = saveProfileImageOnS3(new ByteArrayInputStream(image), user, currentTimeMillis);

        String profilePictureUrl = String.format("%s/%s", s3Endpoint, path);
        user.setProfilePicture(profilePictureUrl);
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

    public List<String> getStyles(String filter) {
        return styleRepository.searchByFreeText(filter).stream().map(Style::getName).toList();
    }

    @Transactional
    public UserDto changeName(ChangeNameDto changeNameDto) {
        User user = getUser();
        user.setName(changeNameDto.name());
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    @Transactional(readOnly = true)
    public Page<UserSearchDto> searchUsers(String userName, PageRequest page) {
        if (userName.length() < 3) {
            throw new ParameterValidationException("userName search parameter must be at least 3 characters long!");
        }
        Long userId = getUser().getId();
        Map<Long, Long> requestedIdMap = friendRequestRepository
                .findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(FriendRequest::getFriendId, FriendRequest::getId));

        Set<Long> friendIdSet = friendRepository
                .findAllByUserId(userId)
                .stream()
                .map(it -> it.getFriend().getId())
                .collect(Collectors.toSet());

        return userRepository.findByUsernameContainingIgnoreCaseAndIdNot(userName, userId, page)
                .map(user -> new UserSearchDto(
                        user,
                        friendIdSet.contains(user.getId()),
                        requestedIdMap.get(user.getId())
                ));
    }

    @Transactional
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public boolean isCurrentUserFriendWith(Long userId) {
        Long currentUserId = getUser().getId();
        return friendRepository.existsByUserIdAndFriendId(currentUserId, userId);
    }

    @Transactional
    public void logout() {
        updateDeviceToken(null);
    }

    @Transactional
    public void updateDeviceToken(String deviceToken) {
        User user = getUser();
        user.setDeviceToken(deviceToken);
        userRepository.save(user);
    }

    public void sendHelloWorldPushNotification() {
        pushNotificationService.sendPushNotification(getUser().getId(), "Hello, World!");
    }
}
