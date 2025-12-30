package kr.java.java.domain.matching.service;

import kr.java.java.domain.matching.dto.MatchingRequest;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.repository.MatchingRepository;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final MatchingRepository matchingRepository;

    public void createMatching(MatchingRequest request, Long loginUserId){
        Space targetSpace = spaceRepository.findById(request.spaceId())
                .orElseThrow(() -> {
                    log.error("[매칭 실패] Space 존재하지 않음 - ID: {}", request.spaceId());
                    return new IllegalArgumentException("Space를 찾을 수 없습니다.");
                });
        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.error("[매칭 실패] 대상 User 존재하지 않음 - ID: {}", request.userId());
                    return new IllegalArgumentException("User를 찾을 수 없습니다.");
                });
        User loginUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> {
                    log.error("[매칭 실패] 로그인 유저 정보 없음 - ID: {}", loginUserId);
                    return new IllegalArgumentException("User를 찾을 수 없습니다.");
                });

        User sender = loginUser;
        User receiver;

        if(loginUser.getId().equals(targetUser.getId())){
            receiver = targetSpace.getUser();
        } else{
            receiver = loginUser;
        }

        validateMatching(targetSpace, targetUser, sender, receiver);

        Matching matching = Matching.builder()
                .sender(sender)
                .receiver(receiver)
                .space(targetSpace)
                .user(targetUser)
                .message(request.message())
                .build();

        matchingRepository.save(matching);
    }

    private void validateMatching(Space space, User targetUser, User sender, User receiver){
        if(sender.getId().equals(receiver.getId())){
            log.warn("[매칭 검증 실패] 본인 매칭 시도 - UserId: {}", sender.getId());
            throw new IllegalArgumentException("본인과의 매칭은 진행할 수 없습니다.");
        }

        List<MatchStatus> activeStatuses = List.of(MatchStatus.WAITING, MatchStatus.ONGOING);

        boolean alreadyActive = matchingRepository.existsBySpaceAndUserAndStatusIn(space, targetUser, activeStatuses);

        if(alreadyActive){
            log.warn("[매칭 검증 실패] 이미 활성화된 매칭 존재 - SpaceID: {}, TargetUserID: {}",
                    space.getId(), targetUser.getId());
            throw new IllegalStateException("이미 대기 중이거나 진행 중인 매칭이 존재합니다.");
        }
    }
}
