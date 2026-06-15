package com.engagementservice.listener;

import com.engagementservice.config.RabbitConfig;
import com.engagementservice.model.Notification;
import com.engagementservice.repository.NotificationRepository;
import com.engagementservice.service.NotificationService;
import com.engagementservice.types.NotificationStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.engagement.mode", havingValue = "server", matchIfMissing = true)
public class NotificationListener {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public NotificationListener(NotificationService notificationService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void handleNotification(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getStatus() == NotificationStatus.SENT) {
            return;
        }

        notification.setStatus(NotificationStatus.PROCESSING);

        notificationService.sendWithRetry(notification);
    }
}
