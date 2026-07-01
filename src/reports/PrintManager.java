package reports;

import exceptions.ReportException;
import model.Complaint;
import util.Constants;
import util.DateUtil;

import java.awt.*;
import java.awt.print.*;
import java.util.List;

/**
 * Implements {@link IReport} by sending a formatted complaint report to
 * the system print dialog (physical printer or PDF export).
 *
 * <p><strong>OOP Role — Polymorphism:</strong>
 * {@link service.ReportService} references this class only through the
 * {@link IReport} interface. The print rendering logic is fully encapsulated
 * inside this class, keeping the service and controller clean of AWT details.
 * </p>
 *
 * <p>The printed report renders a title header, column headers, and one row
 * per complaint with pagination across multiple pages.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class PrintManager implements IReport, Printable {

    // ------------------------------------------------------------------
    // Print layout constants (in points: 72 points = 1 inch)
    // ------------------------------------------------------------------

    private static final int MARGIN_TOP    = 60;
    private static final int MARGIN_LEFT   = 40;
    private static final int ROW_HEIGHT    = 18;
    private static final int HEADER_HEIGHT = 36;
    private static final int FONT_SIZE_H1  = 14;
    private static final int FONT_SIZE_H2  = 9;
    private static final int FONT_SIZE_BODY= 8;

    /** Column widths (must sum to printable width ≈ 680 points for A4 landscape). */
    private static final int[] COL_WIDTHS = {90, 150, 70, 60, 80, 60, 100, 80};

    /** Column headers matching the complaint fields displayed. */
    private static final String[] COL_HEADERS =
        {"Complaint #", "Title", "Category", "Priority", "Status", "Location", "Department", "Assigned To"};

    /** The data snapshot set just before the print job is submitted. */
    private List<Complaint> complaints;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Default constructor. Data is supplied via {@link #generate(List)}.
     */
    public PrintManager() {}

    // ------------------------------------------------------------------
    // IReport implementation
    // ------------------------------------------------------------------

    /**
     * Opens the system print dialog and initiates printing.
     * The data snapshot is stored in {@link #complaints} before the
     * Printable callback is invoked.
     *
     * @param complaints the complaint list to print
     * @throws ReportException if the print job is cancelled or fails
     */
    @Override
    public void generate(List<Complaint> complaints) throws ReportException {
        this.complaints = complaints;

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(Constants.App.NAME + " — Complaint Report");

        // Landscape A4 page format
        PageFormat pf = job.defaultPage();
        pf.setOrientation(PageFormat.LANDSCAPE);

        job.setPrintable(this, pf);

        // Show system print dialog — returns false if user cancels
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                throw new ReportException(
                    "Print job failed: " + e.getMessage(),
                    ReportException.ReportType.PRINT, e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getReportTypeName() {
        return "Print Report";
    }

    // ------------------------------------------------------------------
    // Printable implementation
    // ------------------------------------------------------------------

    /**
     * Renders a single page of the complaint report.
     * Called repeatedly by the print framework — once per page.
     *
     * @param g         the graphics context for the page
     * @param pf        the page format (dimensions, orientation)
     * @param pageIndex the zero-based page index
     * @return {@link Printable#PAGE_EXISTS} or {@link Printable#NO_SUCH_PAGE}
     */
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (complaints == null) return NO_SUCH_PAGE;

        int printableHeight = (int) pf.getImageableHeight() - MARGIN_TOP;
        int rowsPerPage     = (printableHeight - HEADER_HEIGHT) / ROW_HEIGHT;

        if (complaints.isEmpty() && pageIndex > 0) return NO_SUCH_PAGE;
        if (!complaints.isEmpty() && pageIndex * rowsPerPage >= complaints.size() && pageIndex > 0)
            return NO_SUCH_PAGE;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = (int) pf.getImageableX() + MARGIN_LEFT;
        int y = (int) pf.getImageableY() + MARGIN_TOP;

        // ---- Page title ----
        g2.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE_H1));
        g2.setColor(new Color(30, 60, 100));
        g2.drawString(Constants.App.NAME + " — Complaint Report", x, y);

        y += 6;
        g2.setFont(new Font("SansSerif", Font.PLAIN, FONT_SIZE_H2));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Generated: " + DateUtil.nowDisplay() +
                      "   |   Total Records: " + complaints.size() +
                      "   |   Page " + (pageIndex + 1), x, y + 14);

        y += HEADER_HEIGHT;

        // ---- Column headers ----
        g2.setColor(new Color(30, 60, 100));
        g2.fillRect(x - 2, y - ROW_HEIGHT + 4, getTableWidth(), ROW_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE_BODY));

        int cx = x;
        for (int i = 0; i < COL_HEADERS.length; i++) {
            g2.drawString(COL_HEADERS[i], cx + 3, y - 3);
            cx += COL_WIDTHS[i];
        }

        y += 4;

        // ---- Data rows ----
        int startRow = pageIndex * rowsPerPage;
        int endRow   = Math.min(startRow + rowsPerPage, complaints.size());

        for (int rowIdx = startRow; rowIdx < endRow; rowIdx++) {
            Complaint c = complaints.get(rowIdx);

            // Alternate row background
            if ((rowIdx - startRow) % 2 == 0) {
                g2.setColor(new Color(240, 245, 252));
                g2.fillRect(x - 2, y - ROW_HEIGHT + 4, getTableWidth(), ROW_HEIGHT);
            }

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, FONT_SIZE_BODY));

            cx = x;
            String[] row = buildRow(c);
            for (int col = 0; col < row.length; col++) {
                String cell = truncate(row[col], COL_WIDTHS[col] - 6, g2);
                g2.drawString(cell, cx + 3, y - 3);
                cx += COL_WIDTHS[col];
            }

            // Row separator
            g2.setColor(new Color(220, 220, 225));
            g2.drawLine(x - 2, y + 4, x - 2 + getTableWidth(), y + 4);

            y += ROW_HEIGHT;
        }

        return PAGE_EXISTS;
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private int getTableWidth() {
        int w = 0;
        for (int cw : COL_WIDTHS) w += cw;
        return w;
    }

    private String[] buildRow(Complaint c) {
        return new String[]{
            c.getComplaintNumber(),
            c.getTitle(),
            c.getCategory()  != null ? c.getCategory().getDisplayName()  : "",
            c.getPriority()  != null ? c.getPriority().getDisplayName()  : "",
            c.getStatus()    != null ? c.getStatus().getDisplayName()    : "",
            c.getLocation() != null  ? c.getLocation()  : "",
            c.getDepartment() != null ? c.getDepartment() : "",
            c.getAssignedToName() != null ? c.getAssignedToName() : "Unassigned"
        };
    }

    /**
     * Truncates a string if it would exceed the given pixel width.
     *
     * @param text      the original text
     * @param maxPixels the maximum pixel width
     * @param g2        the graphics context used to measure string width
     * @return the (possibly truncated) string
     */
    private String truncate(String text, int maxPixels, Graphics2D g2) {
        if (text == null) return "";
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxPixels) return text;
        String ellipsis = "...";
        while (text.length() > 0 &&
               fm.stringWidth(text + ellipsis) > maxPixels) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }
}
