package kr.java.java.domain.space.controller;

import kr.java.java.domain.space.dto.SpaceListResponse;
import kr.java.java.domain.space.dto.SpaceRequest;
import kr.java.java.domain.space.dto.SpaceResponse;
import kr.java.java.domain.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping("/piece/spaces")
    public ResponseEntity<Void> createSpace(@RequestBody SpaceRequest request, Long loginUserId){
        log.info("공간 등록 시도 - UserId : {}",loginUserId);
        //TODO 인증이 완성되지 않아서 임시 테스트용으로 더미데이터를 직접 삽입
        spaceService.createSpace(request,1L);
        // TODO 응답바디에 DTO를 넣어서 어떤 데이터를 저장했는지 확인하도록 수정예정
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/piece/spaces")
    public ResponseEntity<List<SpaceListResponse>> getAllSpaces() {
        log.info("공간 전체 조회");
        List<SpaceListResponse> responses = spaceService.getAllSpaces();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/piece/spaces/{id}")
    public ResponseEntity<SpaceResponse> getSpace(@PathVariable Long id){
        log.info("공간 단건 조회 id = {}", id);
        SpaceResponse response = spaceService.getSpace(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/piece/spaces/users/{userId}")
    public ResponseEntity<List<SpaceListResponse>> getAllSpacesByUserId(@PathVariable Long userId){
        log.info("특정 유저의 공간 전체 조회 userId = {}",userId);
        List<SpaceListResponse> responses = spaceService.getSpacesByUserId(1L);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/piece/spaces/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id, Long userId){
        log.info("공간 삭제 시도 - 공간ID: {}, 작성자: {}", id, userId);
        spaceService.deleteSpace(id, 1L);
        return ResponseEntity.noContent().build();
    }


}
