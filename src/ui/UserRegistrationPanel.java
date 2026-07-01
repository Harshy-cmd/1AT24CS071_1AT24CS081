package ui;

import components.RoundedButton;
import components.ThemeManager;
import controller.CitizenController;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.Citizen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Registration panel form for Citizens in the dual-section login page.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class UserRegistrationPanel extends JPanel {

    private final LoginPanel parent;
    private final CitizenController citizenController;

    private JTextField txtFullName;
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtAddress;

    private JLabel errorLabel;
    private RoundedButton registerButton;

    public UserRegistrationPanel(LoginPanel parent) {
        this.parent = parent;
        this.citizenController = new CitizenController();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 24, 0, 24));
        buildUI();
    }

    private void buildUI() {
        // Title
        JLabel title = new JLabel("Register Citizen Account");
        title.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Create a new account to file complaints");
        subtitle.setFont(ThemeManager.getFontSubtitle());
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fields Setup
        txtFullName = makeTextField("Full Name");
        txtUsername = makeTextField("Username");
        txtEmail = makeTextField("Email Address");
        txtPhone = makeTextField("Phone Number");
        txtPassword = new JPasswordField();
        stylePasswordField(txtPassword);
        txtConfirmPassword = new JPasswordField();
        stylePasswordField(txtConfirmPassword);
        txtAddress = makeTextField("Address (Optional)");

        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.getFontSmall());
        errorLabel.setForeground(ThemeManager.getDanger());
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        registerButton = new RoundedButton("Register", RoundedButton.Style.SUCCESS);
        registerButton.addActionListener(e -> performRegistration());

        RoundedButton backButton = new RoundedButton("Back", RoundedButton.Style.SECONDARY);
        backButton.addActionListener(e -> {
            clearForm();
            parent.showLoginPanel();
        });

        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        // Assembly
        add(Box.createVerticalStrut(10));
        add(title);
        add(Box.createVerticalStrut(2));
        add(subtitle);
        add(Box.createVerticalStrut(16));

        // Form Fields (Scroll pane not needed if we keep components compact)
        add(makeFieldLabel("Full Name"));
        add(Box.createVerticalStrut(3));
        add(txtFullName);
        add(Box.createVerticalStrut(8));

        add(makeFieldLabel("Username"));
        add(Box.createVerticalStrut(3));
        add(txtUsername);
        add(Box.createVerticalStrut(8));

        add(makeFieldLabel("Email Address"));
        add(Box.createVerticalStrut(3));
        add(txtEmail);
        add(Box.createVerticalStrut(8));

        add(makeFieldLabel("Phone Number"));
        add(Box.createVerticalStrut(3));
        add(txtPhone);
        add(Box.createVerticalStrut(8));

        add(makeFieldLabel("Password"));
        add(Box.createVerticalStrut(3));
        add(txtPassword);
        add(Box.createVerticalStrut(8));

        add(makeFieldLabel("Confirm Password"));
        add(Box.createVerticalStrut(3));
        add(txtConfirmPassword);
        add(Box.createVerticalStrut(8));

        add(makeFieldLabel("Address (Optional)"));
        add(Box.createVerticalStrut(3));
        add(txtAddress);
        add(Box.createVerticalStrut(6));

        add(errorLabel);
        add(Box.createVerticalStrut(8));
        add(buttonPanel);
    }

    private void performRegistration() {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (fullName.equals("Full Name")) fullName = "";
        if (username.equals("Username")) username = "";
        if (email.equals("Email Address")) email = "";
        if (phone.equals("Phone Number")) phone = "";
        if (address.equals("Address (Optional)")) address = "";

        final String finalFullName = fullName;
        final String finalUsername = username;
        final String finalEmail = email;
        final String finalPhone = phone;
        final String finalAddress = address;

        errorLabel.setText(" ");
        registerButton.setEnabled(false);
        registerButton.repaint();

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                Citizen c = new Citizen(finalFullName, finalUsername, "", finalEmail, finalPhone);
                if (!finalAddress.isEmpty()) {
                    c.setAddress(finalAddress);
                }
                return citizenController.registerCitizen(c, password, confirmPassword);
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(parent,
                            "Account successfully registered! You can now log in.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Clear fields and switch back to user login panel
                    clearForm();
                    parent.showLoginPanel();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof ValidationException) {
                        errorLabel.setText("⚠  " + cause.getMessage());
                    } else if (cause instanceof DatabaseException) {
                        errorLabel.setText("⚠  Database error: " + cause.getMessage());
                    } else {
                        errorLabel.setText("⚠  Unexpected error during registration.");
                        cause.printStackTrace();
                    }
                }
            }
        };
        worker.execute();
    }

    private void clearForm() {
        txtFullName.setText("Full Name");
        txtFullName.setForeground(new Color(100, 116, 139));
        txtUsername.setText("Username");
        txtUsername.setForeground(new Color(100, 116, 139));
        txtEmail.setText("Email Address");
        txtEmail.setForeground(new Color(100, 116, 139));
        txtPhone.setText("Phone Number");
        txtPhone.setForeground(new Color(100, 116, 139));
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        txtAddress.setText("Address (Optional)");
        txtAddress.setForeground(new Color(100, 116, 139));
        errorLabel.setText(" ");
    }

    private JLabel makeFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.getFontBold());
        label.setForeground(new Color(203, 213, 225));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField field = new JTextField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        ThemeManager.RADIUS_MD, ThemeManager.RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(ThemeManager.getFontBody());
        field.setForeground(new Color(100, 116, 139));
        field.setCaretColor(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(100, 116, 139));
                }
            }
        });
        return field;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setEchoChar('•');
        field.setFont(ThemeManager.getFontBody());
        field.setForeground(new Color(148, 163, 184));
        field.setCaretColor(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
