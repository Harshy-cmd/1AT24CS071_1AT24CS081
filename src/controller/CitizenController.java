package controller;

import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.Citizen;
import service.CitizenService;

/**
 * Controller class coordinating Citizen operations between the UI and Service layer.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class CitizenController {

    private final CitizenService citizenService;

    /**
     * Default constructor — initializes standard CitizenService.
     */
    public CitizenController() {
        this.citizenService = new CitizenService();
    }

    /**
     * Injection constructor.
     *
     * @param citizenService custom implementation of CitizenService
     */
    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    /**
     * Coordinates the registration of a new citizen.
     *
     * @param citizen         the citizen model
     * @param password        the typed password
     * @param confirmPassword the confirmation password
     * @return the persisted citizen user_id
     * @throws ValidationException on invalid user fields
     * @throws DatabaseException   on database errors
     */
    public int registerCitizen(Citizen citizen, String password, String confirmPassword)
            throws ValidationException, DatabaseException {
        return citizenService.registerCitizen(citizen, password, confirmPassword);
    }
}
