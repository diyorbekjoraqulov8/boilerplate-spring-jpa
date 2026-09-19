package uz.app.projectv1.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.app.projectv1.rbac.entity.Permission;
import uz.app.projectv1.rbac.entity.Role;
import uz.app.projectv1.user.entity.UserEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public record CustomUserDetails(
        Long id,
        String email,
        String password,
        boolean active,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isEnabled() { return active; }

    public static CustomUserDetails from(UserEntity user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission p : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(p.getName()));
            }
        }
        return new CustomUserDetails(user.getId(), user.getEmail(),
                user.getPassword(), user.isActive(), authorities);
    }
}