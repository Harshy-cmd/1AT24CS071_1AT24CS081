package reports;

import exceptions.ReportException;
import model.Complaint;
import util.DateUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Implements {@link IReport} by writing complaint data to a CSV file.
 *
 * <p><strong>OOP Role — Polymorphism:</strong>
 * {@link service.ReportService} references this class only through the
 * {@link IReport} interface. At runtime, the service decides to use
 * {@code CSVExporter} for CSV output without the controller needing to
 * know the class name.</p>
 *
 * <p>The generated CSV file:
 * <ul>
 *   <li>Has a UTF-8 BOM so Excel opens it correctly</li>
 *   <li>Quotes all fields that may contain commas</li>
 *   <li>Includes a header row</li>
 * </ul>
 * </p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class CSVExporter implements IReport {

    /** UTF-8 BOM bytes that tell Excel to read the file as UTF-8. */
    private static final String UTF8_BOM = "\uFEFF";

    /** CSV header row — one column per Complaint field rendered in the report. */
    private static final String CSV_HEADER =
        "Complaint No,Title,Category,Priority,Status,Location,Department," +
        "Assigned To,Created By,Date Created,Date Updated,Resolution Date,Remarks";

    /** The absolute file path of the output CSV file. */
    private final String filePath;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Constructs a CSVExporter that will write to the specified file path.
     *
     * @param filePath the absolute path of the output .csv file
     */
    public CSVExporter(String filePath) {
        this.filePath = filePath;
    }

    // ------------------------------------------------------------------
    // IReport implementation
    // ------------------------------------------------------------------

    /**
     * Writes the complaint list to the configured CSV file.
     *
     * @param complaints the data to write; may be empty but never null
     * @throws ReportException if the file cannot be created or written
     */
    @Override
    public void generate(List<Complaint> complaints) throws ReportException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {

            // Write BOM + header
            writer.write(UTF8_BOM);
            writer.write(CSV_HEADER);
            writer.newLine();

            // Write one row per complaint
            for (Complaint c : complaints) {
                writer.write(buildRow(c));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ReportException(
                "Failed to write CSV file: " + e.getMessage(),
                ReportException.ReportType.CSV_EXPORT, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getReportTypeName() {
        return "CSV Export";
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Converts a single {@link Complaint} to a CSV row string.
     * All field values are escaped and optionally quoted.
     *
     * @param c the complaint to convert
     * @return a comma-separated row string
     */
    private String buildRow(Complaint c) {
        return String.join(",",
            quote(c.getComplaintNumber()),
            quote(c.getTitle()),
            quote(c.getCategory()  != null ? c.getCategory().getDisplayName()  : ""),
            quote(c.getPriority()  != null ? c.getPriority().getDisplayName()  : ""),
            quote(c.getStatus()    != null ? c.getStatus().getDisplayName()    : ""),
            quote(c.getLocation()),
            quote(c.getDepartment()),
            quote(c.getAssignedToName()),
            quote(c.getCreatedByName()),
            quote(DateUtil.formatDateTime(c.getDateCreated())),
            quote(DateUtil.formatDateTime(c.getDateUpdated())),
            quote(DateUtil.formatDate(c.getResolutionDate())),
            quote(c.getRemarks())
        );
    }

    /**
     * Wraps a value in double-quotes and escapes any internal double-quotes
     * by doubling them (RFC 4180 compliant).
     *
     * @param value the raw field value (may be null)
     * @return a quoted, escaped CSV field
     */
    private String quote(String value) {
        if (value == null || value.equals("\u2014")) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
