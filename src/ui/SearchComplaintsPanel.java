package ui;

import components.*;
import controller.ComplaintController;
import exceptions.DatabaseException;
import model.*;
import util.Constants;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel providing a keyword search interface for complaints.
 *
 * <p>The user can enter a keyword and optionally restrict the search to a
 * specific field (title, description, location, department). Results are
 * displayed in a table identical in style to {@link ViewComplaintsPanel}.</p>
 *
 * @author  CMS Development Team
 * @version 1.0.0
 * @since   2024
 */
public class SearchComplaintsPanel extends JPanel {

    private final ComplaintController controller;
    private final MainFrame           mainFrame;

    private JTextField    keywordField;
    private JComboBox<String> fieldCombo;
    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            resultLabel;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public SearchComplaintsPanel(ComplaintController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame  = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getContentBackground());
        buildUI();
    }

    // ------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------

    private void buildUI() {
        add(new HeaderPanel("Search Complaints"), BorderLayout.NORTH);

        // Search form
        JPanel searchCard = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.PADDING_MD, ThemeManager.PADDING_SM));
        searchCard.setBackground(ThemeManager.getHeaderBackground());
        searchCard.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getHeaderBorder()));

        JLabel kwLabel = new JLabel("Keyword:");
        kwLabel.setFont(ThemeManager.getFontBold());
        kwLabel.setForeground(ThemeManager.getTextSecondary());

        keywordField = new JTextField(30);
        keywordField.setFont(ThemeManager.getFontBody());
        keywordField.setForeground(ThemeManager.getTextPrimary());
        keywordField.setBackground(ThemeManager.getInputBackground());
        keywordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getInputBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        keywordField.addActionListener(e -> performSearch());

        JLabel fieldLabel = new JLabel("Search in:");
        fieldLabel.setFont(ThemeManager.getFontBold());
        fieldLabel.setForeground(ThemeManager.getTextSecondary());

        fieldCombo = new JComboBox<>(new String[]{"All Fields", "title", "description", "location", "department"});
        fieldCombo.setFont(ThemeManager.getFontBody());
        fieldCombo.setBackground(ThemeManager.getInputBackground());
        fieldCombo.setForeground(ThemeManager.getTextPrimary());

        RoundedButton searchBtn = new RoundedButton("Search", RoundedButton.Style.PRIMARY);
        searchBtn.addActionListener(e -> performSearch());

        RoundedButton clearBtn = new RoundedButton("Clear", RoundedButton.Style.SECONDARY);
        clearBtn.addActionListener(e -> reset());

        searchCard.add(kwLabel);
        searchCard.add(keywordField);
        searchCard.add(Box.createHorizontalStrut(8));
        searchCard.add(fieldLabel);
        searchCard.add(fieldCombo);
        searchCard.add(Box.createHorizontalStrut(8));
        searchCard.add(searchBtn);
        searchCard.add(clearBtn);

        // Results table
        String[] cols = {"#", "Complaint No", "Title", "Category", "Priority", "Status", "Assigned To", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int id = (int) tableModel.getValueAt(row, 0);
                        mainFrame.showComplaintDetail(id);
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(ThemeManager.getTableBackground());

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        statusBar.setBackground(ThemeManager.getStatusBarBackground());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getCardBorder()));
        resultLabel = new JLabel("Enter a keyword and press Search.");
        resultLabel.setFont(ThemeManager.getFontSmall());
        resultLabel.setForeground(ThemeManager.getTextSecondary());
        statusBar.add(resultLabel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(new HeaderPanel("Search Complaints"), BorderLayout.NORTH);
        topPanel.add(searchCard, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(sp,        BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void performSearch() {
        String keyword = keywordField.getText().trim();
        String field   = (String) fieldCombo.getSelectedItem();

        if (keyword.isBlank()) {
            resultLabel.setText("Please enter a keyword.");
            return;
        }

        SwingWorker<List<Complaint>, Void> worker = new SwingWorker<>() {
            @Override protected List<Complaint> doInBackground() throws Exception {
                if ("All Fields".equals(field)) {
                    return controller.search(keyword);
                } else {
                    return controller.search(keyword, field);
                }
            }
            @Override protected void done() {
                try {
                    List<Complaint> results = get();
                    tableModel.setRowCount(0);
                    for (Complaint c : results) addRow(c);
                    resultLabel.setText(results.size() + " result(s) for \"" + keyword + "\"");
                    mainFrame.setStatusText("Search complete — " + results.size() + " results.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SearchComplaintsPanel.this,
                        e.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /** Resets the search form and result table. */
    public void reset() {
        keywordField.setText("");
        fieldCombo.setSelectedIndex(0);
        tableModel.setRowCount(0);
        resultLabel.setText("Enter a keyword and press Search.");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void addRow(Complaint c) {
        tableModel.addRow(new Object[]{
            c.getComplaintId(),
            c.getComplaintNumber(),
            c.getTitle().length() > 50 ? c.getTitle().substring(0, 50) + "…" : c.getTitle(),
            c.getCategory()  != null ? c.getCategory().getDisplayName()  : "",
            c.getPriority()  != null ? c.getPriority().getDisplayName()  : "",
            c.getStatus()    != null ? c.getStatus().getDisplayName()    : "",
            c.getAssignedToName(),
            DateUtil.formatDate(c.getDateCreated() != null ? c.getDateCreated().toLocalDate() : null)
        });
    }

    private void styleTable() {
        table.setFont(ThemeManager.getFontTable());
        table.setForeground(ThemeManager.getTextPrimary());
        table.setBackground(ThemeManager.getTableBackground());
        table.setGridColor(ThemeManager.getTableGrid());
        table.setRowHeight(Constants.UI.TABLE_ROW_HEIGHT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(ThemeManager.getTableSelection());
        table.setSelectionForeground(ThemeManager.getTextPrimary());
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getTableHeader().setFont(ThemeManager.getFontTableHeader());
        table.getTableHeader().setBackground(ThemeManager.getTableHeader());
        table.getTableHeader().setForeground(ThemeManager.getTextSecondary());
    }
}
