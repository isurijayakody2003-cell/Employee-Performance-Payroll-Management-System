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

public class AttendanceTrackerPanel extends JPanel {

    private JTable tblAttendance;
    private DefaultTableModel tableModel;
    private JTextField txtDateFilter;
    private JComboBox<String> cmbEmpIdFilter; //  FIXED: JTextField වෙනුවට JComboBox කළා
    private JButton btnFilter, btnReset, btnManualCheckIn, btnManualCheckOut;

    public AttendanceTrackerPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ========================================================
        // TOP CONTROL DECK (Title & Advanced Filters)
        // ========================================================
        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        // Header Titles
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Workforce Attendance & Punch Log Registry");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSubtitle = new JLabel("Monitor live field biometric entries, shift logs, and manage manual site check-ins.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        topContainer.add(titlePanel, BorderLayout.WEST);

        // Filter Controls Deck Row
        JPanel filterRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterRowPanel.setBackground(new Color(248, 250, 252));
        filterRowPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));


        cmbEmpIdFilter = new JComboBox<>();
        cmbEmpIdFilter.setPreferredSize(new Dimension(200, 32));
        cmbEmpIdFilter.setBackground(Color.WHITE);
        cmbEmpIdFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtDateFilter = new JTextField(LocalDate.now().toString()); // Default to current date
        txtDateFilter.setPreferredSize(new Dimension(130, 32));
        txtDateFilter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1), new EmptyBorder(0, 8, 0, 8)));

        btnFilter = new JButton("Apply Filters");
        btnReset = new JButton("Reset View");
        styleActionMiniButton(btnFilter, new Color(30, 41, 59));
        styleActionMiniButton(btnReset, new Color(148, 163, 184));

        filterRowPanel.add(new JLabel("Select Employee:"));
        filterRowPanel.add(cmbEmpIdFilter);
        filterRowPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        filterRowPanel.add(txtDateFilter);
        filterRowPanel.add(btnFilter);
        filterRowPanel.add(btnReset);
        topContainer.add(filterRowPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // ========================================================
        // CENTER LOGS VIEW (The Attendance Data Grid)
        // ========================================================
        String[] columns = {"Log ID", "Employee ID", "Full Name", "Date", "Check-In", "Check-Out", "Hours Worked", "OT Hours", "Status"};
        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblAttendance = new JTable(tableModel);
        styleModernDataGrid(tblAttendance);

        JScrollPane scrollGridWrapper = new JScrollPane(tblAttendance);
        scrollGridWrapper.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollGridWrapper.getViewport().setBackground(Color.WHITE);
        add(scrollGridWrapper, BorderLayout.CENTER);

        // ========================================================
        // BOTTOM ACTION TOOL DECK (Check In/Out Controls)
        // ========================================================
        JPanel actionDeckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionDeckPanel.setOpaque(false);

        btnManualCheckIn = new JButton("Manual Check-In");
        btnManualCheckOut = new JButton("Manual Check-Out");

        styleActionStaticButton(btnManualCheckIn, new Color(34, 197, 94));    
        styleActionStaticButton(btnManualCheckOut, new Color(239, 68, 68));  

        actionDeckPanel.add(btnManualCheckIn);
        actionDeckPanel.add(btnManualCheckOut);
        add(actionDeckPanel, BorderLayout.SOUTH);

        // ========================================================
        // CORE EVENT HANDLERS
        // ========================================================
        

        btnFilter.addActionListener(e -> {
            String selectedItem = cmbEmpIdFilter.getSelectedItem().toString();
            
            String empId = selectedItem.startsWith("ALL") ? "" : selectedItem.split(" - ")[0];
            fetchAttendanceSQLRecords(txtDateFilter.getText().trim(), empId);
        });

        btnReset.addActionListener(e -> {
            cmbEmpIdFilter.setSelectedIndex(0); 
            txtDateFilter.setText(LocalDate.now().toString());
            fetchAttendanceSQLRecords(LocalDate.now().toString(), "");
        });

        btnManualCheckIn.addActionListener(e -> {
            DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
            String sqlFetchActive = "SELECT employee_id, full_name FROM employees WHERE status = 'Active' ORDER BY employee_id ASC";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sqlFetchActive);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    comboModel.addElement(rs.getString("employee_id") + " - " + rs.getString("full_name"));
                }
            } catch (Exception ex) {
                System.out.println("Selector Query Error: " + ex.getMessage());
            }

            if (comboModel.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "No active registered employees found in the system!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JComboBox<String> cmbEmployees = new JComboBox<>(comboModel);
            cmbEmployees.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cmbEmployees.setBackground(Color.WHITE);

            JPanel popPanel = new JPanel(new BorderLayout(0, 8));
            popPanel.setOpaque(false);
            JLabel lblSelect = new JLabel("Select Employee for Site Check-In:");
            lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSelect.setForeground(new Color(100, 116, 139));
            popPanel.add(lblSelect, BorderLayout.NORTH);
            popPanel.add(cmbEmployees, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(this, popPanel, "Manual Check-In Wizard", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION && cmbEmployees.getSelectedItem() != null) {
                String selectedValue = cmbEmployees.getSelectedItem().toString();
                String empId = selectedValue.split(" - ")[0];
                executeManualCheckIn(empId);
            }
        });

        btnManualCheckOut.addActionListener(e -> {
            int selectedRow = tblAttendance.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an active checked-in employee row from table to Check-Out!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String logId = tblAttendance.getValueAt(selectedRow, 0).toString();
            String status = tblAttendance.getValueAt(selectedRow, 8).toString();

            if (!status.equalsIgnoreCase("Present")) {
                JOptionPane.showMessageDialog(this, "Cannot execute check-out on an inactive or already finalized attendance log!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            executeManualCheckOut(logId);
        });


        populateEmpIdFilter();
        
        // Load current date records instantly on creation
        fetchAttendanceSQLRecords(LocalDate.now().toString(), "");
    }


    private void populateEmpIdFilter() {
        cmbEmpIdFilter.removeAllItems();
        cmbEmpIdFilter.addItem("ALL - All Employees"); 
        
        String sql = "SELECT employee_id, full_name FROM employees ORDER BY employee_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                cmbEmpIdFilter.addItem(rs.getString("employee_id") + " - " + rs.getString("full_name"));
            }
        } catch (Exception ex) {
            System.out.println("Error populating filter dropdown: " + ex.getMessage());
        }
    }

    public void fetchAttendanceSQLRecords(String dateFilter, String empIdFilter) {
        tableModel.setRowCount(0);

        String query = "SELECT a.attendance_id, a.employee_id, e.full_name, a.date, a.check_in, a.check_out, a.working_hours, a.ot_hours, a.attendance_status " +
                       "FROM attendance a " +
                       "INNER JOIN employees e ON a.employee_id = e.employee_id WHERE 1=1 ";

        if (!dateFilter.isEmpty()) query += "AND a.date = ? ";
        if (!empIdFilter.isEmpty()) query += "AND a.employee_id = ? ";
        
        query += "ORDER BY a.attendance_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (!dateFilter.isEmpty()) pst.setString(paramIndex++, dateFilter);
            if (!empIdFilter.isEmpty()) pst.setString(paramIndex++, empIdFilter);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("attendance_id"),
                        rs.getString("employee_id"),
                        rs.getString("full_name"),
                        rs.getDate("date").toString(),
                        rs.getTime("check_in") != null ? rs.getTime("check_in").toString() : "--:--",
                        rs.getTime("check_out") != null ? rs.getTime("check_out").toString() : "--:--",
                        rs.getDouble("working_hours") + " hrs",
                        rs.getDouble("ot_hours") + " hrs",
                        rs.getString("attendance_status")
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Logs Extraction Refusal: " + ex.getMessage(), "DB Failure", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeManualCheckIn(String empId) {
        String insertPunch = "INSERT INTO attendance (employee_id, date, check_in, attendance_status) VALUES (?, CURDATE(), CURTIME(), 'Present')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstInsert = conn.prepareStatement(insertPunch)) {
            pstInsert.setString(1, empId);
            pstInsert.executeUpdate();
            JOptionPane.showMessageDialog(this, "Punch Entry Logged! Employee " + empId + " checked in successfully.");
            fetchAttendanceSQLRecords(LocalDate.now().toString(), ""); 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Punch Error: " + ex.getMessage(), "DB Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeManualCheckOut(String logId) {
        String updatePunch = "UPDATE attendance SET check_out = CURTIME(), " +
                             "working_hours = ROUND(TIMESTAMPDIFF(MINUTE, check_in, CURTIME()) / 60.0, 2), " +
                             "ot_hours = IF(ROUND(TIMESTAMPDIFF(MINUTE, check_in, CURTIME()) / 60.0, 2) > 8.0, ROUND((TIMESTAMPDIFF(MINUTE, check_in, CURTIME()) / 60.0) - 8.0, 2), 0.0) " +
                             "WHERE attendance_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(updatePunch)) {

            pst.setInt(1, Integer.parseInt(logId));
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Punch-Out Logged! Time stamp captured and analytics calculated.");
            fetchAttendanceSQLRecords(LocalDate.now().toString(), ""); 

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Punch Out Failure: " + ex.getMessage(), "DB Transaction Error", JOptionPane.ERROR_MESSAGE);
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
                    comp.setBackground(new Color(34, 197, 94)); 
                    comp.setForeground(Color.WHITE);
                } else {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                    if (c == 8 && val != null) {
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        setForeground(new Color(34, 197, 94)); 
                    } else {
                        setForeground(new Color(51, 65, 85));
                    }
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
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleActionMiniButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(110, 32));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}