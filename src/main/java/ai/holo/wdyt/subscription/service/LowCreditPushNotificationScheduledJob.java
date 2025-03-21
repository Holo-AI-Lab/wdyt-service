package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.common.notification.model.NotificationType;
import ai.holo.wdyt.common.notification.repository.PushNotificationRepository;
import ai.holo.wdyt.common.notification.service.PushNotificationService;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@Slf4j
public class LowCreditPushNotificationScheduledJob {
    private final byte LOW_CREDIT_THRESHOLD = 2;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;
    private final PushNotificationRepository notificationRepository;

    public LowCreditPushNotificationScheduledJob(UserRepository userRepository, PushNotificationService pushNotificationService,
                                                 PushNotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.pushNotificationService = pushNotificationService;
        this.notificationRepository = notificationRepository;
    }

    // Run every 2 hours
    @Scheduled(cron = "0 0 */2 * * ?")
    @SchedulerLock(name = "Scheduler_sendLowCreditNotificationsLock", lockAtLeastFor = "PT5M", lockAtMostFor = "PT55M")
    public void sendLowCreditNotifications() {
        log.info("Starting low credit notification job at {}", LocalDateTime.now());
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Stream<User> lowCreditUsers = userRepository.findAllByCreditBalanceLessThanEqualAndDeviceTokenIsNotNull(LOW_CREDIT_THRESHOLD);

        AtomicInteger pushNotificationSentCount = new AtomicInteger();
        lowCreditUsers.filter(isUserLocalTimeBetween8And20())
                .filter(hasNotReceivedRecentLowCreditNotification(oneWeekAgo))
                .forEach(user -> {
                        sendPushNotification(user);
                        pushNotificationSentCount.getAndIncrement();
                });

        log.info("Finished low credit notification job at {}. Sent {} push notifications", LocalDateTime.now(), pushNotificationSentCount.get());
    }

    private Predicate<User> hasNotReceivedRecentLowCreditNotification(LocalDateTime oneWeekAgo) {
        return user -> !notificationRepository.existsByUserIdAndNotificationTypeAndCreatedAtAfter(
                user.getId(), NotificationType.LOW_CREDIT, oneWeekAgo);
    }

    private void sendPushNotification(User user) {
        pushNotificationService.sendPushNotification(user.getId(), "Low Credit",
                "You have low credit balance", NotificationType.LOW_CREDIT);
    }

    private Predicate<User> isUserLocalTimeBetween8And20() {
        return user -> getUsersLocalHour(user)
                .map(hour -> hour >= 8 && hour < 20)
                .orElse(false);
    }

    private Optional<Integer> getUsersLocalHour(User user) {
        String timezone = user.getTimezone();
        if (StringUtils.isEmpty(timezone)) {
            log.error("Timezone is null or empty for user {}", user.getId());
            return Optional.empty();
        }
        try {
            ZoneId userZone = ZoneId.of(user.getTimezone());
            int hour = ZonedDateTime.now(userZone).getHour();
            return Optional.of(hour);
        } catch (DateTimeException e) {
            log.error("Error parsing timezone for user {}: {}. Exception: {}", user.getId(), user.getTimezone(), e.getMessage());
            return Optional.empty();
        }
    }
}
