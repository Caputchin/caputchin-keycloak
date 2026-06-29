package com.caputchin.keycloak.config;

/**
 * The three ways the Caputchin verification surface can render on a Keycloak page.
 *
 * <ul>
 *   <li>{@code CHECKBOX} renders the visible {@code <caputchin-widget>} checkbox.</li>
 *   <li>{@code INVISIBLE} renders {@code <caputchin-widget invisible>} (no UI, verifies in the background).</li>
 *   <li>{@code GAME} renders {@code <caputchin-game>} (the game challenge).</li>
 * </ul>
 *
 * Each mode maps to a custom element name and, for the checkbox widget, whether the
 * {@code invisible} boolean attribute is set.
 */
public enum WidgetMode {
    CHECKBOX("caputchin-widget", false),
    INVISIBLE("caputchin-widget", true),
    GAME("caputchin-game", false);

    private final String element;
    private final boolean invisible;

    WidgetMode(String element, boolean invisible) {
        this.element = element;
        this.invisible = invisible;
    }

    /** The custom element tag name to render. */
    public String element() {
        return element;
    }

    /** Whether the {@code invisible} boolean attribute should be set on the element. */
    public boolean invisible() {
        return invisible;
    }

    public boolean isGame() {
        return this == GAME;
    }

    /** Parse a config value, defaulting to {@link #CHECKBOX} for null or unknown input. */
    public static WidgetMode fromValue(String value) {
        if (value == null) {
            return CHECKBOX;
        }
        switch (value.trim().toLowerCase()) {
            case "invisible":
                return INVISIBLE;
            case "game":
                return GAME;
            case "checkbox":
            default:
                return CHECKBOX;
        }
    }
}
