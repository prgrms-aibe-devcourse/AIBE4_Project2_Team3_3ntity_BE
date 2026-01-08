package kr.java.java.domain.user.controller;

import jakarta.validation.Valid;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.portfolio.dto.PortfolioResponse;
import kr.java.java.domain.review.dto.ReviewResponse;
import kr.java.java.domain.space.dto.SpaceListResponse;
import kr.java.java.domain.user.dto.MypageResponse;
import kr.java.java.domain.user.dto.ProfileUpdateRequest;
import kr.java.java.domain.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/piece/mypages")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    @GetMapping
    public ResponseEntity<MypageResponse> getMypage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MypageResponse response = mypageService.getMypage(userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

}
