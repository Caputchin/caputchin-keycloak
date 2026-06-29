# Compatibility

| Item | Support |
| --- | --- |
| Keycloak | 26.x (Quarkus distribution). Built and tested against the 26.0 line and a later 26.x in CI. |
| Java (build) | JDK 17 and 21. The JAR targets Java 17 bytecode. |
| Distribution | Standard provider JAR for `providers/`, compatible with `kc.sh build --optimized`. |
| Login theme | Inherits `keycloak.v2` (the default login theme since 26.0). |
| Optional features | Unaffected by `--features=organization`. Uses no preview features. |

## Why it stays stable across 26.x

The server-side verification call uses Keycloak's `HttpClientProvider` rather than the `SimpleHttp`
helper, whose package and method signatures shifted within the 26.x line. The providers extend the
stable `UsernamePasswordForm` and `ResetCredentialChooseUser` authenticators and implement the
`FormAction` contract, all of which are long-lived SPI surfaces.

## Pinning a different Keycloak version

The build compiles against the version in `pom.xml` (`keycloak.version`). To build against another
26.x patch:

```bash
mvn -Dkeycloak.version=26.3.0 -DskipTests package
```

The same JAR runs across the 26.x line; rebuilding per version is only needed if you want to compile
and test against a specific one.
