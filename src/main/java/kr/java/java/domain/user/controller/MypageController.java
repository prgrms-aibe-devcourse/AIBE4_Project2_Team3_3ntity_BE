package kr.java.java.domain.user.controller;

import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.portfolio.dto.PortfolioResponse;
import kr.java.java.domain.review.dto.ReviewResponse;
import kr.java.java.domain.space.dto.SpaceListResponse;
import kr.java.java.domain.user.dto.MypageResponse;
import kr.java.java.domain.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/piece/mypages")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    // 마이페이지 메인
    @GetMapping
    public ResponseEntity<MypageResponse> getMypage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MypageResponse response = mypageService.getMypage(userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    // 내가 올린 공간 게시글 목록
    @GetMapping("/spaces")
    public ResponseEntity<List<SpaceListResponse>> getMySpaces(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SpaceListResponse> spaces = mypageService.getMySpaces(userDetails.getUuid());
        return ResponseEntity.ok(spaces);
    }

    // 내가 올린 포트폴리오 목록
    @GetMapping("/portfolios")
    public ResponseEntity<List<PortfolioResponse>> getMyPortfolios(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PortfolioResponse> portfolios = mypageService.getMyPortfolios(userDetails.getUuid());
        return ResponseEntity.ok(portfolios);
    }

    // 내가 쓴 리뷰 목록
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReviewResponse> reviews = mypageService.getMyReviews(userDetails.getUuid());
        return ResponseEntity.ok(reviews);
    }

    // 내 매칭 목록 - 전체 (상태별 필터)
    @GetMapping("/matchings")
    public ResponseEntity<List<MatchingResponse>> getMyMatchings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) MatchStatus status) {
        List<MatchingResponse> matchings = mypageService.getMyMatchings(
                userDetails.getUuid(), status);
        return ResponseEntity.ok(matchings);
    }

}
