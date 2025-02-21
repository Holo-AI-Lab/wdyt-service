package ai.holo.wdyt.subscription.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record TransactionPendingDTO(
        boolean pendingFlag
) {}