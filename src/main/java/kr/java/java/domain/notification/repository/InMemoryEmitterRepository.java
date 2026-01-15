package kr.java.java.domain.notification.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryEmitterRepository implements EmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCaches = new ConcurrentHashMap<>();
    private static final int MAX_EVENT_CACHE_SIZE = 5; // 최신 5개만 유지

    @Override
    public SseEmitter saveEmitter(String emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);
        return sseEmitter;
    }

    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        eventCaches.put(eventCacheId, event);
        
        // 사용자 ID 추출 (eventCacheId 형식: userId_timestamp)
        String userId = eventCacheId.split("_")[0];
        trimEventCache(userId);
    }

    private void trimEventCache(String userId) {
        Map<String, Object> userEvents = eventCaches.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (userEvents.size() > MAX_EVENT_CACHE_SIZE) {
            userEvents.keySet().stream()
                    .sorted() // timestamp 기준 오름차순 정렬 (오래된 순)
                    .limit(userEvents.size() - MAX_EVENT_CACHE_SIZE) // 삭제할 개수만큼 선택
                    .forEach(eventCaches::remove);
        }
    }

    @Override
    public Map<String, SseEmitter> findAllEmitterStartWithUserId(String userId) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> findAllEventCacheStartWithUserId(String userId) {
        return eventCaches.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void deleteEmitterById(String emitterId) {
        emitters.remove(emitterId);
    }

    @Override
    public void deleteAllEmitterStartWithUserId(String userId) {
        emitters.forEach(
                (key, emitter) -> {
                    if (key.startsWith(userId)) {
                        emitters.remove(key);
                    }
                }
        );
    }

    @Override
    public void deleteAllEventCacheStartWithUserId(String userId) {
        eventCaches.forEach(
                (key, event) -> {
                    if (key.startsWith(userId)) {
                        eventCaches.remove(key);
                    }
                }
        );
    }
}
