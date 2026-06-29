# Configuration

Every provider (login, reset, registration) exposes the same per-execution configuration. Set it in
the Admin Console (Authentication then your flow then the execution then the gear icon) or in
realm-export JSON as an `authenticatorConfig` (see [flows.md](flows.md)).

## Properties

| Key | Type | Default | Notes |
| --- | --- | --- | --- |
| `siteKey` | string | (none) | Your Caputchin public site key (`cpt_pub_...`). The only value sent to the browser. |
| `secretKey` | string (masked) | (none) | The secret (`cpt_sec_...`). Prefer `secretKeyEnvVar` or a `${vault.*}` value so it stays out of the realm export. |
| `secretKeyEnvVar` | string | (none) | Name of an environment variable holding the secret. When set, it wins over `secretKey`. |
| `verifyHost` | string | `https://verify.caputchin.com` | Verify host base URL. Also reads the `CAPUTCHIN_VERIFY_HOST` env var when unset. |
| `widgetMode` | choice | `checkbox` | `checkbox`, `invisible`, or `game`. |
| `size` | choice | `normal` | `normal` or `compact` (checkbox widget). |
| `locale` | string | `kc` | `kc` follows Keycloak's resolved page locale; otherwise a preset name or BCP-47 tag (`en`, `ar`). |
| `skin` | string | `auto` | `auto`, `light`, `dark`, or a preset name. |
| `loaderSrc` | string | jsDelivr CDN | Override the widget loader script URL (for self-hosting under a strict CSP). |
| `apiHost` | string | (none) | Override the widget bootstrap/verify host the browser calls (self-hosted Caputchin). |
| `failClosed` | boolean | `true` | When verification cannot be completed, block (on) rather than allow (off). |
| `game`, `games`, `gameSrc`, `layout` | string | (none) | Game-mode only. A marketplace id, comma-separated ids, a hosted bundle URL, and the layout. |

Only `siteKey` and a secret are required. Everything else has a safe default; the turnkey path needs
nothing more.

## Sourcing the secret

The secret is resolved in this order:

1. **`secretKeyEnvVar`** names an environment variable; the secret is read from it at verify time.
   The realm export then carries only the variable name, never the secret. This fits secret managers
   that inject environment variables (for example SOPS).
2. **`${vault.key}`** in `secretKey` is resolved through Keycloak's configured Vault SPI.
3. **A literal** in `secretKey` is used as-is (stored masked).

The resolved secret is never logged and never written into the page; only the public `siteKey`
reaches the browser.

## What lives here versus in the Caputchin dashboard

This configuration carries the per-flow widget knobs, the keys, and the verify policy. The richer
appearance is governed by the Caputchin dashboard on the site key itself, and applies to every place
that key is used:

| Set in Keycloak (here) | Set in the Caputchin dashboard (on the site key) |
| --- | --- |
| Site key, secret, verify host, fail-closed | Which game pool runs, game difficulty |
| Widget mode, size, locale, skin, layout | Locale / skin / config preset banks (server-resolved) |
| Loader and API host overrides | White-label brand strip |

So you do not duplicate skins or game settings here; pick the widget mode and let the dashboard drive
the look.

## Fail-open vs fail-closed

`failClosed` only affects the indeterminate case (verify host unreachable, timeout, or a
misconfiguration such as a missing secret). A definitive negative from the verify host (an expired,
reused, or forged token) is always rejected, regardless of this setting.
