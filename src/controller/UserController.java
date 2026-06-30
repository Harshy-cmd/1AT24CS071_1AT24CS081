package controller;

import exceptions.AuthenticationException;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.ActivityLog;
import model.User;
import service.UserService;

import java.util.List;

/**
 * Thin controller layer for user authentication and profile operations.
 *
 * <p>Delegates all business logic to {@link UserService} without adding
 * any processing of its own — following the single responsibility of
 * this layer: bridging UI input to the service boundary.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class UserController {

    private final UserService userService;

    /** Creates a controller using the default service implementation. */
    public UserController() {
        this.userService = new UserService();
    }

    /** Creates a controller with an injected service (for testing). */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ------------------------------------------------------------------
    // Authentication delegates
    // ------------------------------------------------------------------

    public User login(String username, String password)
            throws ValidationException, AuthenticationException, DatabaseException {
        return userService.login(username, password);
    }

    public void logout() {
        userService.logout();
    }

    // ------------------------------------------------------------------
    // Profile delegates
    // ------------------------------------------------------------------

    public void updateProfile(User user)
            throws ValidationException, DatabaseException {
        userService.updateProfile(user);
    }

    public void changePassword(int userId, String oldPlain,
                               String newPlain, String confirmPlain)
            throws ValidationException, AuthenticationException, DatabaseException {
        userService.changePassword(userId, oldPlain, newPlain, confirmPlain);
    }

    // ------------------------------------------------------------------
    // User retrieval delegates
    // ------------------------------------------------------------------

    public List<User> getAllEmployees() throws DatabaseException {
        return userService.getAllEmployees();
    }

    public List<User> getAllUsers() throws DatabaseException {
        return userService.getAllUsers();
    }

    public User getUserById(int userId) throws DatabaseException {
        return userService.getUserById(userId);
    }

    public List<ActivityLog> getRecentActivity(int limit) throws DatabaseException {
        return userService.getRecentActivity(limit);
    }
}
