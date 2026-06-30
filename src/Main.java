import database.DatabaseConnection;
import exceptions.DatabaseException;
import ui.LoginPanel;
import ui.SplashScreen;

import javax.swing.*;

/**
 * Application entry point for the Complaint Management System.
 *
 * <p>This class is responsible for:
 * <ol>
 *   <li>Configuring the Swing Look and Feel</li>
 *   <li>Validating the database connection before showing any UI</li>
 *   <li>Displaying the {@link SplashScreen}</li>
 *   <li>Launching the {@link LoginPanel} after the splash completes</li>
 *   <li>Registering a JVM shutdown hook to close the DB connection cleanly</li>
 * </ol>
 * </p>
 *
 * <p><strong>OOP context:</strong> This class is intentionally minimal —
 * it only orchestrates startup. Business logic, UI construction, and data
 * access are delegated to the appropriate layers of the MVC architecture.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class Main {

    // ------------------------------------------------------------------
    // Entry point
    // ------------------------------------------------------------------

    /**
     * Application entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // All Swing operations must run on the Event Dispatch Thread
        SwingUtilities.invokeLater(Main::launch);
    }

    // ------------------------------------------------------------------
    // Startup sequence
    // ------------------------------------------------------------------

    /**
     * Runs the full startup sequence on the EDT.
     */
    private static void launch() {
        // Step 1: Configure Swing Look and Feel
        configureLookAndFeel();

        // Step 2: Verify database connectivity BEFORE showing any UI
        if (!verifyDatabaseConnection()) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to the database.\n\n" +
                "Please ensure:\n" +
                "  • MySQL 8 is running on localhost:3306\n" +
                "  • The database 'complaint_management' exists\n" +
                "  • Credentials in Constants.DB are correct (root/Harsh@1234)\n\n" +
                "Run the SQL setup script first:\n" +
                "  sql/complaint_management.sql",
                "Database Connection Failed",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Step 3: Register shutdown hook to close the DB connection gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(
            () -> DatabaseConnection.getInstance().closeConnection(),
            "DB-Shutdown-Hook"));

        // Step 4: Show splash screen; show login panel when it finishes
        new SplashScreen(() ->
            SwingUtilities.invokeLater(LoginPanel::new));
    }

    // ------------------------------------------------------------------
    // Look and Feel Configuration
    // ------------------------------------------------------------------

    /**
     * Sets the Swing Look and Feel to the system default (Windows / macOS native
     * appearance). If that fails, falls back to Metal (always available).
     *
     * <p>Desktop rendering hints are also applied to enable sub-pixel
     * anti-aliasing where supported.</p>
     */
    private static void configureLookAndFeel() {
        // Enable font anti-aliasing on all platforms
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext",               "true");

        try {
            // Attempt to use the system look-and-feel for native window decorations
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback: Metal is always guaranteed to be present in any JRE
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Metal will load implicitly if everything else fails
            }
        }

        // Override standard Swing defaults for cleaner rendering
        UIManager.put("TabbedPane.contentBorderInsets", new java.awt.Insets(0, 0, 0, 0));
        UIManager.put("ScrollBar.thumbDarkShadow",     new java.awt.Color(80, 80, 80));
        UIManager.put("ScrollBar.thumb",               new java.awt.Color(100, 100, 100));
        UIManager.put("ScrollBar.track",               new java.awt.Color(40, 40, 40));
    }

    // ------------------------------------------------------------------
    // Database Verification
    // ------------------------------------------------------------------

    /**
     * Performs a quick connectivity test to the MySQL database before the
     * application shows any windows. Uses a 3-second timeout.
     *
     * @return {@code true} if the database responded; {@code false} otherwise
     */
    private static boolean verifyDatabaseConnection() {
        try {
            return DatabaseConnection.getInstance().testConnection();
        } catch (Exception e) {
            return false;
        }
    }
}
