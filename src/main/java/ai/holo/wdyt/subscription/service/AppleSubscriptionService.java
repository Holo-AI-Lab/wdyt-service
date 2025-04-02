package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.common.event.service.EventPublisher;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.subscription.model.dto.*;
import ai.holo.wdyt.subscription.model.entity.AppleNotification;
import ai.holo.wdyt.subscription.model.entity.AppleTransaction;
import ai.holo.wdyt.subscription.model.entity.SubscriptionPlan;
import ai.holo.wdyt.subscription.model.entity.UserSubscription;
import ai.holo.wdyt.subscription.model.event.AppleNotificationReceivedEvent;
import ai.holo.wdyt.subscription.model.event.AppleTransactionCreatedEvent;
import ai.holo.wdyt.subscription.repository.AppleNotificationRepository;
import ai.holo.wdyt.subscription.repository.AppleTransactionRepository;
import ai.holo.wdyt.subscription.repository.UserSubscriptionRepository;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AppleSubscriptionService {
    private final AppleJwsVerificationService appleJwsVerificationService;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserService userService;
    private final AppleTransactionRepository appleTransactionRepository;
    private final AppleNotificationRepository appleNotificationRepository;
    private final EventPublisher eventPublisher;

    public AppleSubscriptionService(
            AppleJwsVerificationService appleJwsVerificationService,
            UserSubscriptionRepository userSubscriptionRepository,
            UserService userService,
            AppleTransactionRepository appleTransactionRepository,
            AppleNotificationRepository appleNotificationRepository,
            EventPublisher eventPublisher) {
        this.appleJwsVerificationService = appleJwsVerificationService;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userService = userService;
        this.appleTransactionRepository = appleTransactionRepository;
        this.appleNotificationRepository = appleNotificationRepository;
        this.eventPublisher = eventPublisher;
    }
    @Transactional
    public UserSubscriptionDto initiateSubscription() {
        User user = userService.getUser();
        Optional<UserSubscription> subscription = userSubscriptionRepository.findByUserId(user.getId());
        if (subscription.isPresent()) {
            return new UserSubscriptionDto(subscription.get());
        }
        String appAccountToken = generateUniqueAppAccountToken();
        UserSubscription userSubscription = new UserSubscription(user.getId(), appAccountToken);
        UserSubscription savedSubscription = userSubscriptionRepository.save(userSubscription);
        return new UserSubscriptionDto(savedSubscription);
    }

    public UserSubscriptionDto getUserSubscription() {
        User user = userService.getUser();
        UserSubscription subscription = userSubscriptionRepository.findByUserId(user.getId()).orElseThrow(NotFoundException::new);
        return new UserSubscriptionDto(subscription);
    }

    @Transactional
    public void createTransaction(UserTransactionDto userTransactionDto, boolean callbackFromApple) {
        Optional<UserSubscription> subscription = userSubscriptionRepository.findByAppAccountToken(userTransactionDto.appAccountToken());
        if (subscription.isEmpty()) {
            throw new BadRequestException("Subscription not found for App Account Token: " + userTransactionDto.appAccountToken());
        }
        UserSubscription userSubscription = subscription.get();
        SubscriptionPlan subscriptionPlan = SubscriptionPlan.getPlanByProductId(userTransactionDto.productId())
                .orElseThrow(() -> new BadRequestException("Invalid product id: " + userTransactionDto.productId()));
        LocalDateTime purchaseDate = Instant.ofEpochMilli(userTransactionDto.purchaseDate())
                .atZone(ZoneId.systemDefault()) // Use system default time zone
                .toLocalDateTime();

        String callSource = callbackFromApple ? "Callback from Apple" : "Call from Ios App";
        if (appleTransactionRepository.existsByTransactionId(userTransactionDto.transactionId())) {
            log.warn("Transaction already exists for transaction Id: {} , call-source is {} - more details : {}", userTransactionDto.transactionId(), callSource , userTransactionDto);
            return;
        }
        AppleTransaction appleTransaction = new AppleTransaction(userSubscription.getUserId(), subscriptionPlan, userTransactionDto.originalTransactionId(),
                userTransactionDto.transactionId(), purchaseDate);
        appleTransactionRepository.save(appleTransaction);
        eventPublisher.publishEvent(new AppleTransactionCreatedEvent(appleTransaction.getId()));
        log.info("Transaction created for transaction Id: {} and call-source is {}", userTransactionDto.transactionId(), callSource);
    }

    private String generateUniqueAppAccountToken() {
        return UUID.randomUUID().toString();
    }

    public void processNotification(String jwsToken) {
        AppleNotificationPayload notification = appleJwsVerificationService.verifyAndDecodeNotification(jwsToken);
        if (notification == null) {
            log.error("Invalid JWS or parse error");
            throw new RuntimeException("Invalid JWS or parse error");
        }
        AppleNotificationData data = notification.data();
        if (data == null) {
            log.error("Data is null in AppleNotificationPayload");
            throw new RuntimeException("Data is null in AppleNotificationPayload");
        }

        String notificationUUID = notification.notificationUUID();
        if(appleNotificationRepository.existsByNotificationId(notificationUUID)) {
            log.warn("Notification already exists for notificationUUID: {} - Notification Details: {}", notificationUUID, notification);
            return;
        }
        AppleNotification appleNotification = new AppleNotification(notificationUUID, notification.notificationType(),
                notification.subtype(), notification.version(), notification.data().signedTransactionInfo());
        AppleNotification savedNotification = appleNotificationRepository.save(appleNotification);
        eventPublisher.publishEvent(new AppleNotificationReceivedEvent(savedNotification.getId()));
    }

    @Transactional
    public void updateTransactionPending(String appAccountToken, boolean transactionPendingStatus) {
        if (appAccountToken == null) {
            User user = userService.getUser();
            appAccountToken = userSubscriptionRepository.findByUserId(user.getId()).orElseThrow(NotFoundException::new).getAppAccountToken();
            log.info("updateTransactionPending method called by endpoint, using App Account Token from User: {}", appAccountToken);
        }
        Optional<UserSubscription> subscription = userSubscriptionRepository.findByAppAccountToken(appAccountToken);
        if (subscription.isEmpty()) {
            log.error("Subscription not found for App Account Token: {}", appAccountToken);
            return;
        }
        UserSubscription userSubscription = subscription.get();
        userSubscription.setTransactionPending(transactionPendingStatus);
        userSubscriptionRepository.save(userSubscription);
        new UserSubscriptionDto(userSubscription);
    }

    @Transactional
    public UserSubscriptionDto updateTransactionPending(TransactionPendingDTO pendingDTO) {
        User user = userService.getUser();
        UserSubscription subscription = userSubscriptionRepository.findByUserId(user.getId()).orElseThrow(NotFoundException::new);
        subscription.setTransactionPending(pendingDTO.pending());
        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);
        return new UserSubscriptionDto(savedSubscription);
    }
}