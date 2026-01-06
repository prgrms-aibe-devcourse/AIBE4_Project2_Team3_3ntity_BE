package kr.java.java.domain.matching.service;

import kr.java.java.domain.matching.dto.CreateMatchingRequest;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.exception.MatchingErrorCode;
import kr.java.java.domain.matching.exception.MatchingException;
import kr.java.java.domain.matching.repository.MatchingRepository;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.exception.NotFoundSpaceException;
import kr.java.java.domain.space.exception.NotFoundUserException;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final MatchingRepository matchingRepository;

    //TODO 해당 서비스 페이지에 있는 User 에러처리는 추후 User 도메인의 exception에 생기면 변경

    @Transactional
    public void createMatching(CreateMatchingRequest request, Long loginUserId){
        Space targetSpace = spaceRepository.findById(request.spaceId())
                .orElseThrow(() -> {
                    log.error("[매칭 실패] Space 존재하지 않음 - ID: {}", request.spaceId());
                    return new NotFoundSpaceException("해당 공간이 없습니다. id=" + request.spaceId());
                });
        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.error("[매칭 실패] 대상 User 존재하지 않음 - ID: {}", request.userId());
                    return new NotFoundUserException("존재하지 않는 유저입니다. ID: " + request.userId());
                });
        User loginUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> {
                    log.error("[매칭 실패] 로그인 유저 정보 없음 - ID: {}", loginUserId);
                    return new NotFoundUserException("존재하지 않는 유저입니다. ID: " + loginUserId);
                });

        User sender = loginUser;
        User receiver;

        if(loginUser.getId().equals(targetSpace.getUser().getId())){
            receiver = targetUser;
        } else{
            receiver = targetSpace.getUser();
        }

        log.info("[매칭 ID 확인] Sender ID: {}, Receiver ID: {}", sender.getId(), receiver.getId());

        validateMatching(targetSpace, targetUser, sender, receiver);

        Matching matching = Matching.builder()
                .sender(sender)
                .receiver(receiver)
                .space(targetSpace)
                .user(targetUser)
                .message(request.message())
                .startDate(request.startDate())
                .months(request.months())
                .build();

        matchingRepository.save(matching);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchings(Long loginUserId) {
        User loginUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> new NotFoundUserException("존재하지 않는 유저입니다. ID: " + loginUserId));

        List<Matching> matchings = matchingRepository.findBySenderOrReceiver(loginUser, loginUser);

        return matchings.stream()
                .map(matching -> MatchingResponse.from(matching, loginUserId))
                .collect(Collectors.toList());
    }

    private void validateMatching(Space space, User targetUser, User sender, User receiver){
        if(sender.getId().equals(receiver.getId())){
            log.warn("[매칭 검증 실패] 본인 매칭 시도 - UserId: {}", sender.getId());
            throw new MatchingException(MatchingErrorCode.SELF_MATCHING_NOT_ALLOWED);
        }

        List<MatchStatus> activeStatuses = List.of(MatchStatus.WAITING, MatchStatus.ONGOING);

        boolean alreadyActive = matchingRepository.existsBySpaceAndUserAndStatusIn(space, targetUser, activeStatuses);

        if(alreadyActive){
            log.warn("[매칭 검증 실패] 이미 활성화된 매칭 존재 - SpaceID: {}, TargetUserID: {}",
                    space.getId(), targetUser.getId());
            throw new MatchingException(MatchingErrorCode.ALREADY_ACTIVE_MATCHING_EXISTS);
        }
    }
}
