package com.caputchin.keycloak.authenticator;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.form.CaputchinFormDecorator;
import com.caputchin.keycloak.verify.SiteverifyResult;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;

/**
 * The standard username + password login step, gated by a Caputchin challenge rendered on the same
 * page.
 *
 * <p>The widget configuration is placed on the login form so the active theme can render the
 * element; the posted token is verified before the credentials are checked. A definitive
 * verification failure (or, when fail-closed, an indeterminate one) re-renders the credentials page
 * with a generic error and a fresh challenge.
 *
 * <p>Instances are created per request by {@link CaputchinUsernamePasswordFormFactory}, so the
 * captured configuration field is request-scoped and safe.
 */
public class CaputchinUsernamePasswordForm extends UsernamePasswordForm {

    private static final Logger LOG = Logger.getLogger(CaputchinUsernamePasswordForm.class);

    private CaputchinConfig config;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        this.config = CaputchinConfig.of(context.getAuthenticatorConfig());
        super.authenticate(context);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        this.config = CaputchinConfig.of(context.getAuthenticatorConfig());

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        SiteverifyResult result = CaputchinFormDecorator.validate(context.getSession(), config, formData);

        if (!CaputchinFormDecorator.allows(result, config)) {
            LOG.warnf("Caputchin login challenge failed (%s); error-codes=%s", result.outcome, result.errorCodes);
            Response challenge = challenge(context, CaputchinFormDecorator.ERROR_FAILED);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        if (result.outcome == SiteverifyResult.Outcome.ERROR) {
            LOG.warn("Caputchin verification was indeterminate; proceeding because fail-open is configured.");
        }

        super.action(context);
    }

    /** Initial render path: decorate the form before building the username/password page. */
    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();
        if (formData.size() > 0) {
            forms.setFormData(formData);
        }
        CaputchinFormDecorator.decorate(forms, CaputchinConfig.of(context.getAuthenticatorConfig()));
        return forms.createLoginUsernamePassword();
    }

    /** Error re-render path (used by the parent challenge helpers): decorate before rendering. */
    @Override
    protected Response createLoginForm(LoginFormsProvider form) {
        if (config != null) {
            CaputchinFormDecorator.decorate(form, config);
        }
        return super.createLoginForm(form);
    }
}
