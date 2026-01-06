package kr.java.java.domain.auth.security;

import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID uuid = UUID.fromString(username);


        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with UUID: " + uuid));

        return new CustomUserDetails(
                user.getUuid(),
                user.getEmail(),
                user.getRole()
        );
    }

    public UserDetails loadUserByUuid(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with UUID: " + uuid));

        return new CustomUserDetails(
                user.getUuid(),
                user.getEmail(),
                user.getRole()
        );
    }
}
