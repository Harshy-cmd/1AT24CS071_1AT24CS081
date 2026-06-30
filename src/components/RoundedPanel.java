package components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A JPanel subclass that paints a rounded rectangle background with
 * an optional border and full theme integration.
 *
 * <p>Used as the visual container for cards, form sections, and dialogs
 * throughout the application. Eliminates the boilerplate of repeating
 * {@code setBorder()} and {@code setOpaque(false)} on every panel.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class RoundedPanel extends JPanel {

    private final int    radius;
    private final Color  background;
    private final Color  borderColor;
    private final boolean hasBorder;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a panel with the theme's card background and default radius.
     */
    public RoundedPanel() {
        this(ThemeManager.RADIUS_LG, ThemeManager.getCardBackground(),
             ThemeManager.getCardBorder(), true);
    }

    /**
     * Creates a panel with a custom background colour.
     *
     * @param background the fill colour for this panel
     */
    public RoundedPanel(Color background) {
        this(ThemeManager.RADIUS_LG, background, ThemeManager.getCardBorder(), true);
    }

    /**
     * Creates a panel with custom background colour and border toggle.
     *
     * @param background the fill colour
     * @param hasBorder  whether to draw a 1px border
     */
    public RoundedPanel(Color background, boolean hasBorder) {
        this(ThemeManager.RADIUS_LG, background, ThemeManager.getCardBorder(), hasBorder);
    }

    /**
     * Full constructor with all parameters.
     *
     * @param radius      corner arc radius in pixels
     * @param background  fill colour
     * @param borderColor border colour
     * @param hasBorder   whether to draw the border
     */
    public RoundedPanel(int radius, Color background, Color borderColor, boolean hasBorder) {
        super();
        this.radius      = radius;
        this.background  = background;
        this.borderColor = borderColor;
        this.hasBorder   = hasBorder;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    // ------------------------------------------------------------------
    // Custom painting
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D shape =
            new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.setColor(background);
        g2.fill(shape);

        if (hasBorder) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(shape);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
