package ai.holo.wdyt.deeplink.controller;

import ai.holo.wdyt.deeplink.model.dto.ReferralLinkDto;
import ai.holo.wdyt.deeplink.model.dto.SaveUserFingerprintDto;
import ai.holo.wdyt.deeplink.service.DeeplinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;

@RestController
@RequestMapping("/api/v1/referral")
public class DeeplinkController {
    private final DeeplinkService deeplinkService;

    public DeeplinkController(DeeplinkService deeplinkService) {
        this.deeplinkService = deeplinkService;
    }

    @GetMapping("/generate-link")
    public ReferralLinkDto generateReferralLink() {
        return deeplinkService.generateReferralLink();
    }

    @PostMapping("/use/{nonce}")
    public void useReferral(@PathVariable String nonce) {
        deeplinkService.useReferral(nonce);
    }

    @PostMapping("/use/fingerprint/{fingerprint}")
    public void useReferralWithFingerprint(@PathVariable String fingerprint) {
        deeplinkService.useReferralWithFingerprint(fingerprint);
    }

    @GetMapping("/redirect/referral/{nonce}")
    public ResponseEntity<Resource> getDeepLinkForReferral(@PathVariable String nonce) throws IOException {
        String content = deeplinkService.getDeeplinkReferralRedirectHtml(nonce);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
    }

    @PostMapping("/record/{nonce}")
    public void recordReferralWithFingerprint(@PathVariable String nonce, @RequestBody SaveUserFingerprintDto dto) {
        deeplinkService.saveUserFingerprint(nonce, dto.fingerprint());
    }
}
