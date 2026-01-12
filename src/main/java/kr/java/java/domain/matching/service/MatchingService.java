package kr.java.java.domain.matching.service;

import kr.java.java.domain.image.service.ImageService;
import kr.java.java.domain.matching.dto.CreateMatchingCommand;
import kr.java.java.domain.matching.dto.CreateMatchingToSpaceRequest;
import kr.java.java.domain.matching.dto.CreateMatchingToUserRequest;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.exception.MatchingErrorCode;
import kr.java.java.domain.matching.exception.MatchingException;
import kr.java.java.domain.matching.repository.MatchingRepository;
import kr.java.java.domain.notification.service.NotificationService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final MatchingRepository matchingRepository;
    private final ImageService imageService;

    //TODO 해당 서비스 페이지에 있는 User 에러처리는 추후 User 도메인의 exception에 생기면 변경

    @Transactional
    public void createUserToSpace(
            Long spaceId,
            CreateMatchingToSpaceRequest request,
            Long loginUserId
    ) {
        Space space = spaceRepository.findById(spaceId).orElse(null);

        CreateMatchingCommand command = new CreateMatchingCommand(
                spaceId,
                loginUserId,
                space.getUser().getId(),
                request.message(),
                request.startDate(),
                request.months()
        );

        createMatchingInternal(command);
    }

    @Transactional
    public void createSpaceToUser(
            Long targetUserId,
            CreateMatchingToUserRequest request,
            Long loginUserId
    ) {
        User sender = userRepository.findById(loginUserId)
                .orElseThrow(() -> new NotFoundUserException("로그인 유저 없음"));
        Space space = spaceRepository.findById(request.spaceId())
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + request.spaceId()));

        if (!space.getUser().getId().equals(sender.getId())) {
            throw new MatchingException(MatchingErrorCode.HOST_CANNOT_MATCH_OTHER_SPACE);
        }

        CreateMatchingCommand command = new CreateMatchingCommand(
                space.getId(),
                sender.getId(),
                targetUserId,
                request.message(),
                request.startDate(),
                request.months()
        );

        createMatchingInternal(command);
    }

    private void createMatchingInternal(
            CreateMatchingCommand command
    ) {
        User sender = userRepository.findById(command.senderId())
                .orElseThrow(() -> new NotFoundUserException("로그인 유저 없음"));

        User receiver = userRepository.findById(command.receiverId())
                .orElseThrow(() -> new NotFoundUserException("로그인 유저 없음"));

        Space space = spaceRepository.findById(command.spaceId())
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + command.spaceId()));

        validateMatching(space, sender, receiver);

        Matching matching = Matching.builder()
                .user(sender)
                .receiver(receiver)
                .space(space)
                .message(command.message())
                .startDate(command.startDate())
                .months(command.months())
                .build();

        matchingRepository.save(matching);
    }

    private void validateMatching(Space space, User sender, User receiver){
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
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchings(Long userId, MatchStatus status) {
        List<Matching> matchings = matchingRepository.findAllByUserIdAndStatus(userId, status);

        return convertToResponse(matchings, userId);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchingsAsHost(Long userId, MatchStatus status) {
        List<Matching> matchings = matchingRepository.findAllBySpaceHostId(userId, status);
        return convertToResponse(matchings, userId);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchingsAsMaker(Long userId, MatchStatus status) {
        List<Matching> matchings = matchingRepository.findAllAsMakerId(userId, status);
        return convertToResponse(matchings, userId);
    }

    // TODO space entity에 썸네일 url을 추가할지 의논 후 로직 최종 결정
    private List<MatchingResponse> convertToResponse(List<Matching> matchings, Long userId) {
        if (matchings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> spaceIds = matchings.stream()
                .map(m -> m.getSpace().getId())
                .distinct()
                .toList();

        Map<Long, String> thumbnailMap = imageService.getThumnailsBySpaceIds(spaceIds);

        return matchings.stream()
                .map(matching -> {
                    String mainImageUrl = thumbnailMap.getOrDefault(
                            matching.getSpace().getId(),
                            "default-image-url"
                    );
                    return MatchingResponse.from(matching, userId, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptMatching(Long matchingId, Long userId){
        Matching matching = findMatchingById(matchingId);
        validateReceiverAndStatus(matching, userId);

        matching.updateStatus(MatchStatus.ONGOING);

        boolean isHost = matching.getSpace().getUser().getId().equals(userId);

        if(isHost){
            autoRejectOverlappingMatchings(matching);
            log.info("[매칭 수락 - HOST] MatchingID: {}, 수락자: {}", matchingId, userId);
        } else{
            log.info("[매칭 수락 - USER] MatchingID: {}, 수락자: {}", matchingId, userId);
        }
    }

    private void autoRejectOverlappingMatchings(Matching confirmedMatching) {
        int rejectedCount = matchingRepository.bulkUpdateStatusForOthers(
                confirmedMatching.getSpace().getId(),
                MatchStatus.WAITING,
                MatchStatus.REJECTED,
                confirmedMatching.getId(),
                confirmedMatching.getStartDate(),
                confirmedMatching.getEndDate()
        );
        log.info("[매칭 확정] SpaceID: {}, 자동 거절된 건수: {}건",
                confirmedMatching.getSpace().getId(), rejectedCount);
    }

    @Transactional
    public void rejectMatching(Long matchingId, Long userId) {
        Matching matching = findMatchingById(matchingId);
        validateReceiverAndStatus(matching, userId);

        matching.updateStatus(MatchStatus.REJECTED);
        log.info("[매칭 거절] MatchingID: {}, 거절자: {}", matchingId, userId);
    }

    private Matching findMatchingById(Long matchingId){
        return matchingRepository.findById(matchingId)
                .orElseThrow(() -> new MatchingException(MatchingErrorCode.MATCHING_NOT_FOUND));
    }

    private void validateReceiverAndStatus(Matching matching, Long userId) {
        log.info("[검증 로그] DB ReceiverID: {}, 요청 LoginUserID: {}",
                matching.getReceiver().getId(), userId);
        if (!matching.getReceiver().getId().equals(userId)) {
            throw new MatchingException(MatchingErrorCode.NOT_AUTHORIZED_RECEIVER);
        }

        if (matching.getStatus() != MatchStatus.WAITING) {
            throw new MatchingException(MatchingErrorCode.INVALID_MATCH_STATUS);
        }
    }

    @Transactional
    public void cancelMatching(Long matchingId, Long userId) {
        Matching matching = findMatchingById(matchingId);
        validateSenderAndStatus(matching, userId);

        matching.updateStatus(MatchStatus.CANCELLED);
        log.info("[매칭 취소] MatchingID: {}, 거절자: {}", matchingId, userId);
    }

    private void validateSenderAndStatus(Matching matching, Long userId) {
        if (!matching.getUser().getId().equals(userId)) {
            throw new MatchingException(MatchingErrorCode.NOT_AUTHORIZED_SENDER);
        }

        if (matching.getStatus() != MatchStatus.WAITING) {
            throw new MatchingException(MatchingErrorCode.INVALID_MATCH_STATUS);
        }
    }

    @Transactional
    public void processExpiredMatchings() {
        LocalDate today = LocalDate.now();

        List<Matching> expiredMatchings = matchingRepository.findExpiredMatchingsWithUser(today, MatchStatus.ONGOING);

        log.info("총 {}건의 만료 대상 매칭 발견", expiredMatchings.size());

        for (Matching matching : expiredMatchings) {
            try{
                matching.completeMatch();
                log.info("[매칭 종료] ID: {}, 발신자: {}, 수신자: {}",
                        matching.getId(),
                        matching.getUser().getNickname(),
                        matching.getReceiver().getNickname());

                // TODO: 발신자에게 알림 전송

                // TODO: 수신자에게 알림 전송
            } catch(Exception e){
                log.error("[매칭 상태 변경] 실패: ID={}, 사유={}", matching.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void processOverdueWaitingMatchings() {
        LocalDate today = LocalDate.now();

        List<Matching> overdueMatchings = matchingRepository.findOverdueWaitingMatchings(today, MatchStatus.WAITING);

        log.info("총 {}건의 만료 대상 매칭 발견", overdueMatchings.size());

        for(Matching matching : overdueMatchings){
            try{
                matching.rejectMatch();

                log.info("[매칭 거절] ID: {}, 발신자: {}, 수신자: {}",
                        matching.getId(),
                        matching.getUser().getNickname(),
                        matching.getReceiver().getNickname());

                // TODO: 발신자에게 알림 전송

            } catch(Exception e){
                log.error("[자동 거절 실패] ID: {}, 사유: {}", matching.getId(), e.getMessage());
            }
        }
    }
}

