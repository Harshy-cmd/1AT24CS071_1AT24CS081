package model;

/**
 * Enumeration of all complaint categories supported by the
 * Complaint Management System.
 *
 * <p>Each constant carries a display name and a representative icon
 * character (Unicode emoji / symbol) suitable for rendering in the UI
 * alongside category labels without requiring external image assets.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public enum ComplaintCategory {

    // ------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------

    ELECTRICITY("Electricity",  "\u26A1"),  // ⚡
    WATER      ("Water",        "\uD83D\uDCA7"),  // 💧
    ROAD       ("Road",         "\uD83D\uDEE3"),  // 🛣
    GARBAGE    ("Garbage",      "\uD83D\uDDD1"),  // 🗑
    SECURITY   ("Security",     "\uD83D\uDEE1"),  // 🛡
    INTERNET   ("Internet",     "\uD83C\uDF10"),  // 🌐
    HEALTH     ("Health",       "\u2764"),         // ❤
    EDUCATION  ("Education",    "\uD83C\uDF93"),  // 🎓
    OTHERS     ("Others",       "\uD83D\uDCCB");  // 📋

    // ------------------------------------------------------------------
    // Fields (encapsulation — private)
    // ------------------------------------------------------------------

    /** User-facing label shown in the UI (e.g., "Electricity"). */
    private final String displayName;

    /** Unicode icon representing this category. */
    private final String icon;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Constructs a ComplaintCategory constant with a display name and icon.
     *
     * @param displayName the label shown in the UI
     * @param icon        the Unicode icon for this category
     */
    ComplaintCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon        = icon;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    /**
     * Returns the human-readable label for this category.
     *
     * @return display name (e.g., "Electricity")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the Unicode icon character string for this category.
     *
     * @return Unicode icon string (e.g., "⚡")
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Returns the icon and display name combined (e.g., "⚡ Electricity").
     * Convenient for rendering in combo boxes with icon labels.
     *
     * @return combined icon and display name
     */
    public String getIconAndName() {
        return icon + "  " + displayName;
    }

    // ------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------

    /**
     * Safely parses a string (case-insensitive) into a
     * {@link ComplaintCategory}, returning {@link #OTHERS} as default.
     *
     * @param value the string to parse (e.g., "WATER", "Road")
     * @return the matching {@link ComplaintCategory}, or {@link #OTHERS}
     */
    public static ComplaintCategory fromString(String value) {
        if (value == null || value.isBlank()) return OTHERS;
        try {
            return ComplaintCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHERS;
        }
    }

    /**
     * Finds a ComplaintCategory by its display name (case-insensitive).
     * Returns {@link #OTHERS} if no match is found.
     *
     * @param displayName the display label (e.g., "Electricity")
     * @return the matching {@link ComplaintCategory}, or {@link #OTHERS}
     */
    public static ComplaintCategory fromDisplayName(String displayName) {
        if (displayName == null) return OTHERS;
        for (ComplaintCategory c : values()) {
            if (c.displayName.equalsIgnoreCase(displayName.trim())) return c;
        }
        return OTHERS;
    }

    /**
     * Returns the display name as the string representation.
     * Enables direct use in JComboBox without a custom renderer.
     *
     * @return the human-readable display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
