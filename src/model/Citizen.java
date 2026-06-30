package model;

import java.time.LocalDateTime;

/**
 * Represents a Citizen (Citizen/User) in the Complaint Management System.
 *
 * <p><strong>OOP Role — Inheritance &amp; Method Overriding:</strong>
 * {@code Citizen} extends {@link User} and specializes it for citizens who
 * register and track their own complaints. A Citizen can submit complaints and
 * track their history, but has no access to administration or report panels,
 * nor can they be assigned to complaints.</p>
 *
 * <p>Hierarchy: {@code Person → User → Citizen}</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class Citizen extends User {

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default no-argument constructor. Delegates to {@link User#User()}.
     */
    public Citizen() {
        super();
    }

    /**
     * Constructs a new Citizen for registration.
     *
     * @param fullName     the citizen's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hash of the password
     * @param email        the email address
     * @param phone        the phone number
     */
    public Citizen(String fullName, String username, String passwordHash,
                   String email, String phone) {
        super(fullName, username, passwordHash, email, phone, "Citizen");
    }

    /**
     * Full constructor used by the DAO when mapping a CITIZEN row from the DB.
     *
     * @param id           the database primary key
     * @param fullName     the citizen's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hash
     * @param email        the email address
     * @param phone        the phone number
     * @param isActive     whether the account is active
     * @param createdAt    the record creation timestamp
     * @param lastLogin    the last login timestamp
     */
    public Citizen(int id, String fullName, String username, String passwordHash,
                   String email, String phone,
                   boolean isActive, LocalDateTime createdAt, LocalDateTime lastLogin) {
        super(id, fullName, username, passwordHash, email, phone,
              "Citizen", isActive, createdAt, lastLogin);
    }

    // ------------------------------------------------------------------
    // Method Overrides (Method Overriding — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Returns "CITIZEN" as the role identifier for this person.
     * Overrides {@link User#getRole()}.
     *
     * @return "CITIZEN"
     */
    @Override
    public String getRole() {
        return "CITIZEN";
    }

    /**
     * Returns the personalized dashboard title for citizens.
     * Overrides {@link User#getDashboardTitle()}.
     *
     * @return "Citizen Dashboard"
     */
    @Override
    public String getDashboardTitle() {
        return "Citizen Dashboard";
    }

    /**
     * Returns {@code false}, confirming citizens do not have admin rights.
     * Overrides {@link User#isAdmin()}.
     *
     * @return {@code false}
     */
    @Override
    public boolean isAdmin() {
        return false;
    }

    /**
     * Returns a detailed string representation.
     * Overrides {@link User#toString()}.
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        return String.format(
            "Citizen{id=%d, username='%s', name='%s', active=%b}",
            getId(), getUsername(), getFullName(), isActive());
    }
}
