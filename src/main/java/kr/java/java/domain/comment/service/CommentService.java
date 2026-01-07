package kr.java.java.domain.comment.service;

import kr.java.java.domain.comment.dto.CommentCreateRequest;
import kr.java.java.domain.comment.entity.Comment;
import kr.java.java.domain.comment.repository.CommentRepository;
import kr.java.java.domain.comment.exception.*;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final PortfolioRepository portfolioRepository;

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

        // 2. 대상 검증 및 조회
        if (request.spaceId() != null) {
            space = spaceRepository.findById(request.spaceId())
                    .orElseThrow(() -> {
                        log.warn("존재하지 않는 공간입니다. spaceId: {}", request.spaceId());
                        return new SpaceNotFoundException("존재하지 않는 공간입니다.");
                    });
        } else if (request.portfolioId() != null) {
            portfolio = portfolioRepository.findById(request.portfolioId())
                    .orElseThrow(() -> {
                        log.warn("존재하지 않는 포트폴리오입니다. portfolioId: {}", request.portfolioId());
                        return new PortfolioNotFoundException("존재하지 않는 포트폴리오입니다.");
                    });
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

        log.info("문의 저장 성공 - commentId: {}", savedComment.getId());

        return savedComment.getId();
    }
}
