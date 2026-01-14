package kr.java.java.domain.space.controller;

import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.space.dto.*;
import kr.java.java.domain.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/piece/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    public ResponseEntity<Void> createSpace(@RequestPart(value = "request") SpaceRequest request,
                                            @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                            @AuthenticationPrincipal CustomUserDetails userDetails ){
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("공간 등록 시도 - UserId : {}",userDetails.getUuid());
        spaceService.createSpace(request,images,userDetails.getUuid());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<SpaceListResponse>> getAllSpaces() {
        log.info("공간 전체 조회");
        List<SpaceListResponse> responses = spaceService.getAllSpaces();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceResponse> getSpace(@PathVariable Long id){
        log.info("공간 단건 조회 id = {}", id);
        SpaceResponse response = spaceService.getSpace(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<SpaceListResponse>> getAllSpacesByUserId(@AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("특정 유저의 공간 전체 조회 userId = {}",userDetails.getUuid());
        List<SpaceListResponse> responses = spaceService.getSpacesByUserId(userDetails.getUuid());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("공간 삭제 시도 - 공간ID: {}, 작성자: {}", id, userDetails.getUuid());
        spaceService.deleteSpace(id, userDetails.getUuid());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpaceResponse> updateSpace(@PathVariable Long id, @RequestBody SpaceUpdateRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("공간 전체 수정 시도 - ID: {}", id);
        SpaceResponse response = spaceService.updateSpace(id, request, userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SpaceResponse> updateSpacePartial(@PathVariable Long id, @RequestBody SpaceUpdateRequest request,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("공간 부분 수정 시도 - ID: {}", id);
        SpaceResponse response = spaceService.updateSpace(id, request, userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SpaceListResponse>> searchSpaces(
            @ModelAttribute SpaceSearchCondition condition
    ) {
        log.info("공간 검색 요청 - 조건: {}", condition);
        List<SpaceListResponse> responses = spaceService.searchSpaces(condition);
        return ResponseEntity.ok(responses);
    }
}
