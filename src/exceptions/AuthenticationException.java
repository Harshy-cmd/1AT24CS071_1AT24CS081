package exceptions;

/**
 * Custom checked exception for authentication and authorization failures in
 * the Complaint Management System.
 *
 * <p>Thrown when a login attempt fails, a session is invalid, an account is
 * disabled, or a user attempts an action without sufficient privileges.</p>
 *
 * <p>Thrown by: {@code UserService}, {@code UserController}, login UI panels.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class AuthenticationException extends Exception {

    /** Serial version UID for safe serialization. */
    private static final long serialVersionUID = 1003L;

    /**
     * Enumeration of known authentication failure reasons, used to let
     * the UI display targeted feedback without string comparison.
     */
    public enum Reason {
        /** Username does not exist in the system. */
        USER_NOT_FOUND,
        /** Password hash does not match the stored hash. */
        INVALID_PASSWORD,
        /** The user account exists but has been deactivated by an admin. */
        ACCOUNT_DISABLED,
        /** The current session has expired and requires re-login. */
        SESSION_EXPIRED,
        /** The user lacks the required role for the requested operation. */
        INSUFFICIENT_PRIVILEGES,
        /** A generic authentication error. */
        UNKNOWN
    }

    /** The specific reason for this authentication failure. */
    private final Reason reason;

    // ------------------------------------------------------------------
    // Constructors  (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates an AuthenticationException with a message and a specific reason.
     * This is the preferred constructor as it provides the richest context.
     *
     * @param message human-readable explanation of the failure
     * @param reason  the {@link Reason} enum value for this failure
     */
    public AuthenticationException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    /**
     * Creates an AuthenticationException with a message, reason, and
     * a root-cause throwable for wrapping lower-level exceptions.
     *
     * @param message human-readable explanation of the failure
     * @param reason  the {@link Reason} enum value for this failure
     * @param cause   the underlying exception that caused the failure
     */
    public AuthenticationException(String message, Reason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    /**
     * Creates an AuthenticationException with a message only.
     * The reason defaults to {@link Reason#UNKNOWN}.
     *
     * @param message human-readable explanation of the failure
     */
    public AuthenticationException(String message) {
        super(message);
        this.reason = Reason.UNKNOWN;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    /**
     * Returns the {@link Reason} that caused this authentication failure.
     *
     * @return the reason enum value; never {@code null}
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Returns a formatted string representation for logging.
     *
     * @return string with class name, reason, and message
     */
    @Override
    public String toString() {
        return String.format("AuthenticationException[reason=%s]: %s", reason, getMessage());
    }
}
