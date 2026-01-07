package kr.java.java.domain.auth.security;

import kr.java.java.domain.user.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final UUID uuid;  // DB의 User ID
    private final String email;
    private final Role role;
    private final String provider;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(UUID uuid, String email, Role role, String provider, Map<String, Object> attributes) {
        this.uuid = uuid;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getName() {
        return provider + "_" + email;
    }
}