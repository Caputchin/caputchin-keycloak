# Install

## Build the provider JAR

With a JDK 17+ and Maven:

```bash
mvn -DskipTests package
# produces target/caputchin-keycloak.jar
```

No JDK on the host? Build in a container:

```bash
docker run --rm -v "$PWD":/work -w /work maven:3.9-eclipse-temurin-21 mvn -DskipTests package
```

## Deploy

Keycloak (Quarkus distribution) discovers providers from its `providers/` directory and must be
rebuilt after a provider is added or changed:

```bash
cp target/caputchin-keycloak.jar "$KEYCLOAK_HOME/providers/"
"$KEYCLOAK_HOME/bin/kc.sh" build
"$KEYCLOAK_HOME/bin/kc.sh" start --optimized
```

`kc.sh build` is required. Without it the providers are not registered and you will see "provider
not found" at runtime.

### Container image

Bake the JAR in and rebuild in the image, so `--optimized` start is fast:

```dockerfile
FROM quay.io/keycloak/keycloak:26.0
COPY caputchin-keycloak.jar /opt/keycloak/providers/
RUN /opt/keycloak/bin/kc.sh build
```

The `COPY` and the `RUN kc.sh build` must be separate, ordered layers.

## The bundled theme

The same JAR ships a `caputchin` login theme (declared in `META-INF/keycloak-themes.json`). It is
discovered automatically once the JAR is in `providers/` and `kc.sh build` has run. Select it per
realm under Realm Settings then Themes then Login theme, or leave it unselected and render the
widget in your own theme (see [theme-contract.md](theme-contract.md)).

## Compatibility with `--features=organization` and friends

The providers are plain authenticators and a form action; they do not depend on optional features
and are unaffected by `--features=organization`. They use no preview features (in particular not the
scripts authenticator).

## Next

- [configuration.md](configuration.md) to set the site key, secret, and widget options.
- [flows.md](flows.md) to bind the executions to your flows.
- [csp.md](csp.md) for the Content Security Policy the widget needs.
