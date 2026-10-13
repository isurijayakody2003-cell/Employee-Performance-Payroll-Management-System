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

public class LeaveManagementPanel extends JPanel {

    private JTable tblLeaves;
    private DefaultTableModel tableModel;
    private JButton btnApprove, btnReject, btnRefresh;

    public LeaveManagementPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ========================================================
        // TOP HEADER AREA
        // ========================================================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Employee Leave Request Desk");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSubtitle = new JLabel("Review, approve, or reject employee general and medical leave requests.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        topBar.add(titlePanel, BorderLayout.WEST);

        btnRefresh = new JButton("Refresh Requests");
        styleActionStaticButton(btnRefresh, new Color(100, 116, 139));
        topBar.add(btnRefresh, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ========================================================
        // CENTER DATA GRID (Table)
        // ========================================================
        String[] columns = {"Leave ID", "Emp ID", "Full Name", "Leave Type", "Start Date", "End Date", "Reason", "Status"};
        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblLeaves = new JTable(tableModel);
        styleModernDataGrid(tblLeaves);

        JScrollPane scrollGridWrapper = new JScrollPane(tblLeaves);
        scrollGridWrapper.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollGridWrapper.getViewport().setBackground(Color.WHITE);
        add(scrollGridWrapper, BorderLayout.CENTER);

        // ========================================================
        // BOTTOM ACTION TOOL DECK (Approve / Reject Control)
        // ========================================================
        JPanel actionDeckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionDeckPanel.setOpaque(false);

        btnApprove = new JButton("Approve Request");
        btnReject = new JButton("Reject Request");

        styleActionStaticButton(btnApprove, new Color(34, 197, 94)); // Emerald Green
        styleActionStaticButton(btnReject, new Color(239, 68, 68));   // Flat Red

        actionDeckPanel.add(btnApprove);
        actionDeckPanel.add(btnReject);
        add(actionDeckPanel, BorderLayout.SOUTH);

        // ========================================================
        // CORE EVENT TRIGGERS
        // ========================================================
        btnRefresh.addActionListener(e -> loadPendingLeaveRequests());
        
        btnApprove.addActionListener(e -> updateLeaveStatus("Approved"));
        btnReject.addActionListener(e -> updateLeaveStatus("Rejected"));

        // Boot data logs
        loadPendingLeaveRequests();
    }

    // ========================================================
    // DATABASE FETCH PIPELINE
    // ========================================================
    private void loadPendingLeaveRequests() {
        tableModel.setRowCount(0);

        String query = "SELECT l.leave_id, l.employee_id, e.full_name, l.leave_type, l.start_date, l.end_date, l.reason, l.status " +
                       "FROM leaves l " +
                       "INNER JOIN employees e ON l.employee_id = e.employee_id " +
                       "ORDER BY l.leave_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("leave_id"),
                    rs.getString("employee_id"),
                    rs.getString("full_name"),
                    rs.getString("leave_type"),
                    rs.getDate("start_date").toString(),
                    rs.getDate("end_date").toString(),
                    rs.getString("reason"),
                    rs.getString("status")
                });
            }
        } catch (Exception ex) {
            System.out.println("Leave Extraction Failure: " + ex.getMessage());
        }
    }

    // ========================================================
    // DATABASE UPDATE ACTION (With Advanced Security Guard Rules)
    // ========================================================
    private void updateLeaveStatus(String newStatus) {
        int selectedRow = tblLeaves.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a leave request row from the ledger first!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String leaveId = tblLeaves.getValueAt(selectedRow, 0).toString();
        
        // 🔮 FIXED BUG: Column Index එක 8 සිට නිවැරදි 7 දක්වා වෙනස් කළා (Status තියෙන්නේ 7වැනි ස්ථානයේ)
        String currentStatus = tblLeaves.getValueAt(selectedRow, 7).toString(); 

        // 🔮 RULE 1: දැනටමත් Approved නම්, කිසිසේත්ම වෙනස් කිරීමට හෝ Reject කිරීමට ඉඩ නොදේ!
        if (currentStatus.equalsIgnoreCase("Approved")) {
            JOptionPane.showMessageDialog(this, "Approved leave requests are officially finalized and cannot be modified or rejected!", "Operation Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 🔮 RULE 2: දැනටමත් Rejected නම්, නැවතත් Reject බටන් එක එබුවොත් බ්ලොක් කරයි (හැබැයි Approve කරන්න ඉඩ දෙයි)
        if (currentStatus.equalsIgnoreCase("Rejected") && newStatus.equalsIgnoreCase("Rejected")) {
            JOptionPane.showMessageDialog(this, "This leave request has already been rejected!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sqlUpdate = "UPDATE leaves SET status = ? WHERE leave_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {

            pst.setString(1, newStatus);
            pst.setInt(2, Integer.parseInt(leaveId));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Leave request reference ID " + leaveId + " has been " + newStatus + " successfully.");
            loadPendingLeaveRequests(); // Refresh grid view

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Transaction Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleModernDataGrid(JTable dataGrid) {
        dataGrid.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dataGrid.setRowHeight(38);
        dataGrid.setShowGrid(false);
        dataGrid.setIntercellSpacing(new Dimension(0, 0));
        dataGrid.setSelectionBackground(new Color(241, 245, 249));
        dataGrid.setSelectionForeground(new Color(15, 23, 42));
        dataGrid.setFocusable(false);

        JTableHeader header = dataGrid.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                setBorder(noFocusBorder);
                
                if (isSel) {
                    comp.setBackground(new Color(241, 245, 249));
                } else {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                }

                // Status Badge Coloring logic dynamically
                if (c == 7 && val != null) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (val.toString().equalsIgnoreCase("Approved")) setForeground(new Color(34, 197, 94));
                    else if (val.toString().equalsIgnoreCase("Rejected")) setForeground(new Color(239, 68, 68));
                    else setForeground(new Color(234, 179, 8)); // Yellow for Pending
                } else {
                    setForeground(new Color(51, 65, 85));
                }
                return comp;
            }
        };
        dataGrid.setDefaultRenderer(Object.class, cellRenderer);
    }

    private void styleActionStaticButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(190, 38));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}