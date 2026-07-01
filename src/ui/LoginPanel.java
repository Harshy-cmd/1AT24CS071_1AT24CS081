package ui;

import components.*;
import controller.UserController;
import exceptions.AuthenticationException;
import model.User;
import util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Modern dual-section login page of the Complaint Management System.
 *
 * @author  CMS Development Team
 * @version 1.1.0
 * @since   2024
 */
public class LoginPanel extends JFrame {

    private final UserController userController;

    // Admin / Employee Portal fields
    private JTextField adminUsernameField;
    private JPasswordField adminPasswordField;
    private JLabel adminErrorLabel;
    private JLabel adminAttemptsLabel;
    private RoundedButton adminLoginButton;

    // Citizen Portal card layout
    private CardLayout citizenCardLayout;
    private JPanel citizenCardPanel;

    private int loginAttempts = 0;

    public LoginPanel() {
        this.userController = new UserController();
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        setTitle(Constants.App.WINDOW_TITLE + " — Dual Portal Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 800);
        setLocationRelativeTo(null);
        setResizable(false);

        // Root container with gradient background
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42),
                        0, getHeight(), new Color(17, 44, 90));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Branded circles
                g2.setColor(new Color(37, 99, 235, 15));
                g2.fillOval(-100, -100, 400, 400);
                g2.setColor(new Color(37, 99, 235, 10));
                g2.fillOval(getWidth() - 300, getHeight() - 300, 450, 450);
                g2.dispose();
            }
        };

        // Header Section (Branding)
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(30, 20, 10, 20));

        JLabel logoLabel = new JLabel("\uD83D\uDCCB");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(Constants.App.NAME);
        titleLabel.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Enterprise Resolution Portal — Select your portal to proceed");
        subtitleLabel.setFont(ThemeManager.getFontSubtitle());
        subtitleLabel.setForeground(new Color(148, 163, 184));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);

        // Body: Side-by-side cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(20, 50, 40, 50));

        // 1. Admin/Employee card
        cardsPanel.add(buildAdminCard());

        // 2. Citizen card container (toggles Login & Registration)
        cardsPanel.add(buildCitizenCardContainer());

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(cardsPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildAdminCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(0, 24, 0, 24));

        // Card Header
        JLabel title = new JLabel("Admin & Staff Portal");
        title.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("For internal administrators and employees");
        desc.setFont(ThemeManager.getFontSubtitle());
        desc.setForeground(new Color(148, 163, 184));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fields
        JLabel usernameLabel = makeFieldLabel("Username");
        adminUsernameField = makeTextField("Enter username");

        JLabel passwordLabel = makeFieldLabel("Password");
        adminPasswordField = new JPasswordField();
        stylePasswordField(adminPasswordField, "Enter password");

        // Show password checkbox
        JCheckBox showPass = new JCheckBox("Show password");
        showPass.setFont(ThemeManager.getFontSmall());
        showPass.setForeground(new Color(148, 163, 184));
        showPass.setOpaque(false);
        showPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPass.addActionListener(e ->
                adminPasswordField.setEchoChar(showPass.isSelected() ? '\0' : '•'));

        // Errors & Attempts
        adminErrorLabel = new JLabel(" ");
        adminErrorLabel.setFont(ThemeManager.getFontSmall());
        adminErrorLabel.setForeground(ThemeManager.getDanger());
        adminErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        adminAttemptsLabel = new JLabel(" ");
        adminAttemptsLabel.setFont(ThemeManager.getFontSmall());
        adminAttemptsLabel.setForeground(ThemeManager.getWarning());
        adminAttemptsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sign In button
        adminLoginButton = new RoundedButton("Sign In", RoundedButton.Style.PRIMARY);
        adminLoginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        adminLoginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        adminLoginButton.addActionListener(e -> handleAdminLogin());

        // Keyboard support
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleAdminLogin();
            }
        };
        adminUsernameField.addKeyListener(enterAdapter);
        adminPasswordField.addKeyListener(enterAdapter);

        // Forgot password Link
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

        // Demo credentials hint
        JLabel hint = new JLabel("<html><div style='color:#64748B'>Demo: admin / Admin@123</div></html>");
        hint.setFont(ThemeManager.getFontSmall());
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Build Inner
        inner.add(Box.createVerticalStrut(20));
        inner.add(title);
        inner.add(Box.createVerticalStrut(4));
        inner.add(desc);
        inner.add(Box.createVerticalStrut(24));
        inner.add(usernameLabel);
        inner.add(Box.createVerticalStrut(6));
        inner.add(adminUsernameField);
        inner.add(Box.createVerticalStrut(14));
        inner.add(passwordLabel);
        inner.add(Box.createVerticalStrut(6));
        inner.add(adminPasswordField);
        inner.add(Box.createVerticalStrut(6));
        inner.add(showPass);
        inner.add(Box.createVerticalStrut(6));
        inner.add(adminErrorLabel);
        inner.add(adminAttemptsLabel);
        inner.add(Box.createVerticalStrut(12));
        inner.add(adminLoginButton);
        inner.add(Box.createVerticalStrut(14));
        inner.add(linksPanel);
        inner.add(Box.createVerticalStrut(14));
        inner.add(hint);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCitizenCardContainer() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());

        citizenCardLayout = new CardLayout();
        citizenCardPanel = new JPanel(citizenCardLayout);
        citizenCardPanel.setOpaque(false);

        UserLoginPanel userLogin = new UserLoginPanel(this);
        UserRegistrationPanel userRegister = new UserRegistrationPanel(this);

        citizenCardPanel.add(userLogin, "login");

        JScrollPane scrollPane = new JScrollPane(userRegister);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        citizenCardPanel.add(scrollPane, "register");

        card.add(citizenCardPanel, BorderLayout.CENTER);

        // Default: Show Citizen login first
        citizenCardLayout.show(citizenCardPanel, "login");

        return card;
    }

    public void showRegistrationPanel() {
        citizenCardLayout.show(citizenCardPanel, "register");
    }

    public void showLoginPanel() {
        citizenCardLayout.show(citizenCardPanel, "login");
    }

    private void handleAdminLogin() {
        String username = adminUsernameField.getText().trim();
        String password = new String(adminPasswordField.getPassword());
        if (username.equals("Enter username")) username = "";
        
        performLogin(username, password, adminErrorLabel, adminAttemptsLabel, adminLoginButton);
    }

    /**
     * Parameterized login execution used by both Admin and Citizen portals.
     */
    public void performLogin(String username, String password, JLabel targetErrorLabel, JLabel targetAttemptsLabel, RoundedButton targetLoginBtn) {
        targetErrorLabel.setText(" ");
        targetLoginBtn.setEnabled(false);
        targetLoginBtn.repaint();

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userController.login(username, password);
            }

            @Override
            protected void done() {
                targetLoginBtn.setEnabled(true);
                try {
                    User user = get();
                    onLoginSuccess(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    loginAttempts++;
                    String msg = cause.getMessage();
                    targetErrorLabel.setText("⚠  " + msg);
                    if (cause instanceof AuthenticationException) {
                        if (loginAttempts >= Constants.DB.MAX_LOGIN_ATTEMPTS) {
                            targetAttemptsLabel.setText(
                                    "⚠  " + loginAttempts + " failed attempts. Verify credentials.");
                        }
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

    // Common styling helpers
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
