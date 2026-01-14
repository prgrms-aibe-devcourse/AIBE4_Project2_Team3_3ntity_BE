package kr.java.java.domain.user.service;

import kr.java.java.domain.user.dto.UserFormResponse;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.exception.UserErrorCode;
import kr.java.java.domain.user.exception.UserException;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserFormResponse getUserInfoForMatching(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return UserFormResponse.from(user);
    }
}
