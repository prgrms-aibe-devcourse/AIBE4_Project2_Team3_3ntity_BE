package kr.java.java.global.exception;

import kr.java.java.domain.matching.exception.MatchingException;
import kr.java.java.domain.notification.exception.NotificationException;
import kr.java.java.domain.notification.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
}
