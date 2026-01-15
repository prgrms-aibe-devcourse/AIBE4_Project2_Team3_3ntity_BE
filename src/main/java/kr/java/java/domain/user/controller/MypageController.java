package kr.java.java.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.UUID;


@RestController
@RequestMapping("/piece/mypages")
@RequiredArgsConstructor
@Tag(name = "Mypage", description = "마이페이지 API")
public class MypageController {

    private final MypageService mypageService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 마이페이지 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MypageResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<MypageResponse> getMypage(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        MypageResponse response = mypageService.getMypage(userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "유저 프로필 조회", description = "UUID를 통해 특정 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MypageResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/users/{userUuid}")
    public ResponseEntity<MypageResponse> getUserProfile(
            @Parameter(description = "조회할 유저의 UUID", required = true) @PathVariable UUID userUuid) {
        MypageResponse response = mypageService.getMypage(userUuid);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 수정", description = "닉네임 정보와 프로필 이미지를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = MypageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MypageResponse> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수정할 프로필 정보", required = false) @RequestPart(value = "request", required = false) @Valid ProfileUpdateRequest request,
            @Parameter(description = "새 프로필 이미지 파일", required = false) @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        MypageResponse response = mypageService.updateProfile(userDetails.getUuid(), request, file);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 이미지 삭제", description = "현재 설정된 프로필 이미지를 삭제하고 기본 이미지로 변경합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/profile/image")
    public ResponseEntity<MypageResponse> deleteProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        MypageResponse response = mypageService.deleteProfileImage(userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴", description = "회원 정보를 삭제하고 탈퇴 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "탈퇴 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        mypageService.deleteUser(userDetails.getUuid());
        return ResponseEntity.noContent().build();
    }

}
