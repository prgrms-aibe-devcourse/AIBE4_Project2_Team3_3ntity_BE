package kr.java.java.domain.notification.repository;

import kr.java.java.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n WHERE n.userId.id = :userId ORDER BY n.isRead ASC, n.createdAt DESC")
    List<Notification> findTop25ByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId.id = :userId AND n.isRead = false")
    long countUnreadNotificationsByUserId(@Param("userId") Long userId);
}
