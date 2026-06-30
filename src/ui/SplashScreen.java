package ui;

import components.ThemeManager;
import util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Animated splash screen displayed for ~3 seconds on application startup.
 *
 * <p>Rendered as an undecorated window with a rounded rectangle, the app
 * name, version, and a progress bar animation. After the timer fires, it
 * disposes itself and triggers the {@link LoginPanel} via a callback.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class SplashScreen extends JWindow {

    private static final int SPLASH_DURATION_MS = 3000;
    private static final int PROGRESS_STEPS     = 100;

    private final JProgressBar progressBar;
    private final Runnable     onComplete;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Creates and shows the splash screen.
     *
     * @param onComplete callback invoked after the splash duration expires
     */
    public SplashScreen(Runnable onComplete) {
        this.onComplete = onComplete;

        setSize(Constants.UI.SPLASH_WIDTH, Constants.UI.SPLASH_HEIGHT);
        setLocationRelativeTo(null);

        // Custom content panel
        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(15, 23, 42),
                    getWidth(), getHeight(), new Color(30, 64, 120));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Decorative circle (right background element)
                g2.setColor(new Color(37, 99, 235, 30));
                g2.fillOval(getWidth() - 180, -60, 280, 280);
                g2.setColor(new Color(37, 99, 235, 15));
                g2.fillOval(getWidth() - 120, getHeight() - 100, 200, 200);

                // App icon (large emoji)
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83D\uDCCB";
                int ix = (getWidth() - fm.stringWidth(icon)) / 2;
                g2.drawString(icon, ix, 130);

                // App name
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.BOLD, 24));
                g2.setColor(Color.WHITE);
                fm = g2.getFontMetrics();
                String title = Constants.App.NAME;
                g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 165);

                // Subtitle
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.PLAIN, 12));
                g2.setColor(new Color(148, 163, 184));
                fm = g2.getFontMetrics();
                String sub = "Professional Complaint Tracking Solution  v" + Constants.App.VERSION;
                g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, 185);

                // Loading text
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.PLAIN, 11));
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("Initialising...", 30, getHeight() - 48);

                // Copyright
                g2.setFont(new Font(ThemeManager.FONT_FAMILY, Font.PLAIN, 10));
                g2.setColor(new Color(71, 85, 105));
                String copy = "© " + Constants.App.YEAR + "  " + Constants.App.AUTHOR;
                fm = g2.getFontMetrics();
                g2.drawString(copy, (getWidth() - fm.stringWidth(copy)) / 2, getHeight() - 12);

                g2.dispose();
            }
        };
        content.setOpaque(false);

        // Progress bar
        progressBar = new JProgressBar(0, PROGRESS_STEPS);
        progressBar.setStringPainted(false);
        progressBar.setBackground(new Color(30, 41, 59));
        progressBar.setForeground(new Color(37, 99, 235));
        progressBar.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        progressBar.setPreferredSize(new Dimension(0, 30));
        content.add(progressBar, BorderLayout.SOUTH);

        setContentPane(content);

        // Make window shape rounded (Java 7+)
        setShape(new RoundRectangle2D.Double(0, 0,
                Constants.UI.SPLASH_WIDTH, Constants.UI.SPLASH_HEIGHT, 20, 20));

        setVisible(true);
        startProgress();
    }

    // ------------------------------------------------------------------
    // Animation
    // ------------------------------------------------------------------

    private void startProgress() {
        int delay = SPLASH_DURATION_MS / PROGRESS_STEPS;
        Timer timer = new Timer(delay, null);
        timer.addActionListener(e -> {
            int val = progressBar.getValue();
            if (val >= PROGRESS_STEPS) {
                timer.stop();
                dispose();
                if (onComplete != null) onComplete.run();
            } else {
                progressBar.setValue(val + 1);
            }
        });
        timer.start();
    }
}
