package ui;

import components.*;
import controller.ComplaintController;
import model.*;
import util.Constants;
import util.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for registering a new complaint in the system.
 *
 * <p>Provides a form with all required complaint fields, input validation
 * (via the {@link util.Validator}), and a submit action that calls
 * {@link ComplaintController#registerComplaint(Complaint)}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class RegisterComplaintPanel extends JPanel {

    private final ComplaintController controller;
    private final MainFrame           mainFrame;

    // Form fields
    private JTextField     titleField;
    private JTextArea      descriptionArea;
    private JComboBox<ComplaintCategory> categoryCombo;
    private JComboBox<Priority>          priorityCombo;
    private JTextField     locationField;
    private JComboBox<String>            departmentCombo;
    private JTextArea      remarksArea;

    private JLabel errorLabel;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public RegisterComplaintPanel(ComplaintController controller, MainFrame mainFrame) {
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
        // Header
        add(new HeaderPanel("Register New Complaint"), BorderLayout.NORTH);

        // Main scroll area
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_XL,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_XL));

        JPanel form = buildForm();

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        content.add(form, new GridBagConstraints());
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));
        card.setPreferredSize(new Dimension(720, 620));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Form title
        JLabel formTitle = new JLabel("New Complaint Details");
        formTitle.setFont(ThemeManager.getFontH3());
        formTitle.setForeground(ThemeManager.getTextPrimary());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4; gbc.weightx = 1;
        card.add(formTitle, gbc);
        row++;

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeManager.getCardBorder());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        card.add(sep, gbc);
        row++;

        // Title field (full width)
        gbc.gridwidth = 4; gbc.gridx = 0; gbc.gridy = row;
        card.add(makeLabel("Complaint Title *"), gbc); row++;
        titleField = makeTextField("Enter a concise title for the complaint (min 5 chars)");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        card.add(titleField, gbc); row++;

        // Category | Priority (two columns)
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.5;
        card.add(makeLabel("Category *"), gbc);
        gbc.gridx = 2;
        card.add(makeLabel("Priority *"), gbc); row++;

        categoryCombo = new JComboBox<>(ComplaintCategory.values());
        styleCombo(categoryCombo);
        gbc.gridx = 0; gbc.gridy = row;
        card.add(categoryCombo, gbc);

        priorityCombo = new JComboBox<>(Priority.values());
        priorityCombo.setSelectedItem(Priority.MEDIUM);
        styleCombo(priorityCombo);
        gbc.gridx = 2;
        card.add(priorityCombo, gbc); row++;

        // Location | Department
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = row;
        card.add(makeLabel("Location"), gbc);
        gbc.gridx = 2;
        card.add(makeLabel("Department"), gbc); row++;

        locationField = makeTextField("Where did the issue occur?");
        gbc.gridx = 0; gbc.gridy = row;
        card.add(locationField, gbc);

        String[] depts = {"IT Department","Infrastructure","Water Supply","Electricity",
                          "Road Maintenance","Sanitation","Security","Health","Education","Other"};
        departmentCombo = new JComboBox<>(depts);
        styleCombo(departmentCombo);
        gbc.gridx = 2;
        card.add(departmentCombo, gbc); row++;

        // Description (full width, multi-line)
        gbc.gridwidth = 4; gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 1;
        card.add(makeLabel("Description *"), gbc); row++;

        descriptionArea = new JTextArea(5, 40);
        descriptionArea.setFont(ThemeManager.getFontBody());
        descriptionArea.setForeground(ThemeManager.getTextPrimary());
        descriptionArea.setBackground(ThemeManager.getInputBackground());
        descriptionArea.setCaretColor(ThemeManager.getTextPrimary());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        card.add(descScroll, gbc); row++;

        // Remarks
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        card.add(makeLabel("Remarks (optional)"), gbc); row++;
        remarksArea = new JTextArea(3, 40);
        remarksArea.setFont(ThemeManager.getFontBody());
        remarksArea.setForeground(ThemeManager.getTextPrimary());
        remarksArea.setBackground(ThemeManager.getInputBackground());
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane remScroll = new JScrollPane(remarksArea);
        remScroll.setBorder(null);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        card.add(remScroll, gbc); row++;

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.getFontSmall());
        errorLabel.setForeground(ThemeManager.getDanger());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        card.add(errorLabel, gbc); row++;

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
        RoundedButton submitBtn = new RoundedButton("Submit Complaint", RoundedButton.Style.PRIMARY);

        cancelBtn.addActionListener(e -> {
            resetForm();
            mainFrame.navigate(Constants.Pages.VIEW_COMPLAINTS);
        });
        submitBtn.addActionListener(e -> submitComplaint());

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        card.add(btnPanel, gbc);

        return card;
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void submitComplaint() {
        errorLabel.setText(" ");

        Complaint complaint = new Complaint(
            titleField.getText().trim(),
            descriptionArea.getText().trim(),
            (ComplaintCategory) categoryCombo.getSelectedItem(),
            (Priority)          priorityCombo.getSelectedItem(),
            locationField.getText().trim(),
            (String)            departmentCombo.getSelectedItem(),
            SessionManager.getCurrentUserId()
        );
        complaint.setRemarks(remarksArea.getText().trim());

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override protected Integer doInBackground() throws Exception {
                return controller.registerComplaint(complaint);
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(RegisterComplaintPanel.this,
                        Constants.Messages.COMPLAINT_REGISTERED +
                        "\nComplaint Number: " + complaint.getComplaintNumber(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    resetForm();
                    mainFrame.navigate(Constants.Pages.VIEW_COMPLAINTS);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    errorLabel.setText("⚠  " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    /** Resets all form fields to their default state. */
    public void resetForm() {
        titleField.setText("");
        descriptionArea.setText("");
        remarksArea.setText("");
        locationField.setText("");
        categoryCombo.setSelectedIndex(0);
        priorityCombo.setSelectedItem(Priority.MEDIUM);
        departmentCombo.setSelectedIndex(0);
        errorLabel.setText(" ");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.getFontBold());
        label.setForeground(ThemeManager.getTextSecondary());
        return label;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(ThemeManager.getFontBody());
        field.setForeground(ThemeManager.getTextPrimary());
        field.setBackground(ThemeManager.getInputBackground());
        field.setCaretColor(ThemeManager.getTextPrimary());
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        field.setToolTipText(placeholder);
        return field;
    }

    private <T> void styleCombo(JComboBox<T> combo) {
        combo.setFont(ThemeManager.getFontBody());
        combo.setBackground(ThemeManager.getInputBackground());
        combo.setForeground(ThemeManager.getTextPrimary());
        combo.setBorder(BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true));
    }
}
