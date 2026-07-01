package service;

import dao.IReportDAO;
import dao.IUserDAO;
import dao.implementation.ReportDAOImpl;
import dao.implementation.UserDAOImpl;
import exceptions.DatabaseException;
import exceptions.ReportException;
import model.ActivityLog;
import model.Complaint;
import reports.CSVExporter;
import reports.IReport;
import reports.PrintManager;
import util.SessionManager;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic service for report generation and export operations.
 *
 * <p>Acts as the bridge between the {@link controller.ReportController}
 * and both the {@link IReportDAO} (data retrieval) and the
 * {@link reports.IReport} implementations (output rendering). This keeps
 * the controller thin and free of any I/O logic.</p>
 *
 * <p><strong>OOP Role — Polymorphism:</strong>
 * The service obtains data and passes it to an {@link IReport} reference.
 * The actual object behind the reference is either a {@link CSVExporter}
 * or a {@link PrintManager}, decided at runtime based on the user's action.
 * </p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ReportService {

    // ------------------------------------------------------------------
    // Dependencies
    // ------------------------------------------------------------------

    private final IReportDAO    reportDAO;
    private final IUserDAO      userDAO;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default constructor — uses production DAO implementations.
     */
    public ReportService() {
        this.reportDAO    = new ReportDAOImpl();
        this.userDAO      = new UserDAOImpl();
    }

    /**
     * Injection constructor — accepts custom DAOs for testing.
     *
     * @param reportDAO    custom {@link IReportDAO}
     * @param userDAO      custom {@link IUserDAO}
     */
    public ReportService(IReportDAO reportDAO, IUserDAO userDAO) {
        this.reportDAO    = reportDAO;
        this.userDAO      = userDAO;
    }

    // ------------------------------------------------------------------
    // CSV Export
    // ------------------------------------------------------------------

    /**
     * Exports filtered complaints to a CSV file at the given file path.
     *
     * <p>Uses an {@link IReport} reference that points to a
     * {@link CSVExporter} at runtime — demonstrating polymorphism.</p>
     *
     * @param filePath   the absolute path of the output .csv file
     * @param status     filter by status name; null = all
     * @param priority   filter by priority name; null = all
     * @param category   filter by category name; null = all
     * @param department filter by department; null = all
     * @param fromDate   start date; null = no lower bound
     * @param toDate     end date; null = no upper bound
     * @throws ReportException   if the CSV file cannot be written
     * @throws DatabaseException if the data query fails
     */
    public void exportToCSV(String filePath,
                            String status, String priority, String category,
                            String department, LocalDate fromDate, LocalDate toDate)
            throws ReportException, DatabaseException {

        List<Complaint> data = reportDAO.getFilteredComplaints(
                status, priority, category, department, fromDate, toDate);

        // Polymorphic: IReport reference → CSVExporter at runtime
        IReport reporter = new CSVExporter(filePath);
        reporter.generate(data);

        logReportAction("EXPORT_CSV", "Exported " + data.size() + " complaints to CSV.");
    }

    /**
     * Exports ALL complaints to CSV (no filter).
     * Overloaded variant. (Method Overloading — OOP requirement)
     *
     * @param filePath the output file path
     * @throws ReportException   if the CSV file cannot be written
     * @throws DatabaseException if the data query fails
     */
    public void exportToCSV(String filePath) throws ReportException, DatabaseException {
        exportToCSV(filePath, null, null, null, null, null, null);
    }

    // ------------------------------------------------------------------
    // Print
    // ------------------------------------------------------------------

    /**
     * Sends filtered complaints to the system print dialog.
     *
     * <p>Uses an {@link IReport} reference that points to a
     * {@link PrintManager} at runtime — demonstrating polymorphism.</p>
     *
     * @param status     filter by status name; null = all
     * @param priority   filter by priority name; null = all
     * @param category   filter by category name; null = all
     * @param department filter by department; null = all
     * @param fromDate   start date; null = no lower bound
     * @param toDate     end date; null = no upper bound
     * @throws ReportException   if the print job fails
     * @throws DatabaseException if the data query fails
     */
    public void printReport(String status, String priority, String category,
                            String department, LocalDate fromDate, LocalDate toDate)
            throws ReportException, DatabaseException {

        List<Complaint> data = reportDAO.getFilteredComplaints(
                status, priority, category, department, fromDate, toDate);

        // Polymorphic: IReport reference → PrintManager at runtime
        IReport reporter = new PrintManager();
        reporter.generate(data);

        logReportAction("PRINT_REPORT", "Printed report with " + data.size() + " complaints.");
    }

    // ------------------------------------------------------------------
    // Data accessors (for preview in the Reports panel)
    // ------------------------------------------------------------------

    /**
     * Returns filtered complaints for preview in the reports table.
     */
    public List<Complaint> getFilteredComplaints(
            String status, String priority, String category,
            String department, LocalDate fromDate, LocalDate toDate)
            throws DatabaseException {
        return reportDAO.getFilteredComplaints(
                status, priority, category, department, fromDate, toDate);
    }

    /**
     * Returns resolved complaints in a date range.
     */
    public List<Complaint> getResolvedByDateRange(LocalDate from, LocalDate to)
            throws DatabaseException {
        return reportDAO.getResolvedComplaintsByDateRange(from, to);
    }

    /**
     * Returns category summary (name, count) for chart rendering.
     */
    public List<Object[]> getCategorySummary() throws DatabaseException {
        return reportDAO.getCategorySummary();
    }

    /**
     * Returns department summary (name, count) for chart rendering.
     */
    public List<Object[]> getDepartmentSummary() throws DatabaseException {
        return reportDAO.getDepartmentSummary();
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Writes an activity log entry for a report action.
     *
     * @param action      the action code
     * @param description the description
     */
    private void logReportAction(String action, String description) {
        var user = SessionManager.getCurrentUser();
        if (user == null) return;
        try {
            userDAO.insertActivityLog(new ActivityLog(
                user.getId(), action, description, "REPORT", 0));
        } catch (DatabaseException e) {
            System.err.println("[ReportService] Failed to log report action: " + e.getMessage());
        }
    }
}
