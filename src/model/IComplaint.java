package model;

/**
 * Defines the contract for all complaint objects in the
 * Complaint Management System.
 *
 * <p><strong>OOP Role — Interface &amp; Polymorphism:</strong>
 * By programming to this interface rather than the concrete
 * {@link Complaint} class, service and controller layers can manipulate
 * complaints polymorphically without depending on implementation details.
 * Future specialised complaint types can be added by implementing this
 * interface without breaking existing code.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 * @see     Complaint
 */
public interface IComplaint {

    // ------------------------------------------------------------------
    // Identity accessors
    // ------------------------------------------------------------------

    /**
     * Returns the database primary key of this complaint.
     *
     * @return the complaint ID (0 if not yet persisted)
     */
    int getComplaintId();

    /**
     * Returns the human-readable complaint number (e.g., "CMS-2024-0001").
     *
     * @return the complaint number; never {@code null} after persistence
     */
    String getComplaintNumber();

    /**
     * Returns the short title of this complaint.
     *
     * @return the title string
     */
    String getTitle();

    // ------------------------------------------------------------------
    // Classification accessors
    // ------------------------------------------------------------------

    /**
     * Returns the category this complaint falls under.
     *
     * @return the {@link ComplaintCategory}; never {@code null}
     */
    ComplaintCategory getCategory();

    /**
     * Returns the priority assigned to this complaint.
     *
     * @return the {@link Priority}; never {@code null}
     */
    Priority getPriority();

    /**
     * Returns the current life-cycle status of this complaint.
     *
     * @return the {@link Status}; never {@code null}
     */
    Status getStatus();

    // ------------------------------------------------------------------
    // State-transition operations
    // ------------------------------------------------------------------

    /**
     * Transitions this complaint to a new status.
     * Implementations should validate that the transition is legal.
     *
     * @param newStatus the new {@link Status} to apply
     */
    void updateStatus(Status newStatus);

    // ------------------------------------------------------------------
    // Convenience queries
    // ------------------------------------------------------------------

    /**
     * Returns {@code true} if the complaint has been resolved or closed.
     *
     * @return {@code true} for {@link Status#RESOLVED} or {@link Status#CLOSED}
     */
    boolean isResolved();

    /**
     * Returns {@code true} if the complaint is in a critical state
     * (priority = {@link Priority#CRITICAL}).
     *
     * @return {@code true} for CRITICAL priority
     */
    boolean isCritical();

    /**
     * Returns a one-line human-readable summary suitable for use in
     * notifications, tool-tips, or log messages.
     *
     * @return summary string (e.g., "[CMS-2024-0001] Street lights not working — HIGH")
     */
    String getSummary();
}
