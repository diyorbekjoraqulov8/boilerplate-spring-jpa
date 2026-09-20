package uz.app.projectv1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionValidator implements OAuth2TokenValidator<Jwt> {

    private final SessionService sessionService;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String sid = jwt.getClaimAsString("sid");

        if (sid == null) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "sid claim yo'q", null));
        }

        UUID sessionId;
        try {
            sessionId = UUID.fromString(sid);
        } catch (IllegalArgumentException e) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "sid formati noto'g'ri", null));
        }

        if (!sessionService.isActive(sessionId)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Sessiya bekor qilingan", null));
        }
        return OAuth2TokenValidatorResult.success();
    }
}