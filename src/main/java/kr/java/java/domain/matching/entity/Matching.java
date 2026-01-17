package kr.java.java.domain.matching.entity;

import jakarta.persistence.*;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.enums.SenderType;
import kr.java.java.domain.matching.exception.MatchingErrorCode;
import kr.java.java.domain.matching.exception.MatchingException;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "matchings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Matching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private SenderType senderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchStatus status;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Matching(User receiver, Space space, User user, String message, LocalDate startDate, int months) {
        this.user = user;
        this.receiver = receiver;
        this.space = space;
        this.message = (message != null) ? message : "메시지가 없습니다.";
        this.status = MatchStatus.WAITING;
        this.senderType = user.getId().equals(space.getUser().getId()) ? SenderType.HOST : SenderType.USER;
        this.startDate = startDate;
        this.endDate = startDate.plusMonths(months).minusDays(1);

        validateDates();
    }

    private void validateDates() {
        if (this.startDate == null) {
            throw new MatchingException(MatchingErrorCode.INVALID_START_DATE);
        }
        if (this.startDate.isBefore(LocalDate.now())) {
            throw new MatchingException(MatchingErrorCode.START_DATE_CANNOT_BE_PAST);
        }
        if (this.endDate.isBefore(this.startDate)) {
            throw new MatchingException(MatchingErrorCode.INVALID_END_DATE);
        }
    }

    public static Matching createMatching(User sender, User receiver, Space space, String message, LocalDate startDate, int months) {
        return Matching.builder()
                .user(sender)
                .receiver(receiver)
                .space(space)
                .message(message)
                .startDate(startDate)
                .months(months)
                .build();
    }

    public void updateStatus(MatchStatus newStatus){
        if(this.status == newStatus){
            return;
        }

        if (isFinalized()) {
            throw new MatchingException(MatchingErrorCode.ALREADY_FINALIZED_MATCHING);
        }

        this.status = newStatus;
    }

    private boolean isFinalized() {
        return this.status == MatchStatus.REJECTED ||
                this.status == MatchStatus.CANCELLED ||
                this.status == MatchStatus.COMPLETED;
    }

    public void completeMatch(){
        this.status = MatchStatus.COMPLETED;
    }

    public void rejectMatch(){
        if(this.status != MatchStatus.WAITING) {
            throw new MatchingException(MatchingErrorCode.INVALID_MATCH_STATUS);
        }
        this.status = MatchStatus.REJECTED;
    }

    public String getRelatedUrl(MatchStatus status) {
        Long notificationReceiverId = switch (status) {
            case WAITING, CANCELLED -> this.receiver.getId();
            case ONGOING, REJECTED -> this.user.getId();
            default -> throw new MatchingException(MatchingErrorCode.MATCHING_NOT_FOUND);
        };

        String targetPath = notificationReceiverId.equals(this.space.getUser().getId()) ? "hosts" : "users";

        return "/piece/matchings/" + targetPath + "?userId=" + notificationReceiverId + "&status=" + status;
    }
}