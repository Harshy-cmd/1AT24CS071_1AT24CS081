package model;

import java.time.LocalDateTime;

/**
 * Represents an Employee user in the Complaint Management System.
 *
 * <p><strong>OOP Role — Inheritance &amp; Method Overriding:</strong>
 * {@code Employee} extends {@link User} and specialises it for staff who
 * handle complaints. An employee can update status, add remarks, and view
 * complaints assigned to them but cannot assign complaints to others,
 * delete records, or manage user accounts.</p>
 *
 * <p>Employees also carry a {@link #designation} and {@link #employeeCode}
 * that are additional domain-specific fields not present in the base User.</p>
 *
 * <p>Hierarchy: {@code Person → User → Employee}</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class Employee extends User {

    // ------------------------------------------------------------------
    // Private data members (Encapsulation — OOP requirement)
    // ------------------------------------------------------------------

    /** The employee's job title / designation (e.g., "Senior Technician"). */
    private String designation;

    /** Optional employee badge or payroll code. */
    private String employeeCode;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default no-argument constructor. Delegates to {@link User#User()}.
     */
    public Employee() {
        super();
    }

    /**
     * Constructs a new Employee for registration (without designation).
     *
     * @param fullName     the employee's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hash of the password
     * @param email        the email address
     * @param phone        the phone number
     * @param department   the employee's department
     */
    public Employee(String fullName, String username, String passwordHash,
                    String email, String phone, String department) {
        super(fullName, username, passwordHash, email, phone, department);
    }

    /**
     * Constructs a new Employee with all fields including designation.
     *
     * @param fullName     the employee's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hash of the password
     * @param email        the email address
     * @param phone        the phone number
     * @param department   the employee's department
     * @param designation  the job title / designation
     */
    public Employee(String fullName, String username, String passwordHash,
                    String email, String phone, String department,
                    String designation) {
        super(fullName, username, passwordHash, email, phone, department);
        this.designation = designation;
    }

    /**
     * Full constructor used by the DAO when mapping an EMPLOYEE row from DB.
     *
     * @param id           the database primary key
     * @param fullName     the employee's full name
     * @param username     the login username
     * @param passwordHash the SHA-256 hash
     * @param email        the email address
     * @param phone        the phone number
     * @param department   the employee's department
     * @param isActive     whether the account is active
     * @param createdAt    the record creation timestamp
     * @param lastLogin    the last login timestamp
     */
    public Employee(int id, String fullName, String username, String passwordHash,
                    String email, String phone, String department,
                    boolean isActive, LocalDateTime createdAt, LocalDateTime lastLogin) {
        super(id, fullName, username, passwordHash, email, phone,
              department, isActive, createdAt, lastLogin);
    }

    // ------------------------------------------------------------------
    // Method Overrides (Method Overriding — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Returns "EMPLOYEE" as the role identifier for this person.
     * Overrides {@link User#getRole()}.
     *
     * @return "EMPLOYEE"
     */
    @Override
    public String getRole() {
        return "EMPLOYEE";
    }

    /**
     * Returns the personalised dashboard title for employees.
     * Overrides {@link User#getDashboardTitle()}.
     *
     * @return "Employee Dashboard"
     */
    @Override
    public String getDashboardTitle() {
        return "Employee Dashboard";
    }

    /**
     * Returns {@code false}, confirming employees do not have admin rights.
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
            "Employee{id=%d, username='%s', name='%s', dept='%s', designation='%s', active=%b}",
            getId(), getUsername(), getFullName(),
            getDepartment(), designation, isActive());
    }

    // ------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------

    /**
     * Returns the employee's job designation.
     *
     * @return the designation, or {@code null} if not set
     */
    public String getDesignation() {
        return designation;
    }

    /**
     * Sets the employee's job designation.
     *
     * @param designation the designation to set
     */
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    /**
     * Returns the employee badge or payroll code.
     *
     * @return the employee code, or {@code null}
     */
    public String getEmployeeCode() {
        return employeeCode;
    }

    /**
     * Sets the employee badge or payroll code.
     *
     * @param employeeCode the code to set
     */
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }
}
