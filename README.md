# Caputchin for Keycloak

Official Keycloak extension that gates the login, reset-credentials and registration flows with
[Caputchin](https://caputchin.com) human verification, and verifies the result server-side.

- Renders the Caputchin checkbox, the invisible widget, or the game challenge on the same page as
  the credentials (no separate interstitial).
- Verifies the token from inside Keycloak against the Caputchin `/siteverify` endpoint, before the
  flow proceeds. Fail-closed by default.
- Two ways to render: a bundled, turnkey login theme for stock deployments, or a headless
  config-only mode in which your own theme renders the widget.
- Privacy-first: no IPs, no fingerprinting; the only value that reaches the browser is your public
  site key.

Works with Keycloak 26.x (Quarkus). Apache-2.0.

## Quickstart (no custom theme)

The fastest path. A working, styled checkbox with no template editing.

1. Build the provider JAR and drop it into Keycloak:

   ```bash
   mvn -DskipTests package
   cp target/caputchin-keycloak.jar "$KEYCLOAK_HOME/providers/"
   "$KEYCLOAK_HOME/bin/kc.sh" build
   "$KEYCLOAK_HOME/bin/kc.sh" start --optimized
   ```

   (No JDK on the host? `docker run --rm -v "$PWD":/work -w /work maven:3.9-eclipse-temurin-21 mvn -DskipTests package`.)

2. Realm Settings then Themes then **Login theme: `caputchin`**.

3. Realm Settings then Security Defenses then Content Security Policy: paste the value from
   [docs/csp.md](docs/csp.md).

4. Authentication then Flows. On the Browser flow, set the Username Password Form execution to
   **Caputchin Username Password Form**. Open its config (the gear) and set your **Site key** and a
   **Secret key** (literal, env-var name, or a `${vault.*}` reference). Do the same on the
   Reset Credentials flow with **Caputchin Reset Credential - Choose User**.

That is it. The checkbox renders on the login and reset pages, follows the realm locale, and verifies
fail-closed. The same steps as committed realm-export JSON are in [docs/flows.md](docs/flows.md).

## Custom theme (headless)

If you ship your own login theme (for example a Keycloakify React theme), run the extension
headless: it exposes the widget configuration into the page context and validates the token; your
theme renders the `<caputchin-widget>` / `<caputchin-game>` element from those attributes. Keep your
own theme as the realm Login theme (do not switch to `caputchin`). The contract (token field name,
context attributes, and a Keycloakify example) is in [docs/theme-contract.md](docs/theme-contract.md).

## What it provides

| Provider id | Type | Flow |
| --- | --- | --- |
| `caputchin-username-password-form` | Authenticator | Browser login (same page as credentials) |
| `caputchin-reset-choose-user` | Authenticator | Reset credentials (email entry page) |
| `caputchin-registration` | Form action | Registration form |

Bot protection is never applied to the direct-grant or client-credentials flows: there is no browser
there, so a challenge cannot render and would break API token requests.

## Local dev / try it

`e2e/` boots a real Keycloak wired to a local Caputchin stack with one command. See
[e2e/README.md](e2e/README.md).

## Documentation

| Doc | Covers |
| --- | --- |
| [docs/install.md](docs/install.md) | Building, deploying, and `kc.sh build` |
| [docs/configuration.md](docs/configuration.md) | Every config property and the three secret-sourcing mechanisms |
| [docs/flows.md](docs/flows.md) | Which executions to bind, with realm-export JSON |
| [docs/theme-contract.md](docs/theme-contract.md) | Token field, context attributes, headless / Keycloakify |
| [docs/csp.md](docs/csp.md) | Content Security Policy for the widget |
| [docs/compatibility.md](docs/compatibility.md) | Keycloak and JDK version support |

## License

Apache-2.0. See [LICENSE](LICENSE), [NOTICE](NOTICE), and [TRADEMARK.md](TRADEMARK.md).
