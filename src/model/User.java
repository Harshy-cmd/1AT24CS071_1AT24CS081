package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a system user (Admin or Employee) in the Complaint Management System.
 *
 * <p><strong>OOP Role — Inheritance &amp; Encapsulation:</strong>
 * {@code User} extends the abstract {@link Person} class and adds
 * authentication credentials (username, password hash), department, and
 * account-state fields. It provides a concrete (but generalisable) user
 * that {@link Admin} and {@link Employee} specialise further.</p>
 *
 * <p>Hierarchy: {@code Person → User → Admin / Employee}</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class User extends Person {

    // ------------------------------------------------------------------
    // Private data members (Encapsulation — OOP requirement)
    // ------------------------------------------------------------------

    /** Login username — must be unique across the system. */
    private String username;

    /**
     * SHA-256 hex digest of the user's password.
     * Never stored as plain text.
     */
    private String passwordHash;

    /** The organisational department this user belongs to. */
    private String department;

    /**
     * Whether this account is active. Inactive accounts cannot log in.
     * Default: {@code true}.
     */
    private boolean isActive;

    /** Timestamp of the user's most recent successful login; may be null. */
    private LocalDateTime lastLogin;

    /** Physical address — optional field for citizens. */
    private String address;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default no-argument constructor.
     * Sets active to {@code true} and delegates to {@link Person#Person()}.
     */
    public User() {
        super();
        this.isActive = true;
    }

    /**
     * Constructs a new User for registration (before database persistence).
     * ID will be assigned by the database.
     *
     * @param fullName     the user's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hex hash of the password
     * @param email        the email address
     * @param phone        the phone number (may be null)
     * @param department   the organisational department
     */
    public User(String fullName, String username, String passwordHash,
                String email, String phone, String department) {
        super(fullName, email, phone);
        this.username     = username;
        this.passwordHash = passwordHash;
        this.department   = department;
        this.isActive     = true;
    }

    /**
     * Full constructor used by the DAO when mapping a database row to a
     * User object (all fields including primary key).
     *
     * @param id           the database primary key
     * @param fullName     the user's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hex hash
     * @param email        the email address
     * @param phone        the phone number (may be null)
     * @param department   the organisational department
     * @param isActive     whether this account is active
     * @param createdAt    the record creation timestamp
     * @param lastLogin    the last login timestamp (may be null)
     */
    public User(int id, String fullName, String username, String passwordHash,
                String email, String phone, String department,
                boolean isActive, LocalDateTime createdAt, LocalDateTime lastLogin) {
        super(id, fullName, email, phone, createdAt);
        this.username     = username;
        this.passwordHash = passwordHash;
        this.department   = department;
        this.isActive     = isActive;
        this.lastLogin    = lastLogin;
    }

    // ------------------------------------------------------------------
    // Abstract method implementations (Method Overriding — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Returns "USER" as the role for this base class.
     * Overridden by {@link Admin} and {@link Employee} to return their
     * specific roles.
     *
     * @return the role string "USER"
     */
    @Override
    public String getRole() {
        return "USER";
    }

    /**
     * Returns the default dashboard title for a generic user.
     * Overridden by subclasses.
     *
     * @return "User Dashboard"
     */
    @Override
    public String getDashboardTitle() {
        return "User Dashboard";
    }

    /**
     * Returns {@code false} for a base User.
     * Overridden by {@link Admin} to return {@code true}.
     *
     * @return {@code false}
     */
    @Override
    public boolean isAdmin() {
        return false;
    }

    // ------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------

    /**
     * Returns the login username.
     *
     * @return the username; never {@code null} after proper initialisation
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the login username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the SHA-256 password hash.
     *
     * @return the password hash hex string
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the SHA-256 password hash.
     *
     * @param passwordHash the hash to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the department this user belongs to.
     *
     * @return the department name, or {@code null}
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Sets the department this user belongs to.
     *
     * @param department the department name
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * Returns whether this user account is currently active.
     *
     * @return {@code true} if active; {@code false} if disabled
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Enables or disables this user account.
     *
     * @param active {@code true} to activate, {@code false} to deactivate
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    /**
     * Returns the timestamp of the user's last successful login.
     *
     * @return last login timestamp, or {@code null} if user has never logged in
     */
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    /**
     * Sets the last login timestamp. Called by {@code UserDAOImpl} after a
     * successful authentication.
     *
     * @param lastLogin the login timestamp
     */
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    // ------------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------------

    /**
     * Returns the display name formatted as "Full Name (username)".
     * Used in combo boxes when assigning complaints.
     *
     * @return formatted display label
     */
    public String getDisplayLabel() {
        return getFullName() + " (" + username + ")";
    }

    /**
     * Returns the physical address of the user.
     *
     * @return the address string; may be null
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the physical address of the user.
     *
     * @param address the physical address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    // ------------------------------------------------------------------
    // Object overrides (Method Overriding — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Returns a detailed string representation for debugging.
     *
     * @return formatted string with all key fields
     */
    @Override
    public String toString() {
        return String.format(
            "User{id=%d, username='%s', name='%s', role='%s', dept='%s', active=%b}",
            getId(), username, getFullName(), getRole(), department, isActive);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        return super.equals(obj);   // delegate to Person's ID-based equality
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
