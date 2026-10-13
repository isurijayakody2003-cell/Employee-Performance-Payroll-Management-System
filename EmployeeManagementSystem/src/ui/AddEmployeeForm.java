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

public class AddEmployeeForm extends JDialog {

    private JTextField txtId, txtName, txtNic, txtDob, txtAddress, txtPhone, txtEmail, txtEmergency;
    private JComboBox<String> cmbGender, cmbMarital, cmbJobTitle, cmbDept, cmbEmpType;
    private JTextField txtLocation, txtSupervisor, txtDateJoined; 
    private JTextField txtSalary, txtAllowances, txtBank, txtAccount, txtEpf, txtOtRate;
    private JComboBox<String> cmbPayMethod;
    private JTextField txtQualifications, txtCertifications, txtSkills, txtExperience; 
    private JButton btnSave, btnCancel;
    private EmployeeManagementPanel parentPanel;

    public AddEmployeeForm(Frame parent, EmployeeManagementPanel parentPanel) {
        super(parent, "Register New Employee Wizard", true);
        this.parentPanel = parentPanel;

        setSize(850, 750); 
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Scrollable Container for the multi-section form
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
        
        txtId = addTextField(pnlPersonal, "Generated Employee ID:");
        txtId.setEditable(false); // 🔮 ID එක වෙනස් කළ නොහැකි ලෙස ලොක් කරන ලදී
        
        txtName = addTextField(pnlPersonal, "Full Name:");
        txtNic = addTextField(pnlPersonal, "NIC / Passport Number:");
        txtDob = addTextField(pnlPersonal, "Date of Birth (YYYY-MM-DD):");
        
        cmbGender = addDropdownField(pnlPersonal, "Gender:", new String[]{"Male", "Female"});
        cmbMarital = addDropdownField(pnlPersonal, "Marital Status:", new String[]{"Single", "Married", "Divorced"});
        
        txtAddress = addTextField(pnlPersonal, "Residential Address:");
        txtPhone = addTextField(pnlPersonal, "Phone Number:");
        txtEmail = addTextField(pnlPersonal, "Email Address:");
        txtEmergency = addTextField(pnlPersonal, "Emergency Contact (Name - No):");
        
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
        
        txtDateJoined = addTextField(pnlJob, "Company Join Date (YYYY-MM-DD):");
        txtDateJoined.setText(LocalDate.now().toString());
        
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

        btnSave = new JButton("Save Records");
        btnCancel = new JButton("Cancel");

        styleButton(btnSave, new Color(34, 197, 94)); 
        styleButton(btnCancel, new Color(239, 68, 68)); 

        buttonDeck.add(btnCancel);
        buttonDeck.add(btnSave);
        add(buttonDeck, BorderLayout.SOUTH);

        // Event Handlers
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> executeMultiTableInsert());

        
        generateNextEmployeeId();
    }

    // ========================================================
    // 🔮 NEW LOGIC:Employee ID 
    // ========================================================
    private void generateNextEmployeeId() {
        String query = "SELECT employee_id FROM employees ORDER BY employee_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            
            if (rs.next()) {
                String lastId = rs.getString("employee_id");
                if (lastId != null && lastId.startsWith("EMP")) {
                    try {
                        int idNum = Integer.parseInt(lastId.substring(3));
                        idNum++; 
                        txtId.setText(String.format("EMP%03d", idNum)); // EMP004 
                    } catch (NumberFormatException e) {
                        txtId.setText("EMP001");
                    }
                } else {
                    txtId.setText("EMP001");
                }
            } else {
                txtId.setText("EMP001");
            }
        } catch (Exception ex) {
            System.out.println("ID Generation Pipeline Error: " + ex.getMessage());
            txtId.setText("EMP001");
        }
    }

    // ========================================================
    // DATABASE TRANSACTION LOGIC (4-Table Inserter Engine)
    // ========================================================
    private void executeMultiTableInsert() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String nic = txtNic.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String dobStr = txtDob.getText().trim();
        String joinDateStr = txtDateJoined.getText().trim();

        // 1. Essential Input Validations
        if (id.isEmpty() || name.isEmpty() || nic.isEmpty() || joinDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out essential fields (Name, NIC, Join Date)!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Phone Formatting Check
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must contain exactly 10 digits!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Email Check
        if (!email.contains("@") || !email.contains(".com")) {
            JOptionPane.showMessageDialog(this, "Invalid Email Format! (Must contain '@' and '.com')", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4. Date Parsers and Validation
        try {
            LocalDate dob = LocalDate.parse(dobStr);
            LocalDate joinDate = LocalDate.parse(joinDateStr);
            int currentYear = LocalDate.now().getYear();
            
            if (dob.getYear() >= currentYear) {
                JOptionPane.showMessageDialog(this, "Date of Birth must be strictly BEFORE the current year!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format detected! Use YYYY-MM-DD pattern.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 🔮 UPDATED SQL: username ,password
        String q1 = "INSERT INTO employees (employee_id, full_name, nic_passport, date_of_birth, gender, marital_status, address, phone, email, emergency_contact, status, username, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active', ?, ?)";
        String q2 = "INSERT INTO job_details (employee_id, job_title, department, employee_type, date_joined, work_location, supervisor) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String q3 = "INSERT INTO education_skills (employee_id, qualifications, certifications, skills, work_experience) VALUES (?, ?, ?, ?, ?)";
        String q4 = "INSERT INTO payroll_details (employee_id, basic_salary, allowances, bank_name, account_number, epf_etf_number, ot_rate, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            
            conn.setAutoCommit(false); // TRANSACTION START

            // QUERY 1: Insert into 'employees' Table (With Auto-Generated Credentials)
            try (PreparedStatement pst1 = conn.prepareStatement(q1)) {
                pst1.setString(1, id);
                pst1.setString(2, name);
                pst1.setString(3, nic);
                pst1.setString(4, dobStr);
                pst1.setString(5, cmbGender.getSelectedItem().toString());
                pst1.setString(6, cmbMarital.getSelectedItem().toString());
                pst1.setString(7, txtAddress.getText().trim());
                pst1.setString(8, phone);
                pst1.setString(9, email);
                pst1.setString(10, txtEmergency.getText().trim());
                pst1.setString(11, name); 
                pst1.setString(12, nic); 
                pst1.executeUpdate();
            }

            // QUERY 2: Insert into 'job_details' Table
            try (PreparedStatement pst2 = conn.prepareStatement(q2)) {
                pst2.setString(1, id);
                pst2.setString(2, cmbJobTitle.getSelectedItem().toString());
                pst2.setString(3, cmbDept.getSelectedItem().toString());
                pst2.setString(4, cmbEmpType.getSelectedItem().toString());
                pst2.setString(5, joinDateStr); 
                pst2.setString(6, txtLocation.getText().trim());
                pst2.setString(7, txtSupervisor.getText().trim());
                pst2.executeUpdate();
            }

            // QUERY 3: Insert into 'education_skills' Table
            try (PreparedStatement pst3 = conn.prepareStatement(q3)) {
                pst3.setString(1, id);
                pst3.setString(2, txtQualifications.getText().trim().isEmpty() ? "N/A" : txtQualifications.getText().trim());
                pst3.setString(3, txtCertifications.getText().trim().isEmpty() ? "N/A" : txtCertifications.getText().trim());
                pst3.setString(4, txtSkills.getText().trim().isEmpty() ? "N/A" : txtSkills.getText().trim());
                pst3.setString(5, txtExperience.getText().trim().isEmpty() ? "N/A" : txtExperience.getText().trim());
                pst3.executeUpdate();
            }

            // QUERY 4: Insert into 'payroll_details' Table
            try (PreparedStatement pst4 = conn.prepareStatement(q4)) {
                pst4.setString(1, id);
                pst4.setDouble(2, txtSalary.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtSalary.getText().trim()));
                pst4.setDouble(3, txtAllowances.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtAllowances.getText().trim()));
                pst4.setString(4, txtBank.getText().trim());
                pst4.setString(5, txtAccount.getText().trim());
                pst4.setString(6, txtEpf.getText().trim());
                pst4.setDouble(7, txtOtRate.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtOtRate.getText().trim()));
                pst4.setString(8, cmbPayMethod.getSelectedItem().toString());
                pst4.executeUpdate();
            }

            conn.commit(); // TRANSACTION COMMIT SUCCESS
            JOptionPane.showMessageDialog(this, "Employee fully registered across 4 system registries!");
            parentPanel.loadEmployeeMasterRecords(""); 
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration Rollbacked: " + ex.getMessage(), "Database Transaction Failure", JOptionPane.ERROR_MESSAGE);
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
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}