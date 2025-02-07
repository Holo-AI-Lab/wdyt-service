package ai.holo.wdyt.subscription.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserTransactionDto(String appAccountToken,
                                 String originalTransactionId,
                                 String productId,
                                 Long purchaseDate,
                                 String transactionId) {
}
