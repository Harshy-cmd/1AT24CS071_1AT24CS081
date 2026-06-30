package components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A JPanel that paints a rounded card background with a soft drop-shadow,
 * giving UI cards a modern elevated appearance without external libraries.
 *
 * <p>The shadow is simulated by drawing a series of increasingly transparent
 * rectangles offset by the shadow delta, creating a smooth blur-like effect
 * entirely in software.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class ShadowPanel extends JPanel {

    private static final int SHADOW_SIZE   = 6;
    private static final int SHADOW_OFFSET = 3;

    private final int   radius;
    private final Color cardColor;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a shadow panel using the theme card background.
     */
    public ShadowPanel() {
        this(ThemeManager.RADIUS_LG, ThemeManager.getCardBackground());
    }

    /**
     * Creates a shadow panel with the specified card colour.
     *
     * @param cardColor the background colour of the card
     */
    public ShadowPanel(Color cardColor) {
        this(ThemeManager.RADIUS_LG, cardColor);
    }

    /**
     * Full constructor.
     *
     * @param radius    corner arc radius in pixels
     * @param cardColor card background fill colour
     */
    public ShadowPanel(int radius, Color cardColor) {
        super(new BorderLayout());
        this.radius    = radius;
        this.cardColor = cardColor;
        setOpaque(false);
        // Add inset to accommodate the shadow offset
        setBorder(BorderFactory.createEmptyBorder(
            SHADOW_SIZE, SHADOW_SIZE,
            SHADOW_SIZE + SHADOW_OFFSET, SHADOW_SIZE + SHADOW_OFFSET));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x      = SHADOW_SIZE;
        int y      = SHADOW_SIZE;
        int width  = getWidth()  - SHADOW_SIZE - SHADOW_OFFSET;
        int height = getHeight() - SHADOW_SIZE - SHADOW_OFFSET;

        // Draw layered shadows
        for (int i = SHADOW_SIZE; i >= 1; i--) {
            int alpha = (int) (35 * (1.0 - (double) i / SHADOW_SIZE));
            g2.setColor(new Color(0, 0, 0, Math.max(alpha, 0)));
            g2.fill(new RoundRectangle2D.Float(
                x + SHADOW_OFFSET - i,
                y + SHADOW_OFFSET - i,
                width  + i * 2,
                height + i * 2,
                radius + i, radius + i));
        }

        // Card background
        g2.setColor(cardColor);
        g2.fill(new RoundRectangle2D.Float(x, y, width, height, radius, radius));

        // Subtle border
        g2.setColor(ThemeManager.getCardBorder());
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));

        g2.dispose();
        super.paintComponent(g);
    }
}
