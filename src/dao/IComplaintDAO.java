package dao;

import exceptions.DatabaseException;
import model.Complaint;
import model.ComplaintHistory;
import model.Priority;
import model.Status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface defining all database operations for
 * {@link Complaint} and {@link ComplaintHistory} entities.
 *
 * <p><strong>OOP Role — Interface &amp; Abstraction:</strong>
 * By depending on this interface rather than the concrete
 * {@link dao.implementation.ComplaintDAOImpl}, the service layer is
 * fully decoupled from JDBC implementation details. This enables future
 * replacement of the implementation (e.g., switching to JPA) without
 * touching any service or controller code — the Open/Closed Principle.</p>
 *
 * <p><strong>Security:</strong>
 * All implementations MUST use {@link java.sql.PreparedStatement} for
 * every query. SQL string concatenation is strictly prohibited.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 * @see     dao.implementation.ComplaintDAOImpl
 */
public interface IComplaintDAO {

    // ------------------------------------------------------------------
    // CRUD — Complaint
    // ------------------------------------------------------------------

    /**
     * Inserts a new complaint into the database and returns the
     * generated primary key.
     *
     * @param complaint the {@link Complaint} to persist; must not be null
     * @return the auto-generated {@code complaint_id}
     * @throws DatabaseException if the INSERT fails
     */
    int insertComplaint(Complaint complaint) throws DatabaseException;

    /**
     * Retrieves a single complaint by its primary key.
     *
     * @param complaintId the primary key to search for
     * @return the matching {@link Complaint}, or {@code null} if not found
     * @throws DatabaseException if the SELECT fails
     */
    Complaint findById(int complaintId) throws DatabaseException;

    /**
     * Retrieves a single complaint by its formatted complaint number.
     *
     * @param complaintNumber the formatted number (e.g., "CMS-2024-0001")
     * @return the matching {@link Complaint}, or {@code null} if not found
     * @throws DatabaseException if the SELECT fails
     */
    Complaint findByNumber(String complaintNumber) throws DatabaseException;

    /**
     * Returns all complaints in the system ordered by date_created descending.
     *
     * @return a non-null (possibly empty) list of all complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> findAll() throws DatabaseException;

    /**
     * Updates the mutable fields of an existing complaint.
     *
     * @param complaint the complaint with updated field values; ID must be set
     * @return {@code true} if exactly one row was updated; {@code false} otherwise
     * @throws DatabaseException if the UPDATE fails
     */
    boolean updateComplaint(Complaint complaint) throws DatabaseException;

    /**
     * Deletes a complaint and its entire history from the database.
     *
     * @param complaintId the primary key of the complaint to delete
     * @return {@code true} if exactly one row was deleted; {@code false} otherwise
     * @throws DatabaseException if the DELETE fails
     */
    boolean deleteComplaint(int complaintId) throws DatabaseException;

    // ------------------------------------------------------------------
    // Status and Assignment
    // ------------------------------------------------------------------

    /**
     * Updates only the {@code status} field of a complaint.
     *
     * @param complaintId the ID of the complaint to update
     * @param newStatus   the new {@link Status} to set
     * @return {@code true} on success
     * @throws DatabaseException if the UPDATE fails
     */
    boolean updateStatus(int complaintId, Status newStatus) throws DatabaseException;

    /**
     * Assigns a complaint to an employee and sets the status to ASSIGNED.
     *
     * @param complaintId the ID of the complaint
     * @param employeeId  the ID of the employee to assign to
     * @return {@code true} on success
     * @throws DatabaseException if the UPDATE fails
     */
    boolean assignComplaint(int complaintId, int employeeId) throws DatabaseException;

    // ------------------------------------------------------------------
    // Search and Filter
    // ------------------------------------------------------------------

    /**
     * Searches complaints by a keyword matching title, description, or
     * complaint number (case-insensitive partial match).
     *
     * @param keyword the search term; must not be null
     * @return list of matching complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> searchByKeyword(String keyword) throws DatabaseException;

    /**
     * Searches complaints by keyword restricted to a specific field.
     * Overloaded variant of {@link #searchByKeyword(String)}.
     *
     * @param keyword   the search term
     * @param fieldName the field to search in ("title","location","department")
     * @return list of matching complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> searchByKeyword(String keyword, String fieldName) throws DatabaseException;

    /**
     * Returns complaints filtered by status.
     *
     * @param status the {@link Status} to filter by
     * @return list of matching complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> findByStatus(Status status) throws DatabaseException;

    /**
     * Returns complaints filtered by priority.
     *
     * @param priority the {@link Priority} to filter by
     * @return list of matching complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> findByPriority(Priority priority) throws DatabaseException;

    /**
     * Returns complaints assigned to a specific employee.
     *
     * @param employeeId the user ID of the assigned employee
     * @return list of matching complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> findByAssignedEmployee(int employeeId) throws DatabaseException;

    /**
     * Returns complaints created on today's date.
     *
     * @return list of today's complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> findTodaysComplaints() throws DatabaseException;

    /**
     * Returns complaints created within the current calendar week.
     *
     * @return list of this week's complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> findWeeklyComplaints() throws DatabaseException;

    /**
     * Returns complaints created within a date range (inclusive).
     *
     * @param from the start date
     * @param to   the end date
     * @return list of matching complaints
     * @throws DatabaseException if the SELECT fails
     */
    List<Complaint> findByDateRange(LocalDate from, LocalDate to) throws DatabaseException;

    // ------------------------------------------------------------------
    // Statistics (for Dashboard)
    // ------------------------------------------------------------------

    /**
     * Returns a map of {@link Status} → count for all complaints.
     * Used to populate dashboard stat cards.
     *
     * @return map from status enum to complaint count
     * @throws DatabaseException if the query fails
     */
    Map<Status, Integer> getStatusCounts() throws DatabaseException;

    /**
     * Returns the total number of complaints in the system.
     *
     * @return total complaint count
     * @throws DatabaseException if the query fails
     */
    int getTotalCount() throws DatabaseException;

    /**
     * Returns the count of CRITICAL priority complaints.
     *
     * @return count of critical complaints
     * @throws DatabaseException if the query fails
     */
    int getCriticalCount() throws DatabaseException;

    /**
     * Returns a map of category name → complaint count for chart rendering.
     *
     * @return map from category display name to count
     * @throws DatabaseException if the query fails
     */
    Map<String, Integer> getCategoryDistribution() throws DatabaseException;

    // ------------------------------------------------------------------
    // Complaint Number Generation
    // ------------------------------------------------------------------

    /**
     * Generates the next unique complaint number in the format
     * {@code CMS-YYYY-NNNN}.
     *
     * @return the next formatted complaint number
     * @throws DatabaseException if the generation query fails
     */
    String generateNextComplaintNumber() throws DatabaseException;

    // ------------------------------------------------------------------
    // History
    // ------------------------------------------------------------------

    /**
     * Inserts a new status-change record into {@code complaint_history}.
     *
     * @param history the history entry to insert
     * @throws DatabaseException if the INSERT fails
     */
    void insertHistory(ComplaintHistory history) throws DatabaseException;

    /**
     * Returns the complete status-change history for a complaint,
     * ordered by {@code change_date} ascending.
     *
     * @param complaintId the complaint whose history to retrieve
     * @return list of history entries (oldest first)
     * @throws DatabaseException if the SELECT fails
     */
    List<ComplaintHistory> findHistoryByComplaintId(int complaintId)
            throws DatabaseException;

    /**
     * Returns complaints created by a specific user.
     *
     * @param creatorId the user ID of the creator (citizen/employee/admin)
     * @return list of complaints registered by this user
     * @throws DatabaseException on database failure
     */
    List<Complaint> findByCreator(int creatorId) throws DatabaseException;
}
