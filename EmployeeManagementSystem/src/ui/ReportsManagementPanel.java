package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ReportsManagementPanel extends JPanel {

    private JComboBox<String> cmbPayrollFilter, cmbLeavesFilter, cmbTasksFilter; // 🔮 Filters for all 3 cards

    public ReportsManagementPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ========================================================
        // TOP TITLE HEAD
        // ========================================================
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Executive Management Reports Hub");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSubtitle = new JLabel("Generate, compile, and download officially verified corporate analytical logs into premium PDF files.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        add(titlePanel, BorderLayout.NORTH);

        // ========================================================
        // REPORTS SELECTION MATRIX (3 Modern Filterable Cards)
        // ========================================================
        JPanel mainGrid = new JPanel(new GridLayout(1, 3, 25, 0));
        mainGrid.setOpaque(false);

        String[] intervals = {"Last Day", "Last Week", "Last Month", "Last Two Months", "Last Six Months", "Last Year"};

        // Card 1: Payroll Statement Report
        JPanel cardPayroll = createReportCard("Corporate Payroll Matrix", "Download the entire active staff financial statements, fixed basic salaries, total overtime allowances and net liability models.");
        JPanel payrollFilterWrapper = createFilterRowDeck("Period:", cmbPayrollFilter = new JComboBox<>(intervals));
        cmbPayrollFilter.setSelectedIndex(2); // Default to Last Month
        JPanel payrollBottomPanel = new JPanel(new BorderLayout());
        payrollBottomPanel.setOpaque(false);
        payrollBottomPanel.add(payrollFilterWrapper, BorderLayout.NORTH);
        JButton btnExportPayroll = new JButton("View Payroll Report Preview");
        styleReportButton(btnExportPayroll, new Color(99, 102, 241)); 
        payrollBottomPanel.add(btnExportPayroll, BorderLayout.SOUTH);
        cardPayroll.add(payrollBottomPanel, BorderLayout.SOUTH);

        // Card 2: Employee Leaves Audit Log
        JPanel cardLeaves = createReportCard("Workforce Leave Audit Logs", "Extract deep summaries of all staff vacation calendars, pending applications, approved durations, and historical reasons.");
        JPanel leavesFilterWrapper = createFilterRowDeck("Period:", cmbLeavesFilter = new JComboBox<>(intervals));
        cmbLeavesFilter.setSelectedIndex(2); // Default to Last Month
        JPanel leavesBottomPanel = new JPanel(new BorderLayout());
        leavesBottomPanel.setOpaque(false);
        leavesBottomPanel.add(leavesFilterWrapper, BorderLayout.NORTH);
        JButton btnExportLeaves = new JButton("View Leaves Report Preview");    
        styleReportButton(btnExportLeaves, new Color(34, 197, 94)); 
        leavesBottomPanel.add(btnExportLeaves, BorderLayout.SOUTH);
        cardLeaves.add(leavesBottomPanel, BorderLayout.SOUTH);

        // Card 3: Construction Task Assignments
        JPanel cardTasks = createReportCard("Site Operations Dispatch Desk", "Compile field assignments status reports, due timelines, construction milestones breakdown and active engineer allocation logs.");
        JPanel tasksFilterWrapper = createFilterRowDeck("Period:", cmbTasksFilter = new JComboBox<>(intervals));
        cmbTasksFilter.setSelectedIndex(2); // Default to Last Month
        JPanel tasksBottomPanel = new JPanel(new BorderLayout());
        tasksBottomPanel.setOpaque(false);
        tasksBottomPanel.add(tasksFilterWrapper, BorderLayout.NORTH);
        JButton btnExportTasks = new JButton("View Tasks Report Preview");       
        styleReportButton(btnExportTasks, new Color(234, 179, 8)); 
        tasksBottomPanel.add(btnExportTasks, BorderLayout.SOUTH);
        cardTasks.add(tasksBottomPanel, BorderLayout.SOUTH);

        mainGrid.add(cardPayroll);
        mainGrid.add(cardLeaves);
        mainGrid.add(cardTasks);
        add(mainGrid, BorderLayout.CENTER);

        // ========================================================
        // EVENT ROUTING DISPATCHERS (Passing Selected Filters)
        // ========================================================
        btnExportPayroll.addActionListener(e -> {
            String filter = cmbPayrollFilter.getSelectedItem().toString();
            launchReportPreviewWizard("Corporate Payroll Master Ledger Summary (" + filter + ")", 1, filter);
        });
        
        btnExportLeaves.addActionListener(e -> {
            String filter = cmbLeavesFilter.getSelectedItem().toString();
            launchReportPreviewWizard("Workforce Leave Application Analytics Ledger (" + filter + ")", 2, filter);
        });
        
        btnExportTasks.addActionListener(e -> {
            String filter = cmbTasksFilter.getSelectedItem().toString();
            launchReportPreviewWizard("Site Operations & Engineering Dispatch Records (" + filter + ")", 3, filter);
        });
    }

    private JPanel createFilterRowDeck(String labelText, JComboBox<String> combo) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        combo.setBackground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        wrapper.add(lbl, BorderLayout.WEST);
        wrapper.add(combo, BorderLayout.CENTER);
        return wrapper;
    }

    private void launchReportPreviewWizard(String title, int type, String timeFilter) {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ReportPreviewDialog previewWizard = new ReportPreviewDialog(topFrame, title, type, timeFilter);
        previewWizard.setVisible(true);
    }

    private JPanel createReportCard(String headline, String description) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                headline, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(15, 23, 42)
        ));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(), new EmptyBorder(18, 18, 18, 18)));

        JTextArea txtDesc = new JTextArea(description);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDesc.setForeground(new Color(100, 116, 139));
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setEditable(false);
        txtDesc.setOpaque(false);

        card.add(txtDesc, BorderLayout.CENTER);
        return card;
    }

    private void styleReportButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(0, 42));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}