package cocha.vive.backend.auth;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookTokenVerifier {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public FacebookPayload verify(String token) {
        try {
            log.debug("Verifying Facebook token");

            URI uri = UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/me")
                .queryParam("fields", "id,name,first_name,last_name,email,picture.width(256).height(256)")
                .queryParam("access_token", token)
                .build()
                .toUri();

            String response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

            JsonNode node = objectMapper.readTree(response);

            if (!node.has("id")) {
                log.error("No ID in Facebook response");
                throw new IllegalArgumentException("Invalid Facebook token: no ID in response");
            }

            FacebookPayload payload = objectMapper.treeToValue(node, FacebookPayload.class);

            log.info("Facebook user/page verified: id={}, isPerson={}",
                    payload.getId(), payload.isPerson());

            return payload;

        } catch (IllegalArgumentException e) {
            log.error("Facebook token validation error: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Failed to verify Facebook token", e);
            throw new IllegalArgumentException("Invalid Facebook token: " +  e.getMessage(), e);
        }
    }
}
