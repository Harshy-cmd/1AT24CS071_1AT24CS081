package dao.implementation;

import dao.CitizenDAO;
import database.DatabaseConnection;
import exceptions.DatabaseException;
import model.Citizen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC implementation of the CitizenDAO interface.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class CitizenDAOImpl implements CitizenDAO {

    @Override
    public int registerCitizen(Citizen citizen) throws DatabaseException {
        final String sql =
            "INSERT INTO users (full_name, username, password_hash, email, phone, " +
            "                   role, department, is_active, address) " +
            "VALUES (?, ?, ?, ?, ?, 'CITIZEN', NULL, 1, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, citizen.getFullName());
            ps.setString(2, citizen.getUsername());
            ps.setString(3, citizen.getPasswordHash());
            ps.setString(4, citizen.getEmail());
            ps.setString(5, citizen.getPhone());
            ps.setString(6, citizen.getAddress());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Failed to register citizen, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new DatabaseException("Failed to register citizen, no key returned.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert citizen: " + e.getMessage(), e, e.getErrorCode());
        }
    }

    @Override
    public boolean usernameExists(String username) throws DatabaseException {
        final String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check username existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean emailExists(String email) throws DatabaseException {
        final String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check email existence: " + e.getMessage(), e);
        }
    }
}
