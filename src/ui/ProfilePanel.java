package ui;

import components.*;
import controller.UserController;
import exceptions.AuthenticationException;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.User;
import util.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Profile panel for viewing and editing the current user's information.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ProfilePanel extends JPanel {

    private final UserController userController;

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField departmentField;
    private JLabel     usernameLabel;
    private JLabel     roleLabel;
    private JLabel     errorLabel;

    // Password change fields
    private JPasswordField oldPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;
    private JLabel         passErrorLabel;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public ProfilePanel(UserController userController) {
        this.userController = userController;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getContentBackground());
        buildUI();
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        add(new HeaderPanel("My Profile"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, ThemeManager.PADDING_LG, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        content.add(buildProfileCard());
        content.add(buildPasswordCard());

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildProfileCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Profile Information");
        title.setFont(ThemeManager.getFontH3());
        title.setForeground(ThemeManager.getTextPrimary());
        title.setAlignmentX(LEFT_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 12));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);

        usernameLabel = new JLabel("—");
        usernameLabel.setFont(ThemeManager.getFontBody());
        usernameLabel.setForeground(ThemeManager.getTextMuted());

        roleLabel = new JLabel("—");
        roleLabel.setFont(ThemeManager.getFontBody());
        roleLabel.setForeground(ThemeManager.getTextMuted());

        fullNameField    = makeEditableField();
        emailField       = makeEditableField();
        phoneField       = makeEditableField();
        departmentField  = makeEditableField();

        grid.add(makeKeyLabel("Username:"));    grid.add(usernameLabel);
        grid.add(makeKeyLabel("Role:"));        grid.add(roleLabel);
        grid.add(makeKeyLabel("Full Name:"));   grid.add(fullNameField);
        grid.add(makeKeyLabel("Email:"));       grid.add(emailField);
        grid.add(makeKeyLabel("Phone:"));       grid.add(phoneField);
        grid.add(makeKeyLabel("Department:"));  grid.add(departmentField);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.getFontSmall());
        errorLabel.setForeground(ThemeManager.getDanger());
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);

        RoundedButton saveBtn = new RoundedButton("Save Changes", RoundedButton.Style.PRIMARY);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> saveProfile());

        card.add(title);
        card.add(Box.createVerticalStrut(16));
        card.add(grid);
        card.add(Box.createVerticalStrut(12));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(saveBtn);
        return card;
    }

    private JPanel buildPasswordCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Change Password");
        title.setFont(ThemeManager.getFontH3());
        title.setForeground(ThemeManager.getTextPrimary());
        title.setAlignmentX(LEFT_ALIGNMENT);

        oldPassField     = makePasswordField();
        newPassField     = makePasswordField();
        confirmPassField = makePasswordField();

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 12));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.add(makeKeyLabel("Current Password:"));     grid.add(oldPassField);
        grid.add(makeKeyLabel("New Password:"));         grid.add(newPassField);
        grid.add(makeKeyLabel("Confirm New Password:")); grid.add(confirmPassField);

        passErrorLabel = new JLabel(" ");
        passErrorLabel.setFont(ThemeManager.getFontSmall());
        passErrorLabel.setForeground(ThemeManager.getDanger());
        passErrorLabel.setAlignmentX(LEFT_ALIGNMENT);

        RoundedButton changeBtn = new RoundedButton("Change Password", RoundedButton.Style.WARNING);
        changeBtn.setAlignmentX(LEFT_ALIGNMENT);
        changeBtn.addActionListener(e -> changePassword());

        JLabel hint = new JLabel("<html><div style='color:#64748B'>Password must be at least 8 characters.</div></html>");
        hint.setFont(ThemeManager.getFontSmall());
        hint.setAlignmentX(LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(16));
        card.add(grid);
        card.add(Box.createVerticalStrut(8));
        card.add(passErrorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(changeBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(hint);
        return card;
    }

    // ------------------------------------------------------------------
    // Refresh
    // ------------------------------------------------------------------

    public void refresh() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        usernameLabel.setText(user.getUsername());
        roleLabel.setText(user.getRole());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        departmentField.setText(user.getDepartment() != null ? user.getDepartment() : "");
        errorLabel.setText(" ");
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void saveProfile() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        user.setFullName(fullNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());
        user.setDepartment(departmentField.getText().trim());

        try {
            userController.updateProfile(user);
            errorLabel.setForeground(ThemeManager.getSuccess());
            errorLabel.setText("✓  Profile updated successfully.");
        } catch (ValidationException | DatabaseException e) {
            errorLabel.setForeground(ThemeManager.getDanger());
            errorLabel.setText("⚠  " + e.getMessage());
        }
    }

    private void changePassword() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String old     = new String(oldPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        try {
            userController.changePassword(user.getId(), old, newPass, confirm);
            passErrorLabel.setForeground(ThemeManager.getSuccess());
            passErrorLabel.setText("✓  Password changed successfully.");
            oldPassField.setText("");
            newPassField.setText("");
            confirmPassField.setText("");
        } catch (ValidationException | AuthenticationException | DatabaseException e) {
            passErrorLabel.setForeground(ThemeManager.getDanger());
            passErrorLabel.setText("⚠  " + e.getMessage());
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

    private JTextField makeEditableField() {
        JTextField field = new JTextField();
        field.setFont(ThemeManager.getFontBody());
        field.setForeground(ThemeManager.getTextPrimary());
        field.setBackground(ThemeManager.getInputBackground());
        field.setCaretColor(ThemeManager.getTextPrimary());
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private JPasswordField makePasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(ThemeManager.getFontBody());
        field.setForeground(ThemeManager.getTextPrimary());
        field.setBackground(ThemeManager.getInputBackground());
        field.setCaretColor(ThemeManager.getTextPrimary());
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }
}
