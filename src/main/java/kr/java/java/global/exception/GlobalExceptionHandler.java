package kr.java.java.global.exception;

import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.image.exception.ImageException;
import kr.java.java.domain.matching.exception.MatchingException;
import kr.java.java.domain.notification.exception.NotificationException;
import kr.java.java.domain.notification.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(ImageException.class)
    public ResponseEntity<Map<String, Object>> handleImageException(ImageException e) {
        log.error("[ImageException] {} : {}", e.getErrorCode().name(), e.getErrorCode().getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("code", e.getErrorCode().name());
        body.put("message", e.getErrorCode().getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("[ValidationException] {}", e.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("code", "INVALID_INPUT");

        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError != null) {
            body.put("message", fieldError.getDefaultMessage());
        } else {
            body.put("message", "입력값이 올바르지 않습니다.");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(NotificationException.class)
    public  ResponseEntity<Map<String, Object>> handleNotificationException(NotificationException e) {
        log.error("[NotificationException] {} : {}", e.getErrorCode().name(), e.getErrorCode().getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("code", e.getErrorCode().name());
        body.put("message", e.getErrorCode().getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(body);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        log.error("UserNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException e) {
        log.error("[AuthenticationException] {}", e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("code", "AUTHENTICATION_FAILED");
        body.put("message", "인증에 실패했습니다: " + e.getMessage());
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] {}", e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("code", "INVALID_INPUT");
        body.put("message", "잘못된 입력값입니다: " + e.getMessage());
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body(body);
    }
}
