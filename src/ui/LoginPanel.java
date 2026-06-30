package ui;

import components.*;
import controller.UserController;
import exceptions.AuthenticationException;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import model.User;
import util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Login screen for the Complaint Management System.
 *
 * <p>Presents the branded login form with username and password fields,
 * a "Show password" toggle, and a "Forgot Password" link. On success,
 * the authenticated user is stored in the session and
 * {@link MainFrame} is displayed. On failure, inline error messages are
 * shown without dismissing the form.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class LoginPanel extends JFrame {

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------

    private final UserController userController;

    private JTextField  usernameField;
    private JPasswordField passwordField;
    private JLabel      errorLabel;
    private JLabel      attemptsLabel;
    private RoundedButton loginButton;

    private int loginAttempts = 0;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Creates and shows the Login screen.
     */
    public LoginPanel() {
        this.userController = new UserController();
        buildUI();
        setVisible(true);
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        setTitle(Constants.App.WINDOW_TITLE + " — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);

        // Root panel: two columns
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        root.setBackground(new Color(15, 23, 42));

        // Left panel — branding
        root.add(buildBrandPanel());
        // Right panel — form
        root.add(buildFormPanel());

        setContentPane(root);
    }

    /** Builds the left branding panel with gradient and decorative elements. */
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42),
                        0, getHeight(), new Color(17, 44, 90));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles
                g2.setColor(new Color(37, 99, 235, 25));
                g2.fillOval(-80, -80, 350, 350);
                g2.setColor(new Color(37, 99, 235, 15));
                g2.fillOval(getWidth() - 160, getHeight() - 200, 320, 320);
                g2.setColor(new Color(37, 99, 235, 10));
                g2.fillOval(60, getHeight() / 2, 200, 200);

                // App icon
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
                g2.drawString("\uD83D\uDCCB", getWidth() / 2 - 45, 200);

                // App name
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 26));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String name = Constants.App.NAME;
                g2.drawString(name, (getWidth() - fm.stringWidth(name)) / 2, 255);

                // Tagline
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.PLAIN, 13));
                g2.setColor(new Color(148, 163, 184));
                fm = g2.getFontMetrics();
                String tag = "Register · Track · Resolve";
                g2.drawString(tag, (getWidth() - fm.stringWidth(tag)) / 2, 280);

                // Feature list
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.PLAIN, 12));
                String[] features = {
                    "✓  Register and track complaints",
                    "✓  Assign to responsible employees",
                    "✓  Full audit trail and history",
                    "✓  Export CSV and print reports",
                    "✓  Dashboard analytics"
                };
                int fy = 340;
                for (String feature : features) {
                    g2.setColor(new Color(148, 163, 184));
                    g2.drawString(feature, 60, fy);
                    fy += 28;
                }

                // Version
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.PLAIN, 10));
                g2.setColor(new Color(71, 85, 105));
                g2.drawString("Version " + Constants.App.VERSION, 60, getHeight() - 20);

                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(480, 640));
        return panel;
    }

    /** Builds the right form panel with username, password, and login button. */
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(30, 41, 59));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 48, 0, 48));
        form.setMaximumSize(new Dimension(420, 600));

        // Title
        JLabel title = new JLabel("Sign In");
        title.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your credentials to continue");
        subtitle.setFont(ThemeManager.getFontSubtitle());
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username field
        JLabel usernameLabel = makeFieldLabel("Username");
        usernameField = makeTextField("Enter username");

        // Password field
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

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.getFontSmall());
        errorLabel.setForeground(ThemeManager.getDanger());
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        attemptsLabel = new JLabel(" ");
        attemptsLabel.setFont(ThemeManager.getFontSmall());
        attemptsLabel.setForeground(ThemeManager.getWarning());
        attemptsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        loginButton = new RoundedButton("Sign In", RoundedButton.Style.PRIMARY);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        // Default button on Enter
        getRootPane().setDefaultButton(null);
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        };
        usernameField.addKeyListener(enterAdapter);
        passwordField.addKeyListener(enterAdapter);

        // Forgot password link
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linksPanel.setOpaque(false);
        linksPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel forgotLink = new JLabel("<html><u>Forgot Password?</u></html>");
        forgotLink.setFont(ThemeManager.getFontSmall());
        forgotLink.setForeground(new Color(96, 165, 250));
        forgotLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openForgotPassword(); }
        });
        linksPanel.add(forgotLink);

        // Default credentials hint
        JLabel hint = new JLabel("<html><div style='color:#64748B'>Demo: admin / Admin@123</div></html>");
        hint.setFont(ThemeManager.getFontSmall());
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble form
        form.add(Box.createVerticalStrut(60));
        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(36));
        form.add(usernameLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(16));
        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(6));
        form.add(showPass);
        form.add(Box.createVerticalStrut(8));
        form.add(errorLabel);
        form.add(attemptsLabel);
        form.add(Box.createVerticalStrut(20));
        form.add(loginButton);
        form.add(Box.createVerticalStrut(16));
        form.add(linksPanel);
        form.add(Box.createVerticalStrut(24));
        form.add(hint);

        outer.add(form, new GridBagConstraints());
        return outer;
    }

    // ------------------------------------------------------------------
    // Business logic
    // ------------------------------------------------------------------

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        clearErrors();
        loginButton.setEnabled(false);
        loginButton.repaint();

        // Run auth on background thread to avoid freezing UI
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userController.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    User user = get();
                    onLoginSuccess(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    loginAttempts++;
                    if (cause instanceof ValidationException) {
                        showError(cause.getMessage());
                    } else if (cause instanceof AuthenticationException) {
                        showError(cause.getMessage());
                        if (loginAttempts >= Constants.DB.MAX_LOGIN_ATTEMPTS) {
                            attemptsLabel.setText(
                                "⚠  " + loginAttempts + " failed attempts. Please verify your credentials.");
                        }
                    } else if (cause instanceof DatabaseException) {
                        showError("Database error: " + cause.getMessage());
                    } else {
                        showError("Unexpected error. Please try again.");
                    }
                }
            }
        };
        worker.execute();
    }

    private void onLoginSuccess(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame(user).setVisible(true));
    }

    private void openForgotPassword() {
        new ForgotPasswordDialog(this);
    }

    // ------------------------------------------------------------------
    // UI helpers
    // ------------------------------------------------------------------

    private void showError(String msg) {
        errorLabel.setText("⚠  " + msg);
    }

    private void clearErrors() {
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
        field.setForeground(new Color(148, 163, 184));
        field.setCaretColor(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeManager.INPUT_H + 4));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Placeholder behaviour
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.WHITE);
                }
            }
            @Override public void focusLost(FocusEvent e) {
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
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeManager.INPUT_H + 4));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
