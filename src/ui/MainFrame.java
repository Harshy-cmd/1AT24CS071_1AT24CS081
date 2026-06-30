package ui;

import components.*;
import controller.ComplaintController;
import controller.ReportController;
import controller.UserController;
import model.User;
import util.Constants;
import util.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * The main application window shown after a successful login.
 *
 * <p>Uses a {@link BorderLayout} with:
 * <ul>
 *   <li>WEST  — {@link Sidebar} navigation</li>
 *   <li>CENTER — {@link CardLayout} content area hosting all page panels</li>
 * </ul>
 * </p>
 *
 * <p>Page navigation is driven by the {@link Sidebar.NavigationListener}
 * interface: the sidebar notifies this frame when an item is clicked, and
 * this frame swaps the visible card accordingly.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class MainFrame extends JFrame implements Sidebar.NavigationListener {

    // ------------------------------------------------------------------
    // Controllers (shared across all panels)
    // ------------------------------------------------------------------

    private final ComplaintController complaintController;
    private final UserController      userController;
    private final ReportController    reportController;

    // ------------------------------------------------------------------
    // UI Components
    // ------------------------------------------------------------------

    private final CardLayout    cardLayout;
    private final JPanel        contentArea;
    private final Sidebar       sidebar;
    private final JLabel        statusLabel;

    // Page references (kept for refresh calls)
    private DashboardPanel         dashboardPanel;
    private ViewComplaintsPanel    viewComplaintsPanel;
    private SearchComplaintsPanel  searchPanel;
    private RegisterComplaintPanel registerPanel;
    private ComplaintDetailPanel   detailPanel;
    private ReportsPanel           reportsPanel;
    private ProfilePanel           profilePanel;
    private SettingsPanel          settingsPanel;
    private AboutPanel             aboutPanel;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Constructs and shows the main application frame for the given user.
     *
     * @param user the authenticated user (Admin or Employee subclass)
     */
    public MainFrame(User user) {
        this.complaintController = new ComplaintController();
        this.userController      = new UserController();
        this.reportController    = new ReportController();

        setTitle(Constants.App.WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.UI.WINDOW_WIDTH, Constants.UI.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(Constants.UI.MIN_WINDOW_WIDTH, Constants.UI.MIN_WINDOW_HEIGHT));
        setLocationRelativeTo(null);

        // Register shutdown hook to close DB connection
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            database.DatabaseConnection.getInstance().closeConnection()));

        // Root layout
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(ThemeManager.getSidebarBackground());

        // Sidebar
        sidebar = new Sidebar(this);

        // CardLayout content area
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(ThemeManager.getContentBackground());

        // Build all page panels
        buildPages();

        // Status bar
        JPanel statusBar = buildStatusBar();
        statusLabel = (JLabel) ((BorderLayout) statusBar.getLayout())
                .getLayoutComponent(statusBar, BorderLayout.WEST);

        root.add(sidebar,     BorderLayout.WEST);
        root.add(contentArea, BorderLayout.CENTER);
        root.add(statusBar,   BorderLayout.SOUTH);

        setContentPane(root);

        // Show dashboard by default
        navigate(Constants.Pages.DASHBOARD);
    }

    // ------------------------------------------------------------------
    // Page construction
    // ------------------------------------------------------------------

    private void buildPages() {
        dashboardPanel      = new DashboardPanel(complaintController, userController, this);
        viewComplaintsPanel = new ViewComplaintsPanel(complaintController, this);
        searchPanel         = new SearchComplaintsPanel(complaintController, this);
        registerPanel       = new RegisterComplaintPanel(complaintController, this);
        detailPanel         = new ComplaintDetailPanel(complaintController, userController, this);
        reportsPanel        = new ReportsPanel(reportController);
        profilePanel        = new ProfilePanel(userController);
        settingsPanel       = new SettingsPanel(this);
        aboutPanel          = new AboutPanel();

        contentArea.add(dashboardPanel,      Constants.Pages.DASHBOARD);
        contentArea.add(registerPanel,       Constants.Pages.REGISTER_COMPLAINT);
        contentArea.add(viewComplaintsPanel, Constants.Pages.VIEW_COMPLAINTS);
        contentArea.add(searchPanel,         Constants.Pages.SEARCH_COMPLAINTS);
        contentArea.add(detailPanel,         Constants.Pages.COMPLAINT_DETAIL);
        contentArea.add(reportsPanel,        Constants.Pages.REPORTS);
        contentArea.add(profilePanel,        Constants.Pages.PROFILE);
        contentArea.add(settingsPanel,       Constants.Pages.SETTINGS);
        contentArea.add(aboutPanel,          Constants.Pages.ABOUT);
    }

    // ------------------------------------------------------------------
    // NavigationListener implementation
    // ------------------------------------------------------------------

    @Override
    public void onNavigate(String pageName) {
        navigate(pageName);
    }

    @Override
    public void onLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            userController.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel());
        }
    }

    // ------------------------------------------------------------------
    // Navigation
    // ------------------------------------------------------------------

    /**
     * Switches the visible content panel to the given page and refreshes
     * the sidebar selection and data.
     *
     * @param pageName a key from {@link Constants.Pages}
     */
    public void navigate(String pageName) {
        // Enforce role-based navigation checks
        if (Constants.Pages.REPORTS.equals(pageName) && !SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(this, 
                "Access Denied: You do not have permission to view reports.", 
                "Security Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Constants.Pages.REGISTER_COMPLAINT.equals(pageName) && SessionManager.isEmployee()) {
            JOptionPane.showMessageDialog(this, 
                "Access Denied: Employees are not authorized to register complaints.", 
                "Security Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cardLayout.show(contentArea, pageName);
        sidebar.setActivePage(pageName);
        setStatusText("Ready");

        // Refresh data for the target page
        switch (pageName) {
            case Constants.Pages.DASHBOARD:
                dashboardPanel.refresh();
                break;
            case Constants.Pages.VIEW_COMPLAINTS:
                viewComplaintsPanel.refresh();
                break;
            case Constants.Pages.SEARCH_COMPLAINTS:
                searchPanel.reset();
                break;
            case Constants.Pages.REGISTER_COMPLAINT:
                registerPanel.resetForm();
                break;
            case Constants.Pages.PROFILE:
                profilePanel.refresh();
                break;
        }
    }

    /**
     * Shows the complaint detail panel for the given complaint ID.
     *
     * @param complaintId the ID of the complaint to display
     */
    public void showComplaintDetail(int complaintId) {
        detailPanel.loadComplaint(complaintId);
        navigate(Constants.Pages.COMPLAINT_DETAIL);
    }

    // ------------------------------------------------------------------
    // Public API for child panels
    // ------------------------------------------------------------------

    /**
     * Updates the status bar text at the bottom of the window.
     *
     * @param text the status message to display
     */
    public void setStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText("  " + text);
        }
    }

    /**
     * Refreshes the global theme after a settings change.
     */
    public void applyTheme() {
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    // ------------------------------------------------------------------
    // Status bar
    // ------------------------------------------------------------------

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(ThemeManager.getStatusBarBackground());
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                ThemeManager.getHeaderBorder()));
        bar.setPreferredSize(new Dimension(0, Constants.UI.STATUS_BAR_HEIGHT));

        JLabel left = new JLabel("  Ready");
        left.setFont(ThemeManager.getFontSmall());
        left.setForeground(ThemeManager.getTextSecondary());

        JLabel right = new JLabel(
            Constants.App.NAME + "  v" + Constants.App.VERSION + "  |  " +
            SessionManager.getCurrentUserName() + "   ");
        right.setFont(ThemeManager.getFontSmall());
        right.setForeground(ThemeManager.getTextMuted());

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }
}
