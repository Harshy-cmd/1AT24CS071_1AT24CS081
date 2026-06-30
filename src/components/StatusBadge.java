package components;

import model.Status;
import model.Priority;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A small pill-shaped label used to display {@link Status} or
 * {@link Priority} values with consistent colour coding throughout the UI.
 *
 * <p>Each badge renders a rounded rectangle filled with the semantic
 * background colour of the value, with the display name rendered in the
 * matching foreground colour.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class StatusBadge extends JLabel {

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a badge for the given {@link Status}.
     *
     * @param status the status to render
     */
    public StatusBadge(Status status) {
        super(status != null ? status.getDisplayName() : "—");
        configure(
            status != null ? parseColor(status.getColor())           : Color.GRAY,
            status != null ? parseColor(status.getBackgroundColor()) : new Color(240,240,240)
        );
    }

    /**
     * Creates a badge for the given {@link Priority}.
     *
     * @param priority the priority to render
     */
    public StatusBadge(Priority priority) {
        super(priority != null ? priority.getDisplayName() : "—");
        configure(
            priority != null ? parseColor(priority.getColor())           : Color.GRAY,
            priority != null ? parseColor(priority.getBackgroundColor()) : new Color(240,240,240)
        );
    }

    /**
     * Creates a badge with an arbitrary label and custom colours.
     *
     * @param label           the text to display
     * @param foregroundColor the text / border colour
     * @param backgroundColor the fill colour
     */
    public StatusBadge(String label, Color foregroundColor, Color backgroundColor) {
        super(label);
        configure(foregroundColor, backgroundColor);
    }

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------

    private void configure(Color fg, Color bg) {
        setFont(ThemeManager.getFontSmall());
        setForeground(fg);
        setBackground(bg);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        setPreferredSize(new Dimension(
            getFontMetrics(getFont()).stringWidth(getText()) + 20,
            ThemeManager.BADGE_H));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                ThemeManager.RADIUS_FULL, ThemeManager.RADIUS_FULL));
        g2.dispose();
        super.paintComponent(g);
    }

    // ------------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------------

    /** Parses a hex colour string (e.g., "#E74C3C") into a {@link Color}. */
    private Color parseColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.GRAY;
        }
    }
}
