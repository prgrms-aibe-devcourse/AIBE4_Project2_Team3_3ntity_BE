package kr.java.java.domain.notification.repository;

import kr.java.java.domain.notification.entity.Notification;
import kr.java.java.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserId_IdOrderByCreatedAtDesc(Long user);
}
