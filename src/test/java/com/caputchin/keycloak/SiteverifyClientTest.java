package com.caputchin.keycloak;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.verify.SiteverifyClient;
import com.caputchin.keycloak.verify.SiteverifyResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;
import org.keycloak.vault.VaultStringSecret;
import org.keycloak.vault.VaultTranscriber;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SiteverifyClientTest {

    @Test
    void missingTokenIsDefinitiveFailure() {
        KeycloakSession session = mock(KeycloakSession.class);
        SiteverifyResult result = SiteverifyClient.verify(
                session, CaputchinConfig.of(Map.of(CaputchinConfig.SECRET_KEY, "s")), null);

        assertEquals(SiteverifyResult.Outcome.FAIL, result.outcome);
        // No token means we never reach out to the verify host.
        verifyNoInteractions(session);
    }

    @Test
    void missingSecretIsIndeterminate() {
        KeycloakSession session = mock(KeycloakSession.class);
        SiteverifyResult result = SiteverifyClient.verify(
                session, CaputchinConfig.of(Map.of()), "a-token");

        // No secret configured: indeterminate, so the fail-closed toggle decides (and blocks by default).
        assertEquals(SiteverifyResult.Outcome.ERROR, result.outcome);
    }

    @Test
    void bodyNamesTheKeycloakIntegrationAndNothingElse() throws Exception {
        KeycloakSession session = mock(KeycloakSession.class);
        HttpClientProvider provider = mock(HttpClientProvider.class);
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        VaultTranscriber vault = mock(VaultTranscriber.class);
        VaultStringSecret vaultSecret = mock(VaultStringSecret.class);
        when(session.vault()).thenReturn(vault);
        when(vault.getStringSecret("the-secret")).thenReturn(vaultSecret);
        when(vaultSecret.get()).thenReturn(Optional.of("the-secret"));
        when(session.getProvider(HttpClientProvider.class)).thenReturn(provider);
        when(provider.getHttpClient()).thenReturn(httpClient);
        when(response.getStatusLine().getStatusCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(
                new StringEntity("{\"success\":true}", ContentType.APPLICATION_JSON));
        ArgumentCaptor<HttpPost> request = ArgumentCaptor.forClass(HttpPost.class);
        when(httpClient.execute(request.capture())).thenReturn(response);

        SiteverifyResult result = SiteverifyClient.verify(session, CaputchinConfig.of(Map.of(
                CaputchinConfig.SECRET_KEY, "the-secret",
                CaputchinConfig.VERIFY_HOST, "https://verify.example.test")), "the-token");

        assertEquals(SiteverifyResult.Outcome.PASS, result.outcome);
        assertEquals("https://verify.example.test/v1/siteverify", request.getValue().getURI().toString());
        JsonNode body = JsonSerialization.readValue(
                EntityUtils.toString(request.getValue().getEntity(), StandardCharsets.UTF_8), JsonNode.class);
        // Exactly these three fields: the extension names itself and adds nothing about the visitor.
        assertEquals(List.of("secret", "response", "integration"), fieldNames(body));
        assertEquals("the-secret", body.get("secret").asText());
        assertEquals("the-token", body.get("response").asText());
        assertEquals("keycloak", body.get("integration").asText());
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
