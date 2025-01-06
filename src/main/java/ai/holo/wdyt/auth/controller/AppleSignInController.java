package ai.holo.wdyt.auth.controller;

import ai.holo.wdyt.auth.model.AppleAuthenticationDto;
import ai.holo.wdyt.auth.model.JwtTokenDto;
import ai.holo.wdyt.auth.service.AppleSignInService;
import ai.holo.wdyt.auth.service.JwtService;
import ai.holo.wdyt.common.exception.AuthenticationException;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/apple")
@Slf4j
public class AppleSignInController {
    private final AppleSignInService appleSignInService;
    private final UserService userService;
    private final JwtService jwtService;

    public AppleSignInController(AppleSignInService appleSignInService,
                                 UserService userService, JwtService jwtService) {
        this.appleSignInService = appleSignInService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public JwtTokenDto signInWithApple(@RequestBody @Valid AppleAuthenticationDto authenticationDto) {
        try {
            // Authenticate with Apple
            //Claims claims = appleSignInService.authenticateWithApple(authenticationDto.authorizationCode());

            String email = "belis@mail.com";
            String name = "beli";
            Claims claims = Jwts.claims()
                    .setSubject("dddddsdsdsds") // Subject is typically the user’s email or unique identifier
                    .add("name", name) // Add custom claim for name
                    .add("email", email).build(); // Add custom claim for email

            User user = userService.createOrRetrieveUser(claims.get("email", String.class),
                    claims.get("name", String.class), claims.getSubject());
            return new JwtTokenDto(jwtService.generateJwtToken(user));

        } catch (Exception e) {
            log.error("Authentication failed.", e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }
}
