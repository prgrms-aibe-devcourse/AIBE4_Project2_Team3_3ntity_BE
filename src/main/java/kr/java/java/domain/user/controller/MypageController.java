package kr.java.java.domain.user.controller;

import jakarta.validation.Valid;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.user.dto.MypageResponse;
import kr.java.java.domain.user.dto.ProfileUpdateRequest;
import kr.java.java.domain.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


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

    @PutMapping("/profile")
    public ResponseEntity<MypageResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        MypageResponse response = mypageService.updateProfile(userDetails.getUuid(), request);
        return ResponseEntity.ok(response);
    }

}
