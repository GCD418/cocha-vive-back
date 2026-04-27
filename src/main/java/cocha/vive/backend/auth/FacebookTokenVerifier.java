package cocha.vive.backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookTokenVerifier {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${facebook.app-id}")
    private String facebookAppId;

    @Value("${facebook.app-secret:#{null}}")
    private String facebookAppSecret;

    public FacebookPayload verify(String token) {
        try {
            log.debug("Verifying Facebook token");

            String url = "https://graph.facebook.com/me?" +
                    "fields=id,name,first_name,last_name,email,picture.width(256).height(256)" +
                    "&access_token=" + token;

            log.debug("Calling Facebook API: {}", url.replaceAll("access_token=.*", "access_token=***"));

            String response = restTemplate.getForObject(url, String.class);

            log.debug("Facebook response received: {}", response);

            JsonNode node = objectMapper.readTree(response);

            if (!node.has("id")) {
                log.error("No ID in Facebook response: {}", response);
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
            log.error("Failed to verify Facebook token", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid Facebook token" +  e.getMessage(), e);
        }
    }
}
