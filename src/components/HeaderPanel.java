package components;

import util.Constants;
import util.DateUtil;
import util.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Top header bar displayed above the main content area in the application.
 *
 * <p>Contains the current page title, the logged-in user's name and role,
 * the current date/time, and an optional status bar at the very bottom of
 * the application window.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class HeaderPanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel dateLabel;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a header with the given page title.
     *
     * @param pageTitle the text shown as the current page name
     */
    public HeaderPanel(String pageTitle) {
        this(pageTitle, true);
    }

    /**
     * Creates a header with the given page title and optional subtitle.
     *
     * @param pageTitle     the text shown as the current page name
     * @param showUserInfo  whether to show the user greeting on the right
     */
    public HeaderPanel(String pageTitle, boolean showUserInfo) {
        super(new BorderLayout());
        setBackground(ThemeManager.getHeaderBackground());
        setPreferredSize(new Dimension(0, Constants.UI.HEADER_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getHeaderBorder()),
            BorderFactory.createEmptyBorder(0, ThemeManager.PADDING_LG, 0, ThemeManager.PADDING_LG)));

        // Left side — page title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        titleLabel = new JLabel(pageTitle);
        titleLabel.setFont(ThemeManager.getFontH2());
        titleLabel.setForeground(ThemeManager.getTextPrimary());
        leftPanel.add(titleLabel);

        // Right side — date + user info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setOpaque(false);

        dateLabel = new JLabel(DateUtil.todayDisplay());
        dateLabel.setFont(ThemeManager.getFontSubtitle());
        dateLabel.setForeground(ThemeManager.getTextSecondary());

        rightPanel.add(dateLabel);

        if (showUserInfo) {
            JLabel separator = new JLabel("|");
            separator.setForeground(ThemeManager.getTextMuted());
            rightPanel.add(separator);

            String userName = SessionManager.getCurrentUserName();
            String role     = SessionManager.getCurrentUserRole();
            JLabel userLabel = new JLabel("<html><b>" + userName + "</b> &nbsp;" +
                    "<span style='color:#94A3B8'>" + role + "</span></html>");
            userLabel.setFont(ThemeManager.getFontSubtitle());
            userLabel.setForeground(ThemeManager.getTextPrimary());
            rightPanel.add(userLabel);
        }

        add(leftPanel,  BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Updates the page title displayed in the header.
     *
     * @param title the new page title
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Refreshes the date label with the current date.
     */
    public void refreshDate() {
        dateLabel.setText(DateUtil.todayDisplay());
    }
}
