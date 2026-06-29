package com.caputchin.keycloak.verify;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.secret.SecretResolver;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs the server-side {@code POST /v1/siteverify} call against the Caputchin verify host using
 * Keycloak's pooled HTTP client.
 *
 * <p>The request body is JSON ({@code {"secret": ..., "response": ...}}); the response is JSON
 * ({@code {"success": bool, "error-codes": [...]}}). The secret is resolved per request and is never
 * logged. The pooled client is owned by Keycloak and is never closed here.
 */
public final class SiteverifyClient {

    private static final Logger LOG = Logger.getLogger(SiteverifyClient.class);
    private static final String VERIFY_PATH = "/v1/siteverify";

    private SiteverifyClient() {
    }

    public static SiteverifyResult verify(KeycloakSession session, CaputchinConfig config, String token) {
        if (token == null || token.isBlank()) {
            // No token in the submission: a definitive negative, not an outage.
            return SiteverifyResult.fail("missing-input-response");
        }

        String secret = SecretResolver.resolve(session, config);
        if (secret == null) {
            LOG.error("Caputchin secret is not configured (set 'Secret key', 'Secret key env var', "
                    + "or a ${vault.*} reference). Treating verification as indeterminate.");
            return SiteverifyResult.error("missing-secret");
        }

        String url = config.verifyHost() + VERIFY_PATH;
        try {
            HttpClient httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();

            Map<String, String> body = new LinkedHashMap<>();
            body.put("secret", secret);
            body.put("response", token);

            HttpPost post = new HttpPost(url);
            post.setHeader("Accept", "application/json");
            post.setEntity(new StringEntity(JsonSerialization.writeValueAsString(body), ContentType.APPLICATION_JSON));

            HttpResponse response = httpClient.execute(post);
            try {
                int status = response.getStatusLine().getStatusCode();
                String payload = response.getEntity() == null
                        ? ""
                        : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (status < 200 || status >= 300) {
                    LOG.warnf("Caputchin siteverify returned HTTP %d. Treating as indeterminate.", status);
                    return SiteverifyResult.error("verify-http-" + status);
                }

                JsonNode json;
                try {
                    json = JsonSerialization.readValue(payload, JsonNode.class);
                } catch (IOException parseError) {
                    LOG.warnf("Caputchin siteverify returned an unparseable body: %s. Treating as indeterminate.",
                            parseError.getMessage());
                    return SiteverifyResult.error("verify-bad-response");
                }
                boolean success = json.hasNonNull("success") && json.get("success").asBoolean(false);
                if (success) {
                    return SiteverifyResult.pass();
                }

                List<String> codes = new ArrayList<>();
                JsonNode errorCodes = json.get("error-codes");
                if (errorCodes != null && errorCodes.isArray()) {
                    errorCodes.forEach(node -> codes.add(node.asText()));
                }
                return SiteverifyResult.fail(codes);
            } finally {
                if (response.getEntity() != null) {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
            }
        } catch (IOException e) {
            // Network error or timeout reaching the verify host: indeterminate.
            LOG.warnf("Caputchin siteverify call to %s failed: %s. Treating as indeterminate.", url, e.getMessage());
            return SiteverifyResult.error("verify-unreachable");
        }
    }
}
