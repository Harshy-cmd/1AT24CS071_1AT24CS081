package dao;

import exceptions.DatabaseException;
import model.Complaint;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface defining database read operations that
 * support report generation in the Complaint Management System.
 *
 * <p>These methods retrieve pre-aggregated or date-filtered datasets
 * intended solely for report rendering and CSV export, keeping report
 * queries separate from the main complaint CRUD operations in
 * {@link IComplaintDAO}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 * @see     dao.implementation.ReportDAOImpl
 */
public interface IReportDAO {

    /**
     * Returns all complaints that match the specified filter criteria.
     * Any parameter may be {@code null} / {@code 0} to skip that filter.
     *
     * @param status     filter by status display name; null = all statuses
     * @param priority   filter by priority display name; null = all priorities
     * @param category   filter by category display name; null = all categories
     * @param department filter by department; null = all departments
     * @param fromDate   start of date range; null = no lower bound
     * @param toDate     end of date range; null = no upper bound
     * @return non-null list of matching complaints for the report
     * @throws DatabaseException if the query fails
     */
    List<Complaint> getFilteredComplaints(
            String status, String priority, String category,
            String department, LocalDate fromDate, LocalDate toDate)
            throws DatabaseException;

    /**
     * Returns all complaints resolved within a date range.
     * Used for the "Resolved Summary Report."
     *
     * @param from start of resolution date range
     * @param to   end of resolution date range
     * @return list of resolved complaints in the range
     * @throws DatabaseException if the query fails
     */
    List<Complaint> getResolvedComplaintsByDateRange(LocalDate from, LocalDate to)
            throws DatabaseException;

    /**
     * Returns a summary count grouped by category for the report.
     * Result format: category display name → count.
     *
     * @return ordered list where each element is a 2-element Object array
     *         [String categoryName, Integer count]
     * @throws DatabaseException if the query fails
     */
    List<Object[]> getCategorySummary() throws DatabaseException;

    /**
     * Returns a summary count grouped by department for the report.
     *
     * @return ordered list where each element is a 2-element Object array
     *         [String department, Integer count]
     * @throws DatabaseException if the query fails
     */
    List<Object[]> getDepartmentSummary() throws DatabaseException;
}
