package database;

import exceptions.DatabaseException;
import util.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager for the Complaint Management System.
 *
 * <p>
 * <strong>Design Pattern — Singleton:</strong>
 * This class ensures that only one database connection object exists at a
 * time throughout the application's lifecycle. All DAO implementations
 * retrieve their connection via {@link #getConnection()}, guaranteeing a
 * single, shared, consistent connection to the MySQL database.
 * </p>
 *
 * <p>
 * <strong>Security:</strong>
 * Connection parameters (URL, username, password) are read from
 * {@link Constants.DB}, keeping credentials in one place and out of DAO
 * source files.
 * </p>
 *
 * <p>
 * <strong>Usage:</strong>
 * 
 * <pre>
 * Connection conn = DatabaseConnection.getInstance().getConnection();
 * </pre>
 * </p>
 *
 * @author CMS Development Team
 * @version 1.0.0
 * @since 2024
 */
public class DatabaseConnection {

    // ------------------------------------------------------------------
    // Singleton instance
    // ------------------------------------------------------------------

    /** The sole instance of this class (lazy-initialised). */
    private static DatabaseConnection instance;

    /** The active JDBC connection; may be null if not yet opened or closed. */
    private Connection connection;

    // ------------------------------------------------------------------
    // Constructor (private — Singleton pattern)
    // ------------------------------------------------------------------

    /**
     * Private constructor loads the JDBC driver class.
     * Any {@link ClassNotFoundException} is wrapped and re-thrown as a
     * {@link RuntimeException} because the application cannot function
     * without a valid JDBC driver.
     */
    private DatabaseConnection() {
        try {
            // Load the MySQL Connector/J driver class into the JVM
            Class.forName(Constants.DB.DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "MySQL JDBC driver not found. "
                            + "Ensure 'mysql-connector-j-*.jar' is on the classpath.",
                    e);
        }
    }

    // ------------------------------------------------------------------
    // Singleton accessor
    // ------------------------------------------------------------------

    /**
     * Returns the sole instance of {@code DatabaseConnection}.
     * Thread-safe via {@code synchronized} block with double-checked locking.
     *
     * @return the singleton {@code DatabaseConnection} instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ------------------------------------------------------------------
    // Connection management
    // ------------------------------------------------------------------

    /**
     * Returns an open, valid {@link Connection} to the MySQL database.
     *
     * <p>
     * If the current connection is null, closed, or invalid, a new
     * connection is established using the parameters from {@link Constants.DB}.
     * The connection is validated with a lightweight ping (1-second timeout)
     * before being returned.
     * </p>
     *
     * @return a live {@link Connection} to the {@code complaint_management} DB
     * @throws DatabaseException if the connection cannot be established or
     *                           validated
     */
    public Connection getConnection() throws DatabaseException {
        try {
            // (Re-)open connection if it is null, closed, or invalid
            if (connection == null
                    || connection.isClosed()
                    || !connection.isValid(Constants.DB.CONNECTION_TIMEOUT_SECONDS)) {

                connection = DriverManager.getConnection(
                        Constants.DB.URL,
                        Constants.DB.USERNAME,
                        Constants.DB.PASSWORD);
            }
            return connection;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to connect to the database. "
                            + "Ensure MySQL is running and credentials in Constants.DB are correct.",
                    e, e.getErrorCode());
        }
    }

    /**
     * Closes the active database connection.
     * Should be called when the application is shutting down (e.g., from a
     * shutdown hook registered in {@code Main}).
     *
     * <p>
     * Safe to call even if the connection is already closed or null.
     * </p>
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                // Log warning but do not propagate — we are shutting down
                System.err.println("[DatabaseConnection] Warning: error while closing connection — " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Tests whether the database is reachable by attempting to open and
     * immediately validate a connection. Does not retain the connection.
     *
     * @return {@code true} if the database responded within the timeout;
     *         {@code false} otherwise
     */
    public boolean testConnection() {
        try {
            Connection testConn = DriverManager.getConnection(
                    Constants.DB.URL,
                    Constants.DB.USERNAME,
                    Constants.DB.PASSWORD);

            System.out.println("✅ Database Connected Successfully!");

            boolean valid = testConn.isValid(Constants.DB.CONNECTION_TIMEOUT_SECONDS);

            testConn.close();

            return valid;

        } catch (SQLException e) {

            System.out.println("❌ Database Connection Failed");
            e.printStackTrace();

            return false;
        }
    }

    // ------------------------------------------------------------------
    // Prevent cloning (Singleton integrity)
    // ------------------------------------------------------------------

    /**
     * Overrides {@link Object#clone()} to prevent cloning, which would
     * break the Singleton guarantee.
     *
     * @return never returns
     * @throws CloneNotSupportedException always
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("DatabaseConnection is a Singleton.");
    }
}
