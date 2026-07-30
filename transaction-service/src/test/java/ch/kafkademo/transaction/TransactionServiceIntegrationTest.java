package ch.kafkademo.transaction;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * End-to-end security integration test.
 *
 * <p>Spins up a real Kafka broker and a real Keycloak identity provider (with the
 * {@code kafkademo} realm imported), then exercises the secured REST endpoint with and
 * without valid OAuth 2.0 / OpenID Connect JWT bearer tokens.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionServiceIntegrationTest {

    private static final String REALM = "kafkademo";
    private static final String CLIENT_ID = "kafkademo-client";
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "alice";
    private static final int KEYCLOAK_PORT = 8080;

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"));

    @Container
    static GenericContainer<?> keycloak =
            new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:26.0"))
                    .withExposedPorts(KEYCLOAK_PORT)
                    .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
                    .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
                    .withEnv("KC_HEALTH_ENABLED", "true")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("keycloak/realm-export.json"),
                            "/opt/keycloak/data/import/realm-export.json")
                    .withCommand("start-dev", "--import-realm")
                    // The realm endpoint only returns 200 once the server is up and the
                    // realm has been imported.
                    .waitingFor(Wait.forHttp("/realms/" + REALM)
                            .forPort(KEYCLOAK_PORT)
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(3)));

    private static String keycloakBaseUrl() {
        return "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(KEYCLOAK_PORT);
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakBaseUrl() + "/realms/" + REALM);
    }

    @Value("${local.server.port}")
    private int port;

    @Test
    void postingATransactionWithAValidTokenPublishesAnEventToKafka() throws Exception {
        String token = obtainAccessToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/transactions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"customerId\": 1, \"amount\": 20.00}"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(202);

        ConsumerRecord<String, String> record = pollSingleRecord("transactions");
        assertThat(record.key()).isEqualTo("1");
        assertThat(record.value())
                .contains("\"customerId\":1")
                .contains("\"amount\":20.0");
    }

    @Test
    void postingATransactionWithoutATokenIsUnauthorized() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/transactions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"customerId\": 1, \"amount\": 20.00}"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void postingATransactionWithAnInvalidTokenIsUnauthorized() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/transactions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer not-a-real-jwt")
                .POST(HttpRequest.BodyPublishers.ofString("{\"customerId\": 1, \"amount\": 20.00}"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
    }

    /**
     * Uses Keycloak's Resource Owner Password Credentials grant to obtain an access
     * token for the test user. The imported realm grants the {@code transactions:write}
     * scope to this client by default.
     */
    private String obtainAccessToken() throws Exception {
        String form = "grant_type=password"
                + "&client_id=" + enc(CLIENT_ID)
                + "&username=" + enc(USERNAME)
                + "&password=" + enc(PASSWORD)
                + "&scope=" + enc("openid transactions:write");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakBaseUrl()
                        + "/realms/" + REALM + "/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);

        Matcher matcher = Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(response.body());
        assertThat(matcher.find()).as("access_token present in response").isTrue();
        return matcher.group(1);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private ConsumerRecord<String, String> pollSingleRecord(String topic) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer =
                     new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer())) {
            consumer.subscribe(List.of(topic));

            long deadline = System.currentTimeMillis() + Duration.ofSeconds(20).toMillis();
            while (System.currentTimeMillis() < deadline) {
                var records = consumer.poll(Duration.ofMillis(500));
                if (!records.isEmpty()) {
                    return records.iterator().next();
                }
            }
            throw new AssertionError("No record was produced to topic '" + topic + "'");
        }
    }
}
