package kr.java.java.domain.notification.repository;

import kr.java.java.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiverId = :userId AND n.isRead = false")
    long countUnreadNotificationsByUserId(@Param("userId") UUID userId);

    // 미확인 알림 조회 : lastId가 없으면 전체 조회, 있으면 그보다 작은 것 조회
    @Query("SELECT n FROM Notification n WHERE n.receiverId = :userId AND n.isRead = false AND (:lastId IS NULL OR n.id < :lastId) ORDER BY n.id DESC")
    List<Notification> findUnreadNotifications(@Param("userId") UUID userId,
                                               @Param("lastId") Long lastId,
                                               Pageable pageable);

    // 지난 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.receiverId = :userId AND n.isRead = true AND (:lastId IS NULL OR n.id < :lastId) ORDER BY n.id DESC")
    List<Notification> findReadNotifications(@Param("userId") UUID userId,
                                             @Param("lastId") Long lastId,
                                             Pageable pageable);

    // 사용자별 알림 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.receiverId = :receiverId")
    void deleteByReceiverId(@Param("receiverId") Long receiverId);
}
