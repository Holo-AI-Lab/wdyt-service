package ai.holo.wdyt.deeplink.service;

import ai.holo.wdyt.common.event.service.EventPublisher;
import ai.holo.wdyt.deeplink.model.dto.ReferralLinkDto;
import ai.holo.wdyt.deeplink.model.entity.ClientFingerprint;
import ai.holo.wdyt.deeplink.model.entity.ReferralLink;
import ai.holo.wdyt.deeplink.model.event.ReferralUsedEvent;
import ai.holo.wdyt.deeplink.repository.ClientFingerprintRepository;
import ai.holo.wdyt.deeplink.repository.ReferralLinkRepository;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DeeplinkService {
    private final UserService userService;
    private final ReferralLinkRepository referralLinkRepository;
    private final ClientFingerprintRepository clientFingerprintRepository;
    private final EventPublisher eventPublisher;
    private final String baseUrl;

    public DeeplinkService(UserService userService, ReferralLinkRepository referralLinkRepository,
                           ClientFingerprintRepository clientFingerprintRepository, EventPublisher eventPublisher,
                           @Value("${application.base.url}") String baseUrl) {
        this.userService = userService;
        this.referralLinkRepository = referralLinkRepository;
        this.clientFingerprintRepository = clientFingerprintRepository;
        this.eventPublisher = eventPublisher;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public ReferralLinkDto generateReferralLink() {
        User user = userService.getUser();
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 16); // 16 chars, adjust as needed
        ReferralLink referralLink = new ReferralLink(user.getId(), nonce);
        ReferralLink savedReferralLink = referralLinkRepository.save(referralLink);
        ZonedDateTime expireDate = savedReferralLink.getExpirationDate().atZone(ZoneId.systemDefault());
        return new ReferralLinkDto(savedReferralLink.getNonce(), expireDate);
    }

    @Transactional
    public void saveUserFingerprint(String nonce, String fingerprint) {
        ClientFingerprint clientFingerprint = new ClientFingerprint(nonce, fingerprint);
        clientFingerprintRepository.save(clientFingerprint);
    }

    private String generateUserFingerprint(HttpServletRequest request) {
        String ip = request.getRemoteAddr(); // Client IP
        String userAgent = request.getHeader("User-Agent"); // Device & browser info
        String acceptLanguage = request.getHeader("Accept-Language"); // User language settings

        return String.format("%s|%s|%s", ip, userAgent, acceptLanguage); // Simple fingerprint
    }

    @Transactional
    public void useReferral(String nonce) {
        Optional<ReferralLink> referralLink = referralLinkRepository.findByNonce(nonce);
        if (referralLink.isPresent()) {
            ReferralLink referral = referralLink.get();
            if (referral.isUsed()) {
                // Referral link has already been used
                log.warn("Referral link with nonce {} has already been used", nonce);
                return;
            }

            if (referral.isExpired()) {
                // Referral link has expired
                log.warn("Referral link with nonce {} has expired", nonce);
                return;
            }
            // Use the referral link
            referral.setUsed(true);
            referral.setRedeemedAt(LocalDateTime.now());

            Long invitedUserId = userService.getUser().getId();
            eventPublisher.publishEvent(new ReferralUsedEvent(referral.getUserId(), invitedUserId));
            referralLinkRepository.save(referral);
        }
    }

    public String getDeeplinkReferralRedirectHtml(String nonce) throws IOException {
        Path path = Paths.get("src/main/resources/html/referral.html");
        String content;
        content = Files.readString(path);
        content = content.replace("{{NONCE}}", nonce);
        return content.replace("{{BASE_URL}}", baseUrl);
    }

    @Transactional
    public void useReferralWithFingerprint(String fingerprint) {
        Optional<ClientFingerprint> clientFingerprint = clientFingerprintRepository.findByUserFingerprint(fingerprint);
        if (clientFingerprint.isPresent()) {
            ClientFingerprint fingerprintEntity = clientFingerprint.get();
            if (fingerprintEntity.isExpired()) {
                // Fingerprint has expired
                log.warn("Client fingerprint with nonce {} has expired", fingerprintEntity.getNonce());
                return;
            }
            useReferral(fingerprintEntity.getNonce());
        }
    }
}
