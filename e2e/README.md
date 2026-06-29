# Local dev / e2e Keycloak

A throwaway Keycloak 26.0 that runs the Caputchin extension against your local Caputchin
stack, so you can click through the gated login, reset-credentials and registration flows.

It is self-contained: the Docker build compiles the provider JAR (no host JDK needed) and bakes
it plus the bundled `caputchin` login theme into the image, then imports a realm whose flows are
gated by the Caputchin authenticators.

## Prerequisites

- Docker.
- The local Caputchin verify worker running on `http://localhost:3001` (the `verify` proc in the
  root `mprocs.yaml`).
- A dev site key + secret. Mint one in the local dashboard (`http://localhost:3000`) and allowlist
  `http://localhost:8081` on it.

## Run

```bash
cp .env.example .env     # then edit CPT_PUB / CPT_SEC
docker compose up --build
```

Or start the `keycloak-test` process from the root `mprocs` UI.

Then open:

- Gated login: `http://localhost:8081/realms/caputchin-dev/account` (sign in as `tester` /
  `password`). The Caputchin checkbox renders on the same page as the credentials.
- Registration: follow the "Register" link from the login page.
- Reset credentials: follow "Forgot password?".
- Admin console: `http://localhost:8081` (`admin` / `admin`).

## How it is wired

- The realm sets the widget `api-host` to `http://localhost:3001`, so the browser bootstraps and
  verifies against your local verify worker.
- The extension's server-side `verifyHost` is `http://host.docker.internal:3001`, so the call from
  inside the container reaches the same worker on your host.
- The secret is read from the `CAPUTCHIN_SECRET` environment variable (set from `CPT_SEC`), so it
  never lands in the imported realm JSON. The realm only carries the public site key.
- Login theme is `caputchin` (the bundled reference theme), so this also exercises the turnkey,
  no-custom-theme path.

## Notes

- The widget loads from the jsDelivr CDN by default; the `api-host` attribute redirects its
  bootstrap/verify traffic to your local worker, so you do not need to self-host the widget to test
  the extension.
- `start-dev` runs with an in-memory dev database; everything resets on `docker compose down`.
