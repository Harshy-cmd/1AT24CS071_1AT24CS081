package controller;

import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.*;
import service.ComplaintService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Thin controller layer for complaint operations.
 *
 * <p>UI panels call methods on this controller rather than directly
 * on the service. The controller's responsibilities are minimal:
 * <ol>
 *   <li>Receive the call from the UI</li>
 *   <li>Delegate to {@link ComplaintService}</li>
 *   <li>Return results or exceptions to the UI</li>
 * </ol>
 * No business logic lives here — it belongs in {@link ComplaintService}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ComplaintController {

    private final ComplaintService complaintService;

    /** Creates a controller using the default service implementation. */
    public ComplaintController() {
        this.complaintService = new ComplaintService();
    }

    /** Creates a controller with an injected service (for testing). */
    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // ------------------------------------------------------------------
    // Delegate methods — CRUD
    // ------------------------------------------------------------------

    public int registerComplaint(Complaint complaint)
            throws ValidationException, DatabaseException {
        return complaintService.registerComplaint(complaint);
    }

    public Complaint getComplaintById(int id) throws DatabaseException {
        return complaintService.getComplaintById(id);
    }

    public List<Complaint> getAllComplaints() throws DatabaseException {
        return complaintService.getAllComplaints();
    }

    public List<Complaint> getTodaysComplaints() throws DatabaseException {
        return complaintService.getTodaysComplaints();
    }

    public void updateComplaint(Complaint complaint)
            throws ValidationException, DatabaseException {
        complaintService.updateComplaint(complaint);
    }

    public void updateStatus(int complaintId, Status newStatus)
            throws DatabaseException {
        complaintService.updateStatus(complaintId, newStatus);
    }

    public void updateStatus(int complaintId, Status newStatus, String remarks)
            throws DatabaseException {
        complaintService.updateStatus(complaintId, newStatus, remarks);
    }

    public void assignComplaint(int complaintId, int employeeId)
            throws DatabaseException {
        complaintService.assignComplaint(complaintId, employeeId);
    }

    public void deleteComplaint(int complaintId) throws DatabaseException {
        complaintService.deleteComplaint(complaintId);
    }

    // ------------------------------------------------------------------
    // Delegate methods — Search & Filter
    // ------------------------------------------------------------------

    public List<Complaint> search(String keyword) throws DatabaseException {
        return complaintService.search(keyword);
    }

    public List<Complaint> search(String keyword, String fieldName)
            throws DatabaseException {
        return complaintService.search(keyword, fieldName);
    }

    public List<Complaint> filterByStatus(Status status) throws DatabaseException {
        return complaintService.filterByStatus(status);
    }

    public List<Complaint> filterByPriority(Priority priority) throws DatabaseException {
        return complaintService.filterByPriority(priority);
    }

    public List<Complaint> filterByDateRange(LocalDate from, LocalDate to)
            throws DatabaseException {
        return complaintService.filterByDateRange(from, to);
    }

    // ------------------------------------------------------------------
    // Delegate methods — History
    // ------------------------------------------------------------------

    public List<ComplaintHistory> getComplaintHistory(int complaintId)
            throws DatabaseException {
        return complaintService.getComplaintHistory(complaintId);
    }

    // ------------------------------------------------------------------
    // Delegate methods — Statistics
    // ------------------------------------------------------------------

    public Map<Status, Integer> getStatusCounts() throws DatabaseException {
        return complaintService.getStatusCounts();
    }

    public int getTotalCount() throws DatabaseException {
        return complaintService.getTotalCount();
    }

    public int getCriticalCount() throws DatabaseException {
        return complaintService.getCriticalCount();
    }

    public Map<String, Integer> getCategoryDistribution() throws DatabaseException {
        return complaintService.getCategoryDistribution();
    }
}
