# Binding to flows

You attach the providers as executions in your authentication flows, per realm, either in the Admin
Console or in realm-export JSON.

## In the Admin Console

1. Authentication then Flows. Duplicate the flow you want to protect (for example Browser) so you are
   not editing a built-in flow.
2. Replace the relevant execution's authenticator:
   - Browser flow: the Username Password Form step becomes **Caputchin Username Password Form**.
   - Reset Credentials flow: the Choose User step becomes **Caputchin Reset Credential - Choose User**.
   - Registration: add **Caputchin** to the registration form (alongside Registration User Creation).
3. Open the execution config (the gear) and set the site key and secret (see
   [configuration.md](configuration.md)).
4. Bind the duplicated flow: Authentication then Flows then the flow then Action then Bind flow.

Keep the challenge on browser, reset-credentials, and registration only. Never add it to the
direct-grant or client-credentials flows.

## In realm-export JSON

The provider ids to reference are:

| Provider id | Where |
| --- | --- |
| `caputchin-username-password-form` | Browser flow, replacing `auth-username-password-form` |
| `caputchin-reset-choose-user` | Reset-credentials flow, replacing `reset-credentials-choose-user` |
| `caputchin-registration` | Registration form flow, a form action |

A browser flow plus its config (trimmed to the essentials):

```json
{
  "browserFlow": "browser-caputchin",
  "authenticationFlows": [
    {
      "alias": "browser-caputchin",
      "providerId": "basic-flow",
      "topLevel": true,
      "builtIn": false,
      "authenticationExecutions": [
        { "authenticator": "auth-cookie", "requirement": "ALTERNATIVE", "priority": 10, "autheticatorFlow": false },
        { "flowAlias": "browser-caputchin-forms", "requirement": "ALTERNATIVE", "priority": 20, "autheticatorFlow": true }
      ]
    },
    {
      "alias": "browser-caputchin-forms",
      "providerId": "basic-flow",
      "topLevel": false,
      "builtIn": false,
      "authenticationExecutions": [
        {
          "authenticator": "caputchin-username-password-form",
          "authenticatorConfig": "caputchin-login-config",
          "requirement": "REQUIRED",
          "priority": 10,
          "autheticatorFlow": false
        }
      ]
    }
  ],
  "authenticatorConfig": [
    {
      "alias": "caputchin-login-config",
      "config": {
        "siteKey": "cpt_pub_your_key",
        "secretKeyEnvVar": "CAPUTCHIN_SECRET",
        "widgetMode": "checkbox",
        "locale": "kc",
        "failClosed": "true"
      }
    }
  ]
}
```

The reset-credentials flow is the same shape, with `caputchin-reset-choose-user` as the first
execution, followed by the standard `reset-credential-email` and `reset-password` steps, and its own
`authenticatorConfig`. A full, runnable example (browser, reset, and registration, wired to a local
stack) is in [../e2e/realm-caputchin-dev.json](../e2e/realm-caputchin-dev.json).

The `autheticatorFlow` key is spelled that way on purpose; it is the spelling Keycloak's realm export
uses.

## Provider id length

Keycloak stores the authenticator id in a 36-character column. Custom authenticator ids must stay at
or under 36 characters; the ids above already comply.
