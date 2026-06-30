package exceptions;

/**
 * Custom checked exception thrown when report generation, CSV export,
 * or print operations fail in the Complaint Management System.
 *
 * <p>Thrown by: {@code CSVExporter}, {@code PrintManager},
 * {@code ReportService}, {@code ReportController}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ReportException extends Exception {

    /** Serial version UID for safe serialization. */
    private static final long serialVersionUID = 1004L;

    /**
     * Enumeration of known report-failure categories.
     */
    public enum ReportType {
        /** Failed during CSV file creation or writing. */
        CSV_EXPORT,
        /** Failed during print job setup or submission. */
        PRINT,
        /** Failed during data retrieval for the report. */
        DATA_FETCH,
        /** Generic report failure. */
        GENERAL
    }

    /** The type of report operation that failed. */
    private final ReportType reportType;

    // ------------------------------------------------------------------
    // Constructors  (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a ReportException with a message only.
     * The report type defaults to {@link ReportType#GENERAL}.
     *
     * @param message human-readable explanation of the report failure
     */
    public ReportException(String message) {
        super(message);
        this.reportType = ReportType.GENERAL;
    }

    /**
     * Creates a ReportException with a message and the specific report type
     * that failed.
     *
     * @param message    human-readable explanation of the failure
     * @param reportType the {@link ReportType} that identifies the operation
     */
    public ReportException(String message, ReportType reportType) {
        super(message);
        this.reportType = reportType;
    }

    /**
     * Creates a ReportException wrapping a lower-level exception, with full
     * context about what operation failed and why.
     *
     * @param message    human-readable explanation of the failure
     * @param reportType the {@link ReportType} that identifies the operation
     * @param cause      the underlying exception that caused the failure
     */
    public ReportException(String message, ReportType reportType, Throwable cause) {
        super(message, cause);
        this.reportType = reportType;
    }

    // ------------------------------------------------------------------
    // Getter
    // ------------------------------------------------------------------

    /**
     * Returns the type of report operation that triggered this exception.
     *
     * @return the {@link ReportType}; never {@code null}
     */
    public ReportType getReportType() {
        return reportType;
    }

    /**
     * Returns a formatted string representation for logging.
     *
     * @return string with class name, report type, and message
     */
    @Override
    public String toString() {
        return String.format("ReportException[type=%s]: %s", reportType, getMessage());
    }
}
