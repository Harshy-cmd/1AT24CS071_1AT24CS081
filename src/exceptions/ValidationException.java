package exceptions;

/**
 * Custom runtime exception thrown when input validation fails anywhere in the
 * Complaint Management System.
 *
 * <p>This is an unchecked exception (extends {@link RuntimeException}) so
 * callers are not forced to declare it in {@code throws} clauses, which
 * keeps service and controller method signatures clean.</p>
 *
 * <p>Thrown by: {@code Validator}, service layer, controller layer.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ValidationException extends RuntimeException {

    /** Serial version UID for safe serialization. */
    private static final long serialVersionUID = 1002L;

    /** The name of the field that failed validation, if applicable. */
    private final String fieldName;

    // ------------------------------------------------------------------
    // Constructors  (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a ValidationException with a general message not tied to
     * a specific field.
     *
     * @param message human-readable explanation of the validation failure
     */
    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    /**
     * Creates a ValidationException for a specific field with a descriptive
     * message, suitable for displaying inline field errors in the UI.
     *
     * @param fieldName the UI field or model property that failed validation
     * @param message   human-readable explanation of the validation failure
     */
    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    /**
     * Creates a ValidationException with a message and a root-cause throwable.
     * Used when validation parsing (e.g., date parsing) throws an exception.
     *
     * @param message human-readable explanation of the validation failure
     * @param cause   the underlying exception that caused the failure
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    /**
     * Returns the name of the field that caused the validation failure.
     * May be {@code null} if this exception was created without a field name.
     *
     * @return the field name, or {@code null}
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns whether this exception is associated with a specific UI field.
     *
     * @return {@code true} if a field name is available; {@code false} otherwise
     */
    public boolean hasFieldName() {
        return fieldName != null && !fieldName.isEmpty();
    }

    /**
     * Returns a formatted string representation for logging.
     *
     * @return string with class name, optional field name, and message
     */
    @Override
    public String toString() {
        if (hasFieldName()) {
            return String.format("ValidationException[field='%s']: %s", fieldName, getMessage());
        }
        return String.format("ValidationException: %s", getMessage());
    }
}
