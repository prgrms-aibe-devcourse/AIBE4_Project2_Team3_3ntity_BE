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
@Table(name = "matching")
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
    public Matching(User sender, User receiver, Space space, User user, String message, LocalDate startDate, int months) {
        this.user = user;
        this.receiver = receiver;
        this.space = space;
        this.message = (message != null) ? message : "메시지가 없습니다.";
        this.status = MatchStatus.WAITING;
        this.senderType = sender.getId().equals(space.getUser().getId()) ? SenderType.OWNER : SenderType.USER;
        this.startDate = startDate;
        this.endDate = startDate.plusMonths(months).minusDays(1);
    }

    public void updateStatus(MatchStatus newStatus){
        if(this.status == newStatus){
            return;
        }

        if (this.status == MatchStatus.REJECTED ||
                this.status == MatchStatus.CANCELLED ||
                this.status == MatchStatus.COMPLETED) {
            throw new MatchingException(MatchingErrorCode.ALREADY_FINALIZED_MATCHING);
        }

        this.status = newStatus;
    }
}