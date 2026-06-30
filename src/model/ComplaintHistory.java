package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single entry in the complaint status-change audit trail.
 *
 * <p>Every time a complaint's status is changed, a {@code ComplaintHistory}
 * record is inserted into the {@code complaint_history} database table.
 * This provides an immutable, chronological history of each complaint's
 * life-cycle progression — essential for audit and transparency.</p>
 *
 * <p>This class is immutable by design (all fields set in the constructor,
 * no setters for core state). Only the display-name fields (set by the DAO
 * after joining with {@code users}) have setters.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ComplaintHistory {

    // ------------------------------------------------------------------
    // Private data members (Encapsulation — OOP requirement)
    // ------------------------------------------------------------------

    /** Primary key in complaint_history table. */
    private int historyId;

    /** FK → complaints.complaint_id — which complaint was changed. */
    private int complaintId;

    /** FK → users.user_id — who made the change. */
    private int changedBy;

    /** Display name of the user who made the change (denormalised). */
    private String changedByName;

    /** The status before the transition; null for the first history entry. */
    private Status oldStatus;

    /** The status after the transition. */
    private Status newStatus;

    /** The moment the status change was made. */
    private LocalDateTime changeDate;

    /** Optional remarks provided when making the status change. */
    private String remarks;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default no-argument constructor. Sets changeDate to now.
     */
    public ComplaintHistory() {
        this.changeDate = LocalDateTime.now();
    }

    /**
     * Constructs a new history entry for recording a status transition.
     * Used by the service layer before persisting to the database.
     *
     * @param complaintId the complaint whose status is changing
     * @param changedBy   the user ID of the person making the change
     * @param oldStatus   the prior status (null if first transition)
     * @param newStatus   the new status being applied
     * @param remarks     optional notes accompanying the change
     */
    public ComplaintHistory(int complaintId, int changedBy,
                            Status oldStatus, Status newStatus, String remarks) {
        this.complaintId = complaintId;
        this.changedBy   = changedBy;
        this.oldStatus   = oldStatus;
        this.newStatus   = newStatus;
        this.remarks     = remarks;
        this.changeDate  = LocalDateTime.now();
    }

    /**
     * Full constructor used by the DAO when mapping a database row.
     *
     * @param historyId     the database primary key
     * @param complaintId   the complaint ID
     * @param changedBy     the user ID who made the change
     * @param changedByName the display name of that user
     * @param oldStatus     the old status
     * @param newStatus     the new status
     * @param changeDate    the date/time of the change
     * @param remarks       optional remarks
     */
    public ComplaintHistory(int historyId, int complaintId, int changedBy,
                            String changedByName, Status oldStatus,
                            Status newStatus, LocalDateTime changeDate,
                            String remarks) {
        this.historyId    = historyId;
        this.complaintId  = complaintId;
        this.changedBy    = changedBy;
        this.changedByName = changedByName;
        this.oldStatus    = oldStatus;
        this.newStatus    = newStatus;
        this.changeDate   = changeDate;
        this.remarks      = remarks;
    }

    // ------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------

    /** @return the history record primary key */
    public int getHistoryId() { return historyId; }

    /** @param historyId the primary key to set (used by DAO after INSERT) */
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    /** @return the related complaint ID */
    public int getComplaintId() { return complaintId; }

    /** @param complaintId the complaint ID to set */
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    /** @return the user ID of the person who made the change */
    public int getChangedBy() { return changedBy; }

    /** @param changedBy the user ID to set */
    public void setChangedBy(int changedBy) { this.changedBy = changedBy; }

    /** @return the display name of the user who made the change */
    public String getChangedByName() { return changedByName; }

    /** @param changedByName the display name to set (set by DAO after JOIN) */
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    /** @return the previous status (may be null for the first history entry) */
    public Status getOldStatus() { return oldStatus; }

    /** @param oldStatus the old status to set */
    public void setOldStatus(Status oldStatus) { this.oldStatus = oldStatus; }

    /** @return the new status applied in this transition */
    public Status getNewStatus() { return newStatus; }

    /** @param newStatus the new status to set */
    public void setNewStatus(Status newStatus) { this.newStatus = newStatus; }

    /** @return the date and time of this status change */
    public LocalDateTime getChangeDate() { return changeDate; }

    /** @param changeDate the change datetime to set */
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }

    /** @return any remarks accompanying this status change */
    public String getRemarks() { return remarks; }

    /** @param remarks the remarks to set */
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // ------------------------------------------------------------------
    // Object overrides
    // ------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ComplaintHistory)) return false;
        ComplaintHistory other = (ComplaintHistory) obj;
        return this.historyId != 0 && this.historyId == other.historyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId);
    }

    @Override
    public String toString() {
        return String.format(
            "ComplaintHistory{id=%d, complaintId=%d, %s → %s, by='%s', at=%s}",
            historyId, complaintId,
            oldStatus  != null ? oldStatus.getDisplayName()  : "INITIAL",
            newStatus  != null ? newStatus.getDisplayName()  : "?",
            changedByName, changeDate);
    }
}
