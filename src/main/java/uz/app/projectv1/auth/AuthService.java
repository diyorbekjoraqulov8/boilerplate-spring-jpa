package uz.app.projectv1.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.app.projectv1.auth.dto.LoginRequest;
import uz.app.projectv1.auth.dto.MeResponse;
import uz.app.projectv1.auth.dto.RegisterRequest;
import uz.app.projectv1.common.exception.ConflictException;
import uz.app.projectv1.rbac.RoleNames;
import uz.app.projectv1.rbac.RoleRepository;
import uz.app.projectv1.rbac.entity.Role;
import uz.app.projectv1.security.CustomUserDetails;
import uz.app.projectv1.security.JwtService;
import uz.app.projectv1.security.SessionService;
import uz.app.projectv1.security.entity.Session;
import uz.app.projectv1.user.UserRepository;
import uz.app.projectv1.user.entity.UserEntity;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final JwtService jwtService;

    public record LoginResult(String token, Duration ttl) {}

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Bu email allaqachon ro'yxatdan o'tgan");
        }

        Role userRole = roleRepository.findByName(RoleNames.USER)
                .orElseThrow(() -> new IllegalStateException(
                        "USER roli topilmadi — seed migration buzuq"));

        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        user.getRoles().add(userRole);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Bu email allaqachon ro'yxatdan o'tgan");
        }
    }

    @Transactional
    public LoginResult login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        Session session = sessionService.create(
                user.id(),
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr(),
                Duration.ofDays(7)
        );

        String token = jwtService.generate(user, session.getId());

        return new LoginResult(token, jwtService.accessTokenTtl());
    }

    @Transactional
    public void logout(String sid) {
        sessionService.revoke(UUID.fromString(sid));
    }

    public MeResponse me(Jwt jwt) {
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        String claim = jwt.getClaimAsString("authorities");

        if (claim != null && !claim.isBlank()) {
            for (String authority : claim.split(" ")) {
                if (authority.startsWith("ROLE_")) {
                    roles.add(authority.substring("ROLE_".length()));
                } else {
                    permissions.add(authority);
                }
            }
        }

        return new MeResponse(
                Long.valueOf(jwt.getSubject()),      // sub = user id
                jwt.getClaimAsString("email"),
                roles,
                permissions
        );
    }
}
