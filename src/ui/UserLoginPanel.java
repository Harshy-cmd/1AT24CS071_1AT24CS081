package ui;

import components.RoundedButton;
import components.ThemeManager;
import util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login panel card for Citizens in the dual-section login page.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class UserLoginPanel extends JPanel {

    private final LoginPanel parent;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JLabel attemptsLabel;
    private RoundedButton loginButton;

    public UserLoginPanel(LoginPanel parent) {
        this.parent = parent;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 24, 0, 24));
        buildUI();
    }

    private void buildUI() {
        // Title & Description
        JLabel title = new JLabel("Citizen Portal");
        title.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to submit and track complaints");
        subtitle.setFont(ThemeManager.getFontSubtitle());
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fields
        JLabel usernameLabel = makeFieldLabel("Username");
        usernameField = makeTextField("Enter username");

        JLabel passwordLabel = makeFieldLabel("Password");
        passwordField = new JPasswordField();
        stylePasswordField(passwordField, "Enter password");

        // Show password toggle
        JCheckBox showPass = new JCheckBox("Show password");
        showPass.setFont(ThemeManager.getFontSmall());
        showPass.setForeground(new Color(148, 163, 184));
        showPass.setOpaque(false);
        showPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPass.addActionListener(e ->
                passwordField.setEchoChar(showPass.isSelected() ? '\0' : '•'));

        // Error & Attempts Labels
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.getFontSmall());
        errorLabel.setForeground(ThemeManager.getDanger());
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        attemptsLabel = new JLabel(" ");
        attemptsLabel.setFont(ThemeManager.getFontSmall());
        attemptsLabel.setForeground(ThemeManager.getWarning());
        attemptsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login Button
        loginButton = new RoundedButton("Sign In", RoundedButton.Style.PRIMARY);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        // Keyboard navigation
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        };
        usernameField.addKeyListener(enterAdapter);
        passwordField.addKeyListener(enterAdapter);

        // Switch to registration link
        JPanel registerLinkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        registerLinkPanel.setOpaque(false);
        registerLinkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel registerPrompt = new JLabel("New user? ");
        registerPrompt.setFont(ThemeManager.getFontSmall());
        registerPrompt.setForeground(new Color(148, 163, 184));

        JLabel registerLink = new JLabel("<html><u>Register New Account</u></html>");
        registerLink.setFont(ThemeManager.getFontSmall());
        registerLink.setForeground(new Color(96, 165, 250));
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parent.showRegistrationPanel();
            }
        });

        registerLinkPanel.add(registerPrompt);
        registerLinkPanel.add(registerLink);

        // Demo credentials hint
        JLabel hint = new JLabel("<html><div style='color:#64748B'>Demo: citizen / Citizen@123</div></html>");
        hint.setFont(ThemeManager.getFontSmall());
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assembly
        add(Box.createVerticalStrut(20));
        add(title);
        add(Box.createVerticalStrut(4));
        add(subtitle);
        add(Box.createVerticalStrut(24));
        add(usernameLabel);
        add(Box.createVerticalStrut(6));
        add(usernameField);
        add(Box.createVerticalStrut(14));
        add(passwordLabel);
        add(Box.createVerticalStrut(6));
        add(passwordField);
        add(Box.createVerticalStrut(6));
        add(showPass);
        add(Box.createVerticalStrut(6));
        add(errorLabel);
        add(attemptsLabel);
        add(Box.createVerticalStrut(12));
        add(loginButton);
        add(Box.createVerticalStrut(14));
        add(registerLinkPanel);
        add(Box.createVerticalStrut(14));
        add(hint);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.equals("Enter username")) username = "";
        parent.performLogin(username, password, errorLabel, attemptsLabel, loginButton);
    }

    // Styling helpers to keep UI consistent
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
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeManager.INPUT_H));
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

    private void stylePasswordField(JPasswordField field, String placeholder) {
        field.setEchoChar('•');
        field.setFont(ThemeManager.getFontBody());
        field.setForeground(new Color(148, 163, 184));
        field.setCaretColor(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeManager.INPUT_H));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
