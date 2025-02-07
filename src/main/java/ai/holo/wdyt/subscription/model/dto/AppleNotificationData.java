package ai.holo.wdyt.subscription.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record AppleNotificationData(@JsonProperty("environment") String environment,
                                    @JsonProperty("productId") String productId,
                                    @JsonProperty("transactionId") String transactionId,
                                    @JsonProperty("originalTransactionId") String originalTransactionId,
                                    @JsonProperty("purchaseDate") Instant purchaseDate,
                                    @JsonProperty("expirationDate") Instant expirationDate,
                                    @JsonProperty("autoRenewStatus") Boolean autoRenewStatus,
                                    @JsonProperty("autoRenewProductId") String autoRenewProductId,
                                    @JsonProperty("subscriptionGroupId") String subscriptionGroupId,
                                    @JsonProperty("signedTransactionInfo") String signedTransactionInfo,
                                    @JsonProperty("signedRenewalInfo") String signedRenewalInfo) {
}
