package ai.holo.wdyt.auth.service;

import ai.holo.wdyt.common.event.service.SecurityContextAware;
import ai.holo.wdyt.common.exception.AuthenticationException;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationContext implements SecurityContextAware {
    private final UserRepository userRepository;

    public AuthenticationContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Long getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String email = authentication.getName();

        if (email == null) {
            return null;
        }
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new AuthenticationException(String.format("User with %s email is not found", email)));

        return user == null ? null : user.getId();
    }
}
