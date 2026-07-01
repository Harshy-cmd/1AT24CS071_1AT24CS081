package ui;

import components.*;
import controller.ReportController;
import model.*;
import util.Constants;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Reports panel providing filtered data preview, CSV export, and print.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ReportsPanel extends JPanel {

    private final ReportController reportController;

    // Filter controls
    private JComboBox<String> statusFilter;
    private JComboBox<String> priorityFilter;
    private JComboBox<String> categoryFilter;
    private JTextField        fromDateField;
    private JTextField        toDateField;

    // Preview table
    private DefaultTableModel tableModel;
    private JLabel            resultLabel;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public ReportsPanel(ReportController reportController) {
        this.reportController = reportController;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getContentBackground());
        buildUI();
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(new HeaderPanel("Reports & Export"), BorderLayout.NORTH);
        top.add(buildFilterBar(), BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.GAP, ThemeManager.PADDING_SM));
        bar.setBackground(ThemeManager.getHeaderBackground());
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getHeaderBorder()));

        statusFilter = makeFilterCombo("All Statuses");
        for (Status s : Status.values()) statusFilter.addItem(s.getDisplayName());

        priorityFilter = makeFilterCombo("All Priorities");
        for (Priority p : Priority.values()) priorityFilter.addItem(p.getDisplayName());

        categoryFilter = makeFilterCombo("All Categories");
        for (ComplaintCategory c : ComplaintCategory.values()) categoryFilter.addItem(c.getDisplayName());

        String today = DateUtil.todayIso();
        fromDateField = makeDateField(today.substring(0, 8) + "01"); // first of month
        toDateField   = makeDateField(today);

        RoundedButton previewBtn = new RoundedButton("Preview", RoundedButton.Style.PRIMARY);
        previewBtn.addActionListener(e -> loadPreview());

        bar.add(new JLabel("Status:"));     bar.add(statusFilter);
        bar.add(new JLabel("Priority:"));   bar.add(priorityFilter);
        bar.add(new JLabel("Category:"));   bar.add(categoryFilter);
        bar.add(new JLabel("From:"));       bar.add(fromDateField);
        bar.add(new JLabel("To:"));         bar.add(toDateField);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(previewBtn);
        return bar;
    }

    private JPanel buildTableArea() {
        String[] cols = {"Complaint No", "Title", "Category", "Priority", "Status",
                         "Department", "Assigned To", "Date Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(ThemeManager.getFontTable());
        table.setForeground(ThemeManager.getTextPrimary());
        table.setBackground(ThemeManager.getTableBackground());
        table.setGridColor(ThemeManager.getTableGrid());
        table.setRowHeight(Constants.UI.TABLE_ROW_HEIGHT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(ThemeManager.getFontTableHeader());
        table.getTableHeader().setBackground(ThemeManager.getTableHeader());
        table.getTableHeader().setForeground(ThemeManager.getTextSecondary());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(ThemeManager.getTableBackground());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        resultLabel = new JLabel("  Apply filters and click Preview.");
        resultLabel.setFont(ThemeManager.getFontSmall());
        resultLabel.setForeground(ThemeManager.getTextSecondary());

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(ThemeManager.getStatusBarBackground());
        statusBar.add(resultLabel);

        panel.add(sp,        BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, ThemeManager.GAP, ThemeManager.PADDING_SM));
        bar.setBackground(ThemeManager.getHeaderBackground());
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getHeaderBorder()));

        RoundedButton csvBtn   = new RoundedButton("Export CSV",  RoundedButton.Style.SUCCESS);
        RoundedButton printBtn = new RoundedButton("Print",       RoundedButton.Style.SECONDARY);

        csvBtn.addActionListener(e   -> exportCSV());
        printBtn.addActionListener(e -> printReport());

        bar.add(csvBtn);
        bar.add(printBtn);
        return bar;
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void loadPreview() {
        SwingWorker<List<Complaint>, Void> worker = new SwingWorker<>() {
            @Override protected List<Complaint> doInBackground() throws Exception {
                return reportController.getFilteredComplaints(
                    getStatusValue(), getPriorityValue(), getCategoryValue(), null,
                    parseDate(fromDateField.getText()), parseDate(toDateField.getText()));
            }
            @Override protected void done() {
                try {
                    List<Complaint> data = get();
                    tableModel.setRowCount(0);
                    for (Complaint c : data) {
                        tableModel.addRow(new Object[]{
                            c.getComplaintNumber(),
                            truncate(c.getTitle(), 45),
                            c.getCategory()  != null ? c.getCategory().getDisplayName()  : "",
                            c.getPriority()  != null ? c.getPriority().getDisplayName()  : "",
                            c.getStatus()    != null ? c.getStatus().getDisplayName()    : "",
                            c.getDepartment() != null ? c.getDepartment() : "",
                            c.getAssignedToName(),
                            DateUtil.formatDate(c.getDateCreated() != null
                                    ? c.getDateCreated().toLocalDate() : null)
                        });
                    }
                    resultLabel.setText("  " + data.size() + " records found.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("complaints_report_" + DateUtil.todayIso() + ".csv"));
        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) path += ".csv";
        final String finalPath = path;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                reportController.exportToCSV(finalPath,
                    getStatusValue(), getPriorityValue(), getCategoryValue(), null,
                    parseDate(fromDateField.getText()), parseDate(toDateField.getText()));
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "CSV exported to:\n" + finalPath, "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void printReport() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                reportController.printReport(
                    getStatusValue(), getPriorityValue(), getCategoryValue(), null,
                    parseDate(fromDateField.getText()), parseDate(toDateField.getText()));
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "Print failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private JComboBox<String> makeFilterCombo(String defaultItem) {
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem(defaultItem);
        combo.setFont(ThemeManager.getFontBody());
        combo.setBackground(ThemeManager.getInputBackground());
        combo.setForeground(ThemeManager.getTextPrimary());
        return combo;
    }

    private JTextField makeDateField(String defaultValue) {
        JTextField field = new JTextField(defaultValue, 10);
        field.setFont(ThemeManager.getFontBody());
        field.setForeground(ThemeManager.getTextPrimary());
        field.setBackground(ThemeManager.getInputBackground());
        field.setToolTipText("Format: YYYY-MM-DD");
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return field;
    }

    private String getStatusValue() {
        String v = (String) statusFilter.getSelectedItem();
        return (v == null || v.startsWith("All")) ? null
            : Status.fromDisplayName(v).name();
    }

    private String getPriorityValue() {
        String v = (String) priorityFilter.getSelectedItem();
        return (v == null || v.startsWith("All")) ? null
            : Priority.fromDisplayName(v).name();
    }

    private String getCategoryValue() {
        String v = (String) categoryFilter.getSelectedItem();
        return (v == null || v.startsWith("All")) ? null
            : ComplaintCategory.fromDisplayName(v).name();
    }

    private LocalDate parseDate(String text) {
        try { return LocalDate.parse(text.trim()); } catch (Exception e) { return null; }
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
