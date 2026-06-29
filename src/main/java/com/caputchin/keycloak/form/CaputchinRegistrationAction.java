package com.caputchin.keycloak.form;

import com.caputchin.keycloak.config.CaputchinConfig;
import com.caputchin.keycloak.verify.SiteverifyResult;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Registration-form action that gates account creation with a Caputchin challenge. Provider id
 * {@code caputchin-registration}.
 *
 * <p>This is a {@link FormAction} (a sub-action of the registration form), the same shape as
 * Keycloak's built-in reCAPTCHA. The active theme renders the widget from the form attributes; the
 * posted token is verified before registration proceeds.
 */
public class CaputchinRegistrationAction implements FormAction, FormActionFactory {

    public static final String PROVIDER_ID = "caputchin-registration";

    private static final Logger LOG = Logger.getLogger(CaputchinRegistrationAction.class);
    private static final Requirement[] REQUIREMENT_CHOICES = {Requirement.REQUIRED, Requirement.DISABLED};

    // -- FormAction --------------------------------------------------------

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        CaputchinConfig config = CaputchinConfig.of(context.getAuthenticatorConfig());
        if (config.siteKey() == null) {
            form.addError(new FormMessage(null, CaputchinFormDecorator.ERROR_NOT_CONFIGURED));
            return;
        }
        CaputchinFormDecorator.decorate(form, config);
    }

    @Override
    public void validate(ValidationContext context) {
        CaputchinConfig config = CaputchinConfig.of(context.getAuthenticatorConfig());
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        SiteverifyResult result = CaputchinFormDecorator.validate(context.getSession(), config, formData);

        if (CaputchinFormDecorator.allows(result, config)) {
            context.success();
            return;
        }

        LOG.warnf("Caputchin registration challenge failed (%s); error-codes=%s", result.outcome, result.errorCodes);
        List<FormMessage> errors = new ArrayList<>();
        errors.add(new FormMessage(null, CaputchinFormDecorator.ERROR_FAILED));
        context.error(Errors.INVALID_REGISTRATION);
        context.validationError(formData, errors);
        context.excludeOtherErrors();
    }

    @Override
    public void success(FormContext context) {
        // no-op
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // no-op
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public void close() {
        // no-op
    }

    // -- FormActionFactory -------------------------------------------------

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public FormAction create(KeycloakSession session) {
        // Stateless: a shared instance is safe.
        return this;
    }

    @Override
    public String getDisplayType() {
        return "Caputchin";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return "Adds a Caputchin human-verification challenge to the registration form and verifies "
                + "the result server-side before the account is created.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CaputchinConfig.configProperties();
    }

    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }
}
