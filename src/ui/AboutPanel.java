package ui;

import components.*;
import util.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * About panel — displays application metadata, author, technology stack,
 * and academic context.
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class AboutPanel extends JPanel {

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getContentBackground());
        buildUI();
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        add(new HeaderPanel("About"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        JPanel card = buildCard();
        content.add(card, new GridBagConstraints());

        JScrollPane sp = new JScrollPane(content);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildCard() {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(36, 48, 36, 48));
        card.setPreferredSize(new Dimension(640, 520));

        // App icon
        JLabel icon = new JLabel("\uD83D\uDCCB", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        // App name
        JLabel nameLabel = new JLabel(Constants.App.NAME, SwingConstants.CENTER);
        nameLabel.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 26));
        nameLabel.setForeground(ThemeManager.getTextPrimary());
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Version
        JLabel versionLabel = new JLabel("Version " + Constants.App.VERSION + "  |  " + Constants.App.YEAR,
                SwingConstants.CENTER);
        versionLabel.setFont(ThemeManager.getFontSubtitle());
        versionLabel.setForeground(ThemeManager.getTextSecondary());
        versionLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(ThemeManager.getCardBorder());

        // Description
        JLabel desc = new JLabel(
            "<html><div style='text-align:center;width:480px;line-height:1.5'>" +
            "A production-quality desktop application for managing complaints in " +
            "an organization. Built using <b>Java 17 + Swing + JDBC + MySQL</b> " +
            "following strict <b>MVC architecture</b>, <b>SOLID principles</b>, " +
            "and all four OOP pillars.<br><br>" +
            "Features: Dashboard analytics, complaint registration, tracking, " +
            "employee assignment, status history, CSV export, and print reports." +
            "</div></html>", SwingConstants.CENTER);
        desc.setFont(ThemeManager.getFontBody());
        desc.setForeground(ThemeManager.getTextSecondary());
        desc.setAlignmentX(CENTER_ALIGNMENT);

        // Technology stack
        JPanel techPanel = buildTechRow();

        // Author
        JLabel authorLabel = new JLabel("Developed by: " + Constants.App.AUTHOR,
                SwingConstants.CENTER);
        authorLabel.setFont(ThemeManager.getFontBold());
        authorLabel.setForeground(ThemeManager.getPrimary());
        authorLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Copyright
        JLabel copyLabel = new JLabel(
            "© " + Constants.App.YEAR + " " + Constants.App.AUTHOR +
            "  |  All Rights Reserved", SwingConstants.CENTER);
        copyLabel.setFont(ThemeManager.getFontSmall());
        copyLabel.setForeground(ThemeManager.getTextMuted());
        copyLabel.setAlignmentX(CENTER_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(8));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(versionLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(sep);
        card.add(Box.createVerticalStrut(20));
        card.add(desc);
        card.add(Box.createVerticalStrut(24));
        card.add(techPanel);
        card.add(Box.createVerticalStrut(24));
        card.add(authorLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(copyLabel);
        return card;
    }

    private JPanel buildTechRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(CENTER_ALIGNMENT);

        String[] techs = {"Java 17", "Swing", "JDBC", "MySQL 8"};
        Color[]  colors = {
            ThemeManager.getPrimary(),
            ThemeManager.getSuccess(),
            ThemeManager.getWarning(),
            new Color(0, 188, 140)
        };
        for (int i = 0; i < techs.length; i++) {
            JLabel badge = new JLabel(techs[i]);
            badge.setFont(ThemeManager.getFontSmall());
            badge.setForeground(colors[i]);
            badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colors[i], 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
            row.add(badge);
        }
        return row;
    }
}
