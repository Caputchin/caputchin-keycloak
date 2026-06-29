package com.caputchin.keycloak;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.config.WidgetMode;
import org.junit.jupiter.api.Test;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaputchinConfigTest {

    @Test
    void defaultsAreSafe() {
        CaputchinConfig c = CaputchinConfig.of(Map.of());
        assertEquals(CaputchinConfig.DEFAULT_VERIFY_HOST, c.verifyHost());
        assertTrue(c.failClosed(), "fail-closed must be the default");
        assertEquals(WidgetMode.CHECKBOX, c.widgetMode());
        assertEquals(CaputchinConfig.LOCALE_FOLLOW_KEYCLOAK, c.locale());
        assertEquals("auto", c.skin());
        assertEquals("normal", c.size());
        assertEquals(CaputchinConfig.DEFAULT_LOADER_SRC, c.loaderSrc());
        assertNull(c.siteKey());
        assertNull(c.apiHost());
    }

    @Test
    void verifyHostOverrideStripsTrailingSlash() {
        CaputchinConfig c = CaputchinConfig.of(Map.of(CaputchinConfig.VERIFY_HOST, "https://staging.example.com/"));
        assertEquals("https://staging.example.com", c.verifyHost());
    }

    @Test
    void failClosedToggle() {
        assertTrue(CaputchinConfig.of(Map.of(CaputchinConfig.FAIL_CLOSED, "true")).failClosed());
        assertEquals(false, CaputchinConfig.of(Map.of(CaputchinConfig.FAIL_CLOSED, "false")).failClosed());
    }

    @Test
    void widgetModeMapping() {
        assertEquals("caputchin-widget", CaputchinConfig.of(Map.of(CaputchinConfig.WIDGET_MODE, "checkbox")).widgetMode().element());
        assertTrue(CaputchinConfig.of(Map.of(CaputchinConfig.WIDGET_MODE, "invisible")).widgetMode().invisible());
        assertEquals("caputchin-game", CaputchinConfig.of(Map.of(CaputchinConfig.WIDGET_MODE, "game")).widgetMode().element());
        assertTrue(CaputchinConfig.of(Map.of(CaputchinConfig.WIDGET_MODE, "game")).widgetMode().isGame());
    }

    @Test
    void secretPropertyIsMasked() {
        ProviderConfigProperty secret = CaputchinConfig.configProperties().stream()
                .filter(p -> CaputchinConfig.SECRET_KEY.equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertTrue(secret.isSecret(), "the secret key property must be marked secret so it is masked");
    }
}
