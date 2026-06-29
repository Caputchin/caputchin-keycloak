package com.caputchin.keycloak;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.secret.SecretResolver;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.vault.VaultStringSecret;
import org.keycloak.vault.VaultTranscriber;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SecretResolverTest {

    @Test
    void envVarTakesPrecedenceOverLiteral() {
        KeycloakSession session = mock(KeycloakSession.class);
        // CAPUTCHIN_TEST_SECRET is set by the surefire configuration.
        CaputchinConfig config = CaputchinConfig.of(Map.of(
                CaputchinConfig.SECRET_KEY, "literal-should-be-ignored",
                CaputchinConfig.SECRET_KEY_ENV_VAR, "CAPUTCHIN_TEST_SECRET"));

        assertEquals("test-secret-from-env", SecretResolver.resolve(session, config));
        // The env-var path must not touch the vault.
        verifyNoInteractions(session);
    }

    @Test
    void missingEnvVarResolvesToNull() {
        KeycloakSession session = mock(KeycloakSession.class);
        CaputchinConfig config = CaputchinConfig.of(Map.of(
                CaputchinConfig.SECRET_KEY_ENV_VAR, "CAPUTCHIN_DEFINITELY_NOT_SET_98f3a"));

        assertNull(SecretResolver.resolve(session, config));
    }

    @Test
    void literalIsPassedThroughTheVaultTranscriber() {
        KeycloakSession session = mock(KeycloakSession.class);
        VaultTranscriber vault = mock(VaultTranscriber.class);
        VaultStringSecret vaultSecret = mock(VaultStringSecret.class);
        when(session.vault()).thenReturn(vault);
        when(vault.getStringSecret("cpt_sec_literal")).thenReturn(vaultSecret);
        when(vaultSecret.get()).thenReturn(Optional.of("cpt_sec_literal"));

        CaputchinConfig config = CaputchinConfig.of(Map.of(CaputchinConfig.SECRET_KEY, "cpt_sec_literal"));
        assertEquals("cpt_sec_literal", SecretResolver.resolve(session, config));
    }

    @Test
    void noSecretConfiguredResolvesToNull() {
        KeycloakSession session = mock(KeycloakSession.class);
        assertNull(SecretResolver.resolve(session, CaputchinConfig.of(Map.of())));
    }
}
