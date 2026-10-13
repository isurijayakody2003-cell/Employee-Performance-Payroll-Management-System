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

public class MyAssignedTasksPanel extends JPanel {

    private JTable tblTasks;
    private DefaultTableModel tableModel;
    private JButton btnInProgress, btnComplete;
    private String currentEmpId;

    public MyAssignedTasksPanel(String empId) {
        this.currentEmpId = empId;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Titles
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("My Assigned Field Operations");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSubtitle = new JLabel("View your current site operation assignments and update their completion status.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        add(titlePanel, BorderLayout.NORTH);

        // Tasks Table Data Grid
        String[] columns = {"Task ID", "Task Operation Assignment", "Timeline Due", "Status"};
        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblTasks = new JTable(tableModel);
        styleModernDataGrid(tblTasks);
        JScrollPane scrollGrid = new JScrollPane(tblTasks);
        scrollGrid.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollGrid.getViewport().setBackground(Color.WHITE);
        add(scrollGrid, BorderLayout.CENTER);

        // Action Controls Tool Deck Layout (With professional 15px horizontal gap)
        JPanel actionDeck = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionDeck.setOpaque(false);
        
        // 🔮 Added: In Progress Status Button Instantiation
        btnInProgress = new JButton("Mark as In Progress");
        btnInProgress.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnInProgress.setBackground(new Color(14, 165, 233)); // Sky Blue Accent
        btnInProgress.setForeground(Color.WHITE);
        btnInProgress.setPreferredSize(new Dimension(180, 40));
        btnInProgress.setFocusPainted(false); btnInProgress.setBorderPainted(false);
        btnInProgress.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnComplete = new JButton("Mark Selected Task as Completed");
        btnComplete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnComplete.setBackground(new Color(34, 197, 94)); // Emerald Green Accent
        btnComplete.setForeground(Color.WHITE);
        btnComplete.setPreferredSize(new Dimension(260, 40));
        btnComplete.setFocusPainted(false); btnComplete.setBorderPainted(false);
        btnComplete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        actionDeck.add(btnInProgress);
        actionDeck.add(btnComplete);
        add(actionDeck, BorderLayout.SOUTH);

        // Wiring Listeners Events
        btnInProgress.addActionListener(e -> triggerTaskInProgressPipeline());
        btnComplete.addActionListener(e -> triggerTaskCompletionPipeline());

        loadMyTasksFromDatabase();
    }

    public void loadMyTasksFromDatabase() {
        tableModel.setRowCount(0);
        String query = "SELECT task_id, task_title, due_date, status FROM tasks WHERE employee_id = ? ORDER BY due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, currentEmpId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("task_id"),
                        rs.getString("task_title"),
                        rs.getDate("due_date").toString(),
                        rs.getString("status")
                    });
                }
            }
        } catch (Exception ex) {
            System.out.println("Tasks Fetching Error: " + ex.getMessage());
        }
    }

    // 🔮 Added: In Progress Database Transition Pipeline
    private void triggerTaskInProgressPipeline() {
        int selectedRow = tblTasks.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task row from the table first!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String taskId = tblTasks.getValueAt(selectedRow, 0).toString();
        String currentStatus = tblTasks.getValueAt(selectedRow, 3).toString();

        if (currentStatus.equalsIgnoreCase("Completed")) {
            JOptionPane.showMessageDialog(this, "Cannot change status. This assignment is already completed!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentStatus.equalsIgnoreCase("In Progress")) {
            JOptionPane.showMessageDialog(this, "This operational assignment is already running in progress status!", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String sqlUpdate = "UPDATE tasks SET status = 'In Progress' WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {
            pst.setString(1, taskId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Task status updated to In Progress successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMyTasksFromDatabase(); 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Transaction Failure: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void triggerTaskCompletionPipeline() {
        int selectedRow = tblTasks.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task row from the table first!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String taskId = tblTasks.getValueAt(selectedRow, 0).toString();
        String currentStatus = tblTasks.getValueAt(selectedRow, 3).toString();

        if (currentStatus.equalsIgnoreCase("Completed")) {
            JOptionPane.showMessageDialog(this, "This operational assignment is already marked as completed!", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String sqlUpdate = "UPDATE tasks SET status = 'Completed' WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {
            pst.setString(1, taskId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Task status updated to Completed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMyTasksFromDatabase(); 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Transaction Failure: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleModernDataGrid(JTable dataGrid) {
        dataGrid.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dataGrid.setRowHeight(38); dataGrid.setShowGrid(false);
        dataGrid.setIntercellSpacing(new Dimension(0, 0)); dataGrid.setFocusable(false);

        JTableHeader header = dataGrid.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252)); header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                setBorder(noFocusBorder);
                if (isSel) {
                    comp.setBackground(new Color(241, 245, 249)); comp.setForeground(new Color(15, 23, 42));
                } else {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                    if (c == 3 && val != null) {
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        // 🔮 Render contextual dynamic text colors for workflow state nodes
                        if (val.toString().equalsIgnoreCase("Completed")) {
                            setForeground(new Color(34, 197, 94)); // Emerald Green
                        } else if (val.toString().equalsIgnoreCase("In Progress")) {
                            setForeground(new Color(14, 165, 233)); // Sky Blue
                        } else {
                            setForeground(new Color(234, 179, 8)); // Pending Orange/Yellow
                        }
                    } else { setForeground(new Color(51, 65, 85)); }
                }
                return comp;
            }
        };
        dataGrid.setDefaultRenderer(Object.class, renderer);
    }
}