package ui;

import db.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class UpdateEmployeeForm extends JDialog {

    private String employeeId;
    private JTextField txtId, txtName, txtNic, txtDob, txtAddress, txtPhone, txtEmail, txtEmergency;
    private JComboBox<String> cmbGender, cmbMarital, cmbJobTitle, cmbDept, cmbEmpType, cmbStatus;
    private JTextField txtLocation, txtSupervisor, txtDateJoined; //  Added txtDateJoined
    private JTextField txtSalary, txtAllowances, txtBank, txtAccount, txtEpf, txtOtRate;
    private JComboBox<String> cmbPayMethod;
    private JTextField txtQualifications, txtCertifications, txtSkills, txtExperience; //  Added Education Fields
    private JButton btnUpdate, btnCancel;
    private EmployeeManagementPanel parentPanel;

    public UpdateEmployeeForm(Frame parent, EmployeeManagementPanel parentPanel, String employeeId) {
        super(parent, "Update Employee Records Wizard", true);
        this.parentPanel = parentPanel;
        this.employeeId = employeeId;

        setSize(850, 750);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Scrollable Container
        JPanel mainContainer = new JPanel();
        mainContainer.setBackground(new Color(248, 250, 252));
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBorder(new EmptyBorder(20, 25, 20, 25));

        JScrollPane scrollWrapper = new JScrollPane(mainContainer);
        scrollWrapper.setBorder(BorderFactory.createEmptyBorder());
        scrollWrapper.getVerticalScrollBar().setUnitIncrement(15);
        add(scrollWrapper, BorderLayout.CENTER);

        // ========================================================
        // SECTION 1: Personal Details (Table: employees)
        // ========================================================
        JPanel pnlPersonal = createSectionPanel("1. Personal Identification & Contact");
        
        txtId = addTextField(pnlPersonal, "Employee ID (Read-Only):");
        txtId.setEditable(false); 
        txtId.setBackground(new Color(241, 245, 249));
        
        txtName = addTextField(pnlPersonal, "Full Name:");
        txtNic = addTextField(pnlPersonal, "NIC / Passport Number:");
        txtDob = addTextField(pnlPersonal, "Date of Birth (YYYY-MM-DD):");
        
        cmbGender = addDropdownField(pnlPersonal, "Gender:", new String[]{"Male", "Female"});
        cmbMarital = addDropdownField(pnlPersonal, "Marital Status:", new String[]{"Single", "Married", "Divorced"});
        
        txtAddress = addTextField(pnlPersonal, "Residential Address:");
        txtPhone = addTextField(pnlPersonal, "Phone Number:");
        txtEmail = addTextField(pnlPersonal, "Email Address:");
        txtEmergency = addTextField(pnlPersonal, "Emergency Contact:");
        cmbStatus = addDropdownField(pnlPersonal, "System Status:", new String[]{"Active", "Terminated"});
        
        mainContainer.add(pnlPersonal);
        mainContainer.add(Box.createVerticalStrut(15));

        // ========================================================
        // SECTION 2: Job Deployment (Table: job_details)
        // ========================================================
        JPanel pnlJob = createSectionPanel("2. Company Position & Deployment");
        
        cmbJobTitle = addDropdownField(pnlJob, "Designation / Position:", new String[]{"Manager", "Supervisor", "Mason", "Carpenter", "Electrician", "Plumber", "HR Manager"});
        cmbDept = addDropdownField(pnlJob, "Department Allocation:", new String[]{"Administration", "HR", "Warehouse", "Site Operations"});
        cmbEmpType = addDropdownField(pnlJob, "Employment Type:", new String[]{"Full-time", "Part-time", "Contract", "Casual"});
        
        txtLocation = addTextField(pnlJob, "Work Location / Site:");
        txtSupervisor = addTextField(pnlJob, "Direct Supervisor:");
        txtDateJoined = addTextField(pnlJob, "Company Join Date (YYYY-MM-DD):"); //  Custom Join Date field
        
        mainContainer.add(pnlJob);
        mainContainer.add(Box.createVerticalStrut(15));

        // ========================================================
        // SECTION 3: Academic & Skills (Table: education_skills)
        // ========================================================
       
        JPanel pnlEducation = createSectionPanel("3. Academic Qualifications & Technical Skills");
        
        txtQualifications = addTextField(pnlEducation, "Highest Educational Qualifications:");
        txtCertifications = addTextField(pnlEducation, "Professional Certifications:");
        txtSkills = addTextField(pnlEducation, "Special Technical Skills:");
        txtExperience = addTextField(pnlEducation, "Prior Work Experience / History:");
        
        mainContainer.add(pnlEducation);
        mainContainer.add(Box.createVerticalStrut(15));

        // ========================================================
        // SECTION 4: Payroll Details (Table: payroll_details)
        // ========================================================
        JPanel pnlPayroll = createSectionPanel("4. Financial Ledger & Remuneration");
        
        txtSalary = addTextField(pnlPayroll, "Basic Salary (Rs.):");
        txtAllowances = addTextField(pnlPayroll, "Allowances (Rs.):");
        txtBank = addTextField(pnlPayroll, "Bank Name:");
        txtAccount = addTextField(pnlPayroll, "Account Number:");
        txtEpf = addTextField(pnlPayroll, "EPF / ETF Number:");
        txtOtRate = addTextField(pnlPayroll, "OT Hourly Rate (Rs.):");
        cmbPayMethod = addDropdownField(pnlPayroll, "Payment Method:", new String[]{"Bank Transfer", "Cash", "Cheque"});
        
        mainContainer.add(pnlPayroll);

        // ========================================================
        // BOTTOM BUTTON DECK
        // ========================================================
        JPanel buttonDeck = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        buttonDeck.setBackground(Color.WHITE);
        buttonDeck.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        btnUpdate = new JButton("Update Records");
        btnCancel = new JButton("Cancel");

        styleButton(btnUpdate, new Color(14, 165, 233)); 
        styleButton(btnCancel, new Color(239, 68, 68)); 

        buttonDeck.add(btnCancel);
        buttonDeck.add(btnUpdate);
        add(buttonDeck, BorderLayout.SOUTH);

        // Event Handlers
        btnCancel.addActionListener(e -> dispose());
        btnUpdate.addActionListener(e -> executeMultiTableUpdate());

        loadCurrentEmployeeData();
    }

    // ========================================================
    // DATABASE SELECT LOGIC (Pre-populate Fields across 4 Tables)
    // ========================================================
    private void loadCurrentEmployeeData() {
        String sql = "SELECT e.*, j.*, ed.*, p.* FROM employees e " +
                     "LEFT JOIN job_details j ON e.employee_id = j.employee_id " +
                     "LEFT JOIN education_skills ed ON e.employee_id = ed.employee_id " + 
                     "LEFT JOIN payroll_details p ON e.employee_id = p.employee_id " +
                     "WHERE e.employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, employeeId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    txtId.setText(rs.getString("employee_id"));
                    txtName.setText(rs.getString("full_name"));
                    txtNic.setText(rs.getString("nic_passport"));
                    txtDob.setText(rs.getString("date_of_birth"));
                    cmbGender.setSelectedItem(rs.getString("gender"));
                    cmbMarital.setSelectedItem(rs.getString("marital_status"));
                    txtAddress.setText(rs.getString("address"));
                    txtPhone.setText(rs.getString("phone"));
                    txtEmail.setText(rs.getString("email"));
                    txtEmergency.setText(rs.getString("emergency_contact"));
                    cmbStatus.setSelectedItem(rs.getString("status"));

                    cmbJobTitle.setSelectedItem(rs.getString("job_title"));
                    cmbDept.setSelectedItem(rs.getString("department"));
                    cmbEmpType.setSelectedItem(rs.getString("employee_type"));
                    txtLocation.setText(rs.getString("work_location"));
                    txtSupervisor.setText(rs.getString("supervisor"));
                    txtDateJoined.setText(rs.getString("date_joined")); 

                    //  Loaded Education fields
                    txtQualifications.setText(rs.getString("qualifications"));
                    txtCertifications.setText(rs.getString("certifications"));
                    txtSkills.setText(rs.getString("skills"));
                    txtExperience.setText(rs.getString("work_experience"));

                    txtSalary.setText(String.valueOf(rs.getDouble("basic_salary")));
                    txtAllowances.setText(String.valueOf(rs.getDouble("allowances")));
                    txtBank.setText(rs.getString("bank_name"));
                    txtAccount.setText(rs.getString("account_number"));
                    txtEpf.setText(rs.getString("epf_etf_number"));
                    txtOtRate.setText(String.valueOf(rs.getDouble("ot_rate")));
                    cmbPayMethod.setSelectedItem(rs.getString("payment_method"));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fetch Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========================================================
    // DATABASE TRANSACTION LOGIC (4-Table Updater with Validation)
    // ========================================================
    private void executeMultiTableUpdate() {
        String name = txtName.getText().trim();
        String nic = txtNic.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String dobStr = txtDob.getText().trim();
        String joinDateStr = txtDateJoined.getText().trim();

        // 1. Check for Essential Fields
        if (name.isEmpty() || nic.isEmpty() || joinDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out essential fields (Name, NIC, Join Date)!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Strict Phone Validation (Only digits & exactly 10 characters)
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must contain exactly 10 digits!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Email Validation (Must contain '@' and '.com')
        if (!email.contains("@") || !email.contains(".com")) {
            JOptionPane.showMessageDialog(this, "Invalid Email Format! (Must contain '@' and '.com')", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4. DOB Validation (Must be a valid date and strictly before current year)
        if (dobStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date of Birth cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate dob = LocalDate.parse(dobStr); 
            LocalDate.parse(joinDateStr); // Validates format of join date too
            int currentYear = LocalDate.now().getYear(); 
            
            if (dob.getYear() >= currentYear) {
                JOptionPane.showMessageDialog(this, "Date of Birth must be strictly BEFORE the current year!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format! Please use YYYY-MM-DD pattern.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- All Validations Passed! Process Database Transaction ---
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // TRANSACTION START

            // 1. Update 'employees' Table
            String u1 = "UPDATE employees SET full_name=?, nic_passport=?, date_of_birth=?, gender=?, marital_status=?, address=?, phone=?, email=?, emergency_contact=?, status=? WHERE employee_id=?";
            try (PreparedStatement pst1 = conn.prepareStatement(u1)) {
                pst1.setString(1, name);
                pst1.setString(2, nic);
                pst1.setString(3, dobStr);
                pst1.setString(4, cmbGender.getSelectedItem().toString());
                pst1.setString(5, cmbMarital.getSelectedItem().toString());
                pst1.setString(6, txtAddress.getText().trim());
                pst1.setString(7, phone);
                pst1.setString(8, email);
                pst1.setString(9, txtEmergency.getText().trim());
                pst1.setString(10, cmbStatus.getSelectedItem().toString());
                pst1.setString(11, employeeId);
                pst1.executeUpdate();
            }

            // 2. Update 'job_details' Table
            String u2 = "UPDATE job_details SET job_title=?, department=?, employee_type=?, date_joined=?, work_location=?, supervisor=? WHERE employee_id=?";
            try (PreparedStatement pst2 = conn.prepareStatement(u2)) {
                pst2.setString(1, cmbJobTitle.getSelectedItem().toString());
                pst2.setString(2, cmbDept.getSelectedItem().toString());
                pst2.setString(3, cmbEmpType.getSelectedItem().toString());
                pst2.setString(4, joinDateStr); //  Updated custom join date
                pst2.setString(5, txtLocation.getText().trim());
                pst2.setString(6, txtSupervisor.getText().trim());
                pst2.setString(7, employeeId);
                pst2.executeUpdate();
            }

            // 3. Update 'education_skills' Table

            String u3 = "UPDATE education_skills SET qualifications=?, certifications=?, skills=?, work_experience=? WHERE employee_id=?";
            try (PreparedStatement pst3 = conn.prepareStatement(u3)) {
                pst3.setString(1, txtQualifications.getText().trim().isEmpty() ? "N/A" : txtQualifications.getText().trim());
                pst3.setString(2, txtCertifications.getText().trim().isEmpty() ? "N/A" : txtCertifications.getText().trim());
                pst3.setString(3, txtSkills.getText().trim().isEmpty() ? "N/A" : txtSkills.getText().trim());
                pst3.setString(4, txtExperience.getText().trim().isEmpty() ? "N/A" : txtExperience.getText().trim());
                pst3.setString(5, employeeId);
                pst3.executeUpdate();
            }

            // 4. Update 'payroll_details' Table
            String u4 = "UPDATE payroll_details SET basic_salary=?, allowances=?, bank_name=?, account_number=?, epf_etf_number=?, ot_rate=?, payment_method=? WHERE employee_id=?";
            try (PreparedStatement pst4 = conn.prepareStatement(u4)) {
                pst4.setDouble(1, Double.parseDouble(txtSalary.getText().trim()));
                pst4.setDouble(2, Double.parseDouble(txtAllowances.getText().trim()));
                pst4.setString(3, txtBank.getText().trim());
                pst4.setString(4, txtAccount.getText().trim());
                pst4.setString(5, txtEpf.getText().trim());
                pst4.setDouble(6, Double.parseDouble(txtOtRate.getText().trim()));
                pst4.setString(7, cmbPayMethod.getSelectedItem().toString());
                pst4.setString(8, employeeId);
                pst4.executeUpdate();
            }

            conn.commit(); // COMMIT TRANSACTION
            JOptionPane.showMessageDialog(this, "Employee records updated successfully across all 4 tables!");
            parentPanel.loadEmployeeMasterRecords(""); 
            dispose();

        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception e) { System.out.println(e.getMessage()); }
            }
            JOptionPane.showMessageDialog(this, "Update Failed: " + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 20, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(14, 165, 233)
        ));
        panel.setBorder(BorderFactory.createCompoundBorder(panel.getBorder(), new EmptyBorder(15, 18, 15, 18)));
        return panel;
    }

    private JTextField addTextField(JPanel panel, String labelText) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 35));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1), new EmptyBorder(0, 8, 0, 8)));
        
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(txt, BorderLayout.CENTER);
        panel.add(wrapper);
        return txt;
    }

    private JComboBox<String> addDropdownField(JPanel panel, String labelText, String[] items) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        
        JComboBox<String> cmb = new JComboBox<>(items);
        cmb.setPreferredSize(new Dimension(0, 35));
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmb.setBackground(Color.WHITE);
        
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(cmb, BorderLayout.CENTER);
        panel.add(wrapper);
        return cmb;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}