package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A custom Swing button with rounded corners, hover animation, and
 * full theme integration.
 *
 * <p>Provides four style variants via the {@link Style} enum:
 * <ul>
 *   <li>{@link Style#PRIMARY}   — solid blue, main actions</li>
 *   <li>{@link Style#DANGER}    — solid red, destructive actions</li>
 *   <li>{@link Style#SUCCESS}   — solid green, confirming actions</li>
 *   <li>{@link Style#SECONDARY} — neutral, cancel / secondary actions</li>
 * </ul>
 * </p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class RoundedButton extends JButton {

    // ------------------------------------------------------------------
    // Inner enum for style variants
    // ------------------------------------------------------------------

    /** Available visual styles for the button. */
    public enum Style {
        PRIMARY, DANGER, SUCCESS, SECONDARY, WARNING, INFO
    }

    // ------------------------------------------------------------------
    // State fields
    // ------------------------------------------------------------------

    private final Style style;
    private boolean hovered = false;
    private final int radius;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a PRIMARY style button with default radius.
     *
     * @param text the button label
     */
    public RoundedButton(String text) {
        this(text, Style.PRIMARY, ThemeManager.RADIUS_MD);
    }

    /**
     * Creates a button with the specified style and default radius.
     *
     * @param text  the button label
     * @param style the visual style variant
     */
    public RoundedButton(String text, Style style) {
        this(text, style, ThemeManager.RADIUS_MD);
    }

    /**
     * Full constructor: label, style, and corner radius.
     *
     * @param text   the button label
     * @param style  the visual style variant
     * @param radius the corner arc radius in pixels
     */
    public RoundedButton(String text, Style style, int radius) {
        super(text);
        this.style  = style;
        this.radius = radius;

        setFont(ThemeManager.getFontButton());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setForeground(getForegroundColor());
        setPreferredSize(new Dimension(
            getFontMetrics(getFont()).stringWidth(text) + 36,
            ThemeManager.BUTTON_H));

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
        });
    }

    // ------------------------------------------------------------------
    // Custom painting
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background fill
        g2.setColor(getBackgroundColor());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));

        // Text
        g2.setFont(getFont());
        g2.setColor(getForegroundColor());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth()  - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    // ------------------------------------------------------------------
    // Colour resolution
    // ------------------------------------------------------------------

    private Color getBackgroundColor() {
        if (!isEnabled()) return new Color(150, 150, 150, 120);
        switch (style) {
            case PRIMARY:   return hovered ? ThemeManager.getPrimaryHover()   : ThemeManager.getPrimary();
            case DANGER:    return hovered ? ThemeManager.getDangerHover()    : ThemeManager.getDanger();
            case SUCCESS:   return hovered ? new Color(5, 150, 105)           : ThemeManager.getSuccess();
            case WARNING:   return hovered ? new Color(217, 119, 6)           : ThemeManager.getWarning();
            case INFO:      return hovered ? new Color(8, 145, 178)           : ThemeManager.getInfo();
            case SECONDARY: return hovered ? ThemeManager.getSecondaryHover() : ThemeManager.getSecondary();
            default:        return ThemeManager.getPrimary();
        }
    }

    private Color getForegroundColor() {
        if (!isEnabled()) return new Color(180, 180, 180);
        return style == Style.SECONDARY
            ? ThemeManager.getTextPrimary()
            : ThemeManager.getTextOnPrimary();
    }
}
