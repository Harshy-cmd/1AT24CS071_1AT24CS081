package ui;

import components.*;
import controller.ComplaintController;
import exceptions.DatabaseException;
import model.*;
import util.Constants;
import util.DateUtil;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel that displays all complaints in a sortable, filterable table.
 *
 * <p>Admin users see a full toolbar (New, Assign, Delete).
 * Employee users see only complaints assigned to them.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ViewComplaintsPanel extends JPanel {

    private final ComplaintController controller;
    private final MainFrame           mainFrame;

    private DefaultTableModel tableModel;
    private JTable            table;
    private List<Complaint>   currentData;

    private JLabel countLabel;
    private JComboBox<String> filterStatusCombo;
    private JComboBox<String> filterPriorityCombo;
    private SearchBar         searchBar;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public ViewComplaintsPanel(ComplaintController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame  = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.getContentBackground());
        buildUI();
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        add(new HeaderPanel("View Complaints"), BorderLayout.NORTH);

        // Toolbar
        add(buildToolbar(), BorderLayout.NORTH);
        // Wrap header + toolbar
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(new HeaderPanel("View Complaints"), BorderLayout.NORTH);
        top.add(buildToolbar(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Complaint No", "Title", "Category", "Priority", "Status",
                         "Assigned To", "Date Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };

        table = new JTable(tableModel);
        styleTable();

        // Double-click → detail
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedDetail();
            }
        });

        // Context menu
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showContextMenu(e);
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showContextMenu(e);
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setBackground(ThemeManager.getTableBackground());

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        statusBar.setBackground(ThemeManager.getStatusBarBackground());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getCardBorder()));

        countLabel = new JLabel("Loading...");
        countLabel.setFont(ThemeManager.getFontSmall());
        countLabel.setForeground(ThemeManager.getTextSecondary());
        statusBar.add(countLabel);

        add(sp,        BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bar.setBackground(ThemeManager.getHeaderBackground());
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getHeaderBorder()));

        // Filter: Status
        filterStatusCombo = new JComboBox<>();
        filterStatusCombo.addItem("All Statuses");
        for (Status s : Status.values()) filterStatusCombo.addItem(s.getDisplayName());
        styleCombo(filterStatusCombo);
        filterStatusCombo.addActionListener(e -> applyFilter());

        // Filter: Priority
        filterPriorityCombo = new JComboBox<>();
        filterPriorityCombo.addItem("All Priorities");
        for (Priority p : Priority.values()) filterPriorityCombo.addItem(p.getDisplayName());
        styleCombo(filterPriorityCombo);
        filterPriorityCombo.addActionListener(e -> applyFilter());

        // Search bar
        searchBar = new SearchBar("Search complaints...", q -> applyFilter());

        // Action buttons
        RoundedButton newBtn    = new RoundedButton("+ New",     RoundedButton.Style.PRIMARY);
        RoundedButton detailBtn = new RoundedButton("Open",      RoundedButton.Style.SECONDARY);
        RoundedButton refreshBtn= new RoundedButton("⟳ Refresh", RoundedButton.Style.SECONDARY);
        RoundedButton deleteBtn = new RoundedButton("Delete",    RoundedButton.Style.DANGER);

        newBtn.addActionListener(e -> mainFrame.navigate(Constants.Pages.REGISTER_COMPLAINT));
        detailBtn.addActionListener(e -> openSelectedDetail());
        refreshBtn.addActionListener(e -> refresh());
        deleteBtn.addActionListener(e -> deleteSelected());

        // Admins only: show delete
        deleteBtn.setVisible(SessionManager.isAdmin());

        bar.add(new JLabel("Filter: "));
        bar.add(filterStatusCombo);
        bar.add(filterPriorityCombo);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(searchBar);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(newBtn);
        bar.add(detailBtn);
        bar.add(refreshBtn);
        if (SessionManager.isAdmin()) bar.add(deleteBtn);

        return bar;
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void openSelectedDetail() {
        int row = table.getSelectedRow();
        if (row < 0) { showNoSelectionMessage(); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        mainFrame.showComplaintDetail(id);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showNoSelectionMessage(); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String num = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Permanently delete complaint " + num + "?\n\nThis action cannot be undone.",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            controller.deleteComplaint(id);
            refresh();
            mainFrame.setStatusText("Complaint " + num + " deleted.");
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        if (currentData == null) return;

        String statusFilter   = (String) filterStatusCombo.getSelectedItem();
        String priorityFilter = (String) filterPriorityCombo.getSelectedItem();
        String keyword        = searchBar.getText().trim().toLowerCase();

        boolean allStatus   = "All Statuses".equals(statusFilter);
        boolean allPriority = "All Priorities".equals(priorityFilter);

        tableModel.setRowCount(0);
        int count = 0;
        for (Complaint c : currentData) {
            boolean statusMatch   = allStatus   || c.getStatus().getDisplayName().equals(statusFilter);
            boolean priorityMatch = allPriority || c.getPriority().getDisplayName().equals(priorityFilter);
            boolean keywordMatch  = keyword.isEmpty()
                || c.getTitle().toLowerCase().contains(keyword)
                || c.getComplaintNumber().toLowerCase().contains(keyword)
                || (c.getLocation()   != null && c.getLocation().toLowerCase().contains(keyword))
                || (c.getDepartment() != null && c.getDepartment().toLowerCase().contains(keyword));

            if (statusMatch && priorityMatch && keywordMatch) {
                addRow(c);
                count++;
            }
        }
        countLabel.setText("Showing " + count + " of " + currentData.size() + " complaints");
    }

    private void showContextMenu(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) return;
        table.setRowSelectionInterval(row, row);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem open   = new JMenuItem("Open Detail");
        JMenuItem delete = new JMenuItem("Delete");

        open.addActionListener(a -> openSelectedDetail());
        delete.addActionListener(a -> deleteSelected());
        delete.setEnabled(SessionManager.isAdmin());

        menu.add(open);
        menu.addSeparator();
        menu.add(delete);
        menu.show(table, e.getX(), e.getY());
    }

    private void showNoSelectionMessage() {
        JOptionPane.showMessageDialog(this, "Please select a complaint first.",
            "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------
    // Refresh
    // ------------------------------------------------------------------

    public void refresh() {
        mainFrame.setStatusText("Loading complaints...");
        SwingWorker<List<Complaint>, Void> worker = new SwingWorker<>() {
            @Override protected List<Complaint> doInBackground() throws Exception {
                return controller.getAllComplaints();
            }
            @Override protected void done() {
                try {
                    currentData = get();
                    tableModel.setRowCount(0);
                    for (Complaint c : currentData) addRow(c);
                    countLabel.setText(currentData.size() + " complaints");
                    mainFrame.setStatusText("Loaded " + currentData.size() + " complaints.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ViewComplaintsPanel.this,
                        e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void addRow(Complaint c) {
        tableModel.addRow(new Object[]{
            c.getComplaintId(),
            c.getComplaintNumber(),
            c.getTitle().length() > 50 ? c.getTitle().substring(0, 50) + "…" : c.getTitle(),
            c.getCategory()  != null ? c.getCategory().getDisplayName()  : "",
            c.getPriority()  != null ? c.getPriority().getDisplayName()  : "",
            c.getStatus()    != null ? c.getStatus().getDisplayName()    : "",
            c.getAssignedToName(),
            DateUtil.formatDate(c.getDateCreated() != null ? c.getDateCreated().toLocalDate() : null)
        });
    }

    private void styleTable() {
        table.setFont(ThemeManager.getFontTable());
        table.setForeground(ThemeManager.getTextPrimary());
        table.setBackground(ThemeManager.getTableBackground());
        table.setGridColor(ThemeManager.getTableGrid());
        table.setRowHeight(Constants.UI.TABLE_ROW_HEIGHT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(ThemeManager.getTableSelection());
        table.setSelectionForeground(ThemeManager.getTextPrimary());
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(0);   // hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        table.getTableHeader().setFont(ThemeManager.getFontTableHeader());
        table.getTableHeader().setBackground(ThemeManager.getTableHeader());
        table.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(
            0, 0, 1, 0, ThemeManager.getTableGrid()));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(ThemeManager.getFontBody());
        combo.setBackground(ThemeManager.getInputBackground());
        combo.setForeground(ThemeManager.getTextPrimary());
    }
}
