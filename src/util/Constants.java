package util;

/**
 * Application-wide constants for the Complaint Management System.
 *
 * <p>This class is a non-instantiable utility class that centralises every
 * "magic string" or "magic number" used across the application into a single
 * source of truth. Groups are organised with nested static classes to improve
 * readability and IDE auto-completion.</p>
 *
 * <p>Usage: {@code Constants.DB.URL}, {@code Constants.UI.WINDOW_TITLE}, etc.</p>
 *
 * @author CMS Development Team
 * @version 1.0.0
 * @since 2024
 */
public final class Constants {

    /**
     * Prevent instantiation.
     */
    private Constants() {
        throw new UnsupportedOperationException("Constants is a utility class.");
    }

    // ================================================================
    // DATABASE CONFIGURATION
    // ================================================================

    public static final class DB {

        private DB() {}

        /** MySQL JDBC Driver */
        public static final String DRIVER = "com.mysql.cj.jdbc.Driver";

        /** Database URL */
        public static final String URL =
                "jdbc:mysql://localhost:3306/complaint_management"
                        + "?useSSL=false"
                        + "&serverTimezone=UTC"
                        + "&allowPublicKeyRetrieval=true"
                        + "&characterEncoding=UTF-8";

        /** MySQL Username */
        public static final String USERNAME = "root";

        /** MySQL Password */
        public static final String PASSWORD = "Harsh@1234";

        /** Connection validation timeout (seconds) */
        public static final int CONNECTION_TIMEOUT_SECONDS = 10;

        /** Maximum login attempts */
        public static final int MAX_LOGIN_ATTEMPTS = 3;
    }

    // ================================================================
    // APPLICATION INFORMATION
    // ================================================================

    public static final class App {

        private App() {}

        public static final String NAME = "Complaint Management System";
        public static final String SHORT_NAME = "CMS";
        public static final String VERSION = "1.0.0";
        public static final String AUTHOR = "CMS Development Team";
        public static final String YEAR = "2024";

        public static final String WINDOW_TITLE = NAME + " v" + VERSION;
        public static final String SPLASH_TITLE = NAME;
        public static final String COMPLAINT_PREFIX = "CMS-2024-";
    }

    // ================================================================
    // UI SETTINGS
    // ================================================================

    public static final class UI {

        private UI() {}

        public static final int WINDOW_WIDTH = 1280;
        public static final int WINDOW_HEIGHT = 760;

        public static final int MIN_WINDOW_WIDTH = 1024;
        public static final int MIN_WINDOW_HEIGHT = 600;

        public static final int SPLASH_WIDTH = 600;
        public static final int SPLASH_HEIGHT = 380;

        public static final int SIDEBAR_WIDTH = 230;
        public static final int HEADER_HEIGHT = 64;
        public static final int STATUS_BAR_HEIGHT = 28;

        public static final int CARD_CORNER_RADIUS = 12;
        public static final int BUTTON_CORNER_RADIUS = 8;

        public static final int PANEL_PADDING = 24;
        public static final int GAP = 12;

        public static final int TABLE_ROW_HEIGHT = 36;
        public static final int PAGE_SIZE = 15;
    }

    // ================================================================
    // APPLICATION PAGES
    // ================================================================

    public static final class Pages {

        private Pages() {}

        public static final String DASHBOARD = "DASHBOARD";
        public static final String REGISTER_COMPLAINT = "REGISTER_COMPLAINT";
        public static final String VIEW_COMPLAINTS = "VIEW_COMPLAINTS";
        public static final String SEARCH_COMPLAINTS = "SEARCH_COMPLAINTS";
        public static final String COMPLAINT_DETAIL = "COMPLAINT_DETAIL";
        public static final String REPORTS = "REPORTS";
        public static final String PROFILE = "PROFILE";
        public static final String SETTINGS = "SETTINGS";
        public static final String ABOUT = "ABOUT";
    }

    // ================================================================
    // DATE FORMATS
    // ================================================================

    public static final class DateFormats {

        private DateFormats() {}

        public static final String DISPLAY_DATE = "dd MMM yyyy";
        public static final String DISPLAY_DATETIME = "dd MMM yyyy, hh:mm a";
        public static final String DISPLAY_TIME = "hh:mm a";

        public static final String DB_DATE = "yyyy-MM-dd";
        public static final String DB_DATETIME = "yyyy-MM-dd HH:mm:ss";

        public static final String FILENAME_TS = "yyyyMMdd_HHmmss";
    }

    // ================================================================
    // VALIDATION
    // ================================================================

    public static final class Validation {

        private Validation() {}

        public static final int USERNAME_MIN = 3;
        public static final int USERNAME_MAX = 50;

        // Matches your documentation
        public static final int PASSWORD_MIN = 8;
        public static final int PASSWORD_MAX = 100;

        public static final int NAME_MIN = 2;
        public static final int NAME_MAX = 100;

        public static final int TITLE_MIN = 5;
        public static final int TITLE_MAX = 200;

        public static final int DESC_MIN = 10;
        public static final int DESC_MAX = 2000;

        public static final int PHONE_MIN = 7;
        public static final int PHONE_MAX = 20;

        public static final int EMAIL_MAX = 150;

        public static final String EMAIL_REGEX =
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        public static final String PHONE_REGEX =
                "^[0-9+\\-() ]{7,20}$";

        public static final String USERNAME_REGEX =
                "^[A-Za-z0-9_]{3,50}$";
    }

    // ================================================================
    // APPLICATION MESSAGES
    // ================================================================

    public static final class Messages {

        private Messages() {}

        // Success

        public static final String COMPLAINT_REGISTERED =
                "Complaint registered successfully!";

        public static final String COMPLAINT_UPDATED =
                "Complaint updated successfully!";

        public static final String COMPLAINT_DELETED =
                "Complaint deleted successfully!";

        public static final String COMPLAINT_ASSIGNED =
                "Complaint assigned successfully!";

        public static final String STATUS_UPDATED =
                "Status updated successfully!";

        public static final String EXPORT_SUCCESS =
                "Report exported successfully!";

        public static final String PROFILE_UPDATED =
                "Profile updated successfully!";

        // Errors

        public static final String DB_CONNECTION_ERROR =
                "Cannot connect to database. Please check your settings.";

        public static final String LOGIN_FAILED =
                "Invalid username or password.";

        public static final String ACCOUNT_DISABLED =
                "Your account has been disabled. Contact an administrator.";

        public static final String FILL_REQUIRED_FIELDS =
                "Please fill in all required fields.";

        public static final String CONFIRM_DELETE =
                "Are you sure you want to delete this complaint? This action cannot be undone.";

        public static final String NO_RECORD_SELECTED =
                "Please select a record from the table first.";
    }
}