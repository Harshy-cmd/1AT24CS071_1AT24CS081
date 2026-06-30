package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single entry in the system-wide activity audit log.
 *
 * <p>An {@code ActivityLog} record is written for every significant action
 * taken by any user in the system — logins, complaint creation, status
 * updates, exports, and more. This provides complete accountability and
 * supports forensic investigation of system events.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ActivityLog {

    // ------------------------------------------------------------------
    // Private data members (Encapsulation — OOP requirement)
    // ------------------------------------------------------------------

    /** Primary key in the activity_log table. */
    private int logId;

    /** FK → users.user_id; the user who performed the action. */
    private int userId;

    /** Display name of the user (denormalised). */
    private String userName;

    /**
     * Short action code (e.g., "LOGIN", "CREATE_COMPLAINT", "EXPORT_CSV").
     * Must match values used consistently throughout the application.
     */
    private String action;

    /** Human-readable description of what happened. */
    private String description;

    /**
     * Type of the primary entity affected (e.g., "COMPLAINT", "USER",
     * "REPORT"). Enables targeted filtering.
     */
    private String entityType;

    /** Primary key of the affected entity; may be 0 if not applicable. */
    private int entityId;

    /** The exact moment this action occurred. */
    private LocalDateTime logTimestamp;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default no-argument constructor.
     * Sets logTimestamp to the current instant.
     */
    public ActivityLog() {
        this.logTimestamp = LocalDateTime.now();
    }

    /**
     * Convenience constructor for logging a simple action by a user,
     * without an entity reference.
     *
     * @param userId      the user who performed the action
     * @param action      the action code
     * @param description the human-readable description
     */
    public ActivityLog(int userId, String action, String description) {
        this();
        this.userId      = userId;
        this.action      = action;
        this.description = description;
    }

    /**
     * Full constructor for logging an action that affects a specific entity.
     * Used by the service layer for entity-linked log entries.
     *
     * @param userId      the user who performed the action
     * @param action      the action code
     * @param description the human-readable description
     * @param entityType  the type of entity affected (e.g., "COMPLAINT")
     * @param entityId    the PK of the affected entity
     */
    public ActivityLog(int userId, String action, String description,
                       String entityType, int entityId) {
        this(userId, action, description);
        this.entityType = entityType;
        this.entityId   = entityId;
    }

    /**
     * Full mapping constructor used by the DAO when reading rows from the DB.
     *
     * @param logId        the database primary key
     * @param userId       the user ID
     * @param userName     the user's display name
     * @param action       the action code
     * @param description  the description
     * @param entityType   the entity type
     * @param entityId     the entity PK
     * @param logTimestamp the exact timestamp
     */
    public ActivityLog(int logId, int userId, String userName,
                       String action, String description,
                       String entityType, int entityId,
                       LocalDateTime logTimestamp) {
        this.logId        = logId;
        this.userId       = userId;
        this.userName     = userName;
        this.action       = action;
        this.description  = description;
        this.entityType   = entityType;
        this.entityId     = entityId;
        this.logTimestamp = logTimestamp;
    }

    // ------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------

    /** @return the log record primary key */
    public int getLogId() { return logId; }

    /** @param logId the primary key to set */
    public void setLogId(int logId) { this.logId = logId; }

    /** @return the user ID who performed the action */
    public int getUserId() { return userId; }

    /** @param userId the user ID to set */
    public void setUserId(int userId) { this.userId = userId; }

    /** @return the user's display name */
    public String getUserName() { return userName; }

    /** @param userName the display name to set */
    public void setUserName(String userName) { this.userName = userName; }

    /** @return the short action code */
    public String getAction() { return action; }

    /** @param action the action code to set */
    public void setAction(String action) { this.action = action; }

    /** @return the human-readable description */
    public String getDescription() { return description; }

    /** @param description the description to set */
    public void setDescription(String description) { this.description = description; }

    /** @return the entity type (e.g., "COMPLAINT") */
    public String getEntityType() { return entityType; }

    /** @param entityType the entity type to set */
    public void setEntityType(String entityType) { this.entityType = entityType; }

    /** @return the entity primary key */
    public int getEntityId() { return entityId; }

    /** @param entityId the entity PK to set */
    public void setEntityId(int entityId) { this.entityId = entityId; }

    /** @return the timestamp of this log entry */
    public LocalDateTime getLogTimestamp() { return logTimestamp; }

    /** @param logTimestamp the timestamp to set */
    public void setLogTimestamp(LocalDateTime logTimestamp) { this.logTimestamp = logTimestamp; }

    // ------------------------------------------------------------------
    // Object overrides
    // ------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ActivityLog)) return false;
        ActivityLog other = (ActivityLog) obj;
        return this.logId != 0 && this.logId == other.logId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId);
    }

    @Override
    public String toString() {
        return String.format(
            "ActivityLog{id=%d, user='%s', action='%s', entity=%s#%d, at=%s}",
            logId, userName, action, entityType, entityId, logTimestamp);
    }
}
