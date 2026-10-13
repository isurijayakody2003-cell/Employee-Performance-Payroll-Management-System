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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class MyLeavesPanel extends JPanel {

    private JTable tblLeaves;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbLeaveType;
    private JTextField txtStartDate, txtEndDate, txtReason;
    private JButton btnApply;
    private String currentEmpId;

    public MyLeavesPanel(String empId) {
        this.currentEmpId = empId;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Titles
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Leave Vacation Portal");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSubtitle = new JLabel("Submit new vacation requests and track your real-time status matrix logs.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(lblTitle); titlePanel.add(lblSubtitle);
        add(titlePanel, BorderLayout.NORTH);

        // Split Main Workspace View Container Panel
        JPanel splitContainer = new JPanel(new GridLayout(1, 2, 30, 0));
        splitContainer.setOpaque(false);

        // ========================================================
        // LEFT SIDE: Leave Application Form Panel
        // ========================================================
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1), new EmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        String[] types = {"Annual Leave", "Casual Leave", "Medical Leave"};
        cmbLeaveType = new JComboBox<>(types); styleComponent(cmbLeaveType);
        txtStartDate = new JTextField(); styleComponent(txtStartDate);
        txtEndDate = new JTextField(); styleComponent(txtEndDate);
        txtReason = new JTextField(); styleComponent(txtReason);
        
        btnApply = new JButton("Submit Leave Request");
        btnApply.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnApply.setBackground(new Color(34, 197, 94)); // Green Accent
        btnApply.setForeground(Color.WHITE); btnApply.setPreferredSize(new Dimension(0, 38));
        btnApply.setFocusPainted(false); btnApply.setBorderPainted(false);
        btnApply.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(createFormLabel("Select Leave Category:"), gbc);
        gbc.gridy = 1; formPanel.add(cmbLeaveType, gbc);
        gbc.gridy = 2; formPanel.add(createFormLabel("Start Date (YYYY-MM-DD):"), gbc);
        gbc.gridy = 3; formPanel.add(txtStartDate, gbc);
        gbc.gridy = 4; formPanel.add(createFormLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridy = 5; formPanel.add(txtEndDate, gbc);
        gbc.gridy = 6; formPanel.add(createFormLabel("Reason Statement:"), gbc);
        gbc.gridy = 7; formPanel.add(txtReason, gbc);
        gbc.gridy = 8; gbc.insets = new Insets(18, 10, 5, 10); formPanel.add(btnApply, gbc);

        // ========================================================
        // RIGHT SIDE: My Leave Status Table History
        // ========================================================
        String[] gridColumns = {"Leave ID", "Leave Category", "Duration Logs", "Status"};
        tableModel = new DefaultTableModel(new Object[][]{}, gridColumns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblLeaves = new JTable(tableModel);
        styleModernDataGrid(tblLeaves);
        JScrollPane scrollTable = new JScrollPane(tblLeaves);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollTable.getViewport().setBackground(Color.WHITE);

        splitContainer.add(formPanel);
        splitContainer.add(scrollTable);
        add(splitContainer, BorderLayout.CENTER);

        btnApply.addActionListener(e -> triggerLeaveSubmissionPipeline());

        loadMyLeaveLogsHistory();
    }

    public void loadMyLeaveLogsHistory() {
        tableModel.setRowCount(0);
        String query = "SELECT leave_id, leave_type, start_date, end_date, status FROM leaves WHERE employee_id = ? ORDER BY leave_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, currentEmpId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("leave_id"),
                        rs.getString("leave_type"),
                        rs.getDate("start_date") + " to " + rs.getDate("end_date"),
                        rs.getString("status")
                    });
                }
            }
        } catch (Exception ex) {/**/}
    }

    private void triggerLeaveSubmissionPipeline() {
        String lType = cmbLeaveType.getSelectedItem().toString();
        String sDate = txtStartDate.getText().trim();
        String eDate = txtEndDate.getText().trim();
        String reason = txtReason.getText().trim();

        // 1. Check for empty fields
        if (sDate.isEmpty() || eDate.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required input fields completely!", "Validation Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Convert String inputs to LocalDate for secure modern parsing comparison
            LocalDate today = LocalDate.now();
            LocalDate startDate = LocalDate.parse(sDate);
            LocalDate endDate = LocalDate.parse(eDate);

            // 2. 🔮 CRITICAL VALIDATION: Start Date must be a future date (strictly after today)
            if (!startDate.isAfter(today)) {
                JOptionPane.showMessageDialog(this, "Start Date must be a future date after today!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. 🔮 CRITICAL VALIDATION: End Date must be after the Start Date
            if (!endDate.isAfter(startDate)) {
                JOptionPane.showMessageDialog(this, "End Date must be a date after the chosen Start Date!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Database Insertion Transaction Pipeline
            String sqlInsert = "INSERT INTO leaves (employee_id, leave_type, start_date, end_date, reason, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sqlInsert)) {
                
                pst.setString(1, currentEmpId);
                pst.setString(2, lType);
                pst.setDate(3, java.sql.Date.valueOf(startDate));
                pst.setDate(4, java.sql.Date.valueOf(endDate));
                pst.setString(5, reason);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Your official leave request has been submitted successfully for verification.", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Reset text components fields entries
                txtStartDate.setText(""); txtEndDate.setText(""); txtReason.setText("");
                loadMyLeaveLogsHistory(); // Synchronize UI Table data logs
            }
            
        } catch (DateTimeParseException formatEx) {
            JOptionPane.showMessageDialog(this, "Invalid Date format parameters! Please use YYYY-MM-DD syntax structure.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Transaction Failure: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createFormLabel(String labelText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(71, 85, 105)); return lbl;
    }

    private void styleComponent(JComponent c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13)); c.setPreferredSize(new Dimension(0, 36));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
    }

    private void styleModernDataGrid(JTable dataGrid) {
        dataGrid.setFont(new Font("Segoe UI", Font.PLAIN, 13)); dataGrid.setRowHeight(36);
        dataGrid.setShowGrid(false); dataGrid.setIntercellSpacing(new Dimension(0, 0)); dataGrid.setFocusable(false);

        JTableHeader header = dataGrid.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252)); header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                setBorder(noFocusBorder); comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                if (c == 3 && val != null) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (val.toString().equalsIgnoreCase("Approved")) setForeground(new Color(34, 197, 94));
                    else if (val.toString().equalsIgnoreCase("Pending")) setForeground(new Color(234, 179, 8));
                    else setForeground(new Color(239, 68, 68));
                } else { setForeground(new Color(51, 65, 85)); }
                return comp;
            }
        };
        dataGrid.setDefaultRenderer(Object.class, renderer);
    }
}