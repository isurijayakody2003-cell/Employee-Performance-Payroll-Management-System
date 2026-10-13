package ui;

import db.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeDetailsDialog extends JDialog {

    private String employeeId;

    public EmployeeDetailsDialog(Frame parent, String employeeId) {
        super(parent, "Employee Full Profile - " + employeeId, true);
        this.employeeId = employeeId;

        setSize(850, 580); // Adjusted height elegantly since sections 5 and 6 are removed
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Master Scroll Pane for full profile view
        JPanel mainContainer = new JPanel();
        mainContainer.setBackground(new Color(248, 250, 252));
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBorder(new EmptyBorder(20, 25, 20, 25));

        JScrollPane scrollWrapper = new JScrollPane(mainContainer);
        scrollWrapper.setBorder(BorderFactory.createEmptyBorder());
        scrollWrapper.getVerticalScrollBar().setUnitIncrement(15);
        add(scrollWrapper, BorderLayout.CENTER);

        // Close Button at Bottom
        JButton btnClose = new JButton("Close Profile Window");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setBackground(new Color(30, 41, 59));
        btnClose.setForeground(Color.WHITE);
        btnClose.setPreferredSize(new Dimension(0, 42));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        add(btnClose, BorderLayout.SOUTH);

        // Fetch and Populate Data from Master Tables
        fetchAndRenderEmployeeData(mainContainer);
    }

    private void fetchAndRenderEmployeeData(JPanel container) {
        // SQL MODIFIED: Removed performance table joins and fields
        String sql = "SELECT e.*, j.*, ed.*, p.* " +
                     "FROM employees e " +
                     "LEFT JOIN job_details j ON e.employee_id = j.employee_id " +
                     "LEFT JOIN education_skills ed ON e.employee_id = ed.employee_id " +
                     "LEFT JOIN payroll_details p ON e.employee_id = p.employee_id " +
                     "WHERE e.employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, employeeId);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    
                    // 1. SECTION: Personal Information (Table: employees)
                    JPanel pnlPersonal = createSectionCard("1. Personal Identification Records");
                    pnlPersonal.add(createFieldLabel("Full Name:", rs.getString("full_name")));
                    pnlPersonal.add(createFieldLabel("NIC / Passport:", rs.getString("nic_passport")));
                    pnlPersonal.add(createFieldLabel("Date of Birth:", rs.getString("date_of_birth")));
                    pnlPersonal.add(createFieldLabel("Gender / Marital:", rs.getString("gender") + " / " + rs.getString("marital_status")));
                    pnlPersonal.add(createFieldLabel("Contact Phone:", rs.getString("phone")));
                    pnlPersonal.add(createFieldLabel("Email Address:", rs.getString("email")));
                    pnlPersonal.add(createFieldLabel("Residential Address:", rs.getString("address")));
                    pnlPersonal.add(createFieldLabel("Emergency Contact:", rs.getString("emergency_contact")));
                    container.add(pnlPersonal);
                    container.add(Box.createVerticalStrut(15));

                    // 2. SECTION: Deployment & Job Details (Table: job_details)
                    JPanel pnlJob = createSectionCard("2. Corporate Deployment & Position");
                    pnlJob.add(createFieldLabel("Assigned Designation:", rs.getString("job_title")));
                    pnlJob.add(createFieldLabel("Department Allocation:", rs.getString("department")));
                    pnlJob.add(createFieldLabel("Employment Type:", rs.getString("employee_type")));
                    pnlJob.add(createFieldLabel("Official Date Joined:", rs.getString("date_joined"))); 
                    pnlJob.add(createFieldLabel("Work Location/Site:", rs.getString("work_location")));
                    pnlJob.add(createFieldLabel("Direct Supervisor:", rs.getString("supervisor")));
                    pnlJob.add(createFieldLabel("System Status:", rs.getString("status")));
                    container.add(pnlJob);
                    container.add(Box.createVerticalStrut(15));

                    // 3. SECTION: Academic Qualifications & Skills (Table: education_skills)
                    JPanel pnlSkills = createSectionCard("3. Qualifications & Developed Competency");
                    pnlSkills.add(createFieldLabel("Qualifications:", rs.getString("qualifications")));
                    pnlSkills.add(createFieldLabel("Certifications:", rs.getString("certifications")));
                    pnlSkills.add(createFieldLabel("Technical Skills:", rs.getString("skills")));
                    pnlSkills.add(createFieldLabel("Prior Experience:", rs.getString("work_experience")));
                    container.add(pnlSkills);
                    container.add(Box.createVerticalStrut(15));

                    // 4. SECTION: Payroll Structure (Table: payroll_details)
                    JPanel pnlPayroll = createSectionCard("4. Remuneration & Financial Ledger Reference");
                    pnlPayroll.add(createFieldLabel("Basic Salary:", "Rs. " + String.format("%,.2f", rs.getDouble("basic_salary"))));
                    pnlPayroll.add(createFieldLabel("Fixed Allowances:", "Rs. " + String.format("%,.2f", rs.getDouble("allowances"))));
                    pnlPayroll.add(createFieldLabel("Hourly OT Rate:", "Rs. " + String.format("%,.2f", rs.getDouble("ot_rate"))));
                    pnlPayroll.add(createFieldLabel("Bank / Account:", rs.getString("bank_name") + " - " + rs.getString("account_number")));
                    pnlPayroll.add(createFieldLabel("EPF / ETF Number:", rs.getString("epf_etf_number")));
                    pnlPayroll.add(createFieldLabel("Payment Method:", rs.getString("payment_method")));
                    container.add(pnlPayroll);

                } else {
                    JLabel lblError = new JLabel("No comprehensive records found for this active employee instance.");
                    lblError.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    lblError.setForeground(Color.RED);
                    container.add(lblError);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Profile Retrieval Refusal: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Modern Flat Info Component Card Generator
    private JPanel createSectionCard(String sectionTitle) {
        JPanel card = new JPanel(new GridLayout(0, 2, 15, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                sectionTitle, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(14, 165, 233) 
        ));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(), new EmptyBorder(12, 15, 12, 15)));
        return card;
    }

    // Clean Key-Value label UI creator
    private JPanel createFieldLabel(String key, String value) {
        JPanel cell = new JPanel(new BorderLayout(5, 0));
        cell.setOpaque(false);
        
        JLabel lblKey = new JLabel(key);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKey.setForeground(new Color(100, 116, 139));
        lblKey.setPreferredSize(new Dimension(145, 20));

        JLabel lblValue = new JLabel(value != null && !value.isEmpty() ? value : "Not Provided");
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblValue.setForeground(new Color(30, 41, 59));

        cell.add(lblKey, BorderLayout.WEST);
        cell.add(lblValue, BorderLayout.CENTER);
        return cell;
    }
}