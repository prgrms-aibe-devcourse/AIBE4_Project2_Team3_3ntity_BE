package kr.java.java.domain.comment.service;

import kr.java.java.domain.comment.dto.CommentCreateRequest;
import kr.java.java.domain.comment.dto.CommentResponse;
import kr.java.java.domain.comment.dto.CommentUpdateRequest;
import kr.java.java.domain.comment.entity.Comment;
import kr.java.java.domain.comment.event.CommentCreatedEvent;
import kr.java.java.domain.comment.repository.CommentRepository;
import kr.java.java.domain.comment.exception.*;
import kr.java.java.domain.notification.enums.NotificationType;
import kr.java.java.domain.notification.service.NotificationService;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final PortfolioRepository portfolioRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Long createComment(Long userId, CommentCreateRequest request) {
        log.info("문의 생성 시도 - userId: {}, spaceId: {}, portfolioId: {}",
                userId, request.spaceId(), request.portfolioId());

        // 1. 유저 검증 및 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 유저입니다. userId: {}", userId);
                    return new UserNotFoundException("존재하지 않는 사용자입니다.");
                });

        Space space = null;
        Portfolio portfolio = null;

        User notificationReceiver = null;
        String notificationRelatedUrl = "";

        // 2. 대상 검증 및 조회
        if (request.spaceId() != null) {
            space = spaceRepository.findById(request.spaceId())
                    .orElseThrow(() -> {
                        log.warn("존재하지 않는 공간입니다. spaceId: {}", request.spaceId());
                        return new SpaceNotFoundException("존재하지 않는 공간입니다.");
                    });
            notificationReceiver = space.getUser();
            notificationRelatedUrl = "piece/spaces/" + space.getId();
        } else if (request.portfolioId() != null) {
            portfolio = portfolioRepository.findById(request.portfolioId())
                    .orElseThrow(() -> {
                        log.warn("존재하지 않는 포트폴리오입니다. portfolioId: {}", request.portfolioId());
                        return new PortfolioNotFoundException("존재하지 않는 포트폴리오입니다.");
                    });
            notificationReceiver = portfolio.getUser();
            notificationRelatedUrl = "piece/portfolios/" + portfolio.getId();
        } else {
            log.warn("문의 대상 누락 - userId: {}", userId);
            throw new CommentTargetMissingException("문의를 남길 대상(공간 또는 포트폴리오)이 지정되지 않았습니다.");
        }

        // 3. 문의 엔티티 생성 및 저장
        Comment comment = Comment.builder()
                .user(user)
                .space(space)
                .portfolio(portfolio)
                .content(request.content())
                .isSecret(request.isSecret())
                .build();

        Comment savedComment = commentRepository.save(comment);

        log.info("문의 저장 성공 - commentId: {}, userId: {}", savedComment.getId(), userId);

        if (notificationReceiver != null && !notificationReceiver.getId().equals(userId)) {
            applicationEventPublisher.publishEvent(new CommentCreatedEvent(notificationReceiver.getUuid(), user.getNickname(), notificationRelatedUrl));
        }
        else
        {
            log.error("수신자를 찾을 수 없거나 수신자와 발신자가 같은 문의입니다.");
        }

        return savedComment.getId();
    }

    // 공간별 문의 조회
    public List<CommentResponse> getCommentsBySpace(Long spaceId, Long viewerId) {
        log.info("공간별 문의 조회 요청 - spaceId: {}, viewerId: {}", spaceId, viewerId);
        List<Comment> comments = commentRepository.findAllBySpaceIdOrderByCreatedAtDesc(spaceId);
        log.info("공간(ID:{}) 문의 조회 성공 - 총 {}건", spaceId, comments.size());
        return comments.stream()
                .map(comment -> CommentResponse.of(comment, viewerId))
                .toList();
    }

    // 포트폴리오별 문의 조회
    public List<CommentResponse> getCommentsByPortfolio(Long portfolioId, Long viewerId) {
        log.info("포트폴리오별 문의 조회 요청 - portfolioId: {}, viewerId: {}", portfolioId, viewerId);
        List<Comment> comments = commentRepository.findAllByPortfolioIdOrderByCreatedAtDesc(portfolioId);
        log.info("포트폴리오(ID:{}) 문의 조회 성공 - 총 {}건", portfolioId, comments.size());
        return comments.stream()
                .map(comment -> CommentResponse.of(comment, viewerId))
                .toList();
    }

    // 사용자별 문의 조회
    public List<CommentResponse> getMyComments(Long userId) {
        log.info("사용자별 문의 조회 요청 - userId: {}", userId);
        validateUser(userId);
        List<Comment> comments = commentRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        log.info("사용자(ID:{}) 문의 조회 성공 - 총 {}건", userId, comments.size());
        return comments.stream()
                .map(comment -> CommentResponse.of(comment, userId))
                .toList();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.info("문의 삭제 시도 - commentId: {}, userId: {}", commentId, userId);
        validateUser(userId);

        // 삭제할 문의 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("존재하지 않는 문의입니다."));

        // 권한 검증
        if (!comment.getUser().getId().equals(userId)) {
            log.warn("문의 삭제 실패 - 권한 없음. writerId: {}, requesterId: {}",
                    comment.getUser().getId(), userId);
            throw new CommentAccessDeniedException("본인이 작성한 문의만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
        log.info("문의 삭제 성공 - commentId: {}", commentId);
    }

    @Transactional
    public void updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        log.info("문의 수정 시작 - commentId: {}, userId: {}", commentId, userId);
        validateUser(userId);

        // 문의 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("존재하지 않는 문의입니다."));

        // 권한 검증
        if (!comment.getUser().getId().equals(userId)) {
            log.warn("문의 수정 실패 - 권한 없음. writerId: {}, requesterId: {}",
                    comment.getUser().getId(), userId);
            throw new CommentAccessDeniedException("본인이 작성한 문의만 수정할 수 있습니다.");
        }

        // 정보 업데이트
        comment.update(request.content(), request.isSecret());

        log.info("문의 수정 성공 - commentId: {}", commentId);
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            return;
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));
    }

}
