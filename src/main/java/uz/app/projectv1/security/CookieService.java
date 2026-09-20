package uz.app.projectv1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CookieService {

    public static final String AUTH_COOKIE = "AUTH-TOKEN";

    private final CookieProperties props;

    public ResponseCookie auth(String token, Duration ttl) {
        return ResponseCookie.from(AUTH_COOKIE, token)
                .httpOnly(true)
                .secure(props.secure())
                .sameSite(props.sameSite())
                .path("/")
                .maxAge(ttl)
                .build();
    }

    public ResponseCookie clearAuth() {
        return ResponseCookie.from(AUTH_COOKIE, "")
                .httpOnly(true)
                .secure(props.secure())
                .sameSite(props.sameSite())
                .path("/")
                .maxAge(0)
                .build();
    }
}
