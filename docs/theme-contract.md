# Theme contract (headless / custom themes)

The extension never forces markup or CSS onto your theme. It places the widget configuration into the
page context and validates the posted token; your theme decides how to render the
`<caputchin-widget>` or `<caputchin-game>` element. This is what lets a fully custom theme (for
example a Keycloakify React theme with no PatternFly) integrate cleanly.

The bundled `caputchin` theme is just one consumer of this same contract. To run headless, keep your
own theme as the realm Login theme and render the element yourself.

## The token field

When the widget verifies, it injects a hidden field into the enclosing form, and the providers read
it:

```
caputchin-token
```

Render the `<caputchin-widget>` (or `<caputchin-game>`) element **inside** the login / reset /
registration `<form>`, so the auto-injected `caputchin-token` field is submitted with the form. If
your framework manages the form, you can instead listen for the widget's `pass` event and set a
hidden input named `caputchin-token` yourself.

## Context attributes

The providers set these on the form context (Keycloak FreeMarker attributes). Every attribute is
always present, so the contract is stable.

| Attribute | Type | Meaning |
| --- | --- | --- |
| `caputchinRequired` | boolean | Render the widget on this page. |
| `caputchinSiteKey` | string | Public site key (`cpt_pub_...`). The only secret-free value to expose. |
| `caputchinElement` | string | `caputchin-widget` or `caputchin-game`. |
| `caputchinInvisible` | boolean | Set the `invisible` attribute on the checkbox widget. |
| `caputchinIsGame` | boolean | Convenience flag: render the game element. |
| `caputchinSize` | string | `normal` or `compact`. |
| `caputchinLocale` | string | A BCP-47 tag, or the literal `kc` meaning "use Keycloak's resolved page locale". |
| `caputchinSkin` | string | `auto`, `light`, `dark`, or a preset name. |
| `caputchinTokenField` | string | The hidden field name, always `caputchin-token`. |
| `caputchinLoaderSrc` | string | The widget loader script URL to load. |
| `caputchinApiHost` | string | Optional bootstrap/verify host override (empty when unset). |
| `caputchinGame`, `caputchinGames`, `caputchinGameSrc`, `caputchinLayout` | string | Game-mode passthroughs (empty when unset). |

When `caputchinLocale` is `kc`, resolve it to Keycloak's page locale (`locale.currentLanguageTag` in
FreeMarker, or the equivalent in your framework). Otherwise pass it straight to the element's
`locale` attribute. Keycloak already sets `dir="rtl"` for right-to-left locales such as Arabic, and
the widget mirrors itself to match.

## Error message keys

On a failed challenge the providers set a form error with one of these message keys; define them in
your theme's message bundle (the bundled theme ships English and Arabic):

| Key | When |
| --- | --- |
| `caputchinFailed` | Verification failed; show a generic retry message. |
| `caputchinNotConfigured` | The execution has no site key configured. |

## Styling

The widget is a self-contained custom element. Style it from your theme with CSS parts and custom
properties; nothing leaks in from the extension:

```css
caputchin-widget::part(simple-checkbox) { /* ... */ }
caputchin-widget::part(simple-brand) { display: none; }
caputchin-widget { --cpt-skin-primary: #2F6640; }
```

## Keycloakify example

In a Keycloakify theme, type the attributes the extension adds, then read them from `kcContext` and
render the element. Sketch:

```ts
// kcContext type extension: declare the attributes for the login and reset pages
export type KcContextExtension = {
  caputchinRequired?: boolean;
  caputchinSiteKey?: string;
  caputchinElement?: "caputchin-widget" | "caputchin-game";
  caputchinInvisible?: boolean;
  caputchinSize?: string;
  caputchinLocale?: string;
  caputchinSkin?: string;
  caputchinLoaderSrc?: string;
  caputchinApiHost?: string;
};
```

```tsx
// inside your Login / LoginResetPassword page component, within the <form>
{kcContext.caputchinRequired && (
  <>
    <caputchin-widget
      sitekey={kcContext.caputchinSiteKey}
      trigger="form-submit"
      {...(kcContext.caputchinInvisible ? { invisible: true } : {})}
      size={kcContext.caputchinSize}
      locale={kcContext.caputchinLocale === "kc" ? kcContext.locale.currentLanguageTag : kcContext.caputchinLocale}
      skin={kcContext.caputchinSkin}
      {...(kcContext.caputchinApiHost ? { "api-host": kcContext.caputchinApiHost } : {})}
    />
    <script src={kcContext.caputchinLoaderSrc} async defer />
  </>
)}
```

The element posts `caputchin-token` with the form; the extension reads and verifies it. Nothing else
is required on your side.
