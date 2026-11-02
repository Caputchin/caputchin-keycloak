# Content Security Policy

Keycloak sends a Content Security Policy on its login pages and the Account Console. You set it per
realm under Realm settings then Security defenses then Headers, in the **Content-Security-Policy** field.

## Recommended: keep Keycloak's default

Keycloak's default value works with the extension in every widget mode (checkbox, invisible and game).
Leave it as it is:

```
frame-src 'self'; frame-ancestors 'self'; object-src 'none';
```

It restricts frames, plugins and embedding, not scripts or connections, so the widget loader, its
WebAssembly solver and its calls to the verify host all load. Game mode needs no `frame-src` entry:
the game frame is an inline `srcdoc` document, not a URL that `frame-src` would match.

Tested on Keycloak 26.4.7 and 26.8.1 with this extension's v0.2.0, the stock `caputchin` login theme and the
default `loaderSrc`: a sign-in in each mode reached Server-verified, the realm Account Console
rendered, and the browser reported no policy violation.

## Only if you already run a stricter policy

Add a `script-src` or `default-src` directive only if your security policy requires one. Such a
directive also governs Keycloak's own pages, so the value must allow both Keycloak and the widget:

```
default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' 'wasm-unsafe-eval' https://cdn.jsdelivr.net https://games.caputchin.com; connect-src 'self' https://cdn.jsdelivr.net https://verify.caputchin.com; worker-src blob:; img-src 'self' data: https:; style-src 'self' 'unsafe-inline'; frame-src 'self'; frame-ancestors 'self'; object-src 'none'; base-uri 'self'; form-action 'self'
```

This value passed the same test as the default, in all three modes. What each part is for:

| Directive | Why |
| --- | --- |
| `script-src 'self' 'unsafe-inline'` | Keycloak's login page (theme `keycloak.v2` and the `caputchin` theme built on it) runs six inline scripts and an inline `onsubmit` handler, and the Account Console starts from an inline import map. Without `'unsafe-inline'` the login form and the Account Console break. |
| `script-src https://cdn.jsdelivr.net` | The widget loader script. |
| `script-src 'unsafe-eval'` | The widget's instrumentation check. Without it the check fails and so does the sign-in. You can drop it if you turn instrumentation off on the site key's Security page in the Caputchin dashboard. |
| `script-src 'wasm-unsafe-eval'` | The proof-of-work solver runs as WebAssembly. |
| `script-src https://games.caputchin.com` | Game mode only: the game frame inherits this policy and loads the game bundle from that host. Omit it for the checkbox or invisible widget. |
| `connect-src https://cdn.jsdelivr.net` | The solver fetches its WebAssembly module from the loader host. |
| `connect-src https://verify.caputchin.com` | The widget's browser-side bootstrap and verify calls. |
| `worker-src blob:` | The solver runs in Web Workers created from `blob:` URLs. |

Keycloak 26.4 does not emit a nonce for its inline scripts, and two of them carry per-session values
(a tab id and a session hash), so a nonce or hash list cannot replace `'unsafe-inline'` with the stock
themes. A nonce or hash also makes browsers ignore `'unsafe-inline'`, which blocks the scripts that the
list does not cover. To go further, ship your own login theme without inline scripts.

Notes:

- If you set `loaderSrc` or `apiHost` (self-hosted widget or Caputchin), list those origins instead
  of `https://cdn.jsdelivr.net` (in both `script-src` and `connect-src`) and
  `https://verify.caputchin.com`.
- The server-side verification call (Keycloak to the verify host) is server-to-server and is not
  governed by this browser policy.
- After any change, open the realm's login page and Account Console with the browser console open. A
  blocked resource shows as a Content Security Policy error that names the directive.
