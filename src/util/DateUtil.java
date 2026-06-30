package util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class providing centralised date and time formatting, parsing,
 * and conversion helpers for the Complaint Management System.
 *
 * <p>All methods are static. This class cannot be instantiated.</p>
 *
 * <p>Format constants are taken from {@link Constants.DateFormats} to ensure
 * a single source of truth for every date pattern used in the application.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public final class DateUtil {

    // Pre-built formatters (thread-safe in Java 8+)
    private static final DateTimeFormatter FMT_DISPLAY_DATE     =
            DateTimeFormatter.ofPattern(Constants.DateFormats.DISPLAY_DATE);
    private static final DateTimeFormatter FMT_DISPLAY_DATETIME =
            DateTimeFormatter.ofPattern(Constants.DateFormats.DISPLAY_DATETIME);
    private static final DateTimeFormatter FMT_DISPLAY_TIME     =
            DateTimeFormatter.ofPattern(Constants.DateFormats.DISPLAY_TIME);
    private static final DateTimeFormatter FMT_DB_DATE          =
            DateTimeFormatter.ofPattern(Constants.DateFormats.DB_DATE);
    private static final DateTimeFormatter FMT_DB_DATETIME      =
            DateTimeFormatter.ofPattern(Constants.DateFormats.DB_DATETIME);
    private static final DateTimeFormatter FMT_FILENAME_TS      =
            DateTimeFormatter.ofPattern(Constants.DateFormats.FILENAME_TS);

    /** Private constructor — utility class; not instantiable. */
    private DateUtil() {
        throw new UnsupportedOperationException("DateUtil is a utility class.");
    }

    // ------------------------------------------------------------------
    // Formatting — LocalDate
    // ------------------------------------------------------------------

    /**
     * Formats a {@link LocalDate} for display (e.g., "30 Jun 2024").
     *
     * @param date the date to format; may be {@code null}
     * @return formatted string, or {@code "—"} if date is null
     */
    public static String formatDate(LocalDate date) {
        return (date == null) ? "\u2014" : date.format(FMT_DISPLAY_DATE);
    }

    /**
     * Formats a {@link LocalDate} as a database-compatible string
     * (e.g., "2024-06-30").
     *
     * @param date the date to format; may be {@code null}
     * @return formatted string, or {@code null} if date is null
     */
    public static String formatDateForDB(LocalDate date) {
        return (date == null) ? null : date.format(FMT_DB_DATE);
    }

    // ------------------------------------------------------------------
    // Formatting — LocalDateTime
    // ------------------------------------------------------------------

    /**
     * Formats a {@link LocalDateTime} for display (e.g., "30 Jun 2024, 02:30 PM").
     *
     * @param dt the datetime to format; may be {@code null}
     * @return formatted string, or {@code "—"} if dt is null
     */
    public static String formatDateTime(LocalDateTime dt) {
        return (dt == null) ? "\u2014" : dt.format(FMT_DISPLAY_DATETIME);
    }

    /**
     * Formats a {@link LocalDateTime} to show only the time portion
     * (e.g., "02:30 PM").
     *
     * @param dt the datetime to format; may be {@code null}
     * @return formatted time string, or {@code "—"} if dt is null
     */
    public static String formatTime(LocalDateTime dt) {
        return (dt == null) ? "\u2014" : dt.format(FMT_DISPLAY_TIME);
    }

    /**
     * Formats a {@link LocalDateTime} for use in generated file names
     * (e.g., "20240630_143025").
     *
     * @param dt the datetime to format; may be {@code null}
     * @return file-name-safe timestamp string, or {@code ""} if dt is null
     */
    public static String formatForFilename(LocalDateTime dt) {
        return (dt == null) ? "" : dt.format(FMT_FILENAME_TS);
    }

    // ------------------------------------------------------------------
    // Parsing
    // ------------------------------------------------------------------

    /**
     * Parses a date string from database format ("yyyy-MM-dd") into a
     * {@link LocalDate}.
     *
     * @param dateStr the date string to parse; may be {@code null} or blank
     * @return the parsed {@link LocalDate}, or {@code null} if input is blank
     * @throws DateTimeParseException if the string is not in the expected format
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr.trim(), FMT_DB_DATE);
    }

    /**
     * Attempts to parse a date string and returns {@code null} instead of
     * throwing an exception on failure — safe for user input.
     *
     * @param dateStr the date string to parse; may be {@code null}
     * @return the parsed {@link LocalDate}, or {@code null} on any failure
     */
    public static LocalDate parseDateSafe(String dateStr) {
        try {
            return parseDate(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // SQL Timestamp conversions
    // ------------------------------------------------------------------

    /**
     * Converts a {@link java.sql.Timestamp} retrieved from a JDBC
     * {@code ResultSet} into a {@link LocalDateTime}.
     *
     * @param ts the SQL timestamp; may be {@code null}
     * @return the equivalent {@link LocalDateTime}, or {@code null}
     */
    public static LocalDateTime toLocalDateTime(Timestamp ts) {
        return (ts == null) ? null : ts.toLocalDateTime();
    }

    /**
     * Converts a {@link java.sql.Date} retrieved from a JDBC
     * {@code ResultSet} into a {@link LocalDate}.
     *
     * @param sqlDate the SQL date; may be {@code null}
     * @return the equivalent {@link LocalDate}, or {@code null}
     */
    public static LocalDate toLocalDate(java.sql.Date sqlDate) {
        return (sqlDate == null) ? null : sqlDate.toLocalDate();
    }

    // ------------------------------------------------------------------
    // Relative time helpers
    // ------------------------------------------------------------------

    /**
     * Returns a human-readable "time ago" string relative to now.
     * E.g., "2 days ago", "just now", "1 month ago".
     *
     * @param dt the datetime to compare against now; may be {@code null}
     * @return a relative time string, or {@code "—"} if dt is null
     */
    public static String timeAgo(LocalDateTime dt) {
        if (dt == null) return "\u2014";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dt, now);
        long hours   = ChronoUnit.HOURS.between(dt, now);
        long days    = ChronoUnit.DAYS.between(dt, now);
        long months  = ChronoUnit.MONTHS.between(dt, now);

        if (minutes < 1)   return "just now";
        if (minutes < 60)  return minutes + " min ago";
        if (hours   < 24)  return hours   + " hour"  + (hours   > 1 ? "s" : "") + " ago";
        if (days    < 30)  return days    + " day"   + (days    > 1 ? "s" : "") + " ago";
        if (months  < 12)  return months  + " month" + (months  > 1 ? "s" : "") + " ago";

        long years = ChronoUnit.YEARS.between(dt, now);
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }

    /**
     * Returns today's date formatted for display.
     *
     * @return today as a display-format string (e.g., "30 Jun 2024")
     */
    public static String todayDisplay() {
        return formatDate(LocalDate.now());
    }

    /**
     * Returns the current date and time formatted for display.
     *
     * @return now as a display-format string
     */
    public static String nowDisplay() {
        return formatDateTime(LocalDateTime.now());
    }

    /**
     * Returns today's date as an ISO-8601 string (e.g., "2024-06-30").
     * Used when generating default file names for CSV exports.
     *
     * @return today in "yyyy-MM-dd" format
     */
    public static String todayIso() {
        return LocalDate.now().format(FMT_DB_DATE);
    }
}
