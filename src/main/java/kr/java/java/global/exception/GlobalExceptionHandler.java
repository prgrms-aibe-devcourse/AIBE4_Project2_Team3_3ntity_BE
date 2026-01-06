package kr.java.java.global.exception;

import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.matching.exception.MatchingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MatchingException.class)
    public ResponseEntity<Map<String, Object>> handleMatchingException(MatchingException e) {
        log.error("[MatchingException] {} : {}", e.getErrorCode().name(), e.getErrorCode().getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("code", e.getErrorCode().name());
        body.put("message", e.getErrorCode().getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(body);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException e) {
        log.error("[AuthException] {} : {}", e.getErrorCode().name(), e.getErrorCode().getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("code", e.getErrorCode().name());
        body.put("message", e.getErrorCode().getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(body);
    }
}
