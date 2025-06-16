package Audit.Auditing.repository;

import Audit.Auditing.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipientIdAndIsReadFalseOrderByTimestampDesc(Long recipientId);

    List<Notification> findByRecipientIdOrderByTimestampDesc(Long recipientId);
    
    long countByRecipientIdAndIsReadFalse(Long recipientId);
}