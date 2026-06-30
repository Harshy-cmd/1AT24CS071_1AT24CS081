package util;

import model.User;

/**
 * Manages the current user session for the Complaint Management System.
 *
 * <p>This non-instantiable utility class acts as a lightweight in-memory
 * session store. The currently logged-in {@link User} is held in a static
 * reference that is set on successful login and cleared on logout.</p>
 *
 * <p>Because this is a single-user desktop application, a single static
 * reference is both correct and sufficient. No synchronisation is needed.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public final class SessionManager {

    /** The currently logged-in user. {@code null} if no user is logged in. */
    private static User currentUser;

    /** Private constructor — utility class; not instantiable. */
    private SessionManager() {
        throw new UnsupportedOperationException("SessionManager is a utility class.");
    }

    // ------------------------------------------------------------------
    // Session management
    // ------------------------------------------------------------------

    /**
     * Stores the authenticated user in the session.
     * Called by {@link service.UserService} after successful login.
     *
     * @param user the authenticated user; must not be null
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Returns the currently logged-in user.
     *
     * @return the current {@link User}, or {@code null} if not logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the session, effectively logging out the current user.
     * Called by {@link service.UserService#logout()}.
     */
    public static void clearSession() {
        currentUser = null;
    }

    // ------------------------------------------------------------------
    // Convenience query methods (Method Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Returns {@code true} if a user is currently logged in.
     *
     * @return {@code true} if session is active
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Returns {@code true} if the current user is an Admin.
     * Returns {@code false} if no user is logged in.
     *
     * @return {@code true} for admin sessions
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Returns the current user's ID, or {@code 0} if not logged in.
     *
     * @return current user ID or 0
     */
    public static int getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : 0;
    }

    /**
     * Returns the current user's display name, or {@code "Guest"} if not
     * logged in.
     *
     * @return display name or "Guest"
     */
    public static String getCurrentUserName() {
        return (currentUser != null) ? currentUser.getFullName() : "Guest";
    }

    /**
     * Returns the current user's role string, or {@code "NONE"} if not
     * logged in.
     *
     * @return role string or "NONE"
     */
    public static String getCurrentUserRole() {
        return (currentUser != null) ? currentUser.getRole() : "NONE";
    }

    /**
     * Returns {@code true} if the current user is a Citizen.
     *
     * @return {@code true} if citizen
     */
    public static boolean isCitizen() {
        return currentUser != null && "CITIZEN".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Returns {@code true} if the current user is an Employee.
     *
     * @return {@code true} if employee
     */
    public static boolean isEmployee() {
        return currentUser != null && "EMPLOYEE".equalsIgnoreCase(currentUser.getRole());
    }
}
