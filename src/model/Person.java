package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class for all persons (users and staff) in the
 * Complaint Management System.
 *
 * <p><strong>OOP Role — Abstraction &amp; Inheritance:</strong>
 * This class defines the common identity attributes shared by every person
 * in the system (name, email, phone) and declares the abstract methods that
 * each concrete subclass <em>must</em> implement to express their specific
 * role-based behaviour. It cannot be instantiated directly.</p>
 *
 * <p>Hierarchy:
 * <pre>
 *   Person  (abstract)
 *     └── User
 *           ├── Admin
 *           └── Employee
 * </pre>
 * </p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public abstract class Person {

    // ------------------------------------------------------------------
    // Private data members (Encapsulation — OOP requirement)
    // ------------------------------------------------------------------

    /** Unique database primary key; 0 when not yet persisted. */
    private int id;

    /** Full legal name of this person. */
    private String fullName;

    /** Email address — must be unique in the system. */
    private String email;

    /** Contact phone number; may be {@code null}. */
    private String phone;

    /** Timestamp when this record was created in the database. */
    private LocalDateTime createdAt;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default no-argument constructor.
     * Required for frameworks and DAO mapping.
     */
    public Person() {
        this.id        = 0;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a Person with core identity fields.
     * Used when creating a new person record before database persistence.
     *
     * @param fullName the person's full name
     * @param email    the person's email address
     * @param phone    the person's phone number (may be null)
     */
    public Person(String fullName, String email, String phone) {
        this();
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
    }

    /**
     * Constructs a Person with all fields including the database ID.
     * Used when mapping a row retrieved from the database.
     *
     * @param id        the database primary key
     * @param fullName  the person's full name
     * @param email     the person's email address
     * @param phone     the person's phone number (may be null)
     * @param createdAt the creation timestamp
     */
    public Person(int id, String fullName, String email, String phone,
                  LocalDateTime createdAt) {
        this.id        = id;
        this.fullName  = fullName;
        this.email     = email;
        this.phone     = phone;
        this.createdAt = createdAt;
    }

    // ------------------------------------------------------------------
    // Abstract methods (Abstraction — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Returns the role identifier for this person (e.g., "ADMIN", "EMPLOYEE").
     * Subclasses must override this to return their specific role string.
     *
     * @return non-null role string matching the database ENUM value
     */
    public abstract String getRole();

    /**
     * Returns a descriptive title for this person's dashboard view.
     * Used by {@code MainFrame} to personalise the header greeting.
     *
     * @return dashboard title (e.g., "Administrator Dashboard")
     */
    public abstract String getDashboardTitle();

    /**
     * Returns {@code true} if this person has administrative privileges
     * (e.g., can assign complaints, delete records, manage users).
     *
     * @return {@code true} if the person is an Admin
     */
    public abstract boolean isAdmin();

    // ------------------------------------------------------------------
    // Getters and Setters (Getter/Setter — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Returns the database primary key of this person.
     *
     * @return the ID (0 if not yet persisted)
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the database primary key of this person.
     * Called by the DAO after a successful INSERT.
     *
     * @param id the generated primary key
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the full name of this person.
     *
     * @return the full name; never {@code null} after proper initialisation
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of this person.
     *
     * @param fullName the full name to set; must not be null or blank
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the email address of this person.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of this person.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the phone number of this person.
     *
     * @return the phone number, or {@code null} if not set
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of this person.
     *
     * @param phone the phone number to set; may be null
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the timestamp when this person's record was created.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp (used by DAO during mapping).
     *
     * @param createdAt the creation datetime
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ------------------------------------------------------------------
    // Object overrides
    // ------------------------------------------------------------------

    /**
     * Two Person objects are considered equal if they share the same
     * non-zero database ID.
     *
     * @param obj the object to compare
     * @return {@code true} if both have the same ID and ID is non-zero
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Person)) return false;
        Person other = (Person) obj;
        return this.id != 0 && this.id == other.id;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a concise string representation for debugging.
     *
     * @return formatted string with role, id, and name
     */
    @Override
    public String toString() {
        return String.format("%s{id=%d, name='%s', email='%s'}",
                getRole(), id, fullName, email);
    }
}
