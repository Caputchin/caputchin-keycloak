package com.caputchin.keycloak.authenticator;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.form.CaputchinFormDecorator;
import com.caputchin.keycloak.verify.SiteverifyResult;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.resetcred.ResetCredentialChooseUser;
import org.keycloak.forms.login.LoginFormsProvider;

/**
 * The reset-credentials "choose user" step (the email / username entry form), gated by a Caputchin
 * challenge rendered on the same page.
 *
 * <p>The widget configuration is placed on the reset form before it renders; the posted token is
 * verified before the user is looked up. A definitive verification failure (or, when fail-closed, an
 * indeterminate one) re-renders the form with a generic error and a fresh challenge.
 */
public class CaputchinResetCredentialChooseUser extends ResetCredentialChooseUser {

    private static final Logger LOG = Logger.getLogger(CaputchinResetCredentialChooseUser.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // context.form() returns the request-scoped forms provider; decorating it here means the
        // attributes are present when the parent renders createPasswordReset(). When the parent
        // short-circuits (for example an active SSO session resolves the user), no form is rendered
        // and the decoration is simply unused.
        CaputchinFormDecorator.decorate(context.form(), CaputchinConfig.of(context.getAuthenticatorConfig()));
        super.authenticate(context);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        CaputchinConfig config = CaputchinConfig.of(context.getAuthenticatorConfig());

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        SiteverifyResult result = CaputchinFormDecorator.validate(context.getSession(), config, formData);

        if (!CaputchinFormDecorator.allows(result, config)) {
            LOG.warnf("Caputchin reset challenge failed (%s); error-codes=%s", result.outcome, result.errorCodes);
            LoginFormsProvider form = context.form();
            CaputchinFormDecorator.decorate(form, config);
            form.setError(CaputchinFormDecorator.ERROR_FAILED);
            Response challenge = form.createPasswordReset();
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        if (result.outcome == SiteverifyResult.Outcome.ERROR) {
            LOG.warn("Caputchin verification was indeterminate; proceeding because fail-open is configured.");
        }

        super.action(context);
    }
}
