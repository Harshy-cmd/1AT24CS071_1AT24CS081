package ui;

import components.*;
import util.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Settings panel for toggling application theme (Dark / Light) and
 * other non-critical preferences.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class SettingsPanel extends JPanel {

    private final MainFrame mainFrame;
    private JToggleButton   themeToggle;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getContentBackground());
        buildUI();
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        add(new HeaderPanel("Settings"), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        // Theme card
        RoundedPanel themeCard = new RoundedPanel();
        themeCard.setLayout(new BoxLayout(themeCard, BoxLayout.Y_AXIS));
        themeCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        themeCard.setMaximumSize(new Dimension(500, 160));

        JLabel themeTitle = new JLabel("Appearance");
        themeTitle.setFont(ThemeManager.getFontH3());
        themeTitle.setForeground(ThemeManager.getTextPrimary());
        themeTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel themeSub = new JLabel("Choose between Dark Mode and Light Mode.");
        themeSub.setFont(ThemeManager.getFontSmall());
        themeSub.setForeground(ThemeManager.getTextSecondary());
        themeSub.setAlignmentX(LEFT_ALIGNMENT);

        JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        toggleRow.setOpaque(false);
        toggleRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lightLabel = new JLabel("☀  Light");
        lightLabel.setFont(ThemeManager.getFontBody());
        lightLabel.setForeground(ThemeManager.getTextPrimary());

        themeToggle = new JToggleButton(ThemeManager.isDark() ? "🌙  Dark Mode" : "☀  Light Mode");
        themeToggle.setSelected(ThemeManager.isDark());
        themeToggle.setFont(ThemeManager.getFontButton());
        themeToggle.setBackground(ThemeManager.getPrimary());
        themeToggle.setForeground(Color.WHITE);
        themeToggle.setFocusPainted(false);
        themeToggle.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        themeToggle.addActionListener(e -> toggleTheme());

        toggleRow.add(themeToggle);

        themeCard.add(themeTitle);
        themeCard.add(Box.createVerticalStrut(4));
        themeCard.add(themeSub);
        themeCard.add(Box.createVerticalStrut(12));
        themeCard.add(toggleRow);

        // App info card
        RoundedPanel infoCard = new RoundedPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        infoCard.setMaximumSize(new Dimension(500, 130));

        JLabel infoTitle = new JLabel("Application Info");
        infoTitle.setFont(ThemeManager.getFontH3());
        infoTitle.setForeground(ThemeManager.getTextPrimary());
        infoTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel versionLabel = new JLabel("Version: " + Constants.App.VERSION);
        versionLabel.setFont(ThemeManager.getFontBody());
        versionLabel.setForeground(ThemeManager.getTextSecondary());
        versionLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel authorLabel = new JLabel("Author: " + Constants.App.AUTHOR);
        authorLabel.setFont(ThemeManager.getFontBody());
        authorLabel.setForeground(ThemeManager.getTextSecondary());
        authorLabel.setAlignmentX(LEFT_ALIGNMENT);

        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(8));
        infoCard.add(versionLabel);
        infoCard.add(Box.createVerticalStrut(4));
        infoCard.add(authorLabel);

        content.add(themeCard);
        content.add(Box.createVerticalStrut(ThemeManager.PADDING_MD));
        content.add(infoCard);

        JScrollPane sp = new JScrollPane(content);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void toggleTheme() {
        boolean isDark = !ThemeManager.isDark();
        ThemeManager.setDark(isDark);
        themeToggle.setText(isDark ? "🌙  Dark Mode" : "☀  Light Mode");
        themeToggle.setSelected(isDark);
        mainFrame.applyTheme();
    }
}
