package ai.holo.wdyt.deeplink.controller;

import ai.holo.wdyt.deeplink.model.dto.ReferralLinkDto;
import ai.holo.wdyt.deeplink.model.dto.SaveUserFingerprintDto;
import ai.holo.wdyt.deeplink.service.DeeplinkService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/referral")
public class DeeplinkController {
    private final DeeplinkService deeplinkService;

    public DeeplinkController(DeeplinkService deeplinkService) {
        this.deeplinkService = deeplinkService;
    }

    @GetMapping("/generate-link")
    // This endpoint will be used to generate a referral link
    public ReferralLinkDto generateReferralLink() {
        return deeplinkService.generateReferralLink();
    }

    @PostMapping("/use/{nonce}")
    // This endpoint will be used to associate the users with the referral link
    // It will be called when the referee already has the app installed
    public void useReferral(@PathVariable String nonce) {
        deeplinkService.useReferral(nonce);
    }

    @PostMapping("/use/fingerprint/{fingerprint}")
    // This endpoint will be used to associate the users with the referral link
    // It will be called when the referee doesn't have the app installed
    public void useReferralWithFingerprint(@PathVariable String fingerprint) {
        deeplinkService.useReferralWithFingerprint(fingerprint);
    }

    @GetMapping("/use/{nonce}")
    // This endpoint will be an anonymous endpoint that will be used to redirect the user to the app store
    // but before redirecting, we'll try to record the user's fingerprint to associate it with the referral link
    // There is an js code in the returned html that will call the /record/{nonce} endpoint with the fingerprint
    // retrieved from the user's device
    public ResponseEntity<Resource> getDeepLinkForReferral(@PathVariable String nonce) throws IOException {
        String content = deeplinkService.getDeeplinkReferralRedirectHtml(nonce);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
    }

    @PostMapping("/record/{nonce}")
    // This endpoint will be called by the js code in the html returned by the /use/{nonce} endpoint
    // It will record the user's fingerprint to associate it with the referral link
    public void recordReferralWithFingerprint(@PathVariable String nonce, @RequestBody SaveUserFingerprintDto dto) {
        deeplinkService.saveUserFingerprint(nonce, dto.fingerprint());
    }
}
