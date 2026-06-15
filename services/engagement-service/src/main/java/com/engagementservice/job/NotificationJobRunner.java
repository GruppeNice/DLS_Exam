package com.engagementservice.job;

import com.engagementservice.config.RabbitConfig;
import com.engagementservice.model.Notification;
import com.engagementservice.repository.NotificationRepository;
import com.engagementservice.service.NotificationService;
import com.engagementservice.types.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.engagement.mode", havingValue = "job")
public class NotificationJobRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NotificationJobRunner.class);
    private static final long RECEIVE_TIMEOUT_MS = 5_000L;

    private final RabbitTemplate rabbitTemplate;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ConfigurableApplicationContext applicationContext;

    public NotificationJobRunner(
        RabbitTemplate rabbitTemplate,
        NotificationRepository notificationRepository,
        NotificationService notificationService,
        ConfigurableApplicationContext applicationContext
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer notificationId = (Integer) rabbitTemplate.receiveAndConvert(
            RabbitConfig.NOTIFICATION_QUEUE,
            RECEIVE_TIMEOUT_MS
        );

        if (notificationId == null) {
            log.info("No messages on {}, exiting", RabbitConfig.NOTIFICATION_QUEUE);
            exit(0);
            return;
        }

        try {
            processNotification(notificationId);
            exit(0);
        } catch (Exception exception) {
            log.error("Failed to process notification {}", notificationId, exception);
            exit(1);
        }
    }

    private void processNotification(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getStatus() == NotificationStatus.SENT) {
            log.info("Notification {} already sent, skipping", notificationId);
            return;
        }

        notification.setStatus(NotificationStatus.PROCESSING);
        notificationRepository.save(notification);
        notificationService.sendWithRetry(notification);
        log.info("Notification {} processed", notificationId);
    }

    private void exit(int code) {
        System.exit(org.springframework.boot.SpringApplication.exit(applicationContext, () -> code));
    }
}
