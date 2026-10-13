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

public class EmployeeManagementPanel extends JPanel {

    private JTable tblEmployees;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnSearch, btnAdd, btnUpdate, btnDelete, btnRefresh, btnViewFull;

    public EmployeeManagementPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ========================================================
        // TOP CONTROL BAR (Title + Search Box)
        // ========================================================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2)); 
        titlePanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Employee Master Registry");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        
        JLabel lblSubtitle = new JLabel("View, search, and manage all registered corporate workforce deployment logs.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        topBar.add(titlePanel, BorderLayout.WEST);

        // Search Action Right
        JPanel searchBoxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBoxPanel.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1), new EmptyBorder(0, 10, 0, 10)));
        
        btnSearch = new JButton("Search");
        styleActionStaticButton(btnSearch, new Color(30, 41, 59)); 

        searchBoxPanel.add(new JLabel("Find Employee: "));
        searchBoxPanel.add(txtSearch);
        searchBoxPanel.add(btnSearch);
        topBar.add(searchBoxPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ========================================================
        // CENTER DATA GRID VIEW (The Table)
        // ========================================================
        String[] columns = {"Employee ID", "Full Name", "NIC / Passport", "Job Title", "Department", "Status"};
        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        
        tblEmployees = new JTable(tableModel);
        styleModernDataGrid(tblEmployees);
        
        JScrollPane scrollGridWrapper = new JScrollPane(tblEmployees);
        scrollGridWrapper.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollGridWrapper.getViewport().setBackground(Color.WHITE);
        add(scrollGridWrapper, BorderLayout.CENTER);

        // ========================================================
        // BOTTOM ACTION CONTROL DECK
        // ========================================================
        JPanel actionDeckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionDeckPanel.setOpaque(false);

        btnViewFull = new JButton("View Full Profile"); 
        btnAdd = new JButton("Add New Employee");
        btnUpdate = new JButton("Update Employee");
        btnDelete = new JButton("Terminate / Delete");
        btnRefresh = new JButton("Refresh Table");

        styleActionStaticButton(btnViewFull, new Color(168, 85, 247)); 
        styleActionStaticButton(btnAdd, new Color(34, 197, 94));    
        styleActionStaticButton(btnUpdate, new Color(14, 165, 233)); 
        styleActionStaticButton(btnDelete, new Color(239, 68, 68));  
        styleActionStaticButton(btnRefresh, new Color(100, 116, 139)); 

        actionDeckPanel.add(btnViewFull); 
        actionDeckPanel.add(btnAdd);
        actionDeckPanel.add(btnUpdate);
        actionDeckPanel.add(btnDelete);
        actionDeckPanel.add(btnRefresh);
        add(actionDeckPanel, BorderLayout.SOUTH);

        // ========================================================
        // CORE EVENT TRIGGERS
        // ========================================================
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadEmployeeMasterRecords(""); });
        btnSearch.addActionListener(e -> loadEmployeeMasterRecords(txtSearch.getText().trim()));
        
        btnViewFull.addActionListener(e -> {
            int selectedRow = tblEmployees.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee from the list to view profile!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String empId = tblEmployees.getValueAt(selectedRow, 0).toString();
            
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            EmployeeDetailsDialog detailWizard = new EmployeeDetailsDialog(topFrame, empId);
            detailWizard.setVisible(true);
        });

        btnAdd.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddEmployeeForm addWizard = new AddEmployeeForm(topFrame, this);
            addWizard.setVisible(true);
        });

        // 🛠️ INTEGRATED LOGIC: මෙතන තිබ්බ පරණ JOptionPane එක වෙනුවට Update form එක සම්බන්ධ කළා 👇
        btnUpdate.addActionListener(e -> {
            int selectedRow = tblEmployees.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee row from the table first!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String empId = tblEmployees.getValueAt(selectedRow, 0).toString();
            
            // Open the update wizard window dynamically
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            UpdateEmployeeForm updateWizard = new UpdateEmployeeForm(topFrame, this, empId);
            updateWizard.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = tblEmployees.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee row to delete!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String empId = tblEmployees.getValueAt(selectedRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to completely remove " + empId + " from systems?", "Delete Warning", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                executeDatabaseDeletion(empId);
            }
        });

        loadEmployeeMasterRecords("");
    }

    public void loadEmployeeMasterRecords(String searchToken) {
        tableModel.setRowCount(0); 

        String query = "SELECT e.employee_id, e.full_name, e.nic_passport, j.job_title, j.department, e.status " +
                       "FROM employees e " +
                       "LEFT JOIN job_details j ON e.employee_id = j.employee_id ";

        if (!searchToken.isEmpty()) {
            query += "WHERE e.employee_id LIKE ? OR e.full_name LIKE ? OR e.nic_passport LIKE ?";
        }
        
        query += " ORDER BY e.employee_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            
            if (!searchToken.isEmpty()) {
                String wildcard = "%" + searchToken + "%";
                pst.setString(1, wildcard);
                pst.setString(2, wildcard);
                pst.setString(3, wildcard);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getString("employee_id"),
                        rs.getString("full_name"),
                        rs.getString("nic_passport"),
                        rs.getString("job_title") != null ? rs.getString("job_title") : "Not Assigned",
                        rs.getString("department") != null ? rs.getString("department") : "Not Assigned",
                        rs.getString("status")
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Data Fetch Refusal: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeDatabaseDeletion(String empId) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, empId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Employee records successfully purged.");
            loadEmployeeMasterRecords(""); 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Purge Failure: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleModernDataGrid(JTable dataGrid) {
        dataGrid.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dataGrid.setRowHeight(38);
        dataGrid.setShowGrid(false);
        dataGrid.setIntercellSpacing(new Dimension(0, 0));
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
                    comp.setBackground(new Color(14, 165, 233)); 
                    comp.setForeground(Color.WHITE);             
                } else {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                    
                    if (c == 5 && val != null) {
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        if (val.toString().equalsIgnoreCase("Active")) setForeground(new Color(34, 197, 94));
                        else setForeground(new Color(239, 68, 68));
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
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 25, 38));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}