package model;

/**
 * Enumeration of all possible life-cycle statuses for a complaint in the
 * Complaint Management System.
 *
 * <p>Status flow (typical path):
 * <pre>
 *   PENDING → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
 *                                ↓
 *                             ON_HOLD (optional pause)
 * </pre>
 * </p>
 *
 * <p>Each constant carries a display name and badge colours so the UI can
 * render consistent visual indicators without duplicating color logic.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public enum Status {

    // ------------------------------------------------------------------
    // Constants (ordered by typical workflow progression)
    // ------------------------------------------------------------------

    /** Complaint has been submitted but not yet assigned to anyone. */
    PENDING("Pending", "#7F8C8D", "#F2F3F4"),

    /** Complaint has been assigned to an employee. */
    ASSIGNED("Assigned", "#2980B9", "#EAF2FB"),

    /** Employee is actively working on the complaint. */
    IN_PROGRESS("In Progress", "#F39C12", "#FEF9EC"),

    /** Work is temporarily paused, awaiting information or resources. */
    ON_HOLD("On Hold", "#E67E22", "#FEF3E8"),

    /** The issue has been resolved and is awaiting confirmation. */
    RESOLVED("Resolved", "#27AE60", "#EAF7EF"),

    /** The complaint has been verified and officially closed. */
    CLOSED("Closed", "#2C3E50", "#EAECEE");

    // ------------------------------------------------------------------
    // Fields (encapsulation — private)
    // ------------------------------------------------------------------

    /** User-facing label shown in the UI (e.g., "In Progress"). */
    private final String displayName;

    /** Foreground hex color for the status badge text/border. */
    private final String color;

    /** Background hex color for the status badge fill. */
    private final String backgroundColor;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Constructs a Status constant with its display name and badge colours.
     *
     * @param displayName     label shown in the UI
     * @param color           foreground hex color
     * @param backgroundColor background hex color for badge fill
     */
    Status(String displayName, String color, String backgroundColor) {
        this.displayName     = displayName;
        this.color           = color;
        this.backgroundColor = backgroundColor;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    /**
     * Returns the human-readable label for this status.
     *
     * @return display name (e.g., "In Progress")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the foreground hex color for badge rendering.
     *
     * @return hex color string (e.g., "#27AE60")
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns the background hex color for badge fill rendering.
     *
     * @return background hex color string
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    // ------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------

    /**
     * Returns {@code true} if this status represents a terminal state,
     * meaning no further transitions are expected.
     *
     * @return {@code true} for {@link #CLOSED}; {@code false} otherwise
     */
    public boolean isTerminal() {
        return this == CLOSED;
    }

    /**
     * Returns {@code true} if this status represents an open/active state
     * (i.e., the complaint still requires action).
     *
     * @return {@code true} for PENDING, ASSIGNED, IN_PROGRESS, ON_HOLD
     */
    public boolean isOpen() {
        return this == PENDING || this == ASSIGNED
            || this == IN_PROGRESS || this == ON_HOLD;
    }

    /**
     * Safely parses a string (case-insensitive) into a {@link Status},
     * returning {@link #PENDING} as the default if parsing fails.
     *
     * @param value the string to parse (e.g., "IN_PROGRESS", "in progress")
     * @return the matching {@link Status}, or {@link #PENDING} on failure
     */
    public static Status fromString(String value) {
        if (value == null || value.isBlank()) return PENDING;
        // Handle display names with spaces
        String normalized = value.trim().toUpperCase().replace(' ', '_');
        try {
            return Status.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }

    /**
     * Finds a Status by its display name (case-insensitive).
     * Returns {@link #PENDING} if no match is found.
     *
     * @param displayName the display label (e.g., "In Progress")
     * @return the matching {@link Status}, or {@link #PENDING} on failure
     */
    public static Status fromDisplayName(String displayName) {
        if (displayName == null) return PENDING;
        for (Status s : values()) {
            if (s.displayName.equalsIgnoreCase(displayName.trim())) return s;
        }
        return PENDING;
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
