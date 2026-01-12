package kr.java.java.domain.space.controller;

import kr.java.java.domain.space.dto.*;
import kr.java.java.domain.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> createSpace(@RequestPart(value = "request") SpaceRequest request, Long loginUserId,
                                            @RequestPart(value = "images", required = false) List<MultipartFile> images){
        log.info("공간 등록 시도 - UserId : {}",loginUserId);
        //TODO 인증이 완성되지 않아서 임시 테스트용으로 더미데이터를 직접 삽입
        spaceService.createSpace(request,images,1L);
        // TODO 응답바디에 DTO를 넣어서 어떤 데이터를 저장했는지 확인하도록 수정예정
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
    public ResponseEntity<List<SpaceListResponse>> getAllSpacesByUserId(@PathVariable Long userId){
        log.info("특정 유저의 공간 전체 조회 userId = {}",userId);
        List<SpaceListResponse> responses = spaceService.getSpacesByUserId(1L);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id, Long userId){
        log.info("공간 삭제 시도 - 공간ID: {}, 작성자: {}", id, userId);
        spaceService.deleteSpace(id, 1L);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpaceResponse> updateSpace(@PathVariable Long id, @RequestBody SpaceUpdateRequest request, Long userId) {
        log.info("공간 전체 수정 시도 - ID: {}", id);
        SpaceResponse response = spaceService.updateSpace(id, request, 1L);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SpaceResponse> updateSpacePartial(@PathVariable Long id, @RequestBody SpaceUpdateRequest request, Long userId) {
        log.info("공간 부분 수정 시도 - ID: {}", id);
        SpaceResponse response = spaceService.updateSpace(id, request, 1L);
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
