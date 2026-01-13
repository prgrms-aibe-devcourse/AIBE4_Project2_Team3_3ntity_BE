package kr.java.java.domain.matching.service;

import kr.java.java.domain.image.service.ImageService;
import kr.java.java.domain.matching.dto.CreateMatchingCommand;
import kr.java.java.domain.matching.dto.CreateMatchingToSpaceRequest;
import kr.java.java.domain.matching.dto.CreateMatchingToUserRequest;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.event.*;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final MatchingRepository matchingRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ImageService imageService;

    //TODO 해당 서비스 페이지에 있는 User 에러처리는 추후 User 도메인의 exception에 생기면 변경

    @Transactional
    public void createUserToSpace(
            Long spaceId,
            CreateMatchingToSpaceRequest request,
            UUID userUuid
    ) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다."));

        CreateMatchingCommand command = new CreateMatchingCommand(
                spaceId,
                userUuid,
                space.getUser().getUuid(),
                request.message(),
                request.startDate(),
                request.months()
        );

        createMatchingInternal(command);
    }

    @Transactional
    public void createSpaceToUser(
            UUID targetUserUuid,
            CreateMatchingToUserRequest request,
            UUID userUuid
    ) {
        User sender = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new NotFoundUserException("로그인 유저 없음"));
        Space space = spaceRepository.findById(request.spaceId())
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다."));

        if (!space.getUser().getId().equals(sender.getId())) {
            throw new MatchingException(MatchingErrorCode.HOST_CANNOT_MATCH_OTHER_SPACE);
        }

        CreateMatchingCommand command = new CreateMatchingCommand(
                space.getId(),
                sender.getUuid(),
                targetUserUuid,
                request.message(),
                request.startDate(),
                request.months()
        );

        createMatchingInternal(command);
    }

    private void createMatchingInternal(
            CreateMatchingCommand command
    ) {
        log.info("[매칭 service] 매칭 생성 시작");
        User sender = userRepository.findByUuid(command.senderUuid())
                .orElseThrow(() -> new NotFoundUserException("로그인 유저 없음"));

        User receiver = userRepository.findByUuid(command.receiverUuid())
                .orElseThrow(() -> new NotFoundUserException("로그인 유저 없음"));

        Space space = spaceRepository.findById(command.spaceId())
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + command.spaceId()));

        validateMatching(space, sender, receiver);
        log.info("[매칭 service] validateMatching 통과");
        Matching matching = Matching.builder()
                .user(sender)
                .receiver(receiver)
                .space(space)
                .message(command.message())
                .startDate(command.startDate())
                .months(command.months())
                .build();

        matchingRepository.save(matching);
        log.info("[매칭 service] 매칭 생성 완료 - MatchingID: {}", matching.getId());

        String relatedUrl = createMatchingRelatedUrl(matching, MatchStatus.WAITING);
        applicationEventPublisher.publishEvent(new MatchingCreatedEvent(
                matching.getReceiver().getId(),
                matching.getUser().getNickname(),
                relatedUrl
        ));
    }

    private String createMatchingRelatedUrl(Matching matching, MatchStatus status)
    {
        Long notificationReceiverId = switch (status) {
            case WAITING, CANCELLED -> matching.getReceiver().getId();
            case ONGOING, REJECTED -> matching.getUser().getId();
            default -> throw new MatchingException(MatchingErrorCode.MATCHING_NOT_FOUND);
        };

        String targetPath = notificationReceiverId.equals(matching.getSpace().getUser().getId()) ? "hosts" : "users";

        return "/piece/matchings/" + targetPath + "?userId=" + notificationReceiverId + "&status=" + status;
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
    public List<MatchingResponse> getMatchings(UUID userUuid, MatchStatus status) {
        List<Matching> matchings = matchingRepository.findAllByUserUuidAndStatus(userUuid, status);

        return convertToResponse(matchings, userUuid);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchingsAsHost(UUID userUuid, MatchStatus status) {
        List<Matching> matchings = matchingRepository.findAllBySpaceHostId(userUuid, status);
        return convertToResponse(matchings, userUuid);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchingsAsMaker(UUID userUuid, MatchStatus status) {
        List<Matching> matchings = matchingRepository.findAllAsMakerId(userUuid, status);
        return convertToResponse(matchings, userUuid);
    }

    // TODO space entity에 썸네일 url을 추가할지 의논 후 로직 최종 결정
    private List<MatchingResponse> convertToResponse(List<Matching> matchings, UUID userUuid) {
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
                    return MatchingResponse.from(matching, userUuid, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptMatching(Long matchingId, UUID userUuid){
        Matching matching = findMatchingById(matchingId);
        validateReceiverAndStatus(matching, userUuid);

        matching.updateStatus(MatchStatus.ONGOING);

        boolean isHost = matching.getReceiver().getUuid().equals(userUuid);

        if(isHost){
            autoRejectOverlappingMatchings(matching);
            log.info("[매칭 수락 - HOST] MatchingID: {}, 수락자: {}", matchingId, userUuid);
        } else{
            log.info("[매칭 수락 - USER] MatchingID: {}, 수락자: {}", matchingId, userUuid);
        }

        String relatedUrl = createMatchingRelatedUrl(matching, MatchStatus.ONGOING);
        applicationEventPublisher.publishEvent(new MatchingAcceptedEvent(
                matching.getUser().getId(),
                matching.getReceiver().getNickname(),
                relatedUrl
        ));
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
    public void rejectMatching(Long matchingId, UUID userUuid) {
        Matching matching = findMatchingById(matchingId);
        validateReceiverAndStatus(matching, userUuid);

        matching.updateStatus(MatchStatus.REJECTED);

        String relatedUrl = createMatchingRelatedUrl(matching, MatchStatus.REJECTED);
        applicationEventPublisher.publishEvent(new MatchingRejectedEvent(
                matching.getUser().getId(),
                matching.getReceiver().getNickname(),
                relatedUrl
        ));
    }

    private Matching findMatchingById(Long matchingId){
        return matchingRepository.findByIdWithFetchJoin(matchingId)
                .orElseThrow(() -> new MatchingException(MatchingErrorCode.MATCHING_NOT_FOUND));
    }

    private void validateReceiverAndStatus(Matching matching, UUID userUuid) {
        log.info("[검증 로그] DB ReceiverID: {}, 요청 LoginUserID: {}",
                matching.getReceiver().getUuid(), userUuid);
        if (!matching.getReceiver().getUuid().equals(userUuid)) {
            throw new MatchingException(MatchingErrorCode.NOT_AUTHORIZED_RECEIVER);
        }

        if (matching.getStatus() != MatchStatus.WAITING) {
            throw new MatchingException(MatchingErrorCode.INVALID_MATCH_STATUS);
        }
    }

    @Transactional
    public void cancelMatching(Long matchingId, UUID userUuid) {
        Matching matching = findMatchingById(matchingId);
        validateSenderAndStatus(matching, userUuid);

        matching.updateStatus(MatchStatus.CANCELLED);

        String relatedUrl = createMatchingRelatedUrl(matching, MatchStatus.CANCELLED);
        applicationEventPublisher.publishEvent(new MatchingCanceledEvent(
                matching.getReceiver().getId(),
                matching.getUser().getNickname(),
                relatedUrl
        ));
    }

    private void validateSenderAndStatus(Matching matching, UUID userUuid) {
        if (!matching.getUser().getUuid().equals(userUuid)) {
            throw new MatchingException(MatchingErrorCode.NOT_AUTHORIZED_SENDER);
        }

        if (matching.getStatus() != MatchStatus.WAITING) {
            throw new MatchingException(MatchingErrorCode.INVALID_MATCH_STATUS);
        }
    }
    @Transactional
    public List<MatchingExpiredEvent> processExpiredMatchings() {
        LocalDate today = LocalDate.now();

        List<Matching> expiredMatchings = matchingRepository.findExpiredMatchingsWithUser(today, MatchStatus.ONGOING);

        List<MatchingExpiredEvent> expiredMatchingEvents = new ArrayList<>();

        log.info("총 {}건의 만료 대상 매칭 발견", expiredMatchings.size());

        for (Matching matching : expiredMatchings) {
            try{
                matching.completeMatch();

                MatchingExpiredEvent event = new MatchingExpiredEvent(
                        matching.getId(),
                        matching.getUser().getUuid(),
                        matching.getUser().getNickname(),
                        matching.getReceiver().getUuid(),
                        matching.getReceiver().getNickname()
                );
                expiredMatchingEvents.add(event);
                log.info("[매칭 종료] ID: {}, 발신자: {}, 수신자: {}",
                        matching.getId(),
                        matching.getUser().getNickname(),
                        matching.getReceiver().getNickname());

            } catch(Exception e){
                log.error("[매칭 상태 변경] 실패: ID={}, 사유={}", matching.getId(), e.getMessage());
            }
        }

        log.info("만료 매칭 처리 완료");
        return expiredMatchingEvents;
    }

    @Transactional
    public List<MatchingRejectedEvent> processOverdueWaitingMatchings() {
        LocalDate today = LocalDate.now();

        List<Matching> overdueMatchings = matchingRepository.findOverdueWaitingMatchings(today, MatchStatus.WAITING);

        List<MatchingRejectedEvent> rejectedMatchingEvents = new ArrayList<>();

        log.info("총 {}건의 만료 대상 매칭 발견", overdueMatchings.size());

        for(Matching matching : overdueMatchings){
            try{
                matching.rejectMatch();

                MatchingRejectedEvent event = new MatchingRejectedEvent(
                        matching.getUser().getId(),
                        matching.getReceiver().getNickname(),
                        createMatchingRelatedUrl(matching, MatchStatus.REJECTED));
                rejectedMatchingEvents.add(event);

                log.info("[매칭 거절] ID: {}, 발신자: {}, 수신자: {}",
                        matching.getId(),
                        matching.getUser().getNickname(),
                        matching.getReceiver().getNickname());

            } catch(Exception e){
                log.error("[자동 거절 실패] ID: {}, 사유: {}", matching.getId(), e.getMessage());
            }
        }

        return rejectedMatchingEvents;
    }
}
