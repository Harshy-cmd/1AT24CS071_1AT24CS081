package ui;

import components.*;
import controller.ComplaintController;
import controller.UserController;
import exceptions.DatabaseException;
import model.*;
import util.Constants;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

/**
 * Dashboard panel — the first screen the user sees after login.
 *
 * <p>Displays:
 * <ul>
 *   <li>4 metric cards (Total, Pending, Resolved, Critical)</li>
 *   <li>A custom-painted bar chart of complaints by status</li>
 *   <li>A recent-complaints table (last 10)</li>
 *   <li>An activity log feed</li>
 * </ul>
 * </p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class DashboardPanel extends JPanel {

    // ------------------------------------------------------------------
    // Dependencies
    // ------------------------------------------------------------------

    private final ComplaintController complaintController;
    private final UserController      userController;
    private final MainFrame           mainFrame;

    // ------------------------------------------------------------------
    // Dashboard cards (kept for live updates)
    // ------------------------------------------------------------------

    private DashboardCard cardTotal;
    private DashboardCard cardPending;
    private DashboardCard cardResolved;
    private DashboardCard cardCritical;

    // ------------------------------------------------------------------
    // Chart panel (custom painted)
    // ------------------------------------------------------------------

    private StatusChartPanel chartPanel;

    // ------------------------------------------------------------------
    // Recent complaints table
    // ------------------------------------------------------------------

    private DefaultTableModel recentTableModel;

    // ------------------------------------------------------------------
    // Activity feed
    // ------------------------------------------------------------------

    private NotificationPanel notificationPanel;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public DashboardPanel(ComplaintController complaintController,
                          UserController userController,
                          MainFrame mainFrame) {
        this.complaintController = complaintController;
        this.userController      = userController;
        this.mainFrame           = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.getContentBackground());
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buildUI();
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        // ---- Header ----
        HeaderPanel header = new HeaderPanel("Dashboard");
        add(header, BorderLayout.NORTH);

        // ---- Main scroll area ----
        JPanel mainPanel = new JPanel(new BorderLayout(ThemeManager.GAP, ThemeManager.GAP));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        // Top row: 4 metric cards
        JPanel cardsRow = buildCardsRow();

        // Middle: chart + activity feed (side by side, except for Citizens who don't see Analytics chart)
        boolean isCitizen = util.SessionManager.isCitizen();
        JPanel middleRow;

        notificationPanel = new NotificationPanel();
        JPanel activityCard = wrapInCard("Recent Activity", notificationPanel, 300);

        if (isCitizen) {
            middleRow = new JPanel(new GridLayout(1, 1, ThemeManager.GAP, 0));
            middleRow.setOpaque(false);
            middleRow.add(activityCard);
        } else {
            middleRow = new JPanel(new GridLayout(1, 2, ThemeManager.GAP, 0));
            middleRow.setOpaque(false);

            chartPanel = new StatusChartPanel();
            JPanel chartCard = wrapInCard("Complaints by Status", chartPanel, 300);

            middleRow.add(chartCard);
            middleRow.add(activityCard);
        }

        // Bottom: recent complaints table
        JPanel tableCard = buildRecentComplaintsCard();

        // Assemble main panel
        JPanel topSection = new JPanel(new GridLayout(2, 1, 0, ThemeManager.GAP));
        topSection.setOpaque(false);
        topSection.add(cardsRow);
        topSection.add(middleRow);

        mainPanel.add(topSection,  BorderLayout.NORTH);
        mainPanel.add(tableCard,   BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Quick-action toolbar
        add(buildQuickActions(), BorderLayout.SOUTH);
    }

    private JPanel buildCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, ThemeManager.GAP, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 120));

        cardTotal    = new DashboardCard("Total Complaints", "0", "",
                            ThemeManager.getPrimary(), "\uD83D\uDCCB");
        cardPending  = new DashboardCard("Pending",          "0", "",
                            ThemeManager.getWarning(), "⏳");
        cardResolved = new DashboardCard("Resolved",         "0", "",
                            ThemeManager.getSuccess(), "✅");
        cardCritical = new DashboardCard("Critical",         "0", "",
                            ThemeManager.getDanger(),  "🚨");

        row.add(cardTotal);
        row.add(cardPending);
        row.add(cardResolved);
        row.add(cardCritical);
        return row;
    }

    private JPanel buildRecentComplaintsCard() {
        String[] cols = {"Complaint #", "Title", "Category", "Priority", "Status", "Date"};
        recentTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(recentTableModel);
        styleTable(table);

        // Double-click → detail view
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        String num = (String) recentTableModel.getValueAt(row, 0);
                        try {
                            Complaint c = complaintController.getAllComplaints()
                                .stream().filter(x -> x.getComplaintNumber().equals(num))
                                .findFirst().orElse(null);
                            if (c != null) mainFrame.showComplaintDetail(c.getComplaintId());
                        } catch (DatabaseException ex) {
                            JOptionPane.showMessageDialog(DashboardPanel.this,
                                ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(ThemeManager.getTableBackground());

        return wrapInCard("Recent Complaints (double-click to open)", sp, 220);
    }

    private JPanel buildQuickActions() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.PADDING_MD, 8));
        bar.setBackground(ThemeManager.getHeaderBackground());
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getHeaderBorder()));

        JLabel label = new JLabel("Quick Actions:");
        label.setFont(ThemeManager.getFontBold());
        label.setForeground(ThemeManager.getTextSecondary());

        RoundedButton newBtn  = new RoundedButton("+ New Complaint", RoundedButton.Style.PRIMARY);
        RoundedButton viewBtn = new RoundedButton("View All", RoundedButton.Style.SECONDARY);
        RoundedButton rptBtn  = new RoundedButton("Reports", RoundedButton.Style.INFO);

        newBtn.addActionListener(e  -> mainFrame.navigate(Constants.Pages.REGISTER_COMPLAINT));
        viewBtn.addActionListener(e -> mainFrame.navigate(Constants.Pages.VIEW_COMPLAINTS));
        rptBtn.addActionListener(e  -> mainFrame.navigate(Constants.Pages.REPORTS));

        boolean isAdmin   = util.SessionManager.isAdmin();
        boolean isCitizen = util.SessionManager.isCitizen();
        boolean isEmployee = util.SessionManager.isEmployee();

        if (isCitizen) {
            viewBtn.setText("My Complaints");
        } else if (isEmployee) {
            viewBtn.setText("My Backlog");
        }

        bar.add(label);
        bar.add(Box.createHorizontalStrut(8));

        // Citizens and Admins can create complaints
        if (isAdmin || isCitizen) {
            bar.add(newBtn);
        }
        bar.add(viewBtn);

        // Only admins can see reports
        if (isAdmin) {
            bar.add(rptBtn);
        }
        return bar;
    }

    // ------------------------------------------------------------------
    // Refresh (called every time dashboard becomes visible)
    // ------------------------------------------------------------------

    /**
     * Reloads all dashboard data from the database on a background thread.
     */
    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Map<Status, Integer> statusCounts;
            int total, critical;
            List<Complaint> recent;
            List<ActivityLog> activity;

            @Override
            protected Void doInBackground() {
                try {
                    statusCounts = complaintController.getStatusCounts();
                    total        = complaintController.getTotalCount();
                    critical     = complaintController.getCriticalCount();
                    recent       = complaintController.getAllComplaints();
                    activity     = userController.getRecentActivity(12);
                } catch (DatabaseException e) {
                    System.err.println("[DashboardPanel] Refresh error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                if (statusCounts == null) return;

                int pending  = statusCounts.getOrDefault(Status.PENDING, 0)
                             + statusCounts.getOrDefault(Status.ASSIGNED, 0);
                int resolved = statusCounts.getOrDefault(Status.RESOLVED, 0)
                             + statusCounts.getOrDefault(Status.CLOSED, 0);

                cardTotal.setValue(String.valueOf(total));
                cardPending.setValue(String.valueOf(pending));
                cardResolved.setValue(String.valueOf(resolved));
                cardCritical.setValue(String.valueOf(critical));

                if (chartPanel != null) {
                    chartPanel.setData(statusCounts);
                }

                // Recent complaints table (show last 10)
                recentTableModel.setRowCount(0);
                int limit = Math.min(10, recent != null ? recent.size() : 0);
                for (int i = 0; i < limit; i++) {
                    Complaint c = recent.get(i);
                    recentTableModel.addRow(new Object[]{
                        c.getComplaintNumber(),
                        c.getTitle().length() > 45 ? c.getTitle().substring(0, 45) + "…" : c.getTitle(),
                        c.getCategory() != null ? c.getCategory().getDisplayName() : "",
                        c.getPriority() != null ? c.getPriority().getDisplayName() : "",
                        c.getStatus()   != null ? c.getStatus().getDisplayName()   : "",
                        DateUtil.formatDate(c.getDateCreated() != null ? c.getDateCreated().toLocalDate() : null)
                    });
                }

                if (activity != null) notificationPanel.setLogs(activity);

                mainFrame.setStatusText("Dashboard refreshed — " + DateUtil.nowDisplay());
            }
        };
        worker.execute();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private JPanel wrapInCard(String title, JComponent content, int preferredHeight) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, ThemeManager.PADDING_SM));
        card.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_MD, ThemeManager.PADDING_MD,
            ThemeManager.PADDING_MD, ThemeManager.PADDING_MD));
        card.setPreferredSize(new Dimension(0, preferredHeight));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.getFontH3());
        titleLabel.setForeground(ThemeManager.getTextPrimary());

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.add(titleLabel, BorderLayout.WEST);

        card.add(titleBar, BorderLayout.NORTH);
        card.add(content,  BorderLayout.CENTER);
        return card;
    }

    private void styleTable(JTable table) {
        table.setFont(ThemeManager.getFontTable());
        table.setForeground(ThemeManager.getTextPrimary());
        table.setBackground(ThemeManager.getTableBackground());
        table.setGridColor(ThemeManager.getTableGrid());
        table.setRowHeight(Constants.UI.TABLE_ROW_HEIGHT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(ThemeManager.getTableSelection());
        table.setSelectionForeground(ThemeManager.getTextPrimary());
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(ThemeManager.getFontTableHeader());
        table.getTableHeader().setBackground(ThemeManager.getTableHeader());
        table.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(
            0, 0, 1, 0, ThemeManager.getTableGrid()));
    }

    // ------------------------------------------------------------------
    // Inner class — Status bar chart (custom Java2D rendering)
    // ------------------------------------------------------------------

    /**
     * A custom-painted panel that renders a vertical bar chart of
     * complaint counts grouped by {@link Status}.
     *
     * <p>No external library is used — rendering is done entirely with
     * Java2D {@link Graphics2D}.</p>
     */
    private static class StatusChartPanel extends JPanel {

        private Map<Status, Integer> data;

        StatusChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 250));
        }

        void setData(Map<Status, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int paddingL = 50, paddingB = 50, paddingT = 20, paddingR = 20;
            int chartW   = w - paddingL - paddingR;
            int chartH   = h - paddingT - paddingB;

            // Max value
            int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            if (maxVal == 0) maxVal = 1;

            Status[] statuses = Status.values();
            int barCount      = statuses.length;
            int barWidth      = (chartW / barCount) - ThemeManager.GAP;
            int barSpacing    = chartW / barCount;

            // Horizontal grid lines
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
            g2.setColor(ThemeManager.getTableGrid());
            for (int i = 0; i <= 4; i++) {
                int y = paddingT + (chartH * i) / 4;
                g2.drawLine(paddingL, y, paddingL + chartW, y);

                // Y axis label
                g2.setStroke(new BasicStroke(1f));
                g2.setFont(ThemeManager.getFontSmall());
                g2.setColor(ThemeManager.getTextMuted());
                int labelVal = maxVal - (maxVal * i) / 4;
                g2.drawString(String.valueOf(labelVal), 4, y + 4);
            }
            g2.setStroke(new BasicStroke(1f));

            // Bars
            for (int i = 0; i < statuses.length; i++) {
                Status s    = statuses[i];
                int count   = data.getOrDefault(s, 0);
                int barH    = (int) ((double) count / maxVal * chartH);
                int bx      = paddingL + i * barSpacing + (barSpacing - barWidth) / 2;
                int by      = paddingT + chartH - barH;

                // Bar fill (with gradient)
                Color c = Color.decode(s.getColor());
                GradientPaint gp = new GradientPaint(bx, by, c.brighter(),
                        bx, by + barH, c);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(bx, by, barWidth, barH,
                        ThemeManager.RADIUS_SM, ThemeManager.RADIUS_SM));

                // Count label above bar
                g2.setColor(ThemeManager.getTextPrimary());
                g2.setFont(ThemeManager.getFontSmall());
                String countStr = String.valueOf(count);
                FontMetrics fm  = g2.getFontMetrics();
                g2.drawString(countStr, bx + (barWidth - fm.stringWidth(countStr)) / 2, by - 4);

                // Status label below bar
                g2.setColor(ThemeManager.getTextSecondary());
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.PLAIN, 9));
                fm = g2.getFontMetrics();
                String label = s.getDisplayName();
                if (fm.stringWidth(label) > barSpacing) {
                    label = label.substring(0, Math.min(label.length(), 5)) + ".";
                }
                g2.drawString(label,
                    bx + (barWidth - fm.stringWidth(label)) / 2,
                    paddingT + chartH + 14);
            }

            // Axis lines
            g2.setColor(ThemeManager.getTextMuted());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(paddingL, paddingT, paddingL, paddingT + chartH);
            g2.drawLine(paddingL, paddingT + chartH, paddingL + chartW, paddingT + chartH);

            g2.dispose();
        }
    }
}
