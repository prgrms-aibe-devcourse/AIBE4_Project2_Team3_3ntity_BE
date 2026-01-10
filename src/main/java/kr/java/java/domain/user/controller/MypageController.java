package kr.java.java.domain.user.controller;

import jakarta.validation.Valid;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.user.dto.MypageResponse;
import kr.java.java.domain.user.dto.ProfileUpdateRequest;
import kr.java.java.domain.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


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

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MypageResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "request", required = false) @Valid ProfileUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        MypageResponse response = mypageService.updateProfile(userDetails.getUuid(), request, file);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile/image")
    public ResponseEntity<MypageResponse> deleteProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MypageResponse response = mypageService.deleteProfileImage(userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

}
