package model;

import java.time.LocalDateTime;

/**
 * Represents an Administrator user in the Complaint Management System.
 *
 * <p><strong>OOP Role — Inheritance &amp; Method Overriding:</strong>
 * {@code Admin} extends {@link User} and overrides {@link #getRole()},
 * {@link #getDashboardTitle()}, and {@link #isAdmin()} to express that
 * an Admin has full system privileges. Admins can assign complaints,
 * manage users, delete records, and access all reports.</p>
 *
 * <p>Hierarchy: {@code Person → User → Admin}</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class Admin extends User {

    // ------------------------------------------------------------------
    // Private data members (Encapsulation — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Optional internal admin code used for administrative identification.
     * Not stored in the database — derived at runtime if needed.
     */
    private String adminCode;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default no-argument constructor. Delegates to {@link User#User()}.
     */
    public Admin() {
        super();
    }

    /**
     * Constructs a new Admin for registration.
     *
     * @param fullName     the admin's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hash of the password
     * @param email        the email address
     * @param phone        the phone number
     * @param department   the admin's department
     */
    public Admin(String fullName, String username, String passwordHash,
                 String email, String phone, String department) {
        super(fullName, username, passwordHash, email, phone, department);
    }

    /**
     * Full constructor used by the DAO when mapping an ADMIN row from the DB.
     *
     * @param id           the database primary key
     * @param fullName     the admin's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hash
     * @param email        the email address
     * @param phone        the phone number
     * @param department   the admin's department
     * @param isActive     whether the account is active
     * @param createdAt    the record creation timestamp
     * @param lastLogin    the last login timestamp
     */
    public Admin(int id, String fullName, String username, String passwordHash,
                 String email, String phone, String department,
                 boolean isActive, LocalDateTime createdAt, LocalDateTime lastLogin) {
        super(id, fullName, username, passwordHash, email, phone,
              department, isActive, createdAt, lastLogin);
    }

    // ------------------------------------------------------------------
    // Method Overrides (Method Overriding — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Returns "ADMIN" as the role identifier for this person.
     * Overrides {@link User#getRole()}.
     *
     * @return "ADMIN"
     */
    @Override
    public String getRole() {
        return "ADMIN";
    }

    /**
     * Returns the personalised dashboard title for admins.
     * Overrides {@link User#getDashboardTitle()}.
     *
     * @return "Administrator Dashboard"
     */
    @Override
    public String getDashboardTitle() {
        return "Administrator Dashboard";
    }

    /**
     * Returns {@code true}, indicating that this person has full
     * administrative privileges.
     * Overrides {@link User#isAdmin()}.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAdmin() {
        return true;
    }

    /**
     * Returns a detailed string representation including the admin marker.
     * Overrides {@link User#toString()}.
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        return String.format(
            "Admin{id=%d, username='%s', name='%s', dept='%s', active=%b}",
            getId(), getUsername(), getFullName(), getDepartment(), isActive());
    }

    // ------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------

    /**
     * Returns the optional admin identification code.
     *
     * @return admin code or {@code null}
     */
    public String getAdminCode() {
        return adminCode;
    }

    /**
     * Sets the optional admin identification code.
     *
     * @param adminCode the admin code to set
     */
    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }
}
