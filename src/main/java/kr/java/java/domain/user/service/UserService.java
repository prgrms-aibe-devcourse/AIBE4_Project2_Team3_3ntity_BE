package kr.java.java.domain.user.service;

import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.exception.UserErrorCode;
import kr.java.java.domain.user.exception.UserException;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    @Transactional
    public void upgradeToHostIfFirstSpace(Long userId) {
        long spaceCount = spaceRepository.countSpacesByUserId(userId);
        
        if (spaceCount != 1) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (user.isUser()) {
            user.upgradeToHost();
            log.info("사용자 권한 업그레이드 완료 - UserId: {}, Role: USER -> HOST", userId);
        }
    }
}
