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
import java.util.Optional;
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
    public void createMatching(CreateMatchingRequest request, Long userId){
        long loginUserId = userId;

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

        // 로그인 유저가 공간의 host라면
        if(loginUser.getId().equals(targetSpace.getUser().getId())){
            receiver = targetUser;
        } else{
            receiver = targetSpace.getUser();
        }

        log.info("[매칭 ID 확인] Sender ID: {}, Receiver ID: {}", sender.getId(), receiver.getId());

        validateMatching(targetSpace, targetUser, sender, receiver);

        Matching matching = Matching.builder()
                .user(sender)
                .receiver(receiver)
                .space(targetSpace)
                .message(request.message())
                .startDate(request.startDate())
                .months(request.months())
                .build();

        matchingRepository.save(matching);
    }

    private void validateMatching(Space space, User targetUser, User sender, User receiver){
        if(sender.getId().equals(receiver.getId())){
            log.warn("[매칭 검증 실패] 본인 매칭 시도 - UserId: {}", sender.getId());
            throw new MatchingException(MatchingErrorCode.SELF_MATCHING_NOT_ALLOWED);
        }

        List<MatchStatus> activeStatuses = List.of(MatchStatus.WAITING, MatchStatus.ONGOING);

        Optional<Matching> existingMatching = matchingRepository.findActiveMatchingBetweenUsers(
                space, sender, receiver, activeStatuses
        );

        if (existingMatching.isPresent()) {
            Matching matching = existingMatching.get();

            if (matching.getUser().getId().equals(sender.getId())) {
                log.warn("[매칭 검증 실패] 이미 본인이 신청한 매칭 존재 - MatchingID: {}", matching.getId());
                throw new MatchingException(MatchingErrorCode.ALREADY_ACTIVE_MATCHING_EXISTS);
            } else {
                log.warn("[매칭 검증 실패] 상대방이 보낸 매칭이 이미 존재 - MatchingID: {}", matching.getId());
                throw new MatchingException(MatchingErrorCode.MATCHING_REQUEST_ALREADY_RECEIVED);
                // "상대방의 신청을 확인해주세요"라는 별도의 에러코드를 쓰면 더 좋습니다.
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchings(Long UserId) {
        User loginUser = userRepository.findById(UserId)
                .orElseThrow(() -> new NotFoundUserException("존재하지 않는 유저입니다. ID: " + UserId));

        List<Matching> matchings = matchingRepository.findByUserOrReceiver(loginUser, loginUser);

        return convertToResponse(matchings, UserId);
    }

    public List<MatchingResponse> getMatchingsAsHost(Long UserId) {
        List<Matching> matchings = matchingRepository.findAllBySpaceHostId(UserId);
        return convertToResponse(matchings, UserId);
    }

    public List<MatchingResponse> getMatchingsAsMaker(Long UserId) {
        List<Matching> matchings = matchingRepository.findAllAsMakerId(UserId);
        return convertToResponse(matchings, UserId);
    }

    private List<MatchingResponse> convertToResponse(List<Matching> matchings, Long UserId) {
        return matchings.stream()
                .map(matching -> MatchingResponse.from(matching, UserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptMatching(Long matchingId, Long UserId){
        Matching matching = findMatchingById(matchingId);
        validateReceiverAndStatus(matching, UserId);

        matching.updateStatus(MatchStatus.ONGOING);
        log.info("[매칭 수락] MatchingID: {}, 수락자: {}", matchingId, UserId);

        Long spaceId = matching.getSpace().getId();

        int rejectedCount = matchingRepository.bulkUpdateStatusForOthers(
                spaceId,
                MatchStatus.WAITING,
                MatchStatus.REJECTED,
                matchingId,
                matching.getStartDate(),
                matching.getEndDate()
        );
        log.info("[매칭 확정] MatchingID: {}, 자동 거절된 건수: {}건", matchingId, rejectedCount);
    }

    @Transactional
    public void rejectMatching(Long matchingId, Long UserId) {
        Matching matching = findMatchingById(matchingId);
        validateReceiverAndStatus(matching, UserId);

        matching.updateStatus(MatchStatus.REJECTED);
        log.info("[매칭 거절] MatchingID: {}, 거절자: {}", matchingId, UserId);
    }

    private Matching findMatchingById(Long matchingId){
        return matchingRepository.findById(matchingId)
                .orElseThrow(() -> new MatchingException(MatchingErrorCode.MATCHING_NOT_FOUND));
    }

    private void validateReceiverAndStatus(Matching matching, Long UserId) {
        log.info("[검증 로그] DB ReceiverID: {}, 요청 LoginUserID: {}",
                matching.getReceiver().getId(), UserId);
        if (!matching.getReceiver().getId().equals(UserId)) {
            throw new MatchingException(MatchingErrorCode.NOT_AUTHORIZED_RECEIVER);
        }

        if (matching.getStatus() != MatchStatus.WAITING) {
            throw new MatchingException(MatchingErrorCode.INVALID_MATCH_STATUS);
        }
    }

    @Transactional
    public void cancelMatching(Long matchingId, Long UserId) {
        Matching matching = findMatchingById(matchingId);
        validateSenderAndStatus(matching, UserId);

        matching.updateStatus(MatchStatus.CANCELLED);
        log.info("[매칭 취소] MatchingID: {}, 거절자: {}", matchingId, UserId);
    }

    private void validateSenderAndStatus(Matching matching, Long UserId) {
        if (!matching.getUser().getId().equals(UserId)) {
            throw new MatchingException(MatchingErrorCode.NOT_AUTHORIZED_SENDER);
        }

        if (matching.getStatus() != MatchStatus.WAITING) {
            throw new MatchingException(MatchingErrorCode.INVALID_MATCH_STATUS);
        }
    }
}
