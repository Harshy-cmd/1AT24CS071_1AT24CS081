package dao.implementation;

import dao.IReportDAO;
import database.DatabaseConnection;
import exceptions.DatabaseException;
import model.*;
import util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link IReportDAO}.
 *
 * <p>Provides aggregate and filtered queries intended exclusively for
 * report generation and CSV export. By keeping these queries in a
 * dedicated DAO, the main {@link ComplaintDAOImpl} stays focused on
 * single-entity CRUD, following the Single Responsibility Principle.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ReportDAOImpl implements IReportDAO {

    // Shared SELECT used by all report methods — join users for names
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
    // Filtered report query
    // ------------------------------------------------------------------

    @Override
    public List<Complaint> getFilteredComplaints(
            String status, String priority, String category,
            String department, LocalDate fromDate, LocalDate toDate)
            throws DatabaseException {

        // Build the WHERE clause dynamically — still using PreparedStatement params
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status     != null && !status.isBlank())     { where.append("AND c.status=? ");     params.add(status.toUpperCase()); }
        if (priority   != null && !priority.isBlank())   { where.append("AND c.priority=? ");   params.add(priority.toUpperCase()); }
        if (category   != null && !category.isBlank())   { where.append("AND c.category=? ");   params.add(category.toUpperCase()); }
        if (department != null && !department.isBlank()) { where.append("AND c.department=? "); params.add(department); }
        if (fromDate   != null) { where.append("AND DATE(c.date_created) >= ? "); params.add(Date.valueOf(fromDate)); }
        if (toDate     != null) { where.append("AND DATE(c.date_created) <= ? "); params.add(Date.valueOf(toDate)); }

        final String sql = SELECT_BASE + where + "ORDER BY c.date_created DESC";

        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String)     ps.setString(i + 1, (String) p);
                else if (p instanceof Date)  ps.setDate(i + 1, (Date) p);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Filtered report query failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Resolved complaints in date range
    // ------------------------------------------------------------------

    @Override
    public List<Complaint> getResolvedComplaintsByDateRange(LocalDate from, LocalDate to)
            throws DatabaseException {
        final String sql = SELECT_BASE +
            "WHERE c.status IN ('RESOLVED','CLOSED') " +
            "  AND DATE(c.resolution_date) BETWEEN ? AND ? " +
            "ORDER BY c.resolution_date DESC";

        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Resolved complaints query failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Summary aggregates
    // ------------------------------------------------------------------

    @Override
    public List<Object[]> getCategorySummary() throws DatabaseException {
        final String sql =
            "SELECT category, COUNT(*) AS cnt " +
            "FROM complaints GROUP BY category ORDER BY cnt DESC";

        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ComplaintCategory cat = ComplaintCategory.fromString(rs.getString("category"));
                rows.add(new Object[]{cat.getDisplayName(), rs.getInt("cnt")});
            }
        } catch (SQLException e) {
            throw new DatabaseException("Category summary query failed: " + e.getMessage(), e);
        }
        return rows;
    }

    @Override
    public List<Object[]> getDepartmentSummary() throws DatabaseException {
        final String sql =
            "SELECT department, COUNT(*) AS cnt " +
            "FROM complaints " +
            "WHERE department IS NOT NULL " +
            "GROUP BY department ORDER BY cnt DESC";

        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getString("department"), rs.getInt("cnt")});
            }
        } catch (SQLException e) {
            throw new DatabaseException("Department summary query failed: " + e.getMessage(), e);
        }
        return rows;
    }

    // ------------------------------------------------------------------
    // Private helper — ResultSet row mapper
    // ------------------------------------------------------------------

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
}
