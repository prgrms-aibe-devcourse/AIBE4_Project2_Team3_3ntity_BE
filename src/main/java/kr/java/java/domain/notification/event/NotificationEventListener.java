package kr.java.java.domain.notification.event;

import kr.java.java.domain.comment.event.CommentCreatedEvent;
import kr.java.java.domain.matching.event.*;
import kr.java.java.domain.notification.enums.NotificationType;
import kr.java.java.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchingCreatedEvent(MatchingCreatedEvent event) {
        log.info("[알림 리스너] 매칭 신청 이벤트 수신: receiver ID = {}, sender Nickname = {}", event.receiverId(), event.senderNickname());
        try {
            notificationService.createNotification(
                    event.receiverId(),
                    NotificationType.MATCHING,
                    event.senderNickname() + "님이 매칭을 신청했습니다.",
                    "/matching"
            );
        } catch (Exception e) {
            log.error("[알림 리스너] 매칭 신청 알림 생성 실패", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchingAcceptedEvent(MatchingAcceptedEvent event) {
        log.info("[알림 리스너] 매칭 수락 이벤트 수신: receiver ID = {}, sender Nickname = {}", event.receiverId(), event.senderNickname());
        try {
            notificationService.createNotification(
                    event.receiverId(),
                    NotificationType.MATCHING_COMPLETE,
                    event.senderNickname() + "님이 매칭을 수락하셨습니다.",
                    "/matching"
            );
        } catch (Exception e) {
            log.error("[알림 리스너] 매칭 수락 알림 생성 실패", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchingCanceledEvent(MatchingCanceledEvent event) {
        log.info("[알림 리스너] 매칭 취소 이벤트 수신: receiver ID = {}, sender Nickname = {}", event.receiverId(), event.senderNickname());
        try {
            notificationService.createNotification(
                    event.receiverId(),
                    NotificationType.MATCHING_CANCELED,
                    event.senderNickname() + "님이 매칭을 취소하셨습니다.",
                    "/matching"
            );
        } catch (Exception e) {
            log.error("[알림 리스너] 매칭 취소 알림 생성 실패", e);
        }
    }

    @EventListener
    public void handleMatchingRejectedEvent(MatchingRejectedEvent event) {
        log.info("[알림 리스너] 매칭 거절 이벤트 수신: receiver ID = {}, sender Nickname = {}", event.receiverId(), event.senderNickname());
        try {
            notificationService.createNotification(
                    event.receiverId(),
                    NotificationType.MATCHING_REJECT,
                    event.senderNickname() + "님이 매칭을 거절하셨습니다.",
                    "/matching"
            );
        } catch (Exception e) {
            log.error("[알림 리스너] 매칭 거절 알림 생성 실패", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        log.info("[알림 리스너] 문의 이벤트 수신: receiver ID = {}, sender Nickname = {}", event.receiverId(), event.senderNickname());
        try {
            notificationService.createNotification(
                    event.receiverId(),
                    NotificationType.COMMENT,
                    event.senderNickname() + "님이 문의를 남겼습니다.",
                    event.relatedUrl()
            );
        } catch (Exception e) {
            log.error("[알림 리스너] 문의 알림 생성 실패", e);
        }
    }

    @EventListener
    public void handleMatchingExpiredEvent(MatchingExpiredEvent event) {
        String matchingId = String.valueOf(event.matchingId());
        log.info("[알림 리스너] 매칭 만료 이벤트 수신: space ID = {}", matchingId);

        try {
            //TODO: 만료 시 리뷰 작성 url로 이동
            notificationService.createNotification(
                    event.senderId(),
                    NotificationType.MATCHING_COMPLETE,
                    event.receiverNickname()+"님과의 매칭이 만료되었습니다",
                    "/reviews/write/"+matchingId
            );

            notificationService.createNotification(
                    event.receiverId(),
                    NotificationType.MATCHING_COMPLETE,
                    event.senderNickname()+"님과의 매칭이 만료되었습니다",
                    "/reviews/write/"+matchingId
            );
        } catch (Exception e) {
            log.error("[알림 리스너] 매칭 만료 알림 생성 실패", e);
        }
    }
}
