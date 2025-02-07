package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.subscription.model.dto.AppleNotificationData;
import ai.holo.wdyt.subscription.model.entity.Transaction;
import ai.holo.wdyt.subscription.repository.SubscriptionRepository;
import ai.holo.wdyt.subscription.model.dto.AppleNotificationPayload;
import ai.holo.wdyt.subscription.model.entity.Subscription;
import ai.holo.wdyt.subscription.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
@Service
public class AppleSubscriptionService {
    private final AppleJwsVerificationService appleJwsVerificationService;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;

    public AppleSubscriptionService(
            AppleJwsVerificationService appleJwsVerificationService,
            SubscriptionRepository subscriptionRepository,
            TransactionRepository transactionRepository) {
        this.appleJwsVerificationService = appleJwsVerificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.transactionRepository = transactionRepository;
    }

    public void processNotification(String jwsToken) {
        AppleNotificationPayload payload = appleJwsVerificationService.verifyAndDecode(jwsToken);
        if (payload == null) {
            throw new RuntimeException("Invalid JWS or parse error");
        }
        String notificationType = payload.notificationType();
        AppleNotificationData data = payload.data();
        if (data == null) {
            throw new RuntimeException("Data is null in AppleNotificationPayload");
        }

        String productId = data.productId();
        String transactionId = data.transactionId();
        String originalTransactionId = data.originalTransactionId();
        Instant purchaseDate = data.purchaseDate();
        Instant expirationDate = data.expirationDate();

        switch (notificationType) {
            case "SUBSCRIBED" -> handleSubscribed(productId, transactionId, originalTransactionId, purchaseDate, expirationDate);
//            case "DID_RENEW" -> handleDidRenew(productId, transactionId, originalTransactionId, purchaseDate, expirationDate);
//            case "EXPIRED", "DID_EXPIRE" -> handleExpired(productId, transactionId, originalTransactionId);
//            case "CANCELED" -> handleCanceled(productId, transactionId, originalTransactionId);
//            case "DID_CHANGE_RENEWAL_STATUS" -> handleRenewalStatusChange(data);
            default -> System.out.println("Unhandled notificationType: " + notificationType);
        }
    }

    private void handleSubscribed(String productId, String transactionId, String originalTransactionId,
                                  Instant purchaseDate, Instant expirationDate) {
        Optional<Subscription> existingSubOpt = subscriptionRepository.findByOriginalTransactionId(originalTransactionId);

        Subscription sub;
        if (existingSubOpt.isPresent()) {
            sub = existingSubOpt.get();
            sub.setProductId(productId);
            sub.setLastTransactionId(transactionId);
            sub.setStartDate(purchaseDate);
            sub.setExpireDate(expirationDate);
            sub.setIsActive(true);
            sub.setCredits(getCreditsForProduct(productId));
            subscriptionRepository.save(sub);
        } else {
            sub = new Subscription();
            // Normalde userId'yi iOS tarafında anlamanız gerekir, burada sabit 1L gibi örnek
            sub.setUserId(1L);
            sub.setOriginalTransactionId(originalTransactionId);
            sub.setLastTransactionId(transactionId);
            sub.setProductId(productId);
            sub.setStartDate(purchaseDate);
            sub.setExpireDate(expirationDate);
            sub.setIsActive(true);
            sub.setCredits(getCreditsForProduct(productId));
            subscriptionRepository.save(sub);
        }

        // Şimdi Transaction kaydı ekleyelim
        // "SUBSCRIBED" başlığıyla log tutmak istiyoruz
        Transaction txn = new Transaction();
        txn.setSubscription(sub);  // JPA ManyToOne, subscription_id foreign key
        txn.setTransactionId(transactionId);
        txn.setNotificationType("SUBSCRIBED");
        txn.setProductId(productId);
        txn.setPurchaseDate(purchaseDate);
        txn.setExpirationDate(expirationDate);
        transactionRepository.save(txn);
    }

//    private void handleDidRenew(String productId, String transactionId, String originalTransactionId,
//                                Instant purchaseDate, Instant expirationDate) {
//        Optional<Subscription> existingSubOpt = subscriptionRepository.findByOriginalTransactionId(originalTransactionId);
//        Subscription sub;
//        if (existingSubOpt.isPresent()) {
//            sub = existingSubOpt.get();
//            sub.setProductId(productId);
//            sub.setLastTransactionId(transactionId);
//            sub.setStartDate(purchaseDate);
//            sub.setExpireDate(expirationDate);
//            sub.setIsActive(true);
//            sub.setCredits(sub.getCredits() + getCreditsForProduct(productId));
//            subscriptionRepository.save(sub);
//        } else {
//            sub = handleSubscribedInternal(productId, transactionId, originalTransactionId, purchaseDate, expirationDate);
//        }
//        // Add Transaction
//        Transaction txn = new Transaction();
//        txn.setSubscription(sub);
//        txn.setTransactionId(transactionId);
//        txn.setNotificationType("DID_RENEW");
//        txn.setProductId(productId);
//        txn.setPurchaseDate(purchaseDate);
//        txn.setExpirationDate(expirationDate);
//        transactionRepository.save(txn);
//    }

    private Subscription handleSubscribedInternal(String productId, String transactionId, String originalTransactionId,
                                                  Instant purchaseDate, Instant expirationDate) {
        Subscription sub = new Subscription();
        sub.setUserId(1L);
        sub.setOriginalTransactionId(originalTransactionId);
        sub.setLastTransactionId(transactionId);
        sub.setProductId(productId);
        sub.setStartDate(purchaseDate);
        sub.setExpireDate(expirationDate);
        sub.setIsActive(true);
        sub.setCredits(getCreditsForProduct(productId));
        return subscriptionRepository.save(sub);
    }

//    private void handleCanceled(String productId, String transactionId, String originalTransactionId) {
//        Optional<Subscription> existingSubOpt = subscriptionRepository.findByOriginalTransactionId(originalTransactionId);
//        if (existingSubOpt.isPresent()) {
//            Subscription sub = existingSubOpt.get();
//            sub.setIsActive(false);
//            subscriptionRepository.save(sub);
//            Transaction txn = new Transaction();
//            txn.setSubscription(sub);
//            txn.setTransactionId(transactionId);
//            txn.setNotificationType("CANCELED");
//            txn.setProductId(productId);
//            transactionRepository.save(txn);
//        }
//    }
//
//    private void handleExpired(String productId, String transactionId, String originalTransactionId) {
//        Optional<Subscription> existingSubOpt = subscriptionRepository.findByOriginalTransactionId(originalTransactionId);
//        if (existingSubOpt.isPresent()) {
//            Subscription sub = existingSubOpt.get();
//            sub.setIsActive(false);
//            subscriptionRepository.save(sub);
//            Transaction txn = new Transaction();
//            txn.setSubscription(sub);
//            txn.setTransactionId(transactionId);
//            txn.setNotificationType("EXPIRED");
//            txn.setProductId(productId);
//            transactionRepository.save(txn);
//        }
//    }
//
//    private void handleRenewalStatusChange(AppleNotificationData data) {
//        String originalTransactionId = data.originalTransactionId();
//        Optional<Subscription> existingSubOpt = subscriptionRepository.findByOriginalTransactionId(originalTransactionId);
//        if (existingSubOpt.isPresent()) {
//            Subscription sub = existingSubOpt.get();
//            sub.setProductId(data.productId());
//            sub.setLastTransactionId(data.transactionId());
//            sub.setExpireDate(data.expirationDate());
//            subscriptionRepository.save(sub);
//            Transaction txn = new Transaction();
//            txn.setSubscription(sub);
//            txn.setTransactionId(data.transactionId());
//            txn.setNotificationType("DID_CHANGE_RENEWAL_STATUS");
//            txn.setProductId(data.productId());
//            txn.setPurchaseDate(data.purchaseDate());
//            txn.setExpirationDate(data.expirationDate());
//            transactionRepository.save(txn);
//        }
//    }

    private int getCreditsForProduct(String productId) {
        return switch (productId) {
            case "1001" -> 25;
            case "1002" -> 100;
            case "1003" -> 1200;
            default -> 0;
        };
    }
}
