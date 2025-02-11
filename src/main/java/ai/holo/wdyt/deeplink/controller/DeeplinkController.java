package ai.holo.wdyt.deeplink.controller;

import ai.holo.wdyt.deeplink.model.dto.ReferralLinkDto;
import ai.holo.wdyt.deeplink.service.DeeplinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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

    // This endpoint will be called by the browser when the client device doesn't have the app installed
    // We'll save client's fingerprint and redirect them to the app store
    @GetMapping("/record/{nonce}")
    public RedirectView saveUserFingerprint(@PathVariable String nonce, HttpServletRequest request) {
        deeplinkService.saveUserFingerprint(nonce, request);
        // Redirect user to the App Store (iOS or Play Store)
        String appStoreUrl = "https://apps.apple.com/app/6738694684"; // Replace with your actual app store URL
        return new RedirectView(appStoreUrl);
    }

    @PostMapping("/search/referral")
    public void searchForReferral(HttpServletRequest request) {
        deeplinkService.searchForReferral(request);
    }
}
