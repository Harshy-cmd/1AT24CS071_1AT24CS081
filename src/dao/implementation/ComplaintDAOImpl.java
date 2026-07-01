package dao.implementation;

import dao.IComplaintDAO;
import database.DatabaseConnection;
import exceptions.DatabaseException;
import model.*;
import util.DateUtil;


import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;

/**
 * JDBC implementation of {@link IComplaintDAO}.
 *
 * <p><strong>Security:</strong> Every SQL query uses {@link PreparedStatement}
 * with parameter binding. String concatenation into SQL is never used.</p>
 *
 * <p><strong>OOP Role — Polymorphism:</strong>
 * The service layer holds a reference of type {@link IComplaintDAO} pointing
 * to this implementation. Swapping implementations (e.g., a mock for testing)
 * requires no changes in the service or controller layers.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ComplaintDAOImpl implements IComplaintDAO {

    // Base SELECT with JOIN to denormalise user names for display
    private static final String SELECT_BASE =
        "SELECT c.complaint_id, c.complaint_number, c.title, c.description, " +
        "       c.category, c.priority, c.status, c.location, c.department, " +
        "       c.assigned_to, COALESCE(a.full_name,'Unassigned') AS assigned_name, " +
        "       c.created_by, cr.full_name AS created_name, " +
        "       c.date_created, c.date_updated, c.resolution_date, c.remarks " +
        "FROM complaints c " +
        "LEFT JOIN users a  ON c.assigned_to = a.user_id " +
        "JOIN  users cr ON c.created_by  = cr.user_id ";

    // ------------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------------

    @Override
    public int insertComplaint(Complaint complaint) throws DatabaseException {
        final String sql =
            "INSERT INTO complaints " +
            "(complaint_number, title, description, category, priority, status, " +
            " location, department, assigned_to, created_by, remarks) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, complaint.getComplaintNumber());
            ps.setString(2, complaint.getTitle());
            ps.setString(3, complaint.getDescription());
            ps.setString(4, complaint.getCategory().name());
            ps.setString(5, complaint.getPriority().name());
            ps.setString(6, complaint.getStatus().name());
            ps.setString(7, complaint.getLocation());
            ps.setString(8, complaint.getDepartment());
            if (complaint.getAssignedTo() > 0) {
                ps.setInt(9, complaint.getAssignedTo());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setInt(10, complaint.getCreatedBy());
            ps.setString(11, complaint.getRemarks());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Inserting complaint failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new DatabaseException("Inserting complaint failed, no ID returned.");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert complaint: " + e.getMessage(), e, e.getErrorCode());
        }
    }

    // ------------------------------------------------------------------
    // SELECT — single record
    // ------------------------------------------------------------------

    @Override
    public Complaint findById(int complaintId) throws DatabaseException {
        final String sql = SELECT_BASE + "WHERE c.complaint_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find complaint by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Complaint findByNumber(String complaintNumber) throws DatabaseException {
        final String sql = SELECT_BASE + "WHERE c.complaint_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, complaintNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find complaint by number: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // SELECT — collections
    // ------------------------------------------------------------------

    @Override
    public List<Complaint> findAll() throws DatabaseException {
        final String sql = SELECT_BASE + "ORDER BY c.date_created DESC";
        return executeListQuery(sql);
    }

    @Override
    public List<Complaint> findByStatus(Status status) throws DatabaseException {
        final String sql = SELECT_BASE + "WHERE c.status = ? ORDER BY c.date_created DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find complaints by status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Complaint> findByPriority(Priority priority) throws DatabaseException {
        final String sql = SELECT_BASE + "WHERE c.priority = ? ORDER BY c.date_created DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, priority.name());
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find complaints by priority: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Complaint> findByAssignedEmployee(int employeeId) throws DatabaseException {
        final String sql = SELECT_BASE + "WHERE c.assigned_to = ? ORDER BY c.date_created DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find complaints by assigned employee: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Complaint> findTodaysComplaints() throws DatabaseException {
        final String sql = SELECT_BASE + "WHERE DATE(c.date_created) = CURDATE() ORDER BY c.date_created DESC";
        return executeListQuery(sql);
    }

    @Override
    public List<Complaint> findWeeklyComplaints() throws DatabaseException {
        final String sql = SELECT_BASE +
            "WHERE c.date_created >= DATE_SUB(NOW(), INTERVAL 7 DAY) ORDER BY c.date_created DESC";
        return executeListQuery(sql);
    }

    @Override
    public List<Complaint> findByDateRange(LocalDate from, LocalDate to) throws DatabaseException {
        final String sql = SELECT_BASE +
            "WHERE DATE(c.date_created) BETWEEN ? AND ? ORDER BY c.date_created DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find complaints by date range: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------

    @Override
    public List<Complaint> searchByKeyword(String keyword) throws DatabaseException {
        final String sql = SELECT_BASE +
            "WHERE c.complaint_number LIKE ? " +
            "   OR c.title LIKE ? " +
            "   OR c.description LIKE ? " +
            "   OR c.location LIKE ? " +
            "   OR c.department LIKE ? " +
            "ORDER BY c.date_created DESC";

        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Keyword search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Overloaded search: restricts the LIKE match to a specific column.
     * Method Overloading — OOP requirement.
     */
    @Override
    public List<Complaint> searchByKeyword(String keyword, String fieldName) throws DatabaseException {
        // Whitelist allowed field names to prevent SQL injection even in column names
        String safeField;
        switch (fieldName.toLowerCase()) {
            case "title":       safeField = "c.title";       break;
            case "location":    safeField = "c.location";    break;
            case "department":  safeField = "c.department";  break;
            case "description": safeField = "c.description"; break;
            default: throw new DatabaseException("Invalid search field: " + fieldName);
        }
        final String sql = SELECT_BASE + "WHERE " + safeField + " LIKE ? ORDER BY c.date_created DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Field search failed: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    @Override
    public boolean updateComplaint(Complaint complaint) throws DatabaseException {
        final String sql =
            "UPDATE complaints SET " +
            "  title=?, description=?, category=?, priority=?, status=?, " +
            "  location=?, department=?, assigned_to=?, remarks=?, " +
            "  resolution_date=? " +
            "WHERE complaint_id=?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, complaint.getTitle());
            ps.setString(2, complaint.getDescription());
            ps.setString(3, complaint.getCategory().name());
            ps.setString(4, complaint.getPriority().name());
            ps.setString(5, complaint.getStatus().name());
            ps.setString(6, complaint.getLocation());
            ps.setString(7, complaint.getDepartment());
            if (complaint.getAssignedTo() > 0) {
                ps.setInt(8, complaint.getAssignedTo());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setString(9, complaint.getRemarks());
            if (complaint.getResolutionDate() != null) {
                ps.setDate(10, java.sql.Date.valueOf(complaint.getResolutionDate()));
            } else {
                ps.setNull(10, Types.DATE);
            }
            ps.setInt(11, complaint.getComplaintId());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update complaint: " + e.getMessage(), e, e.getErrorCode());
        }
    }

    @Override
    public boolean updateStatus(int complaintId, Status newStatus) throws DatabaseException {
        final String sql =
            "UPDATE complaints SET status=?, " +
            "resolution_date = CASE WHEN ? IN ('RESOLVED','CLOSED') THEN CURDATE() ELSE resolution_date END " +
            "WHERE complaint_id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, newStatus.name());
            ps.setInt(3, complaintId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean assignComplaint(int complaintId, int employeeId) throws DatabaseException {
        final String sql =
            "UPDATE complaints SET assigned_to=?, status='ASSIGNED' WHERE complaint_id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, complaintId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to assign complaint: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    @Override
    public boolean deleteComplaint(int complaintId) throws DatabaseException {
        final String sql = "DELETE FROM complaints WHERE complaint_id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete complaint: " + e.getMessage(), e, e.getErrorCode());
        }
    }

    // ------------------------------------------------------------------
    // Statistics
    // ------------------------------------------------------------------

    @Override
    public Map<Status, Integer> getStatusCounts() throws DatabaseException {
        final String sql = "SELECT status, COUNT(*) AS cnt FROM complaints GROUP BY status";
        Map<Status, Integer> counts = new LinkedHashMap<>();
        // Initialise all statuses to 0
        for (Status s : Status.values()) counts.put(s, 0);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Status s = Status.fromString(rs.getString("status"));
                counts.put(s, rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get status counts: " + e.getMessage(), e);
        }
        return counts;
    }

    @Override
    public int getTotalCount() throws DatabaseException {
        final String sql = "SELECT COUNT(*) FROM complaints";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get total count: " + e.getMessage(), e);
        }
    }

    @Override
    public int getCriticalCount() throws DatabaseException {
        final String sql = "SELECT COUNT(*) FROM complaints WHERE priority='CRITICAL'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get critical count: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> getCategoryDistribution() throws DatabaseException {
        final String sql =
            "SELECT category, COUNT(*) AS cnt FROM complaints GROUP BY category ORDER BY cnt DESC";
        Map<String, Integer> dist = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ComplaintCategory cat = ComplaintCategory.fromString(rs.getString("category"));
                dist.put(cat.getDisplayName(), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get category distribution: " + e.getMessage(), e);
        }
        return dist;
    }

    // ------------------------------------------------------------------
    // Complaint Number Generation
    // ------------------------------------------------------------------

    @Override
    public String generateNextComplaintNumber() throws DatabaseException {
        int currentYear = Year.now().getValue();
        final String sql =
            "SELECT COUNT(*) FROM complaints WHERE YEAR(date_created) = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentYear);
            try (ResultSet rs = ps.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                return String.format("CMS-%d-%04d", currentYear, count + 1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate complaint number: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Complaint History
    // ------------------------------------------------------------------

    @Override
    public void insertHistory(ComplaintHistory history) throws DatabaseException {
        final String sql =
            "INSERT INTO complaint_history (complaint_id, changed_by, old_status, new_status, remarks) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, history.getComplaintId());
            ps.setInt(2, history.getChangedBy());
            if (history.getOldStatus() != null) {
                ps.setString(3, history.getOldStatus().name());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, history.getNewStatus().name());
            ps.setString(5, history.getRemarks());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert complaint history: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ComplaintHistory> findHistoryByComplaintId(int complaintId)
            throws DatabaseException {
        final String sql =
            "SELECT h.history_id, h.complaint_id, h.changed_by, " +
            "       u.full_name AS changed_by_name, h.old_status, h.new_status, " +
            "       h.change_date, h.remarks " +
            "FROM complaint_history h " +
            "JOIN users u ON h.changed_by = u.user_id " +
            "WHERE h.complaint_id = ? ORDER BY h.change_date ASC";

        List<ComplaintHistory> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Status oldStatus = null;
                    String oldStr = rs.getString("old_status");
                    if (oldStr != null) oldStatus = Status.fromString(oldStr);

                    list.add(new ComplaintHistory(
                        rs.getInt("history_id"),
                        rs.getInt("complaint_id"),
                        rs.getInt("changed_by"),
                        rs.getString("changed_by_name"),
                        oldStatus,
                        Status.fromString(rs.getString("new_status")),
                        DateUtil.toLocalDateTime(rs.getTimestamp("change_date")),
                        rs.getString("remarks")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find complaint history: " + e.getMessage(), e);
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Executes a no-parameter list query using the base SELECT and returns
     * the mapped list. Used for simple findAll-style queries.
     */
    private List<Complaint> executeListQuery(String sql) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("List query failed: " + e.getMessage(), e);
        }
    }

    /**
     * Maps an open {@link ResultSet} to a {@link List} of {@link Complaint}
     * objects, exhausting and closing the ResultSet.
     */
    private List<Complaint> mapList(ResultSet rs) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Complaint}.
     * All column names must match the {@link #SELECT_BASE} alias list.
     */
    private Complaint mapRow(ResultSet rs) throws SQLException {
        return new Complaint(
            rs.getInt("complaint_id"),
            rs.getString("complaint_number"),
            rs.getString("title"),
            rs.getString("description"),
            ComplaintCategory.fromString(rs.getString("category")),
            Priority.fromString(rs.getString("priority")),
            Status.fromString(rs.getString("status")),
            rs.getString("location"),
            rs.getString("department"),
            rs.getInt("assigned_to"),
            rs.getString("assigned_name"),
            rs.getInt("created_by"),
            rs.getString("created_name"),
            DateUtil.toLocalDateTime(rs.getTimestamp("date_created")),
            DateUtil.toLocalDateTime(rs.getTimestamp("date_updated")),
            DateUtil.toLocalDate(rs.getDate("resolution_date")),
            rs.getString("remarks")
        );
    }

    @Override
    public List<Complaint> findByCreator(int creatorId) throws DatabaseException {
        final String sql = SELECT_BASE + "WHERE c.created_by = ? ORDER BY c.date_created DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find complaints by creator: " + e.getMessage(), e);
        }
    }
}
