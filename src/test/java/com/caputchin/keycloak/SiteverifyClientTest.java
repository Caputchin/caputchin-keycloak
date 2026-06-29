package com.caputchin.keycloak;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.verify.SiteverifyClient;
import com.caputchin.keycloak.verify.SiteverifyResult;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

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
}
