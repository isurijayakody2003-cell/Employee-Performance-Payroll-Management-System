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

public class TaskAssignmentsPanel extends JPanel {

    private JTable tblTasks;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbEmpFilter, cmbStatusFilter;
    private JButton btnFilter, btnReset, btnAssignTask, btnUpdateStatus, btnDeleteTask;

    public TaskAssignmentsPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ========================================================
        // TOP CONTROL DECK (Title & Live Filters)
        // ========================================================
        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        // Header Titles
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Construction Site Operations & Task Dispatch");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSubtitle = new JLabel("Deploy task assignments to field engineers, track structural timelines, and audit live progress.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        topContainer.add(titlePanel, BorderLayout.WEST);

        // Filter Bar Row
        JPanel filterRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterRowPanel.setBackground(new Color(248, 250, 252));
        filterRowPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        cmbEmpFilter = new JComboBox<>();
        cmbEmpFilter.setPreferredSize(new Dimension(180, 32));
        cmbEmpFilter.setBackground(Color.WHITE);

        String[] statuses = {"ALL Statuses", "Pending", "In Progress", "Completed"};
        cmbStatusFilter = new JComboBox<>(statuses);
        cmbStatusFilter.setPreferredSize(new Dimension(130, 32));
        cmbStatusFilter.setBackground(Color.WHITE);

        btnFilter = new JButton("Apply Filters");
        btnReset = new JButton("Reset View");
        styleActionMiniButton(btnFilter, new Color(30, 41, 59));
        styleActionMiniButton(btnReset, new Color(148, 163, 184));

        filterRowPanel.add(new JLabel("Staff Member:"));
        filterRowPanel.add(cmbEmpFilter);
        filterRowPanel.add(new JLabel("Progress Status:"));
        filterRowPanel.add(cmbStatusFilter);
        filterRowPanel.add(btnFilter);
        filterRowPanel.add(btnReset);
        topContainer.add(filterRowPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // ========================================================
        // CENTER GRID VIEW (Active Task Matrix)
        // ========================================================
        String[] columns = {"Task ID", "Employee ID", "Full Name", "Task Operation Assignment", "Timeline Due", "Current Status"};
        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblTasks = new JTable(tableModel);
        styleModernDataGrid(tblTasks);

        JScrollPane scrollGridWrapper = new JScrollPane(tblTasks);
        scrollGridWrapper.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollGridWrapper.getViewport().setBackground(Color.WHITE);
        add(scrollGridWrapper, BorderLayout.CENTER);

        // ========================================================
        // BOTTOM ACTION TOOL DECK
        // ========================================================
        JPanel actionDeckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionDeckPanel.setOpaque(false);

        btnAssignTask = new JButton("Assign New Task");
        btnUpdateStatus = new JButton("Update Status");
        btnDeleteTask = new JButton("Cancel Task");

        styleActionStaticButton(btnAssignTask, new Color(14, 165, 233));  
        styleActionStaticButton(btnUpdateStatus, new Color(99, 102, 241)); 
        styleActionStaticButton(btnDeleteTask, new Color(239, 68, 68));    

        actionDeckPanel.add(btnAssignTask);
        actionDeckPanel.add(btnUpdateStatus);
        actionDeckPanel.add(btnDeleteTask);
        add(actionDeckPanel, BorderLayout.SOUTH);

        // ========================================================
        // CORE EVENT TRIGGERS
        // ========================================================
        btnFilter.addActionListener(e -> {
            String empSel = cmbEmpFilter.getSelectedItem().toString();
            String empId = empSel.startsWith("ALL") ? "" : empSel.split(" - ")[0];
            String status = cmbStatusFilter.getSelectedItem().toString();
            if (status.startsWith("ALL")) status = "";
            fetchTaskSQLRecords(empId, status);
        });

        btnReset.addActionListener(e -> {
            cmbEmpFilter.setSelectedIndex(0);
            cmbStatusFilter.setSelectedIndex(0);
            fetchTaskSQLRecords("", "");
        });

        btnAssignTask.addActionListener(e -> launchAssignTaskWizard());
        btnUpdateStatus.addActionListener(e -> launchStatusUpdaterWizard());
        btnDeleteTask.addActionListener(e -> triggerTaskDeletionPipeline());

        populateEmployeeFilters();
        fetchTaskSQLRecords("", "");
    }

    private void populateEmployeeFilters() {
        cmbEmpFilter.removeAllItems();
        cmbEmpFilter.addItem("ALL Active Staff");
        String sql = "SELECT employee_id, full_name FROM employees WHERE status='Active' ORDER BY employee_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                cmbEmpFilter.addItem(rs.getString("employee_id") + " - " + rs.getString("full_name"));
            }
        } catch (Exception ex) {
            System.out.println("Filter Populator Error: " + ex.getMessage());
        }
    }

    public void fetchTaskSQLRecords(String empIdFilter, String statusFilter) {
        tableModel.setRowCount(0);
        String query = "SELECT t.task_id, t.employee_id, e.full_name, t.task_title, t.due_date, t.status " +
                       "FROM tasks t INNER JOIN employees e ON t.employee_id = e.employee_id WHERE 1=1 ";

        if (!empIdFilter.isEmpty()) query += "AND t.employee_id = ? ";
        if (!statusFilter.isEmpty()) query += "AND t.status = ? ";
        query += "ORDER BY t.task_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (!empIdFilter.isEmpty()) pst.setString(paramIndex++, empIdFilter);
            if (!statusFilter.isEmpty()) pst.setString(paramIndex++, statusFilter);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("task_id"),
                        rs.getString("employee_id"),
                        rs.getString("full_name"),
                        rs.getString("task_title"),
                        rs.getDate("due_date").toString(),
                        rs.getString("status")
                    });
                }
            }
        } catch (Exception ex) {
            System.out.println("Tasks Extraction Refusal: " + ex.getMessage());
        }
    }

    private void launchAssignTaskWizard() {
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        String sql = "SELECT employee_id, full_name FROM employees WHERE status='Active' ORDER BY employee_id ASC";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pst = conn.prepareStatement(sql); 
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                comboModel.addElement(rs.getString("employee_id") + " - " + rs.getString("full_name"));
            }
        } catch (Exception ex) {/**/}

        if (comboModel.getSize() == 0) {
            JOptionPane.showMessageDialog(this, "No active registered engineers found to assign work!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> cmbStaff = new JComboBox<>(comboModel);
        JTextField txtTaskTitle = new JTextField();
        JTextField txtDueDate = new JTextField(LocalDate.now().plusDays(7).toString()); 

        JPanel pop = new JPanel(new GridLayout(0, 1, 0, 8));
        pop.add(new JLabel("Select Field Deployee:")); pop.add(cmbStaff);
        pop.add(new JLabel("Task Operation Title / Specification:")); pop.add(txtTaskTitle);
        pop.add(new JLabel("Timeline Due Date (YYYY-MM-DD):")); pop.add(txtDueDate);

        int result = JOptionPane.showConfirmDialog(this, pop, "Task Dispatch Wizard", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION && !txtTaskTitle.getText().trim().isEmpty()) {
            String empId = cmbStaff.getSelectedItem().toString().split(" - ")[0];
            
            // FIXED SQL: Included 'assigned_date' in the INSERT statement
            String insertSql = "INSERT INTO tasks (employee_id, task_title, assigned_date, due_date, status) VALUES (?, ?, ?, ?, 'Pending')";
            
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(insertSql)) {
                pst.setString(1, empId);
                pst.setString(2, txtTaskTitle.getText().trim());
                pst.setString(3, LocalDate.now().toString()); // Set current date as assigned_date
                pst.setString(4, txtDueDate.getText().trim());
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Task Operations Dispatched Successfully! Status set to Pending.");
                fetchTaskSQLRecords("", "");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Transaction Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void launchStatusUpdaterWizard() {
        int row = tblTasks.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a task row from the active matrix to modify progress status!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String taskId = tblTasks.getValueAt(row, 0).toString();
        String currentStatus = tblTasks.getValueAt(row, 5).toString();

        String[] progressOptions = {"Pending", "In Progress", "Completed"};
        JComboBox<String> cmbOptions = new JComboBox<>(progressOptions);
        cmbOptions.setSelectedItem(currentStatus);

        JPanel pop = new JPanel(new GridLayout(0, 1, 0, 5));
        pop.add(new JLabel("Modify Current Task Progress Status Vector:"));
        pop.add(cmbOptions);

        int result = JOptionPane.showConfirmDialog(this, pop, "Status Modification Matrix", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String nextStatus = cmbOptions.getSelectedItem().toString();
            String updateSql = "UPDATE tasks SET status = ? WHERE task_id = ?";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(updateSql)) {
                pst.setString(1, nextStatus);
                pst.setInt(2, Integer.parseInt(taskId));
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Task status updated to: " + nextStatus);
                fetchTaskSQLRecords("", "");
            } catch (Exception ex) {/**/}
        }
    }

    private void triggerTaskDeletionPipeline() {
        int row = tblTasks.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a task assignment row to terminate from ledger!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String taskId = tblTasks.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently cancel Task ID: " + taskId + "?", "Termination Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String deleteSql = "DELETE FROM tasks WHERE task_id = ?";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(deleteSql)) {
                pst.setInt(1, Integer.parseInt(taskId));
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Task deployment structural allocation safely scrubbed from database.");
                fetchTaskSQLRecords("", "");
            } catch (Exception ex) {/**/}
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
                comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                
                if (c == 5 && val != null) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (val.toString().equalsIgnoreCase("Completed")) setForeground(new Color(34, 197, 94));  
                    else if (val.toString().equalsIgnoreCase("Pending")) setForeground(new Color(234, 179, 8)); 
                    else setForeground(new Color(14, 165, 233));                                               
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
        btn.setPreferredSize(new Dimension(185, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleActionMiniButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(120, 32));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}