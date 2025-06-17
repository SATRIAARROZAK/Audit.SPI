// Path: src/main/java/Audit/Auditing/controller/NotificationController.java
package Audit.Auditing.controller;

import Audit.Auditing.config.CustomUserDetails;
import Audit.Auditing.model.Notification;
import Audit.Auditing.repository.NotificationRepository;
import Audit.Auditing.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository; // Diperlukan untuk verifikasi

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        long count = notificationService.getUnreadNotificationCount(userDetails.getUser().getId());
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        List<Notification> notifications = notificationService.getNotificationsForUser(userDetails.getUser().getId());
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/mark-as-read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        notificationService.markAllAsRead(userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/delete-all")
    public ResponseEntity<Void> deleteAllNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        notificationService.deleteAllForUser(userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // Endpoint baru untuk menghapus notifikasi individual
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        Optional<Notification> notifOpt = notificationRepository.findById(id);
        if (notifOpt.isPresent() && notifOpt.get().getRecipient().getId().equals(userDetails.getUser().getId())) {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.status(403).build(); // Forbidden (bukan notifikasi milik user)
    }
}