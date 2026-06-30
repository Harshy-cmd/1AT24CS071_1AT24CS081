package reports;

import exceptions.ReportException;
import model.Complaint;

import java.util.List;

/**
 * Contract for all report output strategies in the Complaint Management System.
 *
 * <p><strong>OOP Role — Interface, Abstraction &amp; Polymorphism:</strong>
 * {@link service.ReportService} holds a reference of type {@code IReport}
 * and calls {@link #generate(List)}. The actual implementation is either
 * {@link CSVExporter} or {@link PrintManager}, decided at runtime based on
 * the user's choice. This is the classic Strategy pattern.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public interface IReport {

    /**
     * Generates the report output from the provided list of complaints.
     *
     * <p>Implementations determine the output medium:
     * <ul>
     *   <li>{@link CSVExporter} — writes to a file on disk</li>
     *   <li>{@link PrintManager} — sends to the system print dialog</li>
     * </ul>
     * </p>
     *
     * @param complaints the list of complaints to include in the report;
     *                   may be empty but never {@code null}
     * @throws ReportException if the output operation fails for any reason
     */
    void generate(List<Complaint> complaints) throws ReportException;

    /**
     * Returns a human-readable description of this report type.
     * Used for display in status messages after report generation.
     *
     * @return report type name (e.g., "CSV Export", "Print Report")
     */
    String getReportTypeName();
}
