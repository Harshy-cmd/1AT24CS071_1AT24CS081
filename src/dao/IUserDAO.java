package dao;

import exceptions.DatabaseException;
import model.ActivityLog;
import model.User;

import java.util.List;

/**
 * Data Access Object interface defining all database operations for
 * {@link User} and {@link ActivityLog} entities.
 *
 * <p><strong>OOP Role — Interface &amp; Abstraction:</strong>
 * Decouples the service layer from JDBC implementation details for
 * user management and activity logging.</p>
 *
 * <p>All implementations MUST use {@link java.sql.PreparedStatement}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 * @see     dao.implementation.UserDAOImpl
 */
public interface IUserDAO {

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------

    /**
     * Attempts to find a user by username and password hash.
     * Returns the matching User (Admin or Employee subclass) if found,
     * or {@code null} if credentials do not match any active account.
     *
     * @param username     the login username
     * @param passwordHash the SHA-256 hex hash of the entered password
     * @return the authenticated {@link User} (or subclass), or {@code null}
     * @throws DatabaseException if the SELECT fails
     */
    User authenticate(String username, String passwordHash) throws DatabaseException;

    /**
     * Updates the {@code last_login} timestamp for the given user to NOW().
     *
     * @param userId the user's primary key
     * @throws DatabaseException if the UPDATE fails
     */
    void updateLastLogin(int userId) throws DatabaseException;

    // ------------------------------------------------------------------
    // CRUD — User
    // ------------------------------------------------------------------

    /**
     * Inserts a new user into the database.
     *
     * @param user the {@link User} (or subclass) to persist
     * @return the auto-generated {@code user_id}
     * @throws DatabaseException if the INSERT fails (e.g., duplicate username)
     */
    int insertUser(User user) throws DatabaseException;

    /**
     * Returns a user by primary key.
     *
     * @param userId the primary key
     * @return the {@link User}, or {@code null} if not found
     * @throws DatabaseException if the SELECT fails
     */
    User findById(int userId) throws DatabaseException;

    /**
     * Returns a user by username (case-sensitive).
     *
     * @param username the username to search for
     * @return the {@link User}, or {@code null} if not found
     * @throws DatabaseException if the SELECT fails
     */
    User findByUsername(String username) throws DatabaseException;

    /**
     * Returns all users in the system.
     *
     * @return non-null list of all users
     * @throws DatabaseException if the SELECT fails
     */
    List<User> findAll() throws DatabaseException;

    /**
     * Returns all users whose role is EMPLOYEE.
     * Used when populating the "Assign to" combo box.
     *
     * @return non-null list of employee users
     * @throws DatabaseException if the SELECT fails
     */
    List<User> findAllEmployees() throws DatabaseException;

    /**
     * Updates the mutable profile fields of an existing user.
     *
     * @param user the user with updated fields; ID must be set
     * @return {@code true} if one row was updated
     * @throws DatabaseException if the UPDATE fails
     */
    boolean updateUser(User user) throws DatabaseException;

    /**
     * Updates the password hash for a user.
     *
     * @param userId          the user's primary key
     * @param newPasswordHash the new SHA-256 hex hash
     * @return {@code true} if one row was updated
     * @throws DatabaseException if the UPDATE fails
     */
    boolean updatePassword(int userId, String newPasswordHash) throws DatabaseException;

    /**
     * Enables or disables a user account.
     *
     * @param userId   the user's primary key
     * @param isActive {@code true} to activate; {@code false} to deactivate
     * @return {@code true} if one row was updated
     * @throws DatabaseException if the UPDATE fails
     */
    boolean setUserActive(int userId, boolean isActive) throws DatabaseException;

    // ------------------------------------------------------------------
    // Existence checks (for validation)
    // ------------------------------------------------------------------

    /**
     * Returns {@code true} if a user with the given username already exists.
     *
     * @param username  the username to check
     * @param excludeId user ID to exclude from the check (0 = exclude none);
     *                  pass the current user's ID when checking on update
     * @return {@code true} if the username is taken
     * @throws DatabaseException if the query fails
     */
    boolean usernameExists(String username, int excludeId) throws DatabaseException;

    /**
     * Returns {@code true} if a user with the given email already exists.
     *
     * @param email     the email to check
     * @param excludeId user ID to exclude (0 = exclude none)
     * @return {@code true} if the email is in use
     * @throws DatabaseException if the query fails
     */
    boolean emailExists(String email, int excludeId) throws DatabaseException;

    // ------------------------------------------------------------------
    // Activity Log
    // ------------------------------------------------------------------

    /**
     * Inserts an activity log entry into the {@code activity_log} table.
     *
     * @param log the {@link ActivityLog} to persist
     * @throws DatabaseException if the INSERT fails
     */
    void insertActivityLog(ActivityLog log) throws DatabaseException;

    /**
     * Returns recent activity log entries, most recent first.
     *
     * @param limit the maximum number of entries to return
     * @return non-null list of log entries
     * @throws DatabaseException if the SELECT fails
     */
    List<ActivityLog> findRecentActivity(int limit) throws DatabaseException;
}
