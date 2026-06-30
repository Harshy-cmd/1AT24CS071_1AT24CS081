package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single complaint in the Complaint Management System.
 *
 * <p><strong>OOP Role — Interface Implementation, Encapsulation,
 * Constructor Overloading:</strong>
 * {@code Complaint} implements the {@link IComplaint} interface, providing
 * concrete implementations for every contract method while encapsulating all
 * data behind private fields accessed only through getters and setters.</p>
 *
 * <p>This class is the central domain object. It maps 1-to-1 with the
 * {@code complaints} database table.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 * @see     IComplaint
 */
public class Complaint implements IComplaint {

    // ------------------------------------------------------------------
    // Private data members (Encapsulation — OOP requirement)
    // ------------------------------------------------------------------

    /** Database primary key; 0 before persistence. */
    private int complaintId;

    /** Human-readable complaint number (e.g., "CMS-2024-0001"). */
    private String complaintNumber;

    /** Short descriptive title of the complaint. */
    private String title;

    /** Full description / details of the complaint. */
    private String description;

    /** The category this complaint belongs to. */
    private ComplaintCategory category;

    /** The severity/priority of this complaint. */
    private Priority priority;

    /** The current life-cycle status. */
    private Status status;

    /** Physical location where the issue was observed. */
    private String location;

    /** The department responsible for resolving this complaint. */
    private String department;

    /**
     * The user ID of the employee assigned to resolve this complaint.
     * {@code 0} or negative if unassigned.
     */
    private int assignedTo;

    /** Display name of the assigned employee (denormalised for display). */
    private String assignedToName;

    /** The user ID of the person who created / filed this complaint. */
    private int createdBy;

    /** Display name of the complaint creator (denormalised for display). */
    private String createdByName;

    /** Timestamp when this complaint was first registered. */
    private LocalDateTime dateCreated;

    /** Timestamp of the most recent update to any field. */
    private LocalDateTime dateUpdated;

    /** The date on which the issue was fully resolved; {@code null} if open. */
    private LocalDate resolutionDate;

    /** Additional remarks or notes added during handling. */
    private String remarks;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default no-argument constructor.
     * Sets sensible defaults: PENDING status, MEDIUM priority, now as dates.
     */
    public Complaint() {
        this.status      = Status.PENDING;
        this.priority    = Priority.MEDIUM;
        this.dateCreated = LocalDateTime.now();
        this.dateUpdated = LocalDateTime.now();
    }

    /**
     * Minimal constructor for creating a new complaint from user input
     * (before generating a complaint number or assigning).
     *
     * @param title       the complaint title
     * @param description the full description
     * @param category    the complaint category
     * @param priority    the complaint priority
     * @param location    the physical location
     * @param department  the responsible department
     * @param createdBy   the user ID of the submitter
     */
    public Complaint(String title, String description, ComplaintCategory category,
                     Priority priority, String location, String department,
                     int createdBy) {
        this();
        this.title       = title;
        this.description = description;
        this.category    = category;
        this.priority    = priority;
        this.location    = location;
        this.department  = department;
        this.createdBy   = createdBy;
    }

    /**
     * Full constructor used by the DAO when mapping a database row to a
     * Complaint object (all fields including generated/computed values).
     *
     * @param complaintId       database PK
     * @param complaintNumber   formatted number (e.g., "CMS-2024-0001")
     * @param title             complaint title
     * @param description       full description
     * @param category          category enum
     * @param priority          priority enum
     * @param status            status enum
     * @param location          physical location
     * @param department        responsible department
     * @param assignedTo        assigned user ID (0 if unassigned)
     * @param assignedToName    assigned user display name (may be null)
     * @param createdBy         creator user ID
     * @param createdByName     creator display name
     * @param dateCreated       creation timestamp
     * @param dateUpdated       last update timestamp
     * @param resolutionDate    resolution date (may be null)
     * @param remarks           additional remarks (may be null)
     */
    public Complaint(int complaintId, String complaintNumber, String title,
                     String description, ComplaintCategory category,
                     Priority priority, Status status, String location,
                     String department, int assignedTo, String assignedToName,
                     int createdBy, String createdByName,
                     LocalDateTime dateCreated, LocalDateTime dateUpdated,
                     LocalDate resolutionDate, String remarks) {
        this.complaintId     = complaintId;
        this.complaintNumber = complaintNumber;
        this.title           = title;
        this.description     = description;
        this.category        = category;
        this.priority        = priority;
        this.status          = status;
        this.location        = location;
        this.department      = department;
        this.assignedTo      = assignedTo;
        this.assignedToName  = assignedToName;
        this.createdBy       = createdBy;
        this.createdByName   = createdByName;
        this.dateCreated     = dateCreated;
        this.dateUpdated     = dateUpdated;
        this.resolutionDate  = resolutionDate;
        this.remarks         = remarks;
    }

    // ------------------------------------------------------------------
    // IComplaint interface implementations (Method Overriding — OOP req.)
    // ------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public int getComplaintId() {
        return complaintId;
    }

    /** {@inheritDoc} */
    @Override
    public String getComplaintNumber() {
        return complaintNumber;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public ComplaintCategory getCategory() {
        return category;
    }

    /** {@inheritDoc} */
    @Override
    public Priority getPriority() {
        return priority;
    }

    /** {@inheritDoc} */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * Updates the life-cycle status of this complaint and stamps
     * {@link #dateUpdated} with the current time.
     *
     * @param newStatus the new {@link Status} to apply; must not be null
     */
    @Override
    public void updateStatus(Status newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Status cannot be null.");
        this.status      = newStatus;
        this.dateUpdated = LocalDateTime.now();
        // Set resolution date when complaint is resolved or closed
        if (newStatus == Status.RESOLVED || newStatus == Status.CLOSED) {
            this.resolutionDate = LocalDate.now();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResolved() {
        return status == Status.RESOLVED || status == Status.CLOSED;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCritical() {
        return priority == Priority.CRITICAL;
    }

    /** {@inheritDoc} */
    @Override
    public String getSummary() {
        return String.format("[%s] %s — %s / %s",
                complaintNumber, title,
                priority != null ? priority.getDisplayName() : "?",
                status   != null ? status.getDisplayName()   : "?");
    }

    // ------------------------------------------------------------------
    // Additional Getters and Setters
    // ------------------------------------------------------------------

    /** @param complaintId the database primary key to set */
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    /** @param complaintNumber the formatted complaint number */
    public void setComplaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; }

    /** @param title the complaint title to set */
    public void setTitle(String title) { this.title = title; }

    /** @return the full description */
    public String getDescription() { return description; }

    /** @param description the full description to set */
    public void setDescription(String description) { this.description = description; }

    /** @param category the category to set */
    public void setCategory(ComplaintCategory category) { this.category = category; }

    /** @param priority the priority to set */
    public void setPriority(Priority priority) { this.priority = priority; }

    /** @param status the status to set directly (bypasses updateStatus logic) */
    public void setStatus(Status status) { this.status = status; }

    /** @return the physical location */
    public String getLocation() { return location; }

    /** @param location the physical location to set */
    public void setLocation(String location) { this.location = location; }

    /** @return the responsible department */
    public String getDepartment() { return department; }

    /** @param department the department to set */
    public void setDepartment(String department) { this.department = department; }

    /** @return the assigned employee user ID (0 = unassigned) */
    public int getAssignedTo() { return assignedTo; }

    /** @param assignedTo the user ID of the assigned employee */
    public void setAssignedTo(int assignedTo) { this.assignedTo = assignedTo; }

    /** @return the assigned employee's display name */
    public String getAssignedToName() { return assignedToName; }

    /** @param assignedToName the assigned employee's display name */
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    /** @return the creator's user ID */
    public int getCreatedBy() { return createdBy; }

    /** @param createdBy the creator's user ID to set */
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    /** @return the creator's display name */
    public String getCreatedByName() { return createdByName; }

    /** @param createdByName the creator's display name to set */
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    /** @return the creation timestamp */
    public LocalDateTime getDateCreated() { return dateCreated; }

    /** @param dateCreated the creation timestamp */
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }

    /** @return the last update timestamp */
    public LocalDateTime getDateUpdated() { return dateUpdated; }

    /** @param dateUpdated the last update timestamp */
    public void setDateUpdated(LocalDateTime dateUpdated) { this.dateUpdated = dateUpdated; }

    /** @return the resolution date, or null if not yet resolved */
    public LocalDate getResolutionDate() { return resolutionDate; }

    /** @param resolutionDate the resolution date */
    public void setResolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; }

    /** @return additional remarks */
    public String getRemarks() { return remarks; }

    /** @param remarks additional remarks to set */
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // ------------------------------------------------------------------
    // Object overrides
    // ------------------------------------------------------------------

    /** Two complaints are equal if they share the same non-zero ID. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Complaint)) return false;
        Complaint other = (Complaint) obj;
        return this.complaintId != 0 && this.complaintId == other.complaintId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(complaintId);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
