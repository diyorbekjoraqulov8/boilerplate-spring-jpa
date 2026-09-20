package uz.app.projectv1.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import uz.app.projectv1.auth.dto.LoginRequest;
import uz.app.projectv1.auth.dto.MeResponse;
import uz.app.projectv1.auth.dto.RegisterRequest;
import uz.app.projectv1.security.CookieService;
import uz.app.projectv1.security.JwtService;
import uz.app.projectv1.security.SessionService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final SessionService sessionService;
    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        this.authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        var result = this.authService.login(request, httpRequest);
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieService.auth(result.token(),
                result.ttl()).toString()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletResponse response
    ) {
        this.authService.logout(jwt.getClaimAsString("sid"));
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.clearAuth().toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.me(jwt);
    }
}
