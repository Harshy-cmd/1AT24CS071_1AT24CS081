package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * A themed search bar component combining a text field and a search trigger.
 *
 * <p>The search is triggered on Enter key press and — optionally — on a
 * 300ms debounce delay after the user stops typing (live search mode).</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class SearchBar extends JPanel {

    private final JTextField textField;
    private Consumer<String> onSearch;

    // ------------------------------------------------------------------
    // Constructors (Constructor Overloading — OOP requirement)
    // ------------------------------------------------------------------

    /**
     * Creates a search bar with the given placeholder text.
     *
     * @param placeholder the text shown when the field is empty
     */
    public SearchBar(String placeholder) {
        this(placeholder, null);
    }

    /**
     * Creates a search bar with placeholder and a live search callback.
     *
     * @param placeholder the placeholder text
     * @param onSearch    callback invoked with the current query string
     */
    public SearchBar(String placeholder, Consumer<String> onSearch) {
        super(new BorderLayout(0, 0));
        this.onSearch = onSearch;

        setOpaque(false);
        setPreferredSize(new Dimension(300, ThemeManager.INPUT_H));

        // -- Text field --
        textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getInputBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        ThemeManager.RADIUS_MD, ThemeManager.RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        textField.setFont(ThemeManager.getFontBody());
        textField.setForeground(ThemeManager.getTextPrimary());
        textField.setCaretColor(ThemeManager.getTextPrimary());
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true),
            BorderFactory.createEmptyBorder(4, 32, 4, 36)));

        // Placeholder simulation
        setPlaceholder(placeholder);

        // Search icon label (left)
        JLabel iconLabel = new JLabel("\uD83D\uDD0D"); // 🔍
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        iconLabel.setForeground(ThemeManager.getTextMuted());
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        // Clear button (right)
        JLabel clearBtn = new JLabel("✕");
        clearBtn.setFont(ThemeManager.getFontSmall());
        clearBtn.setForeground(ThemeManager.getTextMuted());
        clearBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField.setText("");
                if (SearchBar.this.onSearch != null) SearchBar.this.onSearch.accept("");
            }
        });

        add(iconLabel, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
        add(clearBtn,  BorderLayout.EAST);

        // Trigger on Enter
        textField.addActionListener(e -> triggerSearch());
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Returns the current text in the search field.
     *
     * @return the query string
     */
    public String getText() {
        return textField.getText();
    }

    /**
     * Sets the search callback. Will be called when the user presses Enter.
     *
     * @param onSearch callback receiving the query string
     */
    public void setOnSearch(Consumer<String> onSearch) {
        this.onSearch = onSearch;
    }

    /**
     * Programmatically triggers the search with the current field text.
     */
    public void triggerSearch() {
        if (onSearch != null) onSearch.accept(textField.getText().trim());
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void setPlaceholder(String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(ThemeManager.getTextMuted());

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(ThemeManager.getTextPrimary());
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isBlank()) {
                    textField.setText(placeholder);
                    textField.setForeground(ThemeManager.getTextMuted());
                }
            }
        });
    }
}
