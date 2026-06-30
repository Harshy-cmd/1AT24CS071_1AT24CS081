package components;

import java.awt.*;

/**
 * Central theme manager for the Complaint Management System UI.
 *
 * <p>Defines the complete visual language of the application: colour palette,
 * fonts, spacing, and shadow parameters. All custom components and UI panels
 * reference this class for every visual property, ensuring a consistent,
 * professional appearance across every screen.</p>
 *
 * <p>Supports switching between a <strong>Dark Theme</strong> and a
 * <strong>Light Theme</strong> at runtime via {@link #setDark(boolean)},
 * which is used by the Settings panel.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public final class ThemeManager {

    // ------------------------------------------------------------------
    // Theme state
    // ------------------------------------------------------------------

    /** Whether the dark theme is currently active. Default: true. */
    private static boolean dark = true;

    /** Private constructor — utility class. */
    private ThemeManager() {}

    /** Enables or disables dark mode. */
    public static void setDark(boolean isDark) { dark = isDark; }

    /** Returns {@code true} if dark mode is active. */
    public static boolean isDark() { return dark; }

    // ------------------------------------------------------------------
    // Colour palette — Sidebar
    // ------------------------------------------------------------------

    public static Color getSidebarBackground()  { return new Color(15, 23, 42);    }
    public static Color getSidebarHover()        { return new Color(30, 41, 59);    }
    public static Color getSidebarSelected()     { return new Color(37, 99, 235);   }
    public static Color getSidebarText()         { return new Color(148, 163, 184); }
    public static Color getSidebarTextSelected() { return Color.WHITE;              }
    public static Color getSidebarBorder()       { return new Color(30, 41, 59);    }
    public static Color getSidebarLogo()         { return new Color(96, 165, 250);  }

    // ------------------------------------------------------------------
    // Colour palette — Content area
    // ------------------------------------------------------------------

    public static Color getContentBackground() {
        return dark ? new Color(15, 23, 42) : new Color(241, 245, 249);
    }
    public static Color getPanelBackground() {
        return dark ? new Color(30, 41, 59) : Color.WHITE;
    }
    public static Color getCardBackground() {
        return dark ? new Color(30, 41, 59) : Color.WHITE;
    }
    public static Color getCardBorder() {
        return dark ? new Color(51, 65, 85) : new Color(226, 232, 240);
    }
    public static Color getInputBackground() {
        return dark ? new Color(15, 23, 42) : new Color(248, 250, 252);
    }
    public static Color getInputBorder() {
        return dark ? new Color(51, 65, 85) : new Color(203, 213, 225);
    }
    public static Color getInputBorderFocused() {
        return new Color(37, 99, 235);
    }
    public static Color getTableBackground() {
        return dark ? new Color(15, 23, 42) : Color.WHITE;
    }
    public static Color getTableAlternate() {
        return dark ? new Color(22, 33, 53) : new Color(248, 250, 252);
    }
    public static Color getTableHeader() {
        return dark ? new Color(30, 41, 59) : new Color(241, 245, 249);
    }
    public static Color getTableSelection() { return new Color(37, 99, 235, 60);  }
    public static Color getTableGrid()      {
        return dark ? new Color(51, 65, 85) : new Color(226, 232, 240);
    }

    // ------------------------------------------------------------------
    // Colour palette — Typography
    // ------------------------------------------------------------------

    public static Color getTextPrimary() {
        return dark ? new Color(241, 245, 249) : new Color(15, 23, 42);
    }
    public static Color getTextSecondary() {
        return dark ? new Color(148, 163, 184) : new Color(71, 85, 105);
    }
    public static Color getTextMuted() {
        return dark ? new Color(100, 116, 139) : new Color(148, 163, 184);
    }
    public static Color getTextOnPrimary()   { return Color.WHITE; }

    // ------------------------------------------------------------------
    // Colour palette — Semantic colours
    // ------------------------------------------------------------------

    /** Primary brand blue — buttons, links, accents. */
    public static Color getPrimary()        { return new Color(37, 99, 235);  }
    public static Color getPrimaryHover()   { return new Color(29, 78, 216);  }
    public static Color getPrimaryLight()   { return new Color(219, 234, 254);}

    /** Success green — resolved, confirmed actions. */
    public static Color getSuccess()        { return new Color(16, 185, 129); }
    public static Color getSuccessLight()   { return new Color(209, 250, 229);}

    /** Warning amber — pending, attention-needed. */
    public static Color getWarning()        { return new Color(245, 158, 11); }
    public static Color getWarningLight()   { return new Color(254, 243, 199);}

    /** Danger red — critical, delete, error. */
    public static Color getDanger()         { return new Color(239, 68, 68);  }
    public static Color getDangerHover()    { return new Color(220, 38, 38);  }
    public static Color getDangerLight()    { return new Color(254, 226, 226);}

    /** Info cyan — informational actions. */
    public static Color getInfo()           { return new Color(6, 182, 212);  }
    public static Color getInfoLight()      { return new Color(207, 250, 254);}

    /** Neutral / secondary button. */
    public static Color getSecondary()      {
        return dark ? new Color(51, 65, 85) : new Color(226, 232, 240);
    }
    public static Color getSecondaryHover() {
        return dark ? new Color(71, 85, 105) : new Color(203, 213, 225);
    }

    /** Header bar background. */
    public static Color getHeaderBackground() {
        return dark ? new Color(22, 33, 53) : Color.WHITE;
    }
    public static Color getHeaderBorder() {
        return dark ? new Color(51, 65, 85) : new Color(226, 232, 240);
    }

    /** Status bar background. */
    public static Color getStatusBarBackground() {
        return dark ? new Color(15, 23, 42) : new Color(241, 245, 249);
    }

    // ------------------------------------------------------------------
    // Fonts
    // ------------------------------------------------------------------

    /** Base sans-serif font family — falls back gracefully on all OS. */
    public static final String FONT_FAMILY = "Segoe UI";

    public static Font getFontH1()       { return new Font(FONT_FAMILY, Font.BOLD,  22); }
    public static Font getFontH2()       { return new Font(FONT_FAMILY, Font.BOLD,  17); }
    public static Font getFontH3()       { return new Font(FONT_FAMILY, Font.BOLD,  14); }
    public static Font getFontSubtitle() { return new Font(FONT_FAMILY, Font.PLAIN, 12); }
    public static Font getFontBody()     { return new Font(FONT_FAMILY, Font.PLAIN, 13); }
    public static Font getFontSmall()    { return new Font(FONT_FAMILY, Font.PLAIN, 11); }
    public static Font getFontBold()     { return new Font(FONT_FAMILY, Font.BOLD,  13); }
    public static Font getFontMono()     { return new Font("Consolas", Font.PLAIN,  12); }
    public static Font getFontSidebar()  { return new Font(FONT_FAMILY, Font.PLAIN, 13); }
    public static Font getFontSidebarBold() { return new Font(FONT_FAMILY, Font.BOLD, 13); }
    public static Font getFontTable()    { return new Font(FONT_FAMILY, Font.PLAIN, 12); }
    public static Font getFontTableHeader() { return new Font(FONT_FAMILY, Font.BOLD, 12); }
    public static Font getFontButton()   { return new Font(FONT_FAMILY, Font.BOLD,  13); }
    public static Font getFontLogo()     { return new Font(FONT_FAMILY, Font.BOLD,  16); }

    // ------------------------------------------------------------------
    // Spacing / Sizing
    // ------------------------------------------------------------------

    public static final int PADDING_XS  =  4;
    public static final int PADDING_SM  =  8;
    public static final int PADDING_MD  = 16;
    public static final int PADDING_LG  = 24;
    public static final int PADDING_XL  = 32;
    public static final int GAP         = 12;

    public static final int RADIUS_SM   =  6;
    public static final int RADIUS_MD   = 10;
    public static final int RADIUS_LG   = 14;
    public static final int RADIUS_FULL = 24;

    public static final int BUTTON_H    = 36;
    public static final int INPUT_H     = 36;
    public static final int BADGE_H     = 22;

    // ------------------------------------------------------------------
    // Insets helpers
    // ------------------------------------------------------------------

    public static Insets insetsPanel()  { return new Insets(PADDING_LG, PADDING_LG, PADDING_LG, PADDING_LG); }
    public static Insets insetsCard()   { return new Insets(PADDING_MD, PADDING_MD, PADDING_MD, PADDING_MD); }
    public static Insets insetsButton() { return new Insets(6, PADDING_MD, 6, PADDING_MD); }
    public static Insets insetsInput()  { return new Insets(6, PADDING_SM, 6, PADDING_SM); }
}
