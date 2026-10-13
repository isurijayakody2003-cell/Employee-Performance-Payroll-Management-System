package ui;

import db.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StaffDashboard extends JFrame {

    private JPanel sidebarPanel, mainContentPanel, cardDashboard, cardTasks, cardLeaves, cardProfile;
    private CardLayout layoutSwitcher;
    private JButton btnDashboard, btnProfile, btnTasks, btnLeaves, btnLogout; 
    private JLabel lblPendingTasksCount, lblPresentTodayCount, lblLiveClock;
    private JTable tblActiveTasks, tblRecentAttendance;
    private String loggedInEmpId;

    public StaffDashboard(String empId) {
        this.loggedInEmpId = empId;
        setTitle("RN House Plan & Construction - Employee Self Service Workspace");
        setSize(1250, 760);
        setMinimumSize(new Dimension(1100, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ========================================================
        // SIDEBAR NAVIGATION PANEL (Premium Dark Style)
        // ========================================================
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(new Color(11, 19, 36)); 
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 15, 4, 15); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.gridx = 0;

        JPanel logoContainer = new JPanel(); logoContainer.setOpaque(false);
        logoContainer.setLayout(new BoxLayout(logoContainer, BoxLayout.Y_AXIS));
        logoContainer.setBorder(new EmptyBorder(25, 0, 35, 0));
        JLabel lblLogoText1 = new JLabel("RN HOUSE PLAN");
        lblLogoText1.setFont(new Font("Segoe UI", Font.BOLD, 20)); lblLogoText1.setForeground(new Color(56, 189, 248)); lblLogoText1.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoContainer.add(lblLogoText1);

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0); sidebarPanel.add(logoContainer, gbc);

        gbc.insets = new Insets(6, 15, 6, 15);
        btnDashboard = new JButton("   Workspace Dashboard");
        btnProfile = new JButton("   My Personal Profile");
        btnTasks = new JButton("   My Assigned Tasks");
        btnLeaves = new JButton("   Apply / View Leaves");
        btnLogout = new JButton("   Logout System");

        styleSidebarButton(btnDashboard, true); styleSidebarButton(btnProfile, false);
        styleSidebarButton(btnTasks, false); styleSidebarButton(btnLeaves, false);

        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnLogout.setBackground(new Color(239, 68, 68)); btnLogout.setForeground(Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(0, 45)); btnLogout.setFocusPainted(false); btnLogout.setBorderPainted(false);

        gbc.gridy = 1; sidebarPanel.add(btnDashboard, gbc);
        gbc.gridy = 2; sidebarPanel.add(btnProfile, gbc);
        gbc.gridy = 3; sidebarPanel.add(btnTasks, gbc);
        gbc.gridy = 4; sidebarPanel.add(btnLeaves, gbc);

        gbc.weighty = 1.0; gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 5; gbc.insets = new Insets(6, 15, 30, 15); sidebarPanel.add(btnLogout, gbc);
        add(sidebarPanel, BorderLayout.WEST);

        // ========================================================
        // CENTER MULTI-CARD INTERFACE CONTENT
        // ========================================================
        layoutSwitcher = new CardLayout();
        mainContentPanel = new JPanel(layoutSwitcher);
        
        initStaffOverviewCardDeck(); // Populate Premium Home Dashboard
        cardProfile = new ProfilePanel(loggedInEmpId); 
        cardTasks = new MyAssignedTasksPanel(loggedInEmpId); 
        cardLeaves = new MyLeavesPanel(loggedInEmpId);

        mainContentPanel.add(cardDashboard, "DASHBOARD");
        mainContentPanel.add(cardProfile, "PROFILE");
        mainContentPanel.add(cardTasks, "TASKS");
        mainContentPanel.add(cardLeaves, "LEAVES");
        add(mainContentPanel, BorderLayout.CENTER);

        // Action Routing Routing Switches
        btnDashboard.addActionListener(e -> { setActiveTab(btnDashboard); layoutSwitcher.show(mainContentPanel, "DASHBOARD"); fetchLiveSQLStatistics(); });
        btnProfile.addActionListener(e -> { setActiveTab(btnProfile); layoutSwitcher.show(mainContentPanel, "PROFILE"); });
        btnTasks.addActionListener(e -> { setActiveTab(btnTasks); layoutSwitcher.show(mainContentPanel, "TASKS"); ((MyAssignedTasksPanel)cardTasks).loadMyTasksFromDatabase(); });
        btnLeaves.addActionListener(e -> { setActiveTab(btnLeaves); layoutSwitcher.show(mainContentPanel, "LEAVES"); ((MyLeavesPanel)cardLeaves).loadMyLeaveLogsHistory(); });
        
        btnLogout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Logoff secure session?", "Notice", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                new LoginForm().setVisible(true); this.dispose();
            }
        });

        startLiveClockEngine();
        fetchLiveSQLStatistics();
    }

    private void initStaffOverviewCardDeck() {
        cardDashboard = new JPanel(new BorderLayout(0, 25));
        cardDashboard.setBackground(new Color(243, 244, 246));
        cardDashboard.setBorder(new EmptyBorder(30, 35, 30, 35));

        JPanel topHeaderBlock = new JPanel(new BorderLayout()); topHeaderBlock.setOpaque(false);
        JPanel leftText = new JPanel(new GridLayout(2, 1, 0, 2)); leftText.setOpaque(false);
        JLabel lblTitle = new JLabel("Welcome Back to Workspace Desk");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24)); lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSub = new JLabel("Real-time personalized operation and telemetry feed analytics.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblSub.setForeground(new Color(100, 116, 139));
        leftText.add(lblTitle); leftText.add(lblSub);

        lblLiveClock = new JLabel("", SwingConstants.RIGHT);
        lblLiveClock.setFont(new Font("Consolas", Font.BOLD, 15)); lblLiveClock.setForeground(new Color(30, 41, 59));
        lblLiveClock.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)), new EmptyBorder(8, 15, 8, 15)));
        topHeaderBlock.add(leftText, BorderLayout.WEST); topHeaderBlock.add(lblLiveClock, BorderLayout.EAST);
        cardDashboard.add(topHeaderBlock, BorderLayout.NORTH);

        // Personalized Employee KPI Metrics Cards
        JPanel rowKPIContainer = new JPanel(new GridLayout(1, 2, 25, 0)); rowKPIContainer.setOpaque(false);
        lblPendingTasksCount = new JLabel("0"); lblPresentTodayCount = new JLabel("0");
        rowKPIContainer.add(buildPrestigeKPICard("MY PENDING ASSIGNED FIELD TASKS", lblPendingTasksCount, new Color(234, 179, 8)));
        rowKPIContainer.add(buildPrestigeKPICard("MY PRESENT DAYS COUNT (THIS MONTH)", lblPresentTodayCount, new Color(34, 197, 94)));

        // Data Tables Grid Previews
        JPanel lowerVisualRowBlock = new JPanel(new GridLayout(1, 2, 25, 0)); lowerVisualRowBlock.setOpaque(false);
        lowerVisualRowBlock.setPreferredSize(new Dimension(0, 360));

        JPanel pnlTasksWidget = new JPanel(new BorderLayout(0, 12)); pnlTasksWidget.setBackground(Color.WHITE);
        pnlTasksWidget.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), new EmptyBorder(20, 20, 20, 20)));
        JLabel lblTskTitle = new JLabel("My Current Operation Timelines"); lblTskTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); pnlTasksWidget.add(lblTskTitle, BorderLayout.NORTH);
        String[] tskHeaders = {"Emp ID", "Task Title", "Timeline Due", "Status"};
        tblActiveTasks = new JTable(new DefaultTableModel(new Object[][]{}, tskHeaders)); styleModernDataGrid(tblActiveTasks);
        JScrollPane scrollTasks = new JScrollPane(tblActiveTasks); scrollTasks.getViewport().setBackground(Color.WHITE); scrollTasks.setBorder(BorderFactory.createEmptyBorder());
        pnlTasksWidget.add(scrollTasks, BorderLayout.CENTER);

        JPanel pnlAttendanceWidget = new JPanel(new BorderLayout(0, 12)); pnlAttendanceWidget.setBackground(Color.WHITE);
        pnlAttendanceWidget.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), new EmptyBorder(20, 20, 20, 20)));
        JLabel lblAttTitle = new JLabel("My Recent Daily Punch Feeds"); lblAttTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); pnlAttendanceWidget.add(lblAttTitle, BorderLayout.NORTH);
        String[] attHeaders = {"Emp ID", "Full Name", "Punch Time", "Status"};
        tblRecentAttendance = new JTable(new DefaultTableModel(new Object[][]{}, attHeaders)); styleModernDataGrid(tblRecentAttendance);
        JScrollPane scrollAtt = new JScrollPane(tblRecentAttendance); scrollAtt.getViewport().setBackground(Color.WHITE); scrollAtt.setBorder(BorderFactory.createEmptyBorder());
        pnlAttendanceWidget.add(scrollAtt, BorderLayout.CENTER);

        lowerVisualRowBlock.add(pnlTasksWidget); lowerVisualRowBlock.add(pnlAttendanceWidget);

        JPanel middleGroupingWrapper = new JPanel(new BorderLayout(0, 25)); middleGroupingWrapper.setOpaque(false);
        middleGroupingWrapper.add(rowKPIContainer, BorderLayout.NORTH); middleGroupingWrapper.add(lowerVisualRowBlock, BorderLayout.CENTER);
        cardDashboard.add(middleGroupingWrapper, BorderLayout.CENTER);
    }

    private JPanel buildPrestigeKPICard(String headlineText, JLabel metricCountValue, Color dynamicAccentBarColor) {
        JPanel tileCardPanel = new JPanel(new BorderLayout(0, 12)); tileCardPanel.setBackground(Color.WHITE);
        tileCardPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1), new EmptyBorder(20, 22, 20, 22)));
        JLabel lblHead = new JLabel(headlineText); lblHead.setFont(new Font("Segoe UI", Font.BOLD, 11)); lblHead.setForeground(new Color(148, 163, 184));
        metricCountValue.setFont(new Font("Segoe UI", Font.BOLD, 26)); metricCountValue.setForeground(new Color(15, 23, 42));
        JPanel bottomAccentLine = new JPanel(); bottomAccentLine.setBackground(dynamicAccentBarColor); bottomAccentLine.setPreferredSize(new Dimension(0, 4));
        tileCardPanel.add(lblHead, BorderLayout.NORTH); tileCardPanel.add(metricCountValue, BorderLayout.CENTER); tileCardPanel.add(bottomAccentLine, BorderLayout.SOUTH);
        return tileCardPanel;
    }

    private void styleModernDataGrid(JTable dataGrid) {
        dataGrid.setFont(new Font("Segoe UI", Font.PLAIN, 13)); dataGrid.setRowHeight(36); dataGrid.setShowGrid(false);
        dataGrid.setIntercellSpacing(new Dimension(0, 0)); dataGrid.setSelectionBackground(new Color(241, 245, 249)); dataGrid.setFocusable(false);
        JTableHeader dataGridHeader = dataGrid.getTableHeader(); dataGridHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dataGridHeader.setBackground(new Color(248, 250, 252)); dataGridHeader.setForeground(new Color(71, 85, 105));
        dataGridHeader.setPreferredSize(new Dimension(0, 35)); dataGridHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component compInstance = super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c); setBorder(noFocusBorder);
                compInstance.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                if (val != null && (val.toString().equals("Pending") || val.toString().equals("In Progress") || val.toString().equals("Present"))) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (val.toString().equals("Present")) setForeground(new Color(34, 197, 94));
                    else if (val.toString().equals("Pending")) setForeground(new Color(234, 179, 8));
                    else setForeground(new Color(14, 165, 233));
                } else { setForeground(new Color(51, 65, 85)); }
                return compInstance;
            }
        };
        dataGrid.setDefaultRenderer(Object.class, renderer);
    }

    private void fetchLiveSQLStatistics() {
        DefaultTableModel modelTasks = (DefaultTableModel) tblActiveTasks.getModel();
        DefaultTableModel modelAttendance = (DefaultTableModel) tblRecentAttendance.getModel();
        modelTasks.setRowCount(0); modelAttendance.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String qTask = "SELECT employee_id, task_title, due_date, status FROM tasks WHERE employee_id = ? AND status != 'Completed'";
            try (PreparedStatement pst = conn.prepareStatement(qTask)) {
                pst.setString(1, loggedInEmpId);
                try (ResultSet rs = pst.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++; modelTasks.addRow(new Object[]{rs.getString("employee_id"), rs.getString("task_title"), rs.getDate("due_date").toString(), rs.getString("status")});
                    }
                    lblPendingTasksCount.setText(String.valueOf(count));
                }
            }
            String qAttCount = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND MONTH(date) = MONTH(CURDATE()) AND attendance_status = 'Present'";
            try (PreparedStatement pst = conn.prepareStatement(qAttCount)) {
                pst.setString(1, loggedInEmpId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) lblPresentTodayCount.setText(String.valueOf(rs.getInt(1)));
                }
            }
            String qAttLog = "SELECT a.employee_id, e.full_name, a.check_in, a.attendance_status FROM attendance a INNER JOIN employees e ON a.employee_id = e.employee_id WHERE a.employee_id = ? ORDER BY a.attendance_id DESC LIMIT 8";
            try (PreparedStatement pst = conn.prepareStatement(qAttLog)) {
                pst.setString(1, loggedInEmpId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        modelAttendance.addRow(new Object[]{rs.getString("employee_id"), rs.getString("full_name"), rs.getTime("check_in") != null ? rs.getTime("check_in").toString() : "--:--:--", rs.getString("attendance_status")});
                    }
                }
            }
        } catch (Exception ex) { System.out.println("Error: " + ex.getMessage()); }
    }

    private void startLiveClockEngine() {
        Timer liveClockTimer = new Timer(1000, e -> lblLiveClock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd   hh:mm:ss a"))));
        liveClockTimer.start();
    }

    private void setActiveTab(JButton activeBtn) {
        JButton[] group = {btnDashboard, btnProfile, btnTasks, btnLeaves};
        for (JButton b : group) {
            b.setBackground(b == activeBtn ? new Color(22, 32, 51) : new Color(11, 19, 36));
            b.setForeground(b == activeBtn ? new Color(56, 189, 248) : new Color(148, 163, 184));
        }
    }

    private void styleSidebarButton(JButton btn, boolean isActive) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setPreferredSize(new Dimension(0, 46)); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(isActive ? new Color(22, 32, 51) : new Color(11, 19, 36));
        btn.setForeground(isActive ? new Color(56, 189, 248) : new Color(148, 163, 184));
    }
}