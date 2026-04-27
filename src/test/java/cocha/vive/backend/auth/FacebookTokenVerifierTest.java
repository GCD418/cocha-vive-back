package cocha.vive.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FacebookTokenVerifier Tests")
public class FacebookTokenVerifierTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FacebookTokenVerifier facebookTokenVerifier;

    private String validPersonResponse;
    private String validPageResponse;
    private String invalidResponse;

    @BeforeEach
    void setUp() {
        // Inicializar ObjectMapper real
        objectMapper = org.mockito.Mockito.spy(new com.fasterxml.jackson.databind.ObjectMapper());

        // Response válida para una persona
        validPersonResponse = "{\n" +
            "  \"id\": \"123456789\",\n" +
            "  \"name\": \"John Doe\",\n" +
            "  \"first_name\": \"John\",\n" +
            "  \"last_name\": \"Doe\",\n" +
            "  \"email\": \"john@example.com\",\n" +
            "  \"picture\": {\n" +
            "    \"data\": {\n" +
            "      \"url\": \"https://example.com/photo.jpg\",\n" +
            "      \"width\": 256,\n" +
            "      \"height\": 256\n" +
            "    }\n" +
            "  }\n" +
            "}";

        // Response válida para una página
        validPageResponse = "{\n" +
            "  \"id\": \"987654321\",\n" +
            "  \"name\": \"Test Organization\",\n" +
            "  \"picture\": {\n" +
            "    \"data\": {\n" +
            "      \"url\": \"https://example.com/logo.jpg\",\n" +
            "      \"width\": 256,\n" +
            "      \"height\": 256\n" +
            "    }\n" +
            "  }\n" +
            "}";

        // Response inválida (sin ID)
        invalidResponse = "{\n" +
            "  \"name\": \"John Doe\"\n" +
            "}";
    }

    @Test
    @DisplayName("Should verify valid person token and return FacebookPayload")
    void testVerifyValidPersonToken() {
        // Given
        String token = "valid_person_token";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(validPersonResponse);

        // When
        FacebookPayload payload = facebookTokenVerifier.verify(token);

        // Then
        assertNotNull(payload);
        assertEquals("123456789", payload.getId());
        assertEquals("John Doe", payload.getName());
        assertEquals("John", payload.getFirstName());
        assertEquals("Doe", payload.getLastName());
        assertEquals("john@example.com", payload.getEmail());
        assertTrue(payload.isPerson());
    }

    @Test
    @DisplayName("Should verify valid page token and return FacebookPayload")
    void testVerifyValidPageToken() {
        // Given
        String token = "valid_page_token";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(validPageResponse);

        // When
        FacebookPayload payload = facebookTokenVerifier.verify(token);

        // Then
        assertNotNull(payload);
        assertEquals("987654321", payload.getId());
        assertEquals("Test Organization", payload.getName());
        assertNull(payload.getFirstName());
        assertNull(payload.getLastName());
        assertNull(payload.getEmail());
        assertFalse(payload.isPerson());
    }

    @Test
    @DisplayName("Should throw exception for invalid token (no ID in response)")
    void testVerifyInvalidTokenNoId() {
        // Given
        String token = "invalid_token";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(invalidResponse);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            facebookTokenVerifier.verify(token);
        });
    }

    @Test
    @DisplayName("Should throw exception for malformed JSON response")
    void testVerifyMalformedResponse() {
        // Given
        String token = "token_with_malformed_response";
        String malformedResponse = "{invalid json}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(malformedResponse);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            facebookTokenVerifier.verify(token);
        });
    }

    @Test
    @DisplayName("Should throw exception when REST call fails")
    void testVerifyNetworkError() {
        // Given
        String token = "token_causing_network_error";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenThrow(new org.springframework.web.client.RestClientException("Network error"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            facebookTokenVerifier.verify(token);
        });
    }

    @Test
    @DisplayName("Should detect person by presence of first_name or last_name")
    void testIsPerson() {
        // Given - Person with firstName
        FacebookPayload.Picture picture = new FacebookPayload.Picture();
        picture.setData(new FacebookPayload.Picture.PictureData());
        picture.getData().setUrl("https://example.com/photo.jpg");

        FacebookPayload personPayload = FacebookPayload.builder()
            .id("123456789")
            .name("John Doe")
            .firstName("John")
            .lastName(null)
            .picture(picture)
            .build();

        // When & Then
        assertTrue(personPayload.isPerson());
    }

    @Test
    @DisplayName("Should detect page by absence of first_name and last_name")
    void testIsPage() {
        // Given - Page without firstName/lastName
        FacebookPayload.Picture picture = new FacebookPayload.Picture();
        picture.setData(new FacebookPayload.Picture.PictureData());
        picture.getData().setUrl("https://example.com/logo.jpg");

        FacebookPayload pagePayload = FacebookPayload.builder()
            .id("987654321")
            .name("Test Organization")
            .firstName(null)
            .lastName(null)
            .picture(picture)
            .build();

        // When & Then
        assertFalse(pagePayload.isPerson());
    }
}
