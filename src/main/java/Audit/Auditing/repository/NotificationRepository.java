package Audit.Auditing.repository;

import Audit.Auditing.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // GANTI CreatedAt menjadi Timestamp
    List<Notification> findByRecipientIdAndIsReadFalseOrderByTimestampDesc(Long recipientId);

    // GANTI CreatedAt menjadi Timestamp
    List<Notification> findByRecipientIdOrderByTimestampDesc(Long recipientId);
    
    long countByRecipientIdAndIsReadFalse(Long recipientId);
}