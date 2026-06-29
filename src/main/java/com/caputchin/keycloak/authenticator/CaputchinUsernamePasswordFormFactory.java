package com.caputchin.keycloak.authenticator;

import com.caputchin.keycloak.config.CaputchinConfig;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

/** Factory for {@link CaputchinUsernamePasswordForm}. Provider id {@code caputchin-username-password-form}. */
public class CaputchinUsernamePasswordFormFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "caputchin-username-password-form";

    private static final Requirement[] REQUIREMENT_CHOICES = {Requirement.REQUIRED};

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        // Per-request instance: the authenticator captures the execution config in a field.
        return new CaputchinUsernamePasswordForm();
    }

    @Override
    public String getDisplayType() {
        return "Caputchin Username Password Form";
    }

    @Override
    public String getReferenceCategory() {
        return "password";
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
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Validates a username and password, gated by a Caputchin human-verification challenge "
                + "rendered on the same page. Drop-in replacement for the standard Username Password Form.";
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

    @Override
    public void close() {
        // no-op
    }
}
