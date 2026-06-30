package dao;

import exceptions.DatabaseException;
import model.Citizen;

/**
 * Data Access Object interface for Citizen-specific operations.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public interface CitizenDAO {

    /**
     * Registers/persists a new citizen in the database.
     *
     * @param citizen the Citizen object to save
     * @return the auto-generated user_id
     * @throws DatabaseException if the insert query fails
     */
    int registerCitizen(Citizen citizen) throws DatabaseException;

    /**
     * Checks if a username is already taken.
     *
     * @param username the username to check
     * @return true if exists, false otherwise
     * @throws DatabaseException if the query fails
     */
    boolean usernameExists(String username) throws DatabaseException;

    /**
     * Checks if an email is already taken.
     *
     * @param email the email to check
     * @return true if exists, false otherwise
     * @throws DatabaseException if the query fails
     */
    boolean emailExists(String email) throws DatabaseException;
}
