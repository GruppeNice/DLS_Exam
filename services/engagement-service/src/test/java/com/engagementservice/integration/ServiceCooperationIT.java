package com.engagementservice.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.engagementservice.EngagementServiceApplication;
import com.engagementservice.dto.NotificationRequest;
import com.engagementservice.service.NotificationService;
import com.engagementservice.service.UserEmailResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
    classes = EngagementServiceApplication.class,
    properties = {
        "app.messaging.billing-exchange=billing.events",
        "app.messaging.streaming-exchange=streaming.events",
        "app.messaging.catalog-exchange=catalog.events",
        "app.messaging.review-exchange=review.events",
        "app.engagement.broadcast-recipient=demo@dls.local"
    }
)
@ActiveProfiles("test")
@DisplayName("System Cooperation: Billing Service -> Engagement Service via RabbitMQ")
class ServiceCooperationIT {

    private static final String BILLING_JWT_SECRET_BASE64 =
        "VGVzdGluZ0Rldk9ubHlTZWNyZXRLZXlGb3JKV1QxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMw==";
    private static final int BILLING_PORT = 18084;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static Process billingProcess;

    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.12-alpine")
        .withExposedPorts(5672, 15672);

    @Container
    static GenericContainer<?> billingDbContainer = new GenericContainer<>("mysql:8.4")
        .withEnv("MYSQL_DATABASE", "billing_db")
        .withEnv("MYSQL_USER", "billing_user")
        .withEnv("MYSQL_PASSWORD", "billing_password")
        .withEnv("MYSQL_ROOT_PASSWORD", "root")
        .withExposedPorts(3306)
        .waitingFor(Wait.forLogMessage(".*ready for connections.*\\n", 1));

    @MockitoBean
    private UserEmailResolver userEmailResolver;

    @MockitoBean
    private NotificationService notificationService;

    @DynamicPropertySource
    static void rabbitmqProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @BeforeAll
    static void startBillingService() throws Exception {
        Path repoRoot = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize().getParent().getParent();
        Path billingJar = repoRoot.resolve("services/billing-service/target/billing-service-0.0.1-SNAPSHOT.jar");
        if (!billingJar.toFile().exists()) {
            buildBillingJar(repoRoot);
        }

        ProcessBuilder pb = new ProcessBuilder(
            javaExecutable(),
            "-jar",
            billingJar.toString()
        );
        Map<String, String> env = pb.environment();
        env.put("SERVER_PORT", Integer.toString(BILLING_PORT));
        env.put("DB_URL", "jdbc:mysql://localhost:" + billingDbContainer.getMappedPort(3306)
            + "/billing_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC");
        env.put("DB_USERNAME", "billing_user");
        env.put("DB_PASSWORD", "billing_password");
        env.put("RABBITMQ_HOST", rabbitContainer.getHost());
        env.put("RABBITMQ_PORT", Integer.toString(rabbitContainer.getAmqpPort()));
        env.put("RABBITMQ_USERNAME", "guest");
        env.put("RABBITMQ_PASSWORD", "guest");
        env.put("BILLING_EVENTS_EXCHANGE", "billing.events");
        env.put("JWT_SECRET_BASE64", BILLING_JWT_SECRET_BASE64);
        env.put("PAYMENT_SIMULATED_FAILURE_RATE", "0.0");

        File logFile = repoRoot.resolve("services/engagement-service/target/billing-service-it.log").toFile();
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));

        billingProcess = pb.start();
        waitForBillingHealth();
    }

    @AfterAll
    static void stopBillingService() throws Exception {
        if (billingProcess != null && billingProcess.isAlive()) {
            billingProcess.destroy();
            billingProcess.waitFor(10, TimeUnit.SECONDS);
            if (billingProcess.isAlive()) {
                billingProcess.destroyForcibly();
            }
        }
    }

    @Test
    @DisplayName("Billing service activates subscription and Engagement service consumes event")
    void billingToEngagementSubscriptionActivatedFlow() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userEmailResolver.resolve(userId)).thenReturn("user@example.com");

        String planId = fetchAnyBillingPlanId();
        String jwt = createBillingJwt(userId, "user@example.com");

        HttpRequest activate = HttpRequest.newBuilder()
            .uri(URI.create(billingBaseUrl() + "/api/v1/subscriptions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + jwt)
            .header("Idempotency-Key", UUID.randomUUID().toString())
            .POST(HttpRequest.BodyPublishers.ofString("{\"planId\":\"" + planId + "\"}"))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(activate, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Billing service should activate subscription");

        verify(notificationService, timeout(15000).atLeastOnce())
            .queueNotification(any(NotificationRequest.class));
    }

    private String fetchAnyBillingPlanId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(billingBaseUrl() + "/api/v1/plans"))
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Billing plans endpoint should be available");

        JsonNode payload = OBJECT_MAPPER.readTree(response.body());
        assertTrue(payload.isArray() && !payload.isEmpty(), "Expected at least one active billing plan");
        return payload.get(0).get("id").asText();
    }

    private static void waitForBillingHealth() throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        Instant deadline = Instant.now().plus(Duration.ofMinutes(3));
        while (Instant.now().isBefore(deadline)) {
            if (billingProcess != null && !billingProcess.isAlive()) {
                throw new IllegalStateException("Billing service process exited before becoming healthy");
            }
            try {
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(billingBaseUrl() + "/actuator/health"))
                    .GET()
                    .build();
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 200 && res.body().contains("UP")) {
                    return;
                }
            } catch (Exception ignored) {
                // keep retrying until timeout
            }
            Thread.sleep(1500);
        }
        throw new IllegalStateException("Billing service did not become healthy in time");
    }

    private static void buildBillingJar(Path repoRoot) throws Exception {
        ProcessBuilder pb;
        if (isWindows()) {
            String mvnw = repoRoot.resolve("services/engagement-service/mvnw.cmd").toString();
            String cmd = "\"" + mvnw + "\" -B -f \"" + repoRoot.resolve("services/billing-service/pom.xml")
                + "\" -DskipTests package";
            pb = new ProcessBuilder("cmd.exe", "/c", cmd);
        } else {
            String mvnw = repoRoot.resolve("services/engagement-service/mvnw").toString();
            String cmd = mvnw + " -B -f \"" + repoRoot.resolve("services/billing-service/pom.xml")
                + "\" -DskipTests package";
            pb = new ProcessBuilder("/bin/sh", "-lc", cmd);
        }
        pb.directory(repoRoot.toFile());
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(
            repoRoot.resolve("services/engagement-service/target/billing-build-it.log").toFile()
        ));

        Process process = pb.start();
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IllegalStateException("Failed to build billing-service jar for cooperation test");
        }
    }

    private static String billingBaseUrl() {
        return "http://localhost:" + BILLING_PORT;
    }

    private static String createBillingJwt(UUID userId, String email) throws Exception {
        long now = Instant.now().getEpochSecond();
        long exp = now + 600;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = "{"
            + "\"sub\":\"" + email + "\","
            + "\"uid\":\"" + userId + "\","
            + "\"roles\":[\"USER\"],"
            + "\"iat\":" + now + ","
            + "\"exp\":" + exp
            + "}";

        String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = encodedHeader + "." + encodedPayload;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(Base64.getDecoder().decode(BILLING_JWT_SECRET_BASE64), "HmacSHA256"));
        String signature = base64UrlEncode(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));

        return signingInput + "." + signature;
    }

    private static String base64UrlEncode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static String javaExecutable() {
        return Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java").toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}

