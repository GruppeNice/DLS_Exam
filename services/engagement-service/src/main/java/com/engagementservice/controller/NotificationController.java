package com.engagementservice.controller;

import com.engagementservice.dto.NotificationRequest;
import com.engagementservice.model.Notification;
import com.engagementservice.repository.NotificationRepository;
import com.engagementservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationService notificationService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> send(@RequestBody NotificationRequest request) {
        Integer notificationId = notificationService.queueNotification(request);

        if(notificationId == null || notificationId == 0) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "queued");
        response.put("message", "Notification will be sent shortly");
        response.put("notificationId", notificationId);

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getStatus(@PathVariable Integer id) {
        return notificationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
