package com.caputchin.keycloak.form;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.config.WidgetMode;
import com.caputchin.keycloak.verify.SiteverifyClient;
import com.caputchin.keycloak.verify.SiteverifyResult;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;

/**
 * Shared logic for every Caputchin provider: place the widget configuration into the form/template
 * context, read the posted token, and decide whether an attempt may proceed.
 *
 * <p>Rendering of the {@code <caputchin-widget>} / {@code <caputchin-game>} element is the theme's
 * job. This decorator only exposes the data the theme reads (the headless contract). The bundled
 * {@code caputchin} login theme renders from exactly these attributes.
 */
public final class CaputchinFormDecorator {

    private static final Logger LOG = Logger.getLogger(CaputchinFormDecorator.class);

    /** Hidden form field the widget populates and the providers read. */
    public static final String TOKEN_FIELD = "caputchin-token";

    /** Message key shown when a challenge fails (generic retry message, localized by the theme). */
    public static final String ERROR_FAILED = "caputchinFailed";

    /** Message key shown when an execution has no site key configured. */
    public static final String ERROR_NOT_CONFIGURED = "caputchinNotConfigured";

    private CaputchinFormDecorator() {
    }

    /** Place every {@code caputchin*} attribute the theme reads onto the form context. */
    public static void decorate(LoginFormsProvider form, CaputchinConfig config) {
        if (config.siteKey() == null) {
            // The widget renders inert and verification fails closed; surface the misconfiguration to
            // the operator rather than silently blocking every attempt.
            LOG.warn("Caputchin site key is not configured for this execution; the widget will render "
                    + "inert and verification will fail closed.");
        }
        WidgetMode mode = config.widgetMode();

        form.setAttribute("caputchinRequired", Boolean.TRUE);
        form.setAttribute("caputchinSiteKey", nullToEmpty(config.siteKey()));
        form.setAttribute("caputchinElement", mode.element());
        form.setAttribute("caputchinInvisible", mode.invisible());
        form.setAttribute("caputchinIsGame", mode.isGame());
        form.setAttribute("caputchinSize", config.size());
        form.setAttribute("caputchinLocale", config.locale());
        form.setAttribute("caputchinSkin", config.skin());
        form.setAttribute("caputchinTokenField", TOKEN_FIELD);
        form.setAttribute("caputchinLoaderSrc", config.loaderSrc());
        form.setAttribute("caputchinApiHost", nullToEmpty(config.apiHost()));

        // Game-mode passthroughs (always present, empty when not in game mode, for a stable contract).
        form.setAttribute("caputchinGame", nullToEmpty(config.game()));
        form.setAttribute("caputchinGames", nullToEmpty(config.games()));
        form.setAttribute("caputchinGameSrc", nullToEmpty(config.gameSrc()));
        form.setAttribute("caputchinLayout", config.layout());
    }

    /** Read the token from the submission and verify it server-side. */
    public static SiteverifyResult validate(KeycloakSession session, CaputchinConfig config,
                                            MultivaluedMap<String, String> formData) {
        String token = formData == null ? null : formData.getFirst(TOKEN_FIELD);
        return SiteverifyClient.verify(session, config, token);
    }

    /**
     * Whether an attempt with this result may proceed. A definitive failure never proceeds; an
     * indeterminate result proceeds only when fail-open is configured.
     */
    public static boolean allows(SiteverifyResult result, CaputchinConfig config) {
        switch (result.outcome) {
            case PASS:
                return true;
            case ERROR:
                return !config.failClosed();
            case FAIL:
            default:
                return false;
        }
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }
}
