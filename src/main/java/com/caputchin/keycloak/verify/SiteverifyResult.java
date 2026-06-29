package com.caputchin.keycloak.verify;

import java.util.List;

/**
 * Outcome of a Caputchin server-side verification.
 *
 * <p>Three states, because the fail-open / fail-closed toggle only applies to the indeterminate
 * case:
 * <ul>
 *   <li>{@link Outcome#PASS} the token was verified ({@code success: true}).</li>
 *   <li>{@link Outcome#FAIL} the verify host gave a definitive negative ({@code success: false}, or
 *       no token was submitted). Always rejected, regardless of the fail-open toggle.</li>
 *   <li>{@link Outcome#ERROR} verification could not be completed (host unreachable, timeout, HTTP
 *       error, or missing secret). Subject to the fail-closed toggle.</li>
 * </ul>
 */
public final class SiteverifyResult {

    public enum Outcome {
        PASS,
        FAIL,
        ERROR
    }

    public final Outcome outcome;
    public final List<String> errorCodes;

    private SiteverifyResult(Outcome outcome, List<String> errorCodes) {
        this.outcome = outcome;
        this.errorCodes = errorCodes == null ? List.of() : List.copyOf(errorCodes);
    }

    public static SiteverifyResult pass() {
        return new SiteverifyResult(Outcome.PASS, List.of());
    }

    public static SiteverifyResult fail(List<String> errorCodes) {
        return new SiteverifyResult(Outcome.FAIL, errorCodes);
    }

    public static SiteverifyResult fail(String errorCode) {
        return new SiteverifyResult(Outcome.FAIL, List.of(errorCode));
    }

    public static SiteverifyResult error(String errorCode) {
        return new SiteverifyResult(Outcome.ERROR, List.of(errorCode));
    }

    public boolean isPass() {
        return outcome == Outcome.PASS;
    }
}
