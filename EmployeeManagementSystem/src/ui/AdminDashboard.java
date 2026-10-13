package ui;

import db.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboard extends JFrame {

    private JPanel sidebarPanel, mainContentPanel, cardDashboard, cardEmployees, cardAttendance, cardPayroll, cardTasks, cardLeaves, cardReports;
    private CardLayout layoutSwitcher;
    private JButton btnDashboard, btnManageEmp, btnAttendance, btnPayroll, btnTasks, btnLeaves, btnReports, btnLogout; 
    
    // Core Dynamic Counters (From DB Tables)
    private JLabel lblTotalEmpCount, lblPendingTasksCount, lblPresentTodayCount, lblTotalPayrollCost, lblLiveClock;
    
    // Dynamic Tables (From DB Tables)
    private JTable tblActiveTasks, tblRecentAttendance;

    public AdminDashboard() {
        setTitle("RN House Plan & Construction - Admin Executive ERP");
        setSize(1250, 760);
        setMinimumSize(new Dimension(1100, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ========================================================
        // 1. PREMIUM SIDEBAR PANEL (Tailwind Slate-950 Theme)
        // ========================================================
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(new Color(11, 19, 36)); 
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 15, 4, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Branding Area
        JPanel logoContainer = new JPanel();
        logoContainer.setOpaque(false);
        logoContainer.setLayout(new BoxLayout(logoContainer, BoxLayout.Y_AXIS));
        logoContainer.setBorder(new EmptyBorder(25, 0, 35, 0));

        JLabel lblLogoText1 = new JLabel("RN HOUSE PLAN");
        lblLogoText1.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogoText1.setForeground(new Color(56, 189, 248)); // Neon Sky Blue Accent
        lblLogoText1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLogoText2 = new JLabel("& CONSTRUCTION");
        lblLogoText2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLogoText2.setForeground(new Color(148, 163, 184));
        lblLogoText2.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoContainer.add(lblLogoText1);
        logoContainer.add(Box.createVerticalStrut(3));
        logoContainer.add(lblLogoText2);

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);
        sidebarPanel.add(logoContainer, gbc);

        // Navigation Links
        gbc.insets = new Insets(6, 15, 6, 15);
        btnDashboard = new JButton("   Dashboard Overview");
        btnManageEmp = new JButton("   Employee Registry");
        btnAttendance = new JButton("   Attendance Tracker");
        btnPayroll = new JButton("   Payroll & Salaries");
        btnTasks = new JButton("   Task Assignments");
        btnLeaves = new JButton("   Leave Management"); 
        btnReports = new JButton("   Management Reports"); 

        styleSidebarButton(btnDashboard, true); 
        styleSidebarButton(btnManageEmp, false);
        styleSidebarButton(btnAttendance, false);
        styleSidebarButton(btnPayroll, false);
        styleSidebarButton(btnTasks, false);
        styleSidebarButton(btnLeaves, false);
        styleSidebarButton(btnReports, false);
        
        btnLogout = new JButton("   Logout System");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setBackground(new Color(239, 68, 68)); 
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(0, 45));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);

        gbc.gridy = 1; sidebarPanel.add(btnDashboard, gbc);
        gbc.gridy = 2; sidebarPanel.add(btnManageEmp, gbc);
        gbc.gridy = 3; sidebarPanel.add(btnAttendance, gbc);
        gbc.gridy = 4; sidebarPanel.add(btnPayroll, gbc);
        gbc.gridy = 5; sidebarPanel.add(btnTasks, gbc);
        gbc.gridy = 6; sidebarPanel.add(btnLeaves, gbc); 
        gbc.gridy = 7; sidebarPanel.add(btnReports, gbc); 

        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 8; gbc.insets = new Insets(6, 15, 30, 15);
        sidebarPanel.add(btnLogout, gbc);

        add(sidebarPanel, BorderLayout.WEST);

        // ========================================================
        // 2. WORKSPACE PANELS (CardLayout Switcher)
        // ========================================================
        layoutSwitcher = new CardLayout();
        mainContentPanel = new JPanel(layoutSwitcher);
        mainContentPanel.setBackground(new Color(248, 250, 252));

        initDashboardCard(); 
        initPlaceholderCards(); 

        mainContentPanel.add(cardDashboard, "DASHBOARD");
        mainContentPanel.add(cardEmployees, "EMPLOYEES");
        mainContentPanel.add(cardAttendance, "ATTENDANCE");
        mainContentPanel.add(cardPayroll, "PAYROLL");
        mainContentPanel.add(cardTasks, "TASKS");
        mainContentPanel.add(cardLeaves, "LEAVES"); 
        mainContentPanel.add(cardReports, "REPORTS"); 

        add(mainContentPanel, BorderLayout.CENTER);

        // ========================================================
        // 3. EVENT ROUTING LOGIC
        // ========================================================
        btnDashboard.addActionListener(e -> { setActiveTab(btnDashboard); layoutSwitcher.show(mainContentPanel, "DASHBOARD"); fetchLiveSQLStatistics(); });
        btnManageEmp.addActionListener(e -> { setActiveTab(btnManageEmp); layoutSwitcher.show(mainContentPanel, "EMPLOYEES"); });
        btnAttendance.addActionListener(e -> { setActiveTab(btnAttendance); layoutSwitcher.show(mainContentPanel, "ATTENDANCE"); });
        btnPayroll.addActionListener(e -> { setActiveTab(btnPayroll); layoutSwitcher.show(mainContentPanel, "PAYROLL"); });
        btnTasks.addActionListener(e -> { setActiveTab(btnTasks); layoutSwitcher.show(mainContentPanel, "TASKS"); });
        btnLeaves.addActionListener(e -> { setActiveTab(btnLeaves); layoutSwitcher.show(mainContentPanel, "LEAVES"); }); 
        btnReports.addActionListener(e -> { setActiveTab(btnReports); layoutSwitcher.show(mainContentPanel, "REPORTS"); }); 

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to secure logoff?", "Logout Notice", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginForm().setVisible(true);
                this.dispose();
            }
        });

        startLiveClockEngine();
        fetchLiveSQLStatistics(); 
    }

    // ========================================================
    // UI CORE SCREEN MODULE: THE FUNCTIONAL REAL-DATA DASHBOARD
    // ========================================================
    private void initDashboardCard() {
        cardDashboard = new JPanel(new BorderLayout(0, 25));
        cardDashboard.setBackground(new Color(243, 244, 246)); 
        cardDashboard.setBorder(new EmptyBorder(30, 35, 30, 35));

        // Top Header Block
        JPanel topHeaderBlock = new JPanel(new BorderLayout());
        topHeaderBlock.setOpaque(false);

        JPanel leftGreeterText = new JPanel(new GridLayout(2, 1, 0, 2));
        leftGreeterText.setOpaque(false);
        JLabel lblTitle = new JLabel("System Administrator Core Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(15, 23, 42)); 

        JLabel lblSub = new JLabel("Real-time telemetry directly synchronized with RN Construction Master Tables.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));
        leftGreeterText.add(lblTitle);
        leftGreeterText.add(lblSub);

        // Live Clock Component
        lblLiveClock = new JLabel("", SwingConstants.RIGHT);
        lblLiveClock.setFont(new Font("Consolas", Font.BOLD, 15));
        lblLiveClock.setForeground(new Color(30, 41, 59));
        lblLiveClock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1), new EmptyBorder(8, 15, 8, 15)
        ));
        
        topHeaderBlock.add(leftGreeterText, BorderLayout.WEST);
        topHeaderBlock.add(lblLiveClock, BorderLayout.EAST);
        cardDashboard.add(topHeaderBlock, BorderLayout.NORTH);

        // --- KPI MATRIX ROW CONTAINER (Connected to Table 1, 3, 4, 8) ---
        JPanel rowKPIContainer = new JPanel(new GridLayout(1, 4, 25, 0));
        rowKPIContainer.setOpaque(false);

        lblTotalEmpCount = new JLabel("0");
        lblPresentTodayCount = new JLabel("0"); 
        lblPendingTasksCount = new JLabel("0");
        lblTotalPayrollCost = new JLabel("Rs. 0.00");

        rowKPIContainer.add(buildPrestigeKPICard("TOTAL REGISTERED STAFF", lblTotalEmpCount, new Color(14, 165, 233))); 
        rowKPIContainer.add(buildPrestigeKPICard("ATTENDANCE COUNT (TODAY)", lblPresentTodayCount, new Color(34, 197, 94))); 
        rowKPIContainer.add(buildPrestigeKPICard("PENDING FIELD TASKS", lblPendingTasksCount, new Color(234, 179, 8))); 
        rowKPIContainer.add(buildPrestigeKPICard("TOTAL EXPENSE LIABILITY", lblTotalPayrollCost, new Color(168, 85, 247))); 

        // --- LOWER DATA VIEW SPLIT BLOCK (Tables 4 & 8 Real Logs) ---
        JPanel lowerVisualRowBlock = new JPanel(new GridLayout(1, 2, 25, 0)); 
        lowerVisualRowBlock.setOpaque(false);

        // Lower Left: Dynamic Tasks Desk (Table 8)
        JPanel pnlTasksWidget = new JPanel(new BorderLayout(0, 12));
        pnlTasksWidget.setBackground(Color.WHITE);
        pnlTasksWidget.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1), new EmptyBorder(20, 20, 20, 20)));
        
        JLabel lblTskTitle = new JLabel("Active Operations Dispatch Desk (Table 8)");
        lblTskTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTskTitle.setForeground(new Color(15, 23, 42));
        pnlTasksWidget.add(lblTskTitle, BorderLayout.NORTH);

        String[] tskHeaders = {"Emp ID", "Task Title", "Timeline Due", "Status"};
        tblActiveTasks = new JTable(new javax.swing.table.DefaultTableModel(new Object[][]{}, tskHeaders));
        styleModernDataGrid(tblActiveTasks);
        JScrollPane scrollTasks = new JScrollPane(tblActiveTasks);
        scrollTasks.setBorder(BorderFactory.createEmptyBorder());
        scrollTasks.getViewport().setBackground(Color.WHITE);
        pnlTasksWidget.add(scrollTasks, BorderLayout.CENTER);

        // Lower Right: Dynamic Attendance Log (Table 4)
        JPanel pnlAttendanceWidget = new JPanel(new BorderLayout(0, 12));
        pnlAttendanceWidget.setBackground(Color.WHITE);
        pnlAttendanceWidget.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1), new EmptyBorder(20, 20, 20, 20)));

        JLabel lblAttTitle = new JLabel("Live Staff Attendance Feed (Table 4)");
        lblAttTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAttTitle.setForeground(new Color(15, 23, 42));
        pnlAttendanceWidget.add(lblAttTitle, BorderLayout.NORTH);

        String[] attHeaders = {"Emp ID", "Full Name", "Punch Time", "Status"};
        tblRecentAttendance = new JTable(new javax.swing.table.DefaultTableModel(new Object[][]{}, attHeaders));
        styleModernDataGrid(tblRecentAttendance);
        JScrollPane scrollAtt = new JScrollPane(tblRecentAttendance);
        scrollAtt.setBorder(BorderFactory.createEmptyBorder());
        scrollAtt.getViewport().setBackground(Color.WHITE);
        pnlAttendanceWidget.add(scrollAtt, BorderLayout.CENTER);

        lowerVisualRowBlock.add(pnlTasksWidget);
        lowerVisualRowBlock.add(pnlAttendanceWidget);

        // Assembly
        JPanel middleGroupingWrapper = new JPanel(new BorderLayout(0, 25));
        middleGroupingWrapper.setOpaque(false);
        middleGroupingWrapper.add(rowKPIContainer, BorderLayout.NORTH);
        middleGroupingWrapper.add(lowerVisualRowBlock, BorderLayout.CENTER);

        cardDashboard.add(middleGroupingWrapper, BorderLayout.CENTER);
    }

    private JPanel buildPrestigeKPICard(String headlineText, JLabel metricCountValue, Color dynamicAccentBarColor) {
        JPanel tileCardPanel = new JPanel(new BorderLayout(0, 12));
        tileCardPanel.setBackground(Color.WHITE);
        tileCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1), new EmptyBorder(20, 22, 20, 22)));

        JLabel lblHead = new JLabel(headlineText);
        lblHead.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHead.setForeground(new Color(148, 163, 184)); 

        metricCountValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        metricCountValue.setForeground(new Color(15, 23, 42));

        JPanel bottomAccentLine = new JPanel();
        bottomAccentLine.setBackground(dynamicAccentBarColor);
        bottomAccentLine.setPreferredSize(new Dimension(0, 4));

        tileCardPanel.add(lblHead, BorderLayout.NORTH);
        tileCardPanel.add(metricCountValue, BorderLayout.CENTER);
        tileCardPanel.add(bottomAccentLine, BorderLayout.SOUTH);
        return tileCardPanel;
    }

    private void styleModernDataGrid(JTable dataGrid) {
        dataGrid.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dataGrid.setRowHeight(36);
        dataGrid.setShowGrid(false);
        dataGrid.setIntercellSpacing(new Dimension(0, 0));
        dataGrid.setSelectionBackground(new Color(241, 245, 249));
        dataGrid.setFocusable(false);

        JTableHeader dataGridHeader = dataGrid.getTableHeader();
        dataGridHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dataGridHeader.setBackground(new Color(248, 250, 252));
        dataGridHeader.setForeground(new Color(71, 85, 105));
        dataGridHeader.setPreferredSize(new Dimension(0, 35));
        dataGridHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        DefaultTableCellRenderer cellDataCenterRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component compInstance = super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                setBorder(noFocusBorder);
                compInstance.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                
                if (val != null && (val.toString().equals("Pending") || val.toString().equals("In Progress") || val.toString().equals("Present"))) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (val.toString().equals("Present")) setForeground(new Color(34, 197, 94));
                    else if (val.toString().equals("Pending")) setForeground(new Color(234, 179, 8));
                    else setForeground(new Color(14, 165, 233));
                } else {
                    setForeground(new Color(51, 65, 85));
                }
                return compInstance;
            }
        };
        dataGrid.setDefaultRenderer(Object.class, cellDataCenterRenderer);
    }

    private void fetchLiveSQLStatistics() {
        javax.swing.table.DefaultTableModel modelTasks = (javax.swing.table.DefaultTableModel) tblActiveTasks.getModel();
        javax.swing.table.DefaultTableModel modelAttendance = (javax.swing.table.DefaultTableModel) tblRecentAttendance.getModel();
        
        modelTasks.setRowCount(0); 
        modelAttendance.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            
            String q1 = "SELECT COUNT(*) FROM employees";
            try (PreparedStatement pst = conn.prepareStatement(q1); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblTotalEmpCount.setText(String.valueOf(rs.getInt(1)));
            }

            String q2 = "SELECT COUNT(*) FROM tasks WHERE status != 'Completed'";
            try (PreparedStatement pst = conn.prepareStatement(q2); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblPendingTasksCount.setText(String.valueOf(rs.getInt(1)));
            }

            String q3 = "SELECT COUNT(*) FROM attendance WHERE date = CURDATE() AND attendance_status = 'Present'";
            try (PreparedStatement pst = conn.prepareStatement(q3); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblPresentTodayCount.setText(String.valueOf(rs.getInt(1)));
            }

            String q4 = "SELECT SUM(basic_salary + allowances) FROM payroll_details";
            try (PreparedStatement pst = conn.prepareStatement(q4); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double grandTotalCost = rs.getDouble(1);
                    lblTotalPayrollCost.setText(String.format("Rs. %,.2f", grandTotalCost));
                }
            }

            String q5 = "SELECT employee_id, task_title, due_date, status FROM tasks WHERE status != 'Completed' ORDER BY task_id DESC LIMIT 8";
            try (PreparedStatement pst = conn.prepareStatement(q5); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    modelTasks.addRow(new Object[]{
                        rs.getString("employee_id"),
                        rs.getString("task_title"),
                        rs.getDate("due_date").toString(),
                        rs.getString("status")
                    });
                }
            }

            String q6 = "SELECT a.employee_id, e.full_name, a.check_in, a.attendance_status " +
                        "FROM attendance a INNER JOIN employees e ON a.employee_id = e.employee_id " +
                        "ORDER BY a.attendance_id DESC LIMIT 8";
            try (PreparedStatement pst = conn.prepareStatement(q6); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    modelAttendance.addRow(new Object[]{
                        rs.getString("employee_id"),
                        rs.getString("full_name"),
                        rs.getTime("check_in") != null ? rs.getTime("check_in").toString() : "--:--:--",
                        rs.getString("attendance_status")
                    });
                }
            }

        } catch (Exception ex) {
            System.out.println("SQL Synchronizer Pipeline Error: " + ex.getMessage());
        }
    }

    private void startLiveClockEngine() {
        Timer liveClockTimer = new Timer(1000, e -> {
            lblLiveClock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd   hh:mm:ss a")));
        });
        liveClockTimer.start();
    }

    private void setActiveTab(JButton activeBtnTarget) {
        JButton[] linksGroup = {btnDashboard, btnManageEmp, btnAttendance, btnPayroll, btnTasks, btnLeaves, btnReports};
        for (JButton btn : linksGroup) {
            if (btn == activeBtnTarget) {
                btn.setBackground(new Color(22, 32, 51)); 
                btn.setForeground(new Color(56, 189, 248));
            } else {
                btn.setBackground(new Color(11, 19, 36));
                btn.setForeground(new Color(148, 163, 184));
            }
        }
    }

    private void styleSidebarButton(JButton btnInstance, boolean isInitialTabActiveState) {
        btnInstance.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnInstance.setHorizontalAlignment(SwingConstants.LEFT);
        btnInstance.setPreferredSize(new Dimension(0, 46));
        btnInstance.setFocusPainted(false);
        btnInstance.setBorderPainted(false);
        btnInstance.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isInitialTabActiveState) {
            btnInstance.setBackground(new Color(22, 32, 51));
            btnInstance.setForeground(new Color(56, 189, 248));
        } else {
            btnInstance.setBackground(new Color(11, 19, 36));
            btnInstance.setForeground(new Color(148, 163, 184));
        }
    }

    // ========================================================
    // 🛠️ INTEGRATION AREA (FIXED: All Real Panels Loaded)
    // ========================================================
    private void initPlaceholderCards() {
        cardEmployees = new EmployeeManagementPanel(); 
        cardAttendance = new AttendanceTrackerPanel();
        cardPayroll = new PayrollManagementPanel(); 
        cardLeaves = new LeaveManagementPanel();   
        cardReports = new ReportsManagementPanel(); 
        
        cardTasks = new TaskAssignmentsPanel(); 
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}