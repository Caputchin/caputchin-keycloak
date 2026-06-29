package com.caputchin.keycloak.config;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Typed view over a Keycloak per-execution configuration map, plus the list of
 * configuration properties the Admin Console and realm-export JSON expose.
 *
 * The same property set backs all three providers (login, reset-credentials,
 * registration); a given execution only reads the keys relevant to its widget mode.
 */
public final class CaputchinConfig {

    // Configuration keys (stable identifiers used in realm-export JSON).
    public static final String SITE_KEY = "siteKey";
    public static final String SECRET_KEY = "secretKey";
    public static final String SECRET_KEY_ENV_VAR = "secretKeyEnvVar";
    public static final String VERIFY_HOST = "verifyHost";
    public static final String WIDGET_MODE = "widgetMode";
    public static final String SIZE = "size";
    public static final String LOCALE = "locale";
    public static final String SKIN = "skin";
    public static final String LOADER_SRC = "loaderSrc";
    public static final String API_HOST = "apiHost";
    public static final String FAIL_CLOSED = "failClosed";
    public static final String GAME = "game";
    public static final String GAMES = "games";
    public static final String GAME_SRC = "gameSrc";
    public static final String LAYOUT = "layout";

    public static final String DEFAULT_VERIFY_HOST = "https://verify.caputchin.com";
    public static final String VERIFY_HOST_ENV = "CAPUTCHIN_VERIFY_HOST";
    public static final String DEFAULT_LOADER_SRC =
            "https://cdn.jsdelivr.net/npm/@caputchin/widget@3/dist/widget.js";

    /** Sentinel meaning "follow Keycloak's resolved page locale" (see the theme contract). */
    public static final String LOCALE_FOLLOW_KEYCLOAK = "kc";

    private final Map<String, String> config;

    private CaputchinConfig(Map<String, String> config) {
        this.config = config == null ? Collections.emptyMap() : config;
    }

    public static CaputchinConfig of(AuthenticatorConfigModel model) {
        return new CaputchinConfig(model == null ? null : model.getConfig());
    }

    public static CaputchinConfig of(Map<String, String> config) {
        return new CaputchinConfig(config);
    }

    public String siteKey() {
        return trimToNull(config.get(SITE_KEY));
    }

    public String secretKeyLiteral() {
        // Not trimmed: a secret could legitimately contain surrounding characters.
        String v = config.get(SECRET_KEY);
        return (v == null || v.isEmpty()) ? null : v;
    }

    public String secretKeyEnvVar() {
        return trimToNull(config.get(SECRET_KEY_ENV_VAR));
    }

    /** Verify host, honoring (in order) the per-execution value, the {@value #VERIFY_HOST_ENV} env var, then the default. */
    public String verifyHost() {
        String v = trimToNull(config.get(VERIFY_HOST));
        if (v != null) {
            return stripTrailingSlash(v);
        }
        String env = trimToNull(System.getenv(VERIFY_HOST_ENV));
        if (env != null) {
            return stripTrailingSlash(env);
        }
        return DEFAULT_VERIFY_HOST;
    }

    public WidgetMode widgetMode() {
        return WidgetMode.fromValue(config.get(WIDGET_MODE));
    }

    public String size() {
        return orDefault(config.get(SIZE), "normal");
    }

    public String locale() {
        return orDefault(config.get(LOCALE), LOCALE_FOLLOW_KEYCLOAK);
    }

    public String skin() {
        return orDefault(config.get(SKIN), "auto");
    }

    /** Loader script URL, falling back to the pinned CDN default so the theme always has a usable value. */
    public String loaderSrc() {
        return orDefault(config.get(LOADER_SRC), DEFAULT_LOADER_SRC);
    }

    public String apiHost() {
        return trimToNull(config.get(API_HOST));
    }

    /** Fail-closed is the default: an indeterminate verify (outage, timeout, misconfig) blocks the attempt. */
    public boolean failClosed() {
        String v = trimToNull(config.get(FAIL_CLOSED));
        return v == null || Boolean.parseBoolean(v);
    }

    public String game() {
        return trimToNull(config.get(GAME));
    }

    public String games() {
        return trimToNull(config.get(GAMES));
    }

    public String gameSrc() {
        return trimToNull(config.get(GAME_SRC));
    }

    public String layout() {
        return orDefault(config.get(LAYOUT), "auto");
    }

    /** The full property set shown in the Admin Console execution config and settable via realm-export JSON. */
    public static List<ProviderConfigProperty> configProperties() {
        List<ProviderConfigProperty> props = new ArrayList<>();

        props.add(string(SITE_KEY, "Site key",
                "Your Caputchin public site key (cpt_pub_...). This is the only value that reaches the browser.",
                null));

        props.add(secret(SECRET_KEY, "Secret key",
                "Your Caputchin secret key (cpt_sec_...), used for server-side verification. "
                        + "Prefer 'Secret key env var' or a ${vault.*} reference so the secret is not stored in "
                        + "the realm export. If both this and the env var are set, the env var wins."));

        props.add(string(SECRET_KEY_ENV_VAR, "Secret key env var",
                "Name of an environment variable that holds the secret key (for example CAPUTCHIN_SECRET). "
                        + "When set, the secret is read from this variable at verify time and the realm export "
                        + "contains only the variable name, never the secret itself.",
                null));

        props.add(string(VERIFY_HOST, "Verify host",
                "Base URL of the Caputchin verification host. Leave blank to use "
                        + DEFAULT_VERIFY_HOST + " (or the " + VERIFY_HOST_ENV + " environment variable if set). "
                        + "Override for a staging or self-hosted endpoint.",
                null));

        ProviderConfigProperty mode = list(WIDGET_MODE, "Widget mode",
                "Which verification surface to render: the visible checkbox, the invisible widget, or the game.",
                List.of("checkbox", "invisible", "game"));
        mode.setDefaultValue("checkbox");
        props.add(mode);

        ProviderConfigProperty size = list(SIZE, "Widget size",
                "Checkbox widget size.", List.of("normal", "compact"));
        size.setDefaultValue("normal");
        props.add(size);

        props.add(string(LOCALE, "Locale",
                "Widget locale. Use 'kc' (default) to follow Keycloak's resolved page locale, or a preset "
                        + "name / BCP-47 tag such as 'en' or 'ar'.",
                LOCALE_FOLLOW_KEYCLOAK));

        props.add(string(SKIN, "Skin",
                "Widget skin: 'auto' (default, follows the browser color scheme), 'light', 'dark', or a preset name.",
                "auto"));

        props.add(string(LOADER_SRC, "Loader script URL",
                "Override the widget loader <script> URL (for self-hosting under a strict CSP). "
                        + "Leave blank to use the pinned CDN default.",
                null));

        props.add(string(API_HOST, "API host",
                "Override the widget bootstrap/verify host the browser calls (for a self-hosted Caputchin). "
                        + "Leave blank to use the widget default.",
                null));

        ProviderConfigProperty failClosed = bool(FAIL_CLOSED, "Fail closed",
                "When verification cannot be completed (verify host unreachable, timeout, or misconfiguration), "
                        + "block the attempt (on) rather than letting it through (off). Default on.");
        failClosed.setDefaultValue("true");
        props.add(failClosed);

        props.add(string(GAME, "Game id (game mode)",
                "Marketplace game id to play (game mode only). Ignored unless widget mode is 'game'.",
                null));
        props.add(string(GAMES, "Game ids (game mode)",
                "Comma-separated game ids; one is picked at random per session (game mode only).",
                null));
        props.add(string(GAME_SRC, "Game source URL (game mode)",
                "URL of a customer-hosted game bundle (game mode only).",
                null));
        ProviderConfigProperty layout = list(LAYOUT, "Game layout (game mode)",
                "How the game renders (game mode only).", List.of("auto", "inline", "modal", "fullscreen"));
        layout.setDefaultValue("auto");
        props.add(layout);

        return props;
    }

    // -- property builders -------------------------------------------------

    private static ProviderConfigProperty string(String name, String label, String help, String defaultValue) {
        ProviderConfigProperty p = new ProviderConfigProperty();
        p.setName(name);
        p.setLabel(label);
        p.setHelpText(help);
        p.setType(ProviderConfigProperty.STRING_TYPE);
        p.setDefaultValue(defaultValue);
        return p;
    }

    private static ProviderConfigProperty secret(String name, String label, String help) {
        ProviderConfigProperty p = string(name, label, help, null);
        p.setSecret(true);
        return p;
    }

    private static ProviderConfigProperty bool(String name, String label, String help) {
        ProviderConfigProperty p = new ProviderConfigProperty();
        p.setName(name);
        p.setLabel(label);
        p.setHelpText(help);
        p.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        return p;
    }

    private static ProviderConfigProperty list(String name, String label, String help, List<String> options) {
        ProviderConfigProperty p = new ProviderConfigProperty();
        p.setName(name);
        p.setLabel(label);
        p.setHelpText(help);
        p.setType(ProviderConfigProperty.LIST_TYPE);
        p.setOptions(options);
        return p;
    }

    // -- small helpers -----------------------------------------------------

    private static String trimToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String orDefault(String v, String fallback) {
        String t = trimToNull(v);
        return t == null ? fallback : t;
    }

    private static String stripTrailingSlash(String v) {
        return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
    }
}
