package ai.holo.wdyt.deeplink.model.dto;

import java.time.ZonedDateTime;

public record ReferralLinkDto(String nonce, ZonedDateTime expirationDate) {
}
