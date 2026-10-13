package ui;

import db.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JButton btnLogin, btnExit;

    public LoginForm() {
        // Window Configuration (Fixed Size)
        setTitle("RN House Plan & Construction - EMS Login");
        setSize(480, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); 
        setLayout(null); 
        getContentPane().setBackground(new Color(245, 247, 250)); 

        // Main Title
        JLabel lblTitle = new JLabel("RN HOUSE PLAN & CONSTRUCTION", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setBounds(20, 25, 440, 30);
        add(lblTitle);

        // Subtitle
        JLabel lblSubtitle = new JLabel("Employee Management System Portal", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(127, 140, 141));
        lblSubtitle.setBounds(20, 55, 440, 20);
        add(lblSubtitle);

        // Username Input
        JLabel lblUsername = new JLabel("Username / ID:");
        lblUsername.setBounds(50, 110, 100, 30);
        add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setBounds(170, 110, 240, 30);
        add(txtUsername);

        // Password Input
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(50, 160, 100, 30);
        add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(170, 160, 240, 30);
        add(txtPassword);

        // Role Dropdown
        JLabel lblRole = new JLabel("Select Role:");
        lblRole.setBounds(50, 210, 100, 30);
        add(lblRole);

        String[] roles = {"Admin", "HR Manager", "Employee"};
        cmbRole = new JComboBox<>(roles);
        cmbRole.setBounds(170, 210, 240, 30);
        add(cmbRole);

        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.setBounds(170, 270, 110, 35);
        btnLogin.setBackground(new Color(46, 204, 113)); 
        btnLogin.setForeground(Color.WHITE);
        add(btnLogin);

        // Exit Button
        btnExit = new JButton("Exit");
        btnExit.setBounds(300, 270, 110, 35);
        btnExit.setBackground(new Color(231, 76, 60)); 
        btnExit.setForeground(Color.WHITE);
        add(btnExit);

        // Action Listeners
        btnExit.addActionListener(e -> System.exit(0));
        btnLogin.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role = cmbRole.getSelectedItem().toString();

        // Check for empty fields
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Hardcoded Admin Authentication
        if (role.equals("Admin")) {
            if (username.equals("admin") && password.equals("1234")) {
                JOptionPane.showMessageDialog(this, "Login Successful as Admin!");
                new AdminDashboard().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Admin Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // 2. Database Authentication for HR Manager & Employees
        // FIXED SQL: Changed WHERE clause to target the new username and password columns
        String query = "SELECT e.employee_id, j.job_title FROM employees e " +
                       "INNER JOIN job_details j ON e.employee_id = j.employee_id " +
                       "WHERE e.username = ? AND e.password = ? AND e.status = 'Active'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            
            pst.setString(1, username);
            pst.setString(2, password);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String empId = rs.getString("employee_id");
                    String dbJobTitle = rs.getString("job_title");

                    // Role verification against Database job_title
                    if (role.equals("HR Manager") && dbJobTitle.equalsIgnoreCase("HR Manager")) {
                        JOptionPane.showMessageDialog(this, "Login Successful as HR Manager!");
                        
                        // Open HRDashboard with the logged-in employee ID
                        new HRDashboard(empId).setVisible(true);
                        this.dispose();
                        
                    } else if (role.equals("Employee") && !dbJobTitle.equalsIgnoreCase("HR Manager")) {
                        JOptionPane.showMessageDialog(this, "Login Successful as Employee!");
                        
                        // Open StaffDashboard with the logged-in employee ID
                        new StaffDashboard(empId).setVisible(true);
                        this.dispose();
                        
                    } else {
                        JOptionPane.showMessageDialog(this, "Selected role does not match your job profile!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}