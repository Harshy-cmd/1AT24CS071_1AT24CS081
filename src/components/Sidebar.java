package components;

import util.Constants;
import util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The left-side navigation sidebar for the main application window.
 *
 * <p>Renders the app logo/title, a list of navigation items, and a logout
 * button at the bottom. Each navigation item highlights on hover and
 * maintains a "selected" state that visually indicates the current page.</p>
 *
 * <p>Navigation items notify the application of page changes through the
 * {@link NavigationListener} callback interface, keeping the sidebar
 * decoupled from {@link ui.MainFrame}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class Sidebar extends JPanel {

    // ------------------------------------------------------------------
    // Callback interface (Loose Coupling)
    // ------------------------------------------------------------------

    /**
     * Callback interface that {@link ui.MainFrame} implements to be notified
     * when the user selects a navigation item.
     */
    public interface NavigationListener {
        /** Called when a sidebar item is clicked. */
        void onNavigate(String pageName);
        /** Called when the logout button is clicked. */
        void onLogout();
    }

    // ------------------------------------------------------------------
    // Inner class — NavItem
    // ------------------------------------------------------------------

    /** A single navigation item in the sidebar. */
    private static class NavItem extends JPanel {
        private final String pageName;
        private final String label;
        private final String icon;
        private boolean selected = false;
        private boolean hovered  = false;

        NavItem(String icon, String label, String pageName, NavigationListener listener) {
            super(new FlowLayout(FlowLayout.LEFT, 12, 10));
            this.pageName = pageName;
            this.label    = label;
            this.icon     = icon;
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(ThemeManager.PADDING_LG * 2 + 180, 44));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { listener.onNavigate(pageName); }
            });
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            if (selected) {
                g2.setColor(ThemeManager.getSidebarSelected());
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4,
                        ThemeManager.RADIUS_MD, ThemeManager.RADIUS_MD);
            } else if (hovered) {
                g2.setColor(ThemeManager.getSidebarHover());
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4,
                        ThemeManager.RADIUS_MD, ThemeManager.RADIUS_MD);
            }

            // Selected left indicator
            if (selected) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(8, (getHeight() - 20) / 2, 3, 20, 3, 3);
            }

            // Icon
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            g2.setColor(selected ? ThemeManager.getSidebarTextSelected() : ThemeManager.getSidebarText());
            g2.drawString(icon, 20, getHeight() / 2 + 6);

            // Label
            g2.setFont(selected ? ThemeManager.getFontSidebarBold() : ThemeManager.getFontSidebar());
            g2.drawString(label, 44, getHeight() / 2 + 5);

            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------

    private final NavigationListener listener;
    private NavItem[] navItems;
    private String    currentPage = Constants.Pages.DASHBOARD;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Builds the sidebar with all navigation items.
     *
     * @param listener the main frame that handles navigation and logout
     */
    public Sidebar(NavigationListener listener) {
        super(new BorderLayout());
        this.listener = listener;

        setBackground(ThemeManager.getSidebarBackground());
        setPreferredSize(new Dimension(Constants.UI.SIDEBAR_WIDTH, 0));

        // Logo / brand panel
        JPanel logoPanel = buildLogoPanel();

        // Navigation items panel
        JPanel navPanel = buildNavPanel();

        // Bottom: user info + logout
        JPanel bottomPanel = buildBottomPanel();

        // Right-side border
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                ThemeManager.getSidebarBorder()));

        add(logoPanel,  BorderLayout.NORTH);
        add(navPanel,   BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Highlights the nav item corresponding to the given page name and
     * deselects all others.
     *
     * @param pageName the {@link Constants.Pages} key of the active page
     */
    public void setActivePage(String pageName) {
        currentPage = pageName;
        for (NavItem item : navItems) {
            item.setSelected(pageName.equals(item.pageName));
        }
    }

    // ------------------------------------------------------------------
    // Build helpers
    // ------------------------------------------------------------------

    private JPanel buildLogoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(Constants.UI.SIDEBAR_WIDTH, 70));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                ThemeManager.getSidebarBorder()));

        JLabel logo = new JLabel("\uD83D\uDCCB"); // 📋
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        logo.setForeground(ThemeManager.getSidebarLogo());

        JLabel title = new JLabel("<html><b style='font-size:13px'>" +
                Constants.App.SHORT_NAME + "</b><br>" +
                "<span style='color:#94A3B8;font-size:10px'>Management System</span></html>");
        title.setFont(ThemeManager.getFontLogo());
        title.setForeground(Color.WHITE);

        panel.add(logo);
        panel.add(title);
        return panel;
    }

    private JPanel buildNavPanel() {
        boolean isAdmin   = SessionManager.isAdmin();
        boolean isCitizen = SessionManager.isCitizen();

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Define all navigation items with dynamic visibility based on roles
        Object[][] items = {
            {"\uD83D\uDCCA", "Dashboard",          Constants.Pages.DASHBOARD,          true},
            {"\u2795",        "New Complaint",      Constants.Pages.REGISTER_COMPLAINT,  isAdmin || isCitizen},
            {"\uD83D\uDCCB", "View Complaints",    Constants.Pages.VIEW_COMPLAINTS,     true},
            {"\uD83D\uDD0D", "Search",             Constants.Pages.SEARCH_COMPLAINTS,   true},
            {"\uD83D\uDCCA", "Reports",            Constants.Pages.REPORTS,             isAdmin},
            {"\uD83D\uDC64", "My Profile",         Constants.Pages.PROFILE,             true},
            {"\u2699",        "Settings",           Constants.Pages.SETTINGS,            true},
            {"❓",            "About",              Constants.Pages.ABOUT,               true},
        };

        int count = 0;
        for (Object[] item : items) {
            if (!(boolean) item[3]) continue;
            count++;
        }
        navItems = new NavItem[count];

        int idx = 0;
        for (Object[] item : items) {
            if (!(boolean) item[3]) continue;
            NavItem ni = new NavItem(
                (String) item[0], (String) item[1], (String) item[2], listener);
            navItems[idx++] = ni;
            panel.add(ni);
        }

        // Set dashboard as default selected
        if (navItems.length > 0) navItems[0].setSelected(true);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getSidebarBorder()),
            BorderFactory.createEmptyBorder(12, 16, 16, 16)));

        // User info row
        String name = SessionManager.getCurrentUserName();
        String role = SessionManager.getCurrentUserRole();
        JLabel userLabel = new JLabel("<html><b>" + name + "</b><br>" +
                "<span style='color:#94A3B8'>" + role + "</span></html>");
        userLabel.setFont(ThemeManager.getFontSmall());
        userLabel.setForeground(Color.WHITE);

        // Logout button
        RoundedButton logoutBtn = new RoundedButton("Logout", RoundedButton.Style.DANGER);
        logoutBtn.setPreferredSize(new Dimension(Constants.UI.SIDEBAR_WIDTH - 32, 34));
        logoutBtn.addActionListener(e -> listener.onLogout());

        panel.add(userLabel,  BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        panel.add(logoutBtn,  BorderLayout.SOUTH);
        return panel;
    }
}
