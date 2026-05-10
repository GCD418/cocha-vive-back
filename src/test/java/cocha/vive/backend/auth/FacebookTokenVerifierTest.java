package cocha.vive.backend.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FacebookTokenVerifier Tests")
public class FacebookTokenVerifierTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private FacebookTokenVerifier facebookTokenVerifier;

    private String validPersonResponse;
    private String validPageResponse;
    private String invalidResponse;

    @BeforeEach
    void setUp() {
        validPersonResponse = """
            {
              "id": "123456789",
              "name": "John Doe",
              "first_name": "John",
              "last_name": "Doe",
              "email": "john@example.com",
              "picture": {
                "data": {
                  "url": "https://example.com/photo.jpg",
                  "width": 256,
                  "height": 256
                }
              }
            }""";

        validPageResponse = """
            {
              "id": "987654321",
              "name": "Test Organization",
              "picture": {
                "data": {
                  "url": "https://example.com/logo.jpg",
                  "width": 256,
                  "height": 256
                }
              }
            }""";

        invalidResponse = """
            {
              "name": "John Doe"
            }""";
    }

    private void mockRestClientResponse(String responseBody) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(responseBody);
    }

    @Test
    @DisplayName("Should verify valid person token and return FacebookPayload")
    void testVerifyValidPersonToken() {
        mockRestClientResponse(validPersonResponse);

        FacebookPayload payload = facebookTokenVerifier.verify("valid_person_token");

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
        mockRestClientResponse(validPageResponse);

        FacebookPayload payload = facebookTokenVerifier.verify("valid_page_token");

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
        mockRestClientResponse(invalidResponse);

        assertThrows(IllegalArgumentException.class, () ->
            facebookTokenVerifier.verify("invalid_token"));
    }

    @Test
    @DisplayName("Should throw exception for malformed JSON response")
    void testVerifyMalformedResponse() {
        mockRestClientResponse("{invalid json}");

        assertThrows(IllegalArgumentException.class, () ->
            facebookTokenVerifier.verify("token_with_malformed_response"));
    }

    @Test
    @DisplayName("Should throw exception when REST call fails")
    void testVerifyNetworkError() {
        when(restClient.get()).thenThrow(new org.springframework.web.client.RestClientException("Network error"));

        assertThrows(IllegalArgumentException.class, () ->
            facebookTokenVerifier.verify("token_causing_network_error"));
    }

    @Test
    @DisplayName("Should detect person by presence of first_name or last_name")
    void testIsPerson() {
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

        assertTrue(personPayload.isPerson());
    }

    @Test
    @DisplayName("Should detect page by absence of first_name and last_name")
    void testIsPage() {
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

        assertFalse(pagePayload.isPerson());
    }
}
