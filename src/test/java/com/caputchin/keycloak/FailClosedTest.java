package com.caputchin.keycloak;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.form.CaputchinFormDecorator;
import com.caputchin.keycloak.verify.SiteverifyResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The fail-open / fail-closed decision matrix, the security-critical core of the extension. */
class FailClosedTest {

    private final CaputchinConfig failClosed = CaputchinConfig.of(Map.of(CaputchinConfig.FAIL_CLOSED, "true"));
    private final CaputchinConfig failOpen = CaputchinConfig.of(Map.of(CaputchinConfig.FAIL_CLOSED, "false"));

    @Test
    void passAlwaysProceeds() {
        assertTrue(CaputchinFormDecorator.allows(SiteverifyResult.pass(), failClosed));
        assertTrue(CaputchinFormDecorator.allows(SiteverifyResult.pass(), failOpen));
    }

    @Test
    void definitiveFailureNeverProceeds() {
        assertFalse(CaputchinFormDecorator.allows(SiteverifyResult.fail("timeout-or-duplicate"), failClosed));
        // Even with fail-open, a definitive success=false is rejected; fail-open only relaxes outages.
        assertFalse(CaputchinFormDecorator.allows(SiteverifyResult.fail("timeout-or-duplicate"), failOpen));
    }

    @Test
    void indeterminateRespectsTheToggle() {
        assertFalse(CaputchinFormDecorator.allows(SiteverifyResult.error("verify-unreachable"), failClosed));
        assertTrue(CaputchinFormDecorator.allows(SiteverifyResult.error("verify-unreachable"), failOpen));
    }
}
