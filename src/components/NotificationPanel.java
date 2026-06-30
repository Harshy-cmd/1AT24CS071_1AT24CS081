package components;

import model.ActivityLog;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A scrollable panel that displays recent system activity log entries in a
 * feed-style layout, used in the Dashboard.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class NotificationPanel extends JPanel {

    private final JPanel   feedPanel;
    private final JScrollPane scrollPane;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates an empty notification panel.
     */
    public NotificationPanel() {
        this(null);
    }

    /**
     * Creates a notification panel pre-loaded with activity log entries.
     *
     * @param logs the list of activity log entries to display initially
     */
    public NotificationPanel(List<ActivityLog> logs) {
        super(new BorderLayout());
        setOpaque(false);

        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setOpaque(false);

        scrollPane = new JScrollPane(feedPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        if (logs != null && !logs.isEmpty()) {
            setLogs(logs);
        } else {
            addEmptyState();
        }
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Replaces all current entries with the provided log list.
     *
     * @param logs the new list of activity log entries
     */
    public void setLogs(List<ActivityLog> logs) {
        feedPanel.removeAll();
        if (logs == null || logs.isEmpty()) {
            addEmptyState();
        } else {
            for (ActivityLog log : logs) {
                feedPanel.add(buildEntry(log));
                feedPanel.add(Box.createVerticalStrut(2));
            }
        }
        feedPanel.revalidate();
        feedPanel.repaint();
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private JPanel buildEntry(ActivityLog log) {
        JPanel entry = new JPanel(new BorderLayout(8, 0));
        entry.setOpaque(false);
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        entry.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getCardBorder()),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        // Icon dot
        JLabel dot = new JLabel("•");
        dot.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 20));
        dot.setForeground(ThemeManager.getPrimary());
        dot.setVerticalAlignment(SwingConstants.TOP);

        // Text area
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel descLabel = new JLabel(truncate(log.getDescription(), 55));
        descLabel.setFont(ThemeManager.getFontBody());
        descLabel.setForeground(ThemeManager.getTextPrimary());

        JLabel timeLabel = new JLabel(
            (log.getUserName() != null ? log.getUserName() + "  •  " : "") +
            DateUtil.timeAgo(log.getLogTimestamp()));
        timeLabel.setFont(ThemeManager.getFontSmall());
        timeLabel.setForeground(ThemeManager.getTextMuted());

        textPanel.add(descLabel);
        textPanel.add(timeLabel);

        entry.add(dot,       BorderLayout.WEST);
        entry.add(textPanel, BorderLayout.CENTER);
        return entry;
    }

    private void addEmptyState() {
        JLabel empty = new JLabel("No recent activity.", SwingConstants.CENTER);
        empty.setFont(ThemeManager.getFontSubtitle());
        empty.setForeground(ThemeManager.getTextMuted());
        feedPanel.add(empty);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "…";
    }
}
