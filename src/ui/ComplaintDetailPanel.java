package ui;

import components.*;
import controller.ComplaintController;
import controller.UserController;
import exceptions.DatabaseException;
import model.*;
import util.Constants;
import util.DateUtil;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Detail view for a single complaint, providing:
 * <ul>
 *   <li>Read-only complaint metadata display</li>
 *   <li>Status update (employees and admins)</li>
 *   <li>Assign to employee (admins only)</li>
 *   <li>Status history table</li>
 * </ul>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ComplaintDetailPanel extends JPanel {

    private final ComplaintController complaintController;
    private final UserController      userController;
    private final MainFrame           mainFrame;

    private Complaint currentComplaint;

    // Labels for detail fields
    private JLabel lblNumber, lblTitle, lblStatus, lblPriority, lblCategory;
    private JLabel lblLocation, lblDept, lblCreatedBy, lblAssigned, lblDate;
    private JTextArea descArea, remarksArea;

    // Action controls
    private JComboBox<Status> statusCombo;
    private JComboBox<User>   assignCombo;
    private JTextField        remarksField;
    private JLabel            errorLabel;

    // History table
    private DefaultTableModel historyModel;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public ComplaintDetailPanel(ComplaintController complaintController,
                                UserController userController,
                                MainFrame mainFrame) {
        this.complaintController = complaintController;
        this.userController      = userController;
        this.mainFrame           = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getContentBackground());
        buildUI();
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        HeaderPanel header = new HeaderPanel("Complaint Detail");
        add(header, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);
        splitPane.setDividerLocation(620);
        splitPane.setResizeWeight(0.65);

        // LEFT: complaint info + actions
        splitPane.setLeftComponent(buildInfoPanel());
        // RIGHT: history
        splitPane.setRightComponent(buildHistoryPanel());

        add(splitPane, BorderLayout.CENTER);

        // Bottom toolbar
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    private JScrollPane buildInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_MD, ThemeManager.PADDING_LG, ThemeManager.PADDING_MD, ThemeManager.PADDING_MD));

        // -- Metadata card --
        RoundedPanel metaCard = new RoundedPanel();
        metaCard.setLayout(new GridLayout(0, 2, 8, 8));
        metaCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        metaCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        lblNumber   = makeValueLabel("—");
        lblTitle    = makeValueLabel("—");
        lblStatus   = makeValueLabel("—");
        lblPriority = makeValueLabel("—");
        lblCategory = makeValueLabel("—");
        lblLocation = makeValueLabel("—");
        lblDept     = makeValueLabel("—");
        lblCreatedBy= makeValueLabel("—");
        lblAssigned = makeValueLabel("—");
        lblDate     = makeValueLabel("—");

        metaCard.add(makeKeyLabel("Complaint No:")); metaCard.add(lblNumber);
        metaCard.add(makeKeyLabel("Status:"));       metaCard.add(lblStatus);
        metaCard.add(makeKeyLabel("Priority:"));     metaCard.add(lblPriority);
        metaCard.add(makeKeyLabel("Category:"));     metaCard.add(lblCategory);
        metaCard.add(makeKeyLabel("Department:"));   metaCard.add(lblDept);
        metaCard.add(makeKeyLabel("Location:"));     metaCard.add(lblLocation);
        metaCard.add(makeKeyLabel("Created By:"));   metaCard.add(lblCreatedBy);
        metaCard.add(makeKeyLabel("Assigned To:"));  metaCard.add(lblAssigned);
        metaCard.add(makeKeyLabel("Date Created:")); metaCard.add(lblDate);

        // Title
        lblTitle = makeValueLabel("—");
        lblTitle.setFont(ThemeManager.getFontH3());
        lblTitle.setForeground(ThemeManager.getTextPrimary());

        // Description
        JLabel descHeader = makeSectionHeader("Description");
        descArea = new JTextArea(4, 40);
        descArea.setEditable(false);
        descArea.setFont(ThemeManager.getFontBody());
        descArea.setForeground(ThemeManager.getTextPrimary());
        descArea.setBackground(ThemeManager.getInputBackground());
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder()),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        // Remarks
        JLabel remHeader = makeSectionHeader("Remarks");
        remarksArea = new JTextArea(3, 40);
        remarksArea.setEditable(false);
        remarksArea.setFont(ThemeManager.getFontBody());
        remarksArea.setForeground(ThemeManager.getTextPrimary());
        remarksArea.setBackground(ThemeManager.getInputBackground());
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder()),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        // -- Actions card (staff only) --
        boolean isStaff = SessionManager.isAdmin() || SessionManager.isEmployee();
        if (isStaff) {
            RoundedPanel actionsCard = buildActionsCard();
            actionsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

            errorLabel = new JLabel(" ");
            errorLabel.setFont(ThemeManager.getFontSmall());
            errorLabel.setForeground(ThemeManager.getDanger());

            panel.add(lblTitle);
            panel.add(Box.createVerticalStrut(8));
            panel.add(metaCard);
            panel.add(Box.createVerticalStrut(12));
            panel.add(descHeader);
            panel.add(new JScrollPane(descArea));
            panel.add(Box.createVerticalStrut(12));
            panel.add(remHeader);
            panel.add(new JScrollPane(remarksArea));
            panel.add(Box.createVerticalStrut(12));
            panel.add(makeSectionHeader("Actions"));
            panel.add(actionsCard);
            panel.add(errorLabel);
        } else {
            panel.add(lblTitle);
            panel.add(Box.createVerticalStrut(8));
            panel.add(metaCard);
            panel.add(Box.createVerticalStrut(12));
            panel.add(descHeader);
            panel.add(new JScrollPane(descArea));
            panel.add(Box.createVerticalStrut(12));
            panel.add(remHeader);
            panel.add(new JScrollPane(remarksArea));
        }

        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        return sp;
    }

    private RoundedPanel buildActionsCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        boolean isStaff = SessionManager.isAdmin() || SessionManager.isEmployee();

        if (isStaff) {
            // Status update
            gbc.gridx = 0; gbc.gridy = 0;
            card.add(makeKeyLabel("Update Status:"), gbc);

            statusCombo = new JComboBox<>(Status.values());
            gbc.gridx = 1;
            card.add(statusCombo, gbc);

            RoundedButton statusBtn = new RoundedButton("Update Status", RoundedButton.Style.PRIMARY);
            gbc.gridx = 2;
            card.add(statusBtn, gbc);
            statusBtn.addActionListener(e -> updateStatus());

            // Assign (admin only)
            if (SessionManager.isAdmin()) {
                gbc.gridx = 0; gbc.gridy = 1;
                card.add(makeKeyLabel("Assign To:"), gbc);

                assignCombo = new JComboBox<>();
                gbc.gridx = 1;
                card.add(assignCombo, gbc);

                RoundedButton assignBtn = new RoundedButton("Assign", RoundedButton.Style.SUCCESS);
                gbc.gridx = 2;
                card.add(assignBtn, gbc);
                assignBtn.addActionListener(e -> assignComplaint());
            }

            // Remarks for status change
            gbc.gridx = 0; gbc.gridy = 2;
            card.add(makeKeyLabel("Remarks:"), gbc);

            remarksField = new JTextField();
            remarksField.setFont(ThemeManager.getFontBody());
            remarksField.setBackground(ThemeManager.getInputBackground());
            remarksField.setForeground(ThemeManager.getTextPrimary());
            remarksField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getInputBorder()),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            gbc.gridx = 1; gbc.gridwidth = 2;
            card.add(remarksField, gbc);
        } else {
            // Citizen view: actions restricted
            JLabel lblNoActions = new JLabel("Status updates and assignments are restricted to staff.");
            lblNoActions.setFont(ThemeManager.getFontBody());
            lblNoActions.setForeground(ThemeManager.getTextSecondary());
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
            card.add(lblNoActions, gbc);
        }

        return card;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_MD, ThemeManager.PADDING_MD, ThemeManager.PADDING_MD, ThemeManager.PADDING_LG));

        JLabel header = makeSectionHeader("Status History");

        String[] cols = {"Changed By", "Old Status", "New Status", "Date", "Remarks"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyModel);
        historyTable.setFont(ThemeManager.getFontTable());
        historyTable.setForeground(ThemeManager.getTextPrimary());
        historyTable.setBackground(ThemeManager.getTableBackground());
        historyTable.setGridColor(ThemeManager.getTableGrid());
        historyTable.setRowHeight(26);
        historyTable.setShowHorizontalLines(true);
        historyTable.setShowVerticalLines(false);
        historyTable.setFillsViewportHeight(true);
        historyTable.getTableHeader().setFont(ThemeManager.getFontTableHeader());
        historyTable.getTableHeader().setBackground(ThemeManager.getTableHeader());
        historyTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());

        JScrollPane sp = new JScrollPane(historyTable);
        sp.setBorder(null);
        sp.getViewport().setBackground(ThemeManager.getTableBackground());

        panel.add(header, BorderLayout.NORTH);
        panel.add(sp,     BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.PADDING_MD, 8));
        bar.setBackground(ThemeManager.getHeaderBackground());
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getHeaderBorder()));

        RoundedButton backBtn = new RoundedButton("← Back to List", RoundedButton.Style.SECONDARY);
        backBtn.addActionListener(e -> mainFrame.navigate(Constants.Pages.VIEW_COMPLAINTS));

        bar.add(backBtn);
        return bar;
    }

    // ------------------------------------------------------------------
    // Load / Refresh
    // ------------------------------------------------------------------

    /**
     * Loads and displays the specified complaint.
     *
     * @param complaintId the ID of the complaint to display
     */
    public void loadComplaint(int complaintId) {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override protected Object[] doInBackground() throws Exception {
                Complaint c = complaintController.getComplaintById(complaintId);
                List<ComplaintHistory> hist = complaintController.getComplaintHistory(complaintId);
                List<User> employees = userController.getAllEmployees();
                return new Object[]{c, hist, employees};
            }
            @Override protected void done() {
                try {
                    Object[] result = get();
                    currentComplaint = (Complaint) result[0];
                    @SuppressWarnings("unchecked")
                    List<ComplaintHistory> hist = (List<ComplaintHistory>) result[1];
                    @SuppressWarnings("unchecked")
                    List<User> employees = (List<User>) result[2];
                    populateDetail(currentComplaint, hist, employees);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ComplaintDetailPanel.this,
                        e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateDetail(Complaint c, List<ComplaintHistory> hist, List<User> employees) {
        if (c == null) return;

        lblNumber.setText(c.getComplaintNumber());
        lblTitle.setText(c.getTitle());
        lblStatus.setText(c.getStatus() != null ? c.getStatus().getDisplayName() : "—");
        lblPriority.setText(c.getPriority() != null ? c.getPriority().getDisplayName() : "—");
        lblCategory.setText(c.getCategory() != null ? c.getCategory().getDisplayName() : "—");
        lblLocation.setText(c.getLocation() != null ? c.getLocation() : "—");
        lblDept.setText(c.getDepartment() != null ? c.getDepartment() : "—");
        lblCreatedBy.setText(c.getCreatedByName() != null ? c.getCreatedByName() : "—");
        lblAssigned.setText(c.getAssignedToName() != null ? c.getAssignedToName() : "Unassigned");
        lblDate.setText(DateUtil.formatDateTime(c.getDateCreated()));
        descArea.setText(c.getDescription());
        remarksArea.setText(c.getRemarks() != null ? c.getRemarks() : "");

        // Pre-select current status
        statusCombo.setSelectedItem(c.getStatus());

        // Populate assign combo
        if (assignCombo != null) {
            assignCombo.removeAllItems();
            for (User u : employees) assignCombo.addItem(u);
        }

        // History
        historyModel.setRowCount(0);
        for (ComplaintHistory h : hist) {
            historyModel.addRow(new Object[]{
                h.getChangedByName(),
                h.getOldStatus() != null ? h.getOldStatus().getDisplayName() : "—",
                h.getNewStatus().getDisplayName(),
                DateUtil.formatDateTime(h.getChangeDate()),
                h.getRemarks() != null ? h.getRemarks() : ""
            });
        }

        errorLabel.setText(" ");
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void updateStatus() {
        if (currentComplaint == null) return;
        Status newStatus = (Status) statusCombo.getSelectedItem();
        String remarks   = remarksField.getText().trim();
        try {
            complaintController.updateStatus(currentComplaint.getComplaintId(), newStatus, remarks);
            loadComplaint(currentComplaint.getComplaintId());
            mainFrame.setStatusText("Status updated to " + newStatus.getDisplayName());
        } catch (DatabaseException e) {
            errorLabel.setText("⚠  " + e.getMessage());
        }
    }

    private void assignComplaint() {
        if (currentComplaint == null || assignCombo == null) return;
        User selected = (User) assignCombo.getSelectedItem();
        if (selected == null) return;
        try {
            complaintController.assignComplaint(currentComplaint.getComplaintId(), selected.getId());
            loadComplaint(currentComplaint.getComplaintId());
            mainFrame.setStatusText("Assigned to " + selected.getFullName());
        } catch (DatabaseException e) {
            errorLabel.setText("⚠  " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private JLabel makeKeyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getFontBold());
        l.setForeground(ThemeManager.getTextSecondary());
        return l;
    }

    private JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getFontBody());
        l.setForeground(ThemeManager.getTextPrimary());
        return l;
    }

    private JLabel makeSectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getFontH3());
        l.setForeground(ThemeManager.getTextPrimary());
        l.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        return l;
    }
}
