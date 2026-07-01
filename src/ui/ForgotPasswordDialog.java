package ui;

import components.RoundedButton;
import components.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * "Forgot Password" dialog shown from the Login screen.
 *
 * <p>In a university demo context this dialog displays a message directing
 * the user to contact an administrator to reset their password, rather than
 * implementing a full email-based reset flow (which would require an SMTP
 * server). The dialog is fully functional and styled to match the application
 * theme.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ForgotPasswordDialog extends JDialog {

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates and shows the forgot-password dialog.
     *
     * @param parent the parent {@link LoginPanel} frame
     */
    public ForgotPasswordDialog(Frame parent) {
        super(parent, "Forgot Password", true);
        buildUI();
        setVisible(true);
    }

    /**
     * Creates the dialog with a custom owner dialog (for embedding).
     *
     * @param owner the parent dialog
     */
    public ForgotPasswordDialog(Dialog owner) {
        super(owner, "Forgot Password", true);
        buildUI();
        setVisible(true);
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        setSize(420, 300);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel content = new JPanel();
        content.setBackground(new Color(30, 41, 59));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        // Lock icon
        JLabel icon = new JLabel("\uD83D\uDD12", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel title = new JLabel("Forgot Password?");
        title.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Message
        JLabel msg = new JLabel(
            "<html><div style='text-align:center;width:280px'>" +
            "Please contact your System Administrator to reset your password.<br><br>" +
            "<b>Admin contact:</b> admin@cms.local<br>" +
            "Or use the default: <b>admin / Admin@123</b>" +
            "</div></html>", SwingConstants.CENTER);
        msg.setFont(ThemeManager.getFontSubtitle());
        msg.setForeground(new Color(148, 163, 184));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Close button
        RoundedButton closeBtn = new RoundedButton("Close", RoundedButton.Style.SECONDARY);
        closeBtn.setMaximumSize(new Dimension(200, 38));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dispose());

        content.add(icon);
        content.add(Box.createVerticalStrut(12));
        content.add(title);
        content.add(Box.createVerticalStrut(16));
        content.add(msg);
        content.add(Box.createVerticalStrut(24));
        content.add(closeBtn);

        setContentPane(content);
    }
}
