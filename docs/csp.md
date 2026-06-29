# Content Security Policy

The widget loads a script, may instantiate WebAssembly, makes browser-side calls to the Caputchin
host, and (in game mode) renders the game in an iframe. Keycloak's default login Content Security
Policy is stricter than that, so relax it for the realms that use the extension.

Set it under Realm Settings then Security Defenses then Content Security Policy. A working value for
the default hosted setup:

```
frame-src 'self' https://games.caputchin.com; frame-ancestors 'self'; object-src 'none'; script-src 'self' 'wasm-unsafe-eval' https://cdn.jsdelivr.net; connect-src 'self' https://verify.caputchin.com; img-src 'self' data: https:; style-src 'self' 'unsafe-inline'; worker-src 'self' blob:; child-src 'self' blob:
```

What each part is for:

| Directive | Why |
| --- | --- |
| `script-src ... https://cdn.jsdelivr.net` | The widget loader script. Replace with your own host if you self-host the loader. |
| `script-src ... 'wasm-unsafe-eval'` | The proof-of-work runs as WebAssembly. |
| `connect-src ... https://verify.caputchin.com` | The widget's browser-side bootstrap and verify calls. Use your `apiHost` if self-hosted. |
| `frame-src https://games.caputchin.com` | The game iframe (game mode only). Omit for the checkbox or invisible widget. |

Notes:

- Adjust the hosts to match your `loaderSrc` and `apiHost`. If you point those at a self-hosted
  Caputchin, list those origins instead of the defaults.
- The server-side verification call (Keycloak to the verify host) is server-to-server and is not
  governed by this browser CSP.
- If your theme emits inline scripts and your CSP forbids them, use the Keycloak-provided nonce
  (`${cspNonce}` in FreeMarker) on those script tags.
