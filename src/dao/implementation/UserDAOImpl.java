package dao.implementation;

import dao.IUserDAO;
import database.DatabaseConnection;
import exceptions.DatabaseException;
import model.*;
import util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link IUserDAO}.
 *
 * <p>Handles all database operations for users and activity logs.
 * Returns {@link Admin} or {@link Employee} subclass instances based on
 * the {@code role} column — demonstrating <strong>polymorphism</strong>:
 * the caller receives a {@link User} reference that behaves according to
 * the actual runtime type.</p>
 *
 * <p><strong>Security:</strong> All queries use {@link PreparedStatement}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class UserDAOImpl implements IUserDAO {

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------

    @Override
    public User authenticate(String username, String passwordHash) throws DatabaseException {
        final String sql =
            "SELECT user_id, full_name, username, password_hash, email, phone, " +
            "       role, department, is_active, created_at, last_login " +
            "FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;    // credentials do not match
            }
        } catch (SQLException e) {
            throw new DatabaseException("Authentication query failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLastLogin(int userId) throws DatabaseException {
        final String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update last login: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------------

    @Override
    public int insertUser(User user) throws DatabaseException {
        final String sql =
            "INSERT INTO users (full_name, username, password_hash, email, phone, " +
            "                   role, department, is_active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole());
            ps.setString(7, user.getDepartment());
            ps.setBoolean(8, user.isActive());

            int affected = ps.executeUpdate();
            if (affected == 0) throw new DatabaseException("Insert user failed, no rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new DatabaseException("Insert user failed, no key returned.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert user: " + e.getMessage(), e, e.getErrorCode());
        }
    }

    // ------------------------------------------------------------------
    // SELECT — single
    // ------------------------------------------------------------------

    @Override
    public User findById(int userId) throws DatabaseException {
        final String sql = buildSelectBase() + " WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public User findByUsername(String username) throws DatabaseException {
        final String sql = buildSelectBase() + " WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by username: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // SELECT — collections
    // ------------------------------------------------------------------

    @Override
    public List<User> findAll() throws DatabaseException {
        final String sql = buildSelectBase() + " ORDER BY full_name ASC";
        return executeListQuery(sql);
    }

    @Override
    public List<User> findAllEmployees() throws DatabaseException {
        final String sql = buildSelectBase() + " WHERE role='EMPLOYEE' AND is_active=1 ORDER BY full_name ASC";
        return executeListQuery(sql);
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    @Override
    public boolean updateUser(User user) throws DatabaseException {
        final String sql =
            "UPDATE users SET full_name=?, email=?, phone=?, department=? WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getDepartment());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user: " + e.getMessage(), e, e.getErrorCode());
        }
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordHash) throws DatabaseException {
        final String sql = "UPDATE users SET password_hash=? WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update password: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean setUserActive(int userId, boolean isActive) throws DatabaseException {
        final String sql = "UPDATE users SET is_active=? WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to set user active status: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Existence checks
    // ------------------------------------------------------------------

    @Override
    public boolean usernameExists(String username, int excludeId) throws DatabaseException {
        final String sql = excludeId > 0
            ? "SELECT 1 FROM users WHERE username=? AND user_id<>? LIMIT 1"
            : "SELECT 1 FROM users WHERE username=? LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            if (excludeId > 0) ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Username existence check failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean emailExists(String email, int excludeId) throws DatabaseException {
        final String sql = excludeId > 0
            ? "SELECT 1 FROM users WHERE email=? AND user_id<>? LIMIT 1"
            : "SELECT 1 FROM users WHERE email=? LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            if (excludeId > 0) ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Email existence check failed: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Activity Log
    // ------------------------------------------------------------------

    @Override
    public void insertActivityLog(ActivityLog log) throws DatabaseException {
        final String sql =
            "INSERT INTO activity_log (user_id, action, description, entity_type, entity_id) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getDescription());
            ps.setString(4, log.getEntityType());
            if (log.getEntityId() > 0) {
                ps.setInt(5, log.getEntityId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert activity log: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ActivityLog> findRecentActivity(int limit) throws DatabaseException {
        final String sql =
            "SELECT l.log_id, l.user_id, COALESCE(u.full_name,'System') AS user_name, " +
            "       l.action, l.description, l.entity_type, l.entity_id, l.log_timestamp " +
            "FROM activity_log l " +
            "LEFT JOIN users u ON l.user_id = u.user_id " +
            "ORDER BY l.log_timestamp DESC LIMIT ?";

        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ActivityLog(
                        rs.getInt("log_id"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("action"),
                        rs.getString("description"),
                        rs.getString("entity_type"),
                        rs.getInt("entity_id"),
                        DateUtil.toLocalDateTime(rs.getTimestamp("log_timestamp"))
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch recent activity: " + e.getMessage(), e);
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private String buildSelectBase() {
        return "SELECT user_id, full_name, username, password_hash, email, phone, " +
               "       role, department, is_active, created_at, last_login FROM users";
    }

    private List<User> executeListQuery(String sql) throws DatabaseException {
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("User list query failed: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Maps the current row of a ResultSet to an {@link Admin} or
     * {@link Employee} instance based on the {@code role} column.
     *
     * <p>This demonstrates <strong>Polymorphism</strong>: the caller receives
     * a {@link User} reference, but the object is actually an Admin or Employee
     * at runtime, with the correct method behaviour for that subtype.</p>
     */
    private User mapRow(ResultSet rs) throws SQLException {
        int    id           = rs.getInt("user_id");
        String fullName     = rs.getString("full_name");
        String username     = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String email        = rs.getString("email");
        String phone        = rs.getString("phone");
        String role         = rs.getString("role");
        String department   = rs.getString("department");
        boolean isActive    = rs.getBoolean("is_active");
        var createdAt       = DateUtil.toLocalDateTime(rs.getTimestamp("created_at"));
        var lastLogin       = DateUtil.toLocalDateTime(rs.getTimestamp("last_login"));

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, fullName, username, passwordHash, email, phone,
                             department, isActive, createdAt, lastLogin);
        } else if ("CITIZEN".equalsIgnoreCase(role)) {
            return new Citizen(id, fullName, username, passwordHash, email, phone,
                               isActive, createdAt, lastLogin);
        } else {
            return new Employee(id, fullName, username, passwordHash, email, phone,
                                department, isActive, createdAt, lastLogin);
        }
    }
}
