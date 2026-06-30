package components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A metric card for the dashboard that displays a title, a large numeric
 * value, and an optional sub-label with a coloured accent bar on the left.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class DashboardCard extends JPanel {

    private String  title;
    private String  value;
    private String  subLabel;
    private Color   accentColor;
    private String  iconText;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a dashboard card with title, value, and accent colour.
     *
     * @param title       the metric name (e.g., "Total Complaints")
     * @param value       the metric value (e.g., "42")
     * @param accentColor the left-edge accent colour
     */
    public DashboardCard(String title, String value, Color accentColor) {
        this(title, value, "", accentColor, "");
    }

    /**
     * Full constructor with subtitle and icon.
     *
     * @param title       the metric name
     * @param value       the numeric value to display large
     * @param subLabel    smaller text below the value
     * @param accentColor the left-edge accent colour
     * @param iconText    Unicode emoji/icon for visual identity
     */
    public DashboardCard(String title, String value, String subLabel,
                         Color accentColor, String iconText) {
        super(null); // absolute layout for precise positioning
        this.title       = title;
        this.value       = value;
        this.subLabel    = subLabel;
        this.accentColor = accentColor;
        this.iconText    = iconText;

        setOpaque(false);
        setPreferredSize(new Dimension(210, 110));
    }

    // ------------------------------------------------------------------
    // Setters (to allow live updates from the dashboard)
    // ------------------------------------------------------------------

    public void setValue(String value)    { this.value = value;    repaint(); }
    public void setSubLabel(String label) { this.subLabel = label; repaint(); }

    // ------------------------------------------------------------------
    // Custom painting
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Card background
        g2.setColor(ThemeManager.getCardBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, ThemeManager.RADIUS_LG, ThemeManager.RADIUS_LG));

        // Card border
        g2.setColor(ThemeManager.getCardBorder());
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, ThemeManager.RADIUS_LG, ThemeManager.RADIUS_LG));

        // Accent bar (left edge)
        g2.setColor(accentColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, 5, h, ThemeManager.RADIUS_LG, ThemeManager.RADIUS_LG));
        g2.fillRect(3, 0, 5, h); // square-right the outer curve

        int px = 18;
        int py = 16;

        // Icon
        if (iconText != null && !iconText.isBlank()) {
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            g2.setColor(accentColor);
            g2.drawString(iconText, w - 42, py + 20);
        }

        // Title
        g2.setFont(ThemeManager.getFontSmall());
        g2.setColor(ThemeManager.getTextSecondary());
        g2.drawString(title, px, py + 11);

        // Value
        g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 30));
        g2.setColor(ThemeManager.getTextPrimary());
        g2.drawString(value, px, py + 50);

        // Sub-label
        if (subLabel != null && !subLabel.isBlank()) {
            g2.setFont(ThemeManager.getFontSmall());
            g2.setColor(ThemeManager.getTextMuted());
            g2.drawString(subLabel, px, py + 68);
        }

        g2.dispose();
    }
}
