package Audit.Auditing.service;

import Audit.Auditing.model.Notification;
import Audit.Auditing.model.User;
import Audit.Auditing.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(User recipient, String message, String link) {
        Notification notification = new Notification(recipient, message, link);
        notificationRepository.save(notification);
    }

    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByTimestampDesc(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByTimestampDesc(userId);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
}