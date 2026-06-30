package model;

/**
 * Enumeration of complaint priorities in the Complaint Management System.
 *
 * <p>Each constant carries a human-readable display name and a hex color
 * string used by the UI to paint priority badges consistently across all
 * screens without duplicating colour logic.</p>
 *
 * <p>The natural ordering of this enum (LOW → MEDIUM → HIGH → CRITICAL)
 * intentionally reflects ascending severity, making it safe to use
 * {@link Comparable} operations for priority-based sorting.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public enum Priority {

    // ------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------

    /** Low priority — can be scheduled for later resolution. */
    LOW("Low", "#27AE60", "#EAF7EF"),

    /** Medium priority — standard handling, resolve within SLA. */
    MEDIUM("Medium", "#F39C12", "#FEF9EC"),

    /** High priority — requires prompt attention. */
    HIGH("High", "#E67E22", "#FEF3E8"),

    /** Critical priority — requires immediate escalation. */
    CRITICAL("Critical", "#E74C3C", "#FDEDEC");

    // ------------------------------------------------------------------
    // Fields (encapsulation — private)
    // ------------------------------------------------------------------

    /** User-facing label shown in the UI (e.g. "In Progress"). */
    private final String displayName;

    /** Foreground hex color for the priority badge text / border. */
    private final String color;

    /** Background hex color for the priority badge fill. */
    private final String backgroundColor;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Constructs a Priority constant with its display name and badge colours.
     *
     * @param displayName     the label shown in the UI
     * @param color           foreground hex color (e.g., "#E74C3C")
     * @param backgroundColor background hex color for badge fill
     */
    Priority(String displayName, String color, String backgroundColor) {
        this.displayName     = displayName;
        this.color           = color;
        this.backgroundColor = backgroundColor;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    /**
     * Returns the human-readable label for this priority level.
     *
     * @return display name (e.g., "Critical")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the foreground hex color string for badge rendering.
     *
     * @return hex color string (e.g., "#E74C3C")
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns the background hex color string for badge fill rendering.
     *
     * @return background hex color string (e.g., "#FDEDEC")
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    // ------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------

    /**
     * Safely parses a string (case-insensitive) into a {@link Priority},
     * returning {@link #MEDIUM} as a sensible default if the string is
     * null, blank, or does not match any constant.
     *
     * @param value the string to parse
     * @return the matching {@link Priority}, or {@link #MEDIUM} on failure
     */
    public static Priority fromString(String value) {
        if (value == null || value.isBlank()) return MEDIUM;
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEDIUM;
        }
    }

    /**
     * Finds a Priority by its display name (case-insensitive).
     * Returns {@link #MEDIUM} if no match is found.
     *
     * @param displayName the display label (e.g., "Critical")
     * @return the matching {@link Priority}, or {@link #MEDIUM} on failure
     */
    public static Priority fromDisplayName(String displayName) {
        if (displayName == null) return MEDIUM;
        for (Priority p : values()) {
            if (p.displayName.equalsIgnoreCase(displayName.trim())) return p;
        }
        return MEDIUM;
    }

    /**
     * Returns the display name as the string representation of this enum
     * constant. Useful for populating {@link javax.swing.JComboBox} items.
     *
     * @return the human-readable display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
