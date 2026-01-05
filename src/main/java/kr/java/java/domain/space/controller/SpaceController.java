package kr.java.java.domain.space.controller;

import kr.java.java.domain.space.dto.SpaceRequest;
import kr.java.java.domain.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping("/api/space")
    public ResponseEntity<Void> createSpace(@RequestBody SpaceRequest request, Long loginUserId){
        log.info("공간 등록 시도 - UserId : {}",loginUserId);
        spaceService.createSpace(request,loginUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
