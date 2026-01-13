package kr.java.java.domain.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {

    SseEmitter saveEmitter(String emitterId, SseEmitter sseEmitter);

    void saveEventCache(String eventCacheId, Object event);

    Map<String, SseEmitter> findAllEmitterStartWithUserId(String userId);

    Map<String, Object> findAllEventCacheStartWithUserId(String userId);

    void deleteEmitterById(String emitterId);

    void deleteAllEmitterStartWithUserId(String userId);

    void deleteAllEventCacheStartWithUserId(String userId);
}
