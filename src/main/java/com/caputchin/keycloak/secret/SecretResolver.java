package com.caputchin.keycloak.secret;

import com.caputchin.keycloak.config.CaputchinConfig;
import org.keycloak.models.KeycloakSession;
import org.keycloak.vault.VaultStringSecret;

/**
 * Resolves the Caputchin secret key for a given execution configuration.
 *
 * <p>Precedence:
 * <ol>
 *   <li>If a secret-key environment-variable name is configured, the secret is read from that
 *       environment variable. The realm export then contains only the variable name.</li>
 *   <li>Otherwise the literal secret value is passed through Keycloak's vault transcriber, so a
 *       {@code ${vault.key}} reference is resolved from the configured Vault SPI and a plain literal
 *       is returned unchanged.</li>
 * </ol>
 *
 * <p>Returns {@code null} when no secret is configured; the caller fails closed in that case. The
 * resolved secret value is never logged.
 */
public final class SecretResolver {

    private SecretResolver() {
    }

    public static String resolve(KeycloakSession session, CaputchinConfig config) {
        String envVarName = config.secretKeyEnvVar();
        if (envVarName != null) {
            String value = System.getenv(envVarName);
            return (value == null || value.isBlank()) ? null : value.trim();
        }

        String literal = config.secretKeyLiteral();
        if (literal == null || literal.isBlank()) {
            return null;
        }

        // getStringSecret resolves a ${vault.*} expression via the configured Vault SPI and
        // returns a plain literal unchanged.
        try (VaultStringSecret vaultSecret = session.vault().getStringSecret(literal)) {
            return vaultSecret.get().orElse(literal);
        }
    }
}
