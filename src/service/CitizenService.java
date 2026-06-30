package service;

import dao.CitizenDAO;
import dao.implementation.CitizenDAOImpl;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.Citizen;
import util.Validator;

/**
 * Business logic service for Citizen registration and validation.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class CitizenService {

    private final CitizenDAO citizenDAO;

    /**
     * Default constructor — uses production DAO implementation.
     */
    public CitizenService() {
        this.citizenDAO = new CitizenDAOImpl();
    }

    /**
     * Injection constructor for unit testing.
     *
     * @param citizenDAO custom implementation of CitizenDAO
     */
    public CitizenService(CitizenDAO citizenDAO) {
        this.citizenDAO = citizenDAO;
    }

    /**
     * Validates and registers a new citizen in the system.
     *
     * @param citizen         the citizen model containing profile details
     * @param password        the plain-text password
     * @param confirmPassword the plain-text confirmation password
     * @return the generated citizen user_id
     * @throws ValidationException if validation rules fail
     * @throws DatabaseException   if a database error occurs
     */
    public int registerCitizen(Citizen citizen, String password, String confirmPassword)
            throws ValidationException, DatabaseException {

        // Validate matching passwords
        if (password == null || password.isEmpty()) {
            throw new ValidationException("password", "Password cannot be empty.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("confirmPassword", "Passwords do not match.");
        }

        // Validate Citizen Profile (Name, Email, Phone)
        Validator.validateUserProfile(citizen);

        // Validate Username format
        Validator.validateUsername("username", citizen.getUsername());

        // Validate Password strength
        Validator.validatePassword(password);

        // Validate optional Address
        if (citizen.getAddress() != null && !citizen.getAddress().isBlank()) {
            String addr = citizen.getAddress();
            // Check for leading/trailing spaces
            if (addr.startsWith(" ") || addr.endsWith(" ")) {
                throw new ValidationException("address", "Address cannot have leading or trailing spaces.");
            }
            // Check for SQL injection
            Validator.checkSqlInjection("address", addr);
            // Check length bounds
            if (addr.trim().length() > 255) {
                throw new ValidationException("address", "Address cannot exceed 255 characters.");
            }
            citizen.setAddress(addr.trim());
        }

        // Check for Username uniqueness in DB
        if (citizenDAO.usernameExists(citizen.getUsername().trim())) {
            throw new ValidationException("username", "Username '" + citizen.getUsername() + "' is already taken.");
        }

        // Check for Email uniqueness in DB
        if (citizenDAO.emailExists(citizen.getEmail().trim())) {
            throw new ValidationException("email", "Email '" + citizen.getEmail() + "' is already registered.");
        }

        // Hash password before persisting
        String hashed = UserService.hashPassword(password);
        citizen.setPasswordHash(hashed);
        citizen.setUsername(citizen.getUsername().trim());
        citizen.setEmail(citizen.getEmail().trim());
        if (citizen.getPhone() != null) {
            citizen.setPhone(citizen.getPhone().trim());
        }

        // Persist citizen
        return citizenDAO.registerCitizen(citizen);
    }
}
