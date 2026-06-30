package service;

import dao.IUserDAO;
import dao.implementation.UserDAOImpl;
import exceptions.AuthenticationException;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.*;
import util.SessionManager;
import util.Validator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Business logic service for user authentication and profile management.
 *
 * <p>Handles password hashing (SHA-256), login validation, profile updates,
 * and activity logging, ensuring all user-facing operations are validated
 * before reaching the DAO layer.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class UserService {

    // ------------------------------------------------------------------
    // Dependencies
    // ------------------------------------------------------------------

    private final IUserDAO userDAO;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default constructor — uses the production DAO implementation.
     */
    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Injection constructor — accepts a custom DAO for testing.
     *
     * @param userDAO custom implementation of {@link IUserDAO}
     */
    public UserService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------

    /**
     * Authenticates a user with the given username and plain-text password.
     *
     * <p>The plain-text password is SHA-256-hashed before being compared
     * against the stored hash. The plain-text password is never sent to
     * or stored in the database.</p>
     *
     * @param username         the login username
     * @param plainTextPassword the password as typed by the user
     * @return the authenticated {@link User} (Admin or Employee subclass)
     * @throws ValidationException     if username or password is blank
     * @throws AuthenticationException if credentials are invalid or account disabled
     * @throws DatabaseException       if the database query fails
     */
    public User login(String username, String plainTextPassword)
            throws ValidationException, AuthenticationException, DatabaseException {

        // Basic presence validation
        if (username == null || username.isBlank()) {
            throw new ValidationException("username", "Username cannot be empty.");
        }
        if (plainTextPassword == null || plainTextPassword.isBlank()) {
            throw new ValidationException("password", "Password cannot be empty.");
        }

        // Hash the password for comparison
        String hash = hashPassword(plainTextPassword);

        // Look up in the database
        User user = userDAO.authenticate(username.trim(), hash);

        if (user == null) {
            // Check if the username exists (to differentiate error messages)
            User existing = userDAO.findByUsername(username.trim());
            if (existing == null) {
                throw new AuthenticationException(
                    "No account found with username '" + username + "'.",
                    AuthenticationException.Reason.USER_NOT_FOUND);
            } else {
                throw new AuthenticationException(
                    "Incorrect password. Please try again.",
                    AuthenticationException.Reason.INVALID_PASSWORD);
            }
        }

        if (!user.isActive()) {
            throw new AuthenticationException(
                "Your account has been disabled. Contact an administrator.",
                AuthenticationException.Reason.ACCOUNT_DISABLED);
        }

        // Stamp last_login
        userDAO.updateLastLogin(user.getId());

        // Write audit log
        userDAO.insertActivityLog(new ActivityLog(
            user.getId(), "LOGIN",
            user.getFullName() + " logged into the system.",
            "USER", user.getId()));

        // Store in session
        SessionManager.setCurrentUser(user);

        return user;
    }

    /**
     * Logs out the current user, clearing the session and writing an
     * activity log entry.
     */
    public void logout() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            try {
                userDAO.insertActivityLog(new ActivityLog(
                    user.getId(), "LOGOUT",
                    user.getFullName() + " logged out.",
                    "USER", user.getId()));
            } catch (DatabaseException e) {
                // Non-critical — do not prevent logout
                System.err.println("[UserService] Logout log failed: " + e.getMessage());
            }
        }
        SessionManager.clearSession();
    }

    // ------------------------------------------------------------------
    // Profile Management
    // ------------------------------------------------------------------

    /**
     * Updates the profile of the currently logged-in user after validation.
     *
     * @param user the user object with updated profile fields
     * @throws ValidationException if any field fails validation
     * @throws DatabaseException   on database failure
     */
    public void updateProfile(User user)
            throws ValidationException, DatabaseException {

        Validator.validateUserProfile(user);

        // Check email uniqueness (exclude current user)
        if (userDAO.emailExists(user.getEmail(), user.getId())) {
            throw new ValidationException("email", "This email address is already in use.");
        }

        userDAO.updateUser(user);

        // Refresh the session with updated data
        SessionManager.setCurrentUser(user);

        userDAO.insertActivityLog(new ActivityLog(
            user.getId(), "UPDATE_PROFILE",
            "User " + user.getUsername() + " updated their profile.",
            "USER", user.getId()));
    }

    /**
     * Changes the password for the current user after verifying the
     * old password.
     *
     * @param userId          the user's primary key
     * @param oldPlain        the current password in plain text
     * @param newPlain        the new password in plain text
     * @param confirmPlain    the confirmation of the new password
     * @throws ValidationException     if new passwords don't match or are invalid
     * @throws AuthenticationException if the old password is wrong
     * @throws DatabaseException       on database failure
     */
    public void changePassword(int userId, String oldPlain,
                               String newPlain, String confirmPlain)
            throws ValidationException, AuthenticationException, DatabaseException {

        if (!newPlain.equals(confirmPlain)) {
            throw new ValidationException("confirmPassword",
                "New password and confirmation do not match.");
        }
        Validator.validatePassword(newPlain);

        User user = userDAO.findById(userId);
        if (user == null) throw new DatabaseException("User not found.");

        // Verify old password
        String oldHash = hashPassword(oldPlain);
        if (!oldHash.equals(user.getPasswordHash())) {
            throw new AuthenticationException(
                "Current password is incorrect.",
                AuthenticationException.Reason.INVALID_PASSWORD);
        }

        String newHash = hashPassword(newPlain);
        userDAO.updatePassword(userId, newHash);

        userDAO.insertActivityLog(new ActivityLog(
            userId, "CHANGE_PASSWORD",
            "User changed their password.",
            "USER", userId));
    }

    // ------------------------------------------------------------------
    // User retrieval
    // ------------------------------------------------------------------

    /**
     * Returns all employees in the system (for assignment combo boxes).
     *
     * @return list of employee users
     * @throws DatabaseException on failure
     */
    public List<User> getAllEmployees() throws DatabaseException {
        return userDAO.findAllEmployees();
    }

    /**
     * Returns all users in the system.
     *
     * @return list of all users
     * @throws DatabaseException on failure
     */
    public List<User> getAllUsers() throws DatabaseException {
        return userDAO.findAll();
    }

    /**
     * Returns a user by primary key.
     *
     * @param userId the primary key
     * @return the user or null
     * @throws DatabaseException on failure
     */
    public User getUserById(int userId) throws DatabaseException {
        return userDAO.findById(userId);
    }

    /**
     * Returns recent activity log entries for the activity feed.
     *
     * @param limit max number of entries to return
     * @return list of recent activity logs
     * @throws DatabaseException on failure
     */
    public List<ActivityLog> getRecentActivity(int limit) throws DatabaseException {
        List<ActivityLog> allLogs = userDAO.findRecentActivity(limit * 5);
        User user = SessionManager.getCurrentUser();
        if (user == null || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return allLogs.subList(0, Math.min(limit, allLogs.size()));
        }
        List<ActivityLog> filtered = new java.util.ArrayList<>();
        for (ActivityLog log : allLogs) {
            if (log.getUserId() == user.getId()) {
                filtered.add(log);
            }
        }
        return filtered.subList(0, Math.min(limit, filtered.size()));
    }

    // ------------------------------------------------------------------
    // Password hashing
    // ------------------------------------------------------------------

    /**
     * Computes the SHA-256 hex digest of a plain-text password.
     *
     * <p>The same algorithm is used by the MySQL {@code SHA2(password, 256)}
     * function used during database seeding, ensuring compatibility between
     * sample data and the Java authentication flow.</p>
     *
     * @param plainText the password in plain text; must not be null
     * @return a 64-character lowercase hex string (SHA-256 digest)
     * @throws RuntimeException if the SHA-256 algorithm is unavailable
     *                          (should never happen on any standard JVM)
     */
    public static String hashPassword(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                plainText.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to lowercase hex string
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the Java SE specification — cannot happen
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }
    }
}
