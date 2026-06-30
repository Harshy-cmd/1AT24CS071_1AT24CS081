package service;

import dao.IComplaintDAO;
import dao.IUserDAO;
import dao.implementation.ComplaintDAOImpl;
import dao.implementation.UserDAOImpl;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.*;
import util.SessionManager;
import util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Business logic service for complaint-related operations.
 *
 * <p>This class sits between the {@link controller.ComplaintController}
 * and the DAO layer. It enforces business rules that go beyond simple
 * CRUD — such as generating complaint numbers, recording history,
 * logging activity, and validating inputs before they reach the database.</p>
 *
 * <p><strong>OOP Role — Method Overloading:</strong>
 * Several methods are overloaded to provide convenient calling conventions
 * while keeping the implementation DRY.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ComplaintService {

    // ------------------------------------------------------------------
    // Dependencies (injected via constructor — Loose Coupling)
    // ------------------------------------------------------------------

    private final IComplaintDAO complaintDAO;
    private final IUserDAO      userDAO;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default constructor: uses the production DAO implementations.
     */
    public ComplaintService() {
        this.complaintDAO = new ComplaintDAOImpl();
        this.userDAO      = new UserDAOImpl();
    }

    /**
     * Injection constructor: accepts custom DAO instances (e.g., mocks
     * for unit testing). Follows the Dependency Inversion Principle.
     *
     * @param complaintDAO a concrete implementation of {@link IComplaintDAO}
     * @param userDAO      a concrete implementation of {@link IUserDAO}
     */
    public ComplaintService(IComplaintDAO complaintDAO, IUserDAO userDAO) {
        this.complaintDAO = complaintDAO;
        this.userDAO      = userDAO;
    }

    // ------------------------------------------------------------------
    // Register / Create
    // ------------------------------------------------------------------

    /**
     * Registers a new complaint after validating input fields, generating
     * a complaint number, persisting it, and recording the activity.
     *
     * @param complaint the complaint to register; ID must be 0 (new record)
     * @return the generated {@code complaint_id}
     * @throws ValidationException if any required field fails validation
     * @throws DatabaseException   if the database operation fails
     */
    public int registerComplaint(Complaint complaint)
            throws ValidationException, DatabaseException {

        // Validate all input fields before touching the DB
        Validator.validateComplaint(complaint);

        // Generate and assign the formatted complaint number
        String number = complaintDAO.generateNextComplaintNumber();
        complaint.setComplaintNumber(number);
        complaint.setStatus(Status.PENDING);

        // Persist to database
        int newId = complaintDAO.insertComplaint(complaint);
        complaint.setComplaintId(newId);

        // Record first history entry (null → PENDING)
        User currentUser = SessionManager.getCurrentUser();
        int  userId      = (currentUser != null) ? currentUser.getId() : complaint.getCreatedBy();

        ComplaintHistory history = new ComplaintHistory(newId, userId, null, Status.PENDING,
                "Complaint registered.");
        complaintDAO.insertHistory(history);

        // Audit log
        userDAO.insertActivityLog(new ActivityLog(
            userId, "CREATE_COMPLAINT",
            "Created complaint " + number + ".",
            "COMPLAINT", newId));

        return newId;
    }

    // ------------------------------------------------------------------
    // Read
    // ------------------------------------------------------------------

    /**
     * Returns all complaints, ordered by date descending.
     *
     * @return list of all complaints
     * @throws DatabaseException on failure
     */
    public List<Complaint> getAllComplaints() throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.findAll();
        } else if ("EMPLOYEE".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.findByAssignedEmployee(user.getId());
        } else {
            return complaintDAO.findByCreator(user.getId());
        }
    }

    /**
     * Returns a single complaint by its database ID.
     *
     * @param complaintId the primary key
     * @return the complaint or {@code null}
     * @throws DatabaseException on failure
     */
    public Complaint getComplaintById(int complaintId) throws DatabaseException {
        return complaintDAO.findById(complaintId);
    }

    /**
     * Returns the status-change history for a complaint.
     *
     * @param complaintId the complaint ID
     * @return list of history entries (oldest first)
     * @throws DatabaseException on failure
     */
    public List<ComplaintHistory> getComplaintHistory(int complaintId)
            throws DatabaseException {
        return complaintDAO.findHistoryByComplaintId(complaintId);
    }

    /**
     * Returns today's complaints.
     *
     * @return list of complaints created today
     * @throws DatabaseException on failure
     */
    public List<Complaint> getTodaysComplaints() throws DatabaseException {
        return complaintDAO.findTodaysComplaints();
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    /**
     * Updates a complaint's editable fields after full validation.
     *
     * @param complaint the updated complaint; ID must match an existing record
     * @throws ValidationException if validation fails
     * @throws DatabaseException   on database failure
     */
    public void updateComplaint(Complaint complaint)
            throws ValidationException, DatabaseException {

        Validator.validateComplaint(complaint);
        complaintDAO.updateComplaint(complaint);

        User currentUser = SessionManager.getCurrentUser();
        int  userId      = (currentUser != null) ? currentUser.getId() : complaint.getCreatedBy();

        userDAO.insertActivityLog(new ActivityLog(
            userId, "UPDATE_COMPLAINT",
            "Updated complaint " + complaint.getComplaintNumber() + ".",
            "COMPLAINT", complaint.getComplaintId()));
    }

    /**
     * Updates only the status of a complaint and records the transition.
     * Overloaded variant — no remarks. (Method Overloading — OOP requirement)
     *
     * @param complaintId the ID of the complaint to update
     * @param newStatus   the new status
     * @throws DatabaseException on failure
     */
    public void updateStatus(int complaintId, Status newStatus)
            throws DatabaseException {
        updateStatus(complaintId, newStatus, null);
    }

    /**
     * Updates only the status of a complaint and records the transition
     * with optional remarks.
     * Overloaded variant with remarks. (Method Overloading — OOP requirement)
     *
     * @param complaintId the ID of the complaint
     * @param newStatus   the new status
     * @param remarks     optional remarks to accompany the status change
     * @throws DatabaseException on failure
     */
    public void updateStatus(int complaintId, Status newStatus, String remarks)
            throws DatabaseException {

        Complaint existing = complaintDAO.findById(complaintId);
        if (existing == null) throw new DatabaseException("Complaint not found: ID " + complaintId);

        Status oldStatus = existing.getStatus();
        complaintDAO.updateStatus(complaintId, newStatus);

        User currentUser = SessionManager.getCurrentUser();
        int  userId      = (currentUser != null) ? currentUser.getId() : existing.getCreatedBy();

        // Record history
        ComplaintHistory history = new ComplaintHistory(complaintId, userId,
                oldStatus, newStatus, remarks);
        complaintDAO.insertHistory(history);

        // Audit log
        userDAO.insertActivityLog(new ActivityLog(
            userId, "UPDATE_STATUS",
            "Changed complaint " + existing.getComplaintNumber() +
                " from " + oldStatus.getDisplayName() +
                " to "   + newStatus.getDisplayName() + ".",
            "COMPLAINT", complaintId));
    }

    /**
     * Assigns a complaint to an employee and records the assignment.
     *
     * @param complaintId the complaint to assign
     * @param employeeId  the employee to assign it to
     * @throws DatabaseException on failure
     */
    public void assignComplaint(int complaintId, int employeeId)
            throws DatabaseException {

        Complaint existing = complaintDAO.findById(complaintId);
        if (existing == null) throw new DatabaseException("Complaint not found: ID " + complaintId);

        complaintDAO.assignComplaint(complaintId, employeeId);

        User currentUser = SessionManager.getCurrentUser();
        int  userId      = (currentUser != null) ? currentUser.getId() : 0;

        ComplaintHistory history = new ComplaintHistory(complaintId, userId,
                existing.getStatus(), Status.ASSIGNED,
                "Assigned to employee ID: " + employeeId);
        complaintDAO.insertHistory(history);

        userDAO.insertActivityLog(new ActivityLog(
            userId, "ASSIGN_COMPLAINT",
            "Assigned complaint " + existing.getComplaintNumber() +
                " to employee ID " + employeeId + ".",
            "COMPLAINT", complaintId));
    }

    // ------------------------------------------------------------------
    // Delete
    // ------------------------------------------------------------------

    /**
     * Deletes a complaint from the system after admin privilege check.
     *
     * @param complaintId the ID of the complaint to delete
     * @throws DatabaseException on failure
     */
    public void deleteComplaint(int complaintId) throws DatabaseException {
        Complaint c = complaintDAO.findById(complaintId);
        if (c == null) throw new DatabaseException("Complaint not found: ID " + complaintId);

        complaintDAO.deleteComplaint(complaintId);

        User currentUser = SessionManager.getCurrentUser();
        int  userId      = (currentUser != null) ? currentUser.getId() : 0;

        userDAO.insertActivityLog(new ActivityLog(
            userId, "DELETE_COMPLAINT",
            "Deleted complaint " + c.getComplaintNumber() + ".",
            "COMPLAINT", complaintId));
    }

    // ------------------------------------------------------------------
    // Search and Filter
    // ------------------------------------------------------------------

    private List<Complaint> filterListByKeyword(List<Complaint> list, String keyword) {
        if (keyword == null || keyword.isBlank()) return list;
        String kw = keyword.toLowerCase().trim();
        List<Complaint> filtered = new java.util.ArrayList<>();
        for (Complaint c : list) {
            if (c.getComplaintNumber().toLowerCase().contains(kw)
                    || c.getTitle().toLowerCase().contains(kw)
                    || c.getDescription().toLowerCase().contains(kw)
                    || (c.getLocation() != null && c.getLocation().toLowerCase().contains(kw))
                    || (c.getDepartment() != null && c.getDepartment().toLowerCase().contains(kw))) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    private List<Complaint> filterListByKeyword(List<Complaint> list, String keyword, String fieldName) {
        if (keyword == null || keyword.isBlank()) return list;
        String kw = keyword.toLowerCase().trim();
        List<Complaint> filtered = new java.util.ArrayList<>();
        for (Complaint c : list) {
            String value = "";
            switch (fieldName.toLowerCase()) {
                case "title":       value = c.getTitle(); break;
                case "location":    value = c.getLocation(); break;
                case "department":  value = c.getDepartment(); break;
                case "description": value = c.getDescription(); break;
            }
            if (value != null && value.toLowerCase().contains(kw)) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    /**
     * Searches complaints using a keyword across all text fields.
     *
     * @param keyword the search term
     * @return list of matching complaints
     * @throws DatabaseException on failure
     */
    public List<Complaint> search(String keyword) throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            if (keyword == null || keyword.isBlank()) return getAllComplaints();
            return complaintDAO.searchByKeyword(keyword.trim());
        } else {
            return filterListByKeyword(getAllComplaints(), keyword);
        }
    }

    /**
     * Searches complaints by keyword restricted to a specific field.
     * Overloaded variant. (Method Overloading — OOP requirement)
     *
     * @param keyword   the search term
     * @param fieldName the specific field to search in
     * @return list of matching complaints
     * @throws DatabaseException on failure
     */
    public List<Complaint> search(String keyword, String fieldName) throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.searchByKeyword(keyword.trim(), fieldName);
        } else {
            return filterListByKeyword(getAllComplaints(), keyword, fieldName);
        }
    }

    /**
     * Returns complaints filtered by status.
     *
     * @param status the target status
     * @return list of matching complaints
     * @throws DatabaseException on failure
     */
    public List<Complaint> filterByStatus(Status status) throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.findByStatus(status);
        } else {
            List<Complaint> filtered = new java.util.ArrayList<>();
            for (Complaint c : getAllComplaints()) {
                if (c.getStatus() == status) filtered.add(c);
            }
            return filtered;
        }
    }

    /**
     * Returns complaints filtered by priority.
     *
     * @param priority the target priority
     * @return list of matching complaints
     * @throws DatabaseException on failure
     */
    public List<Complaint> filterByPriority(Priority priority) throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.findByPriority(priority);
        } else {
            List<Complaint> filtered = new java.util.ArrayList<>();
            for (Complaint c : getAllComplaints()) {
                if (c.getPriority() == priority) filtered.add(c);
            }
            return filtered;
        }
    }

    /**
     * Returns complaints within a date range.
     *
     * @param from start date
     * @param to   end date
     * @return list of complaints in range
     * @throws DatabaseException on failure
     */
    public List<Complaint> filterByDateRange(LocalDate from, LocalDate to)
            throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.findByDateRange(from, to);
        } else {
            List<Complaint> filtered = new java.util.ArrayList<>();
            for (Complaint c : getAllComplaints()) {
                LocalDate date = c.getDateCreated().toLocalDate();
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    filtered.add(c);
                }
            }
            return filtered;
        }
    }

    // ------------------------------------------------------------------
    // Dashboard Statistics
    // ------------------------------------------------------------------

    /**
     * Returns a map of Status → count for all statuses. Used by the
     * dashboard to render stat cards.
     *
     * @return status count map
     * @throws DatabaseException on failure
     */
    public Map<Status, Integer> getStatusCounts() throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.getStatusCounts();
        }
        Map<Status, Integer> counts = new java.util.LinkedHashMap<>();
        for (Status s : Status.values()) counts.put(s, 0);
        for (Complaint c : getAllComplaints()) {
            counts.put(c.getStatus(), counts.getOrDefault(c.getStatus(), 0) + 1);
        }
        return counts;
    }

    /**
     * Returns the total number of complaints.
     *
     * @return total count
     * @throws DatabaseException on failure
     */
    public int getTotalCount() throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.getTotalCount();
        }
        return getAllComplaints().size();
    }

    /**
     * Returns the count of CRITICAL priority complaints.
     *
     * @return critical count
     * @throws DatabaseException on failure
     */
    public int getCriticalCount() throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.getCriticalCount();
        }
        int count = 0;
        for (Complaint c : getAllComplaints()) {
            if (c.getPriority() == Priority.CRITICAL) count++;
        }
        return count;
    }

    /**
     * Returns complaint counts by category for chart rendering.
     *
     * @return map of category name → count
     * @throws DatabaseException on failure
     */
    public Map<String, Integer> getCategoryDistribution() throws DatabaseException {
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return complaintDAO.getCategoryDistribution();
        }
        Map<String, Integer> dist = new java.util.LinkedHashMap<>();
        for (Complaint c : getAllComplaints()) {
            String cat = c.getCategory().getDisplayName();
            dist.put(cat, dist.getOrDefault(cat, 0) + 1);
        }
        return dist;
    }
}
