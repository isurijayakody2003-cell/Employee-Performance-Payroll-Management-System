package ui;

import db.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfilePanel extends JPanel {

    private JTextField txtEmpId, txtFullName, txtNicPassport, txtDOB, txtMaritalStatus;
    private JTextField txtAddress, txtPhone, txtEmail, txtEmergency, txtStatus, txtUsername, txtPassword;
    private JButton btnUpdate;
    private String currentEmpId;

    public ProfilePanel(String empId) {
        this.currentEmpId = empId;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 25));
        setBorder(new EmptyBorder(30, 35, 30, 35));

        // Header Section
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("My Personal Account Profile");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSubtitle = new JLabel("View your complete profile and update your personal parameters safely within the system.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Form Center Grid Container Panel
        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fields Initialization & Styling
        txtEmpId = new JTextField(); txtEmpId.setEditable(false); styleField(txtEmpId);
        txtFullName = new JTextField(); styleField(txtFullName);
        txtNicPassport = new JTextField(); styleField(txtNicPassport);
        txtDOB = new JTextField(); styleField(txtDOB);
        txtMaritalStatus = new JTextField(); styleField(txtMaritalStatus);
        txtAddress = new JTextField(); styleField(txtAddress);
        txtPhone = new JTextField(); styleField(txtPhone);
        txtEmail = new JTextField(); styleField(txtEmail);
        txtEmergency = new JTextField(); styleField(txtEmergency);
        txtStatus = new JTextField(); styleField(txtStatus);
        txtUsername = new JTextField(); styleField(txtUsername);
        txtPassword = new JTextField(); styleField(txtPassword);

        // ========================================================
        // COLUMN 1: Primary Profile Records (Left Side)
        // ========================================================
        gbc.weightx = 0.1; gbc.gridx = 0;
        gbc.gridy = 0; formWrapper.add(createFormLabel("Employee ID:"), gbc);
        gbc.gridy = 1; formWrapper.add(createFormLabel("Full Name:"), gbc);
        gbc.gridy = 2; formWrapper.add(createFormLabel("NIC / Passport:"), gbc);
        gbc.gridy = 3; formWrapper.add(createFormLabel("Date of Birth:"), gbc);
        gbc.gridy = 4; formWrapper.add(createFormLabel("Marital Status:"), gbc);
        gbc.gridy = 5; formWrapper.add(createFormLabel("Account Status:"), gbc);

        gbc.weightx = 0.4; gbc.gridx = 1;
        gbc.gridy = 0; formWrapper.add(txtEmpId, gbc);
        gbc.gridy = 1; formWrapper.add(txtFullName, gbc);
        gbc.gridy = 2; formWrapper.add(txtNicPassport, gbc);
        gbc.gridy = 3; formWrapper.add(txtDOB, gbc);
        gbc.gridy = 4; formWrapper.add(txtMaritalStatus, gbc);
        gbc.gridy = 5; formWrapper.add(txtStatus, gbc);

        // ========================================================
        // COLUMN 2: Contact & Security Credentials (Right Side)
        // ========================================================
        gbc.weightx = 0.1; gbc.gridx = 2;
        gbc.gridy = 0; formWrapper.add(createFormLabel("Residential Address:"), gbc);
        gbc.gridy = 1; formWrapper.add(createFormLabel("Phone Number:"), gbc);
        gbc.gridy = 2; formWrapper.add(createFormLabel("Email Address:"), gbc);
        gbc.gridy = 3; formWrapper.add(createFormLabel("Emergency Contact:"), gbc);
        gbc.gridy = 4; formWrapper.add(createFormLabel("Login Username:"), gbc);
        gbc.gridy = 5; formWrapper.add(createFormLabel("Login Password:"), gbc);

        gbc.weightx = 0.4; gbc.gridx = 3;
        gbc.gridy = 0; formWrapper.add(txtAddress, gbc);
        gbc.gridy = 1; formWrapper.add(txtPhone, gbc);
        gbc.gridy = 2; formWrapper.add(txtEmail, gbc);
        gbc.gridy = 3; formWrapper.add(txtEmergency, gbc);
        gbc.gridy = 4; formWrapper.add(txtUsername, gbc);
        gbc.gridy = 5; formWrapper.add(txtPassword, gbc);

        add(formWrapper, BorderLayout.CENTER);

        // Bottom Action Button Block
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        bottomPanel.setOpaque(false);
        btnUpdate = new JButton("Save Profile Changes");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdate.setBackground(new Color(34, 197, 94)); 
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setPreferredSize(new Dimension(220, 42));
        btnUpdate.setFocusPainted(false); btnUpdate.setBorderPainted(false);
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(btnUpdate);
        add(bottomPanel, BorderLayout.SOUTH);

        btnUpdate.addActionListener(e -> triggerProfileUpdatePipeline());

        loadPersonalProfileRecords();
    }

    private void loadPersonalProfileRecords() {
        String sql = "SELECT employee_id, full_name, nic_passport, date_of_birth, marital_status, " +
                     "address, phone, email, emergency_contact, status, username, password " +
                     "FROM employees WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, currentEmpId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    txtEmpId.setText(rs.getString("employee_id"));
                    txtFullName.setText(rs.getString("full_name"));
                    txtNicPassport.setText(rs.getString("nic_passport"));
                    txtDOB.setText(rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toString() : "");
                    txtMaritalStatus.setText(rs.getString("marital_status"));
                    txtAddress.setText(rs.getString("address"));
                    txtPhone.setText(rs.getString("phone"));
                    txtEmail.setText(rs.getString("email"));
                    txtEmergency.setText(rs.getString("emergency_contact"));
                    txtStatus.setText(rs.getString("status"));
                    txtUsername.setText(rs.getString("username"));
                    txtPassword.setText(rs.getString("password"));
                }
            }
        } catch (Exception ex) {
            System.out.println("Profile Loading Error: " + ex.getMessage());
        }
    }

    private void triggerProfileUpdatePipeline() {
        // Collect updated text values
        String fullName = txtFullName.getText().trim();
        String nic = txtNicPassport.getText().trim();
        String dob = txtDOB.getText().trim();
        String marital = txtMaritalStatus.getText().trim();
        String address = txtAddress.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String emergency = txtEmergency.getText().trim();
        String status = txtStatus.getText().trim();
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        // Basic fields validation
        if (fullName.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Core fields (Name, Username, Password) cannot be empty!", "Validation Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sqlUpdate = "UPDATE employees SET full_name = ?, nic_passport = ?, date_of_birth = ?, " +
                           "marital_status = ?, address = ?, phone = ?, email = ?, emergency_contact = ?, " +
                           "status = ?, username = ?, password = ? WHERE employee_id = ?";
                           
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {
            
            pst.setString(1, fullName);
            pst.setString(2, nic);
            
            // Date parsing handler
            if (dob.isEmpty()) {
                pst.setNull(3, java.sql.Types.DATE);
            } else {
                pst.setDate(3, java.sql.Date.valueOf(dob));
            }
            
            pst.setString(4, marital);
            pst.setString(5, address);
            pst.setString(6, phone);
            pst.setString(7, email);
            pst.setString(8, emergency);
            pst.setString(9, status);
            pst.setString(10, user);
            pst.setString(11, pass);
            pst.setString(12, currentEmpId);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Your personal account statement and credentials updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IllegalArgumentException dateEx) {
            JOptionPane.showMessageDialog(this, "Invalid Date format! Please use YYYY-MM-DD format.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Transaction Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createFormLabel(String labelText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(71, 85, 105));
        return lbl;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(260, 36));
        f.setBackground(f.isEditable() ? Color.WHITE : new Color(241, 245, 249));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
    }
}