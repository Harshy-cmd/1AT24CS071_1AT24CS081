package exceptions;

/**
 * Custom checked exception for all database-related failures in the
 * Complaint Management System.
 *
 * <p>This exception wraps lower-level {@link java.sql.SQLException} instances
 * and provides meaningful messages that can be shown to end-users without
 * exposing raw SQL details.</p>
 *
 * <p>Thrown by: DAO implementation classes, {@code DatabaseConnection}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class DatabaseException extends Exception {

    /** Serial version UID for safe serialization. */
    private static final long serialVersionUID = 1001L;

    /** Optional error code for programmatic handling (maps to SQL error codes). */
    private final int errorCode;

    // ------------------------------------------------------------------
    // Constructors  (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a DatabaseException with a descriptive message only.
     *
     * @param message human-readable explanation of the database error
     */
    public DatabaseException(String message) {
        super(message);
        this.errorCode = 0;
    }

    /**
     * Creates a DatabaseException with a message and a root-cause throwable.
     * This is the most common constructor used when wrapping a
     * {@link java.sql.SQLException}.
     *
     * @param message human-readable explanation of the database error
     * @param cause   the underlying exception that triggered this error
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
    }

    /**
     * Creates a DatabaseException with a message, root cause, and a numeric
     * SQL error code for detailed programmatic handling.
     *
     * @param message   human-readable explanation of the database error
     * @param cause     the underlying exception that triggered this error
     * @param errorCode the SQL vendor error code (from {@code SQLException.getErrorCode()})
     */
    public DatabaseException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // ------------------------------------------------------------------
    // Getter
    // ------------------------------------------------------------------

    /**
     * Returns the SQL vendor error code associated with this exception.
     * Returns {@code 0} if no specific code was provided.
     *
     * @return the SQL error code, or 0 if not applicable
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns a formatted string representation of this exception for logging.
     *
     * @return string with class name, error code, and message
     */
    @Override
    public String toString() {
        return String.format("DatabaseException[errorCode=%d]: %s", errorCode, getMessage());
    }
}
