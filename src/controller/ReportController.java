package controller;

import exceptions.DatabaseException;
import exceptions.ReportException;
import model.Complaint;
import service.ReportService;

import java.time.LocalDate;
import java.util.List;

/**
 * Thin controller layer for report generation and export operations.
 *
 * <p>Delegates all business logic to {@link ReportService}, keeping the
 * UI free of any file I/O or print-job details.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ReportController {

    private final ReportService reportService;

    /** Creates a controller using the default service implementation. */
    public ReportController() {
        this.reportService = new ReportService();
    }

    /** Creates a controller with an injected service (for testing). */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ------------------------------------------------------------------
    // Export delegate
    // ------------------------------------------------------------------

    public void exportToCSV(String filePath,
                            String status, String priority, String category,
                            String department, LocalDate fromDate, LocalDate toDate)
            throws ReportException, DatabaseException {
        reportService.exportToCSV(filePath, status, priority, category,
                department, fromDate, toDate);
    }

    public void exportToCSV(String filePath) throws ReportException, DatabaseException {
        reportService.exportToCSV(filePath);
    }

    // ------------------------------------------------------------------
    // Print delegate
    // ------------------------------------------------------------------

    public void printReport(String status, String priority, String category,
                            String department, LocalDate fromDate, LocalDate toDate)
            throws ReportException, DatabaseException {
        reportService.printReport(status, priority, category,
                department, fromDate, toDate);
    }

    // ------------------------------------------------------------------
    // Data delegates (for preview table)
    // ------------------------------------------------------------------

    public List<Complaint> getFilteredComplaints(
            String status, String priority, String category,
            String department, LocalDate fromDate, LocalDate toDate)
            throws DatabaseException {
        return reportService.getFilteredComplaints(
                status, priority, category, department, fromDate, toDate);
    }

    public List<Object[]> getCategorySummary() throws DatabaseException {
        return reportService.getCategorySummary();
    }

    public List<Object[]> getDepartmentSummary() throws DatabaseException {
        return reportService.getDepartmentSummary();
    }
}
