package ui;

import db.DatabaseConnection;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class PayrollManagementPanel extends JPanel {

    private JTable tblPayroll;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbMonth, cmbYear;
    private JButton btnCalculate, btnViewPayslip, btnRefresh;

    public PayrollManagementPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ========================================================
        // TOP CONTROL DECK (Title & Month/Year Selectors)
        // ========================================================
        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        // Header Titles
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Automated Payroll Ledger & Salary Engine");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        JLabel lblSubtitle = new JLabel("Automated monthly salary compilation synced with active biometric OT hours.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        topContainer.add(titlePanel, BorderLayout.WEST);

        // Month & Year Filter Row
        JPanel filterRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterRowPanel.setBackground(new Color(248, 250, 252));
        filterRowPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        cmbMonth = new JComboBox<>(months);
        cmbMonth.setSelectedIndex(LocalDate.now().getMonthValue() - 1); 
        cmbMonth.setPreferredSize(new Dimension(130, 32));
        cmbMonth.setBackground(Color.WHITE);

        String[] years = {"2025", "2026", "2027", "2028"};
        cmbYear = new JComboBox<>(years);
        cmbYear.setSelectedItem(String.valueOf(LocalDate.now().getYear())); 
        cmbYear.setPreferredSize(new Dimension(100, 32));
        cmbYear.setBackground(Color.WHITE);

        btnCalculate = new JButton("Run Salary Engine");
        btnRefresh = new JButton("Refresh Ledger");
        styleActionMiniButton(btnCalculate, new Color(30, 41, 59));
        styleActionMiniButton(btnRefresh, new Color(148, 163, 184));

        filterRowPanel.add(new JLabel("Target Payroll Month:"));
        filterRowPanel.add(cmbMonth);
        filterRowPanel.add(new JLabel("Year:"));
        filterRowPanel.add(cmbYear);
        filterRowPanel.add(btnCalculate);
        filterRowPanel.add(btnRefresh);
        topContainer.add(filterRowPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // ========================================================
        // CENTER LEDGER GRID (The Payroll Data Table)
        // ========================================================
        String[] columns = {"Emp ID", "Full Name", "Basic Salary", "Allowances", "Total OT Hours", "OT Earnings", "Gross Salary", "Net Salary"};
        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblPayroll = new JTable(tableModel);
        styleModernDataGrid(tblPayroll);

        JScrollPane scrollGridWrapper = new JScrollPane(tblPayroll);
        scrollGridWrapper.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollGridWrapper.getViewport().setBackground(Color.WHITE);
        add(scrollGridWrapper, BorderLayout.CENTER);

        // ========================================================
        // BOTTOM ACTION TOOL DECK
        // ========================================================
        JPanel actionDeckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionDeckPanel.setOpaque(false);

        btnViewPayslip = new JButton("View / Print Salary Slip");
        styleActionStaticButton(btnViewPayslip, new Color(168, 85, 247)); 

        actionDeckPanel.add(btnViewPayslip);
        add(actionDeckPanel, BorderLayout.SOUTH);

        // ========================================================
        // CORE EVENT HANDLERS
        // ========================================================
        btnCalculate.addActionListener(e -> computeAndLoadPayrollSheet());
        btnRefresh.addActionListener(e -> computeAndLoadPayrollSheet());

        btnViewPayslip.addActionListener(e -> {
            int selectedRow = tblPayroll.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee row from the ledger first!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String empId = tblPayroll.getValueAt(selectedRow, 0).toString();
            String name = tblPayroll.getValueAt(selectedRow, 1).toString();
            String basic = tblPayroll.getValueAt(selectedRow, 2).toString();
            String allowances = tblPayroll.getValueAt(selectedRow, 3).toString();
            String otHrs = tblPayroll.getValueAt(selectedRow, 4).toString();
            String otEarn = tblPayroll.getValueAt(selectedRow, 5).toString();
            String netSal = tblPayroll.getValueAt(selectedRow, 7).toString();
            String monthText = cmbMonth.getSelectedItem().toString() + " " + cmbYear.getSelectedItem().toString();

            launchPayslipInteractiveView(empId, name, basic, allowances, otHrs, otEarn, netSal, monthText);
        });

        computeAndLoadPayrollSheet();
    }

    private void launchPayslipInteractiveView(String empId, String name, String basic, String allowances, String otHrs, String otEarn, String netSal, String period) {
        JDialog previewDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Digital Salary Slip Preview", true);
        previewDialog.setSize(520, 560);
        previewDialog.setLocationRelativeTo(this);
        previewDialog.setLayout(new BorderLayout(0, 15));
        previewDialog.getContentPane().setBackground(Color.WHITE);

        String structuredPreviewText = 
            "  ==================================================\n" +
            "          RN HOUSE PLAN & CONSTRUCTION (PVT) LTD     \n" +
            "              MONTHLY STATEMENT OF REMUNERATION       \n" +
            "  ==================================================\n\n" +
            "   Pay Period        :  " + period + "\n" +
            "   Employee ID       :  " + empId + "\n" +
            "   Employee Name     :  " + name + "\n" +
            "  ------------------------------------------------------------------------------------\n" +
            "   EARNINGS CREDITS BREAKDOWN:\n\n" +
            "     (+) Fixed Basic Salary     :  " + basic + "\n" +
            "     (+) Fixed Allowances       :  " + allowances + "\n" +
            "     (+) Accumulated OT Hours   :  " + otHrs + "\n" +
            "     (+) Total OT Earnings      :  " + otEarn + "\n" +
            "  ------------------------------------------------------------------------------------\n\n" +
            "   FINAL NET TAKE-HOME PAY      :  " + netSal + "\n\n" +
            "  ==================================================\n" +
            "   System Node Status: VERIFIED AND APPROVED PAYABLE \n" +
            "  ==================================================";

        JTextArea txtArea = new JTextArea(structuredPreviewText);
        txtArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtArea.setEditable(false);
        txtArea.setBackground(new Color(248, 250, 252));
        txtArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(txtArea);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(226, 232, 240)));
        previewDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomDeck = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        bottomDeck.setBackground(new Color(248, 250, 252));
        
        JButton btnPrint = new JButton("Download & Print PDF");
        JButton btnClose = new JButton("Close Preview");
        
        stylePopupActionBtn(btnPrint, new Color(34, 197, 94)); 
        stylePopupActionBtn(btnClose, new Color(100, 116, 139)); 

        bottomDeck.add(btnClose);
        bottomDeck.add(btnPrint);
        previewDialog.add(bottomDeck, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> previewDialog.dispose());
        
        btnPrint.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Official Certified PDF Document");
            fileChooser.setSelectedFile(new File("Payslip_" + empId + "_" + period.replace(" ", "_") + ".pdf"));

            int userSelection = fileChooser.showSaveDialog(previewDialog);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                boolean success = generateIndividualPayslipPdf(fileToSave.getAbsolutePath(), empId, name, basic, allowances, otHrs, otEarn, netSal, period);
                if (success) {
                    JOptionPane.showMessageDialog(previewDialog, "Official Payslip PDF generated and saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    previewDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(previewDialog, "Error compiling the PDF parameters.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        previewDialog.setVisible(true);
    }

    // ========================================================
    // 🔮 INDIVIDUAL PAYSLIP PDF ENGINE (100% Fixed & Crash-Free)
    // ========================================================
    private boolean generateIndividualPayslipPdf(String path, String empId, String name, String basic, String allowances, String otHrs, String otEarn, String netSal, String period) {
        Document doc = new Document();
        try {
            // 🔮 FIXED LOGIC: 'writer' එක ලස්සනට වේරියබල් එකකට අරන් Direct Content එකට පාස් කලා
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // Letterhead and Subtitles
            doc.add(new Paragraph("RN HOUSE PLAN & CONSTRUCTION (PVT) LTD", FontFactory.getFont(FontFactory.HELVETICA, 16, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(15, 23, 42))));
            doc.add(new Paragraph("No 45/A, Master Hub Road, Colombo, Sri Lanka", FontFactory.getFont(FontFactory.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.GRAY)));
            doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------\n"));
            
            doc.add(new Paragraph("CONFIDENTIAL STATEMENT OF REMUNERATION", FontFactory.getFont(FontFactory.HELVETICA, 12, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(168, 85, 247))));
            doc.add(new Paragraph("Pay Period Range : " + period, FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD)));
            doc.add(new Paragraph("Issue Date Timestamp : " + LocalDate.now() + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 9, com.itextpdf.text.Font.ITALIC)));

            // Metadata Grid
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{3f, 7f});
            metaTable.addCell(createPayslipLabelCell("Employee ID Reference:"));
            metaTable.addCell(createPayslipValueCell(empId, com.itextpdf.text.Font.BOLD));
            metaTable.addCell(createPayslipLabelCell("Staff Member Name:"));
            metaTable.addCell(createPayslipValueCell(name, com.itextpdf.text.Font.BOLD));
            doc.add(metaTable);

            doc.add(new Paragraph("\n"));

            // Breakdown Earnings Grid
            PdfPTable breakdownTable = new PdfPTable(2);
            breakdownTable.setWidthPercentage(100);
            breakdownTable.setWidths(new float[]{7f, 3f});

            PdfPCell h1 = new PdfPCell(new Phrase("Earnings / Remuneration Type Credits", FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(71, 85, 105))));
            h1.setBackgroundColor(new com.itextpdf.text.BaseColor(241, 245, 249)); h1.setPadding(8f);
            PdfPCell h2 = new PdfPCell(new Phrase("Amount Value (Rs.)", FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(71, 85, 105))));
            h2.setBackgroundColor(new com.itextpdf.text.BaseColor(241, 245, 249)); h2.setPadding(8f); h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            breakdownTable.addCell(h1); breakdownTable.addCell(h2);

            breakdownTable.addCell(createCleanLayoutCell("Fixed Monthly Basic Salary"));
            breakdownTable.addCell(createCleanAmountCell(basic));
            breakdownTable.addCell(createCleanLayoutCell("Fixed Allowances and Incentives"));
            breakdownTable.addCell(createCleanAmountCell(allowances));
            breakdownTable.addCell(createCleanLayoutCell("Accumulated Overtime Work Logs (" + otHrs + ")"));
            breakdownTable.addCell(createCleanAmountCell(otEarn));

            // Summary Totals Row Block
            PdfPCell totalLabel = new PdfPCell(new Phrase("FINAL NET TAKE-HOME LIQUID PAY", FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD)));
            totalLabel.setPadding(10f); totalLabel.setBackgroundColor(new com.itextpdf.text.BaseColor(248, 250, 252));
            totalLabel.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM); totalLabel.setBorderColor(new com.itextpdf.text.BaseColor(15, 23, 42));
            breakdownTable.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(netSal, FontFactory.getFont(FontFactory.HELVETICA, 11, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(168, 85, 247))));
            totalValue.setPadding(10f); totalValue.setBackgroundColor(new com.itextpdf.text.BaseColor(248, 250, 252));
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValue.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM); totalValue.setBorderColor(new com.itextpdf.text.BaseColor(15, 23, 42));
            breakdownTable.addCell(totalValue);
            doc.add(breakdownTable);

            // ========================================================
            // 🔮 FIXED ABSOLUTE BOTTOM FOOTER LOGIC
            // ========================================================
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setTotalWidth(525); 
            footerTable.setWidths(new float[]{5f, 5f});
            
            PdfPCell f1 = new PdfPCell(new Paragraph("--------------------------------------------\nEmployer Signatory Authority", FontFactory.getFont(FontFactory.HELVETICA, 10)));
            f1.setBorder(PdfPCell.NO_BORDER); f1.setHorizontalAlignment(Element.ALIGN_LEFT);
            
            PdfPCell f2 = new PdfPCell(new Paragraph("--------------------------------------------\nEmployee Acknowledgement Receipt", FontFactory.getFont(FontFactory.HELVETICA, 10)));
            f2.setBorder(PdfPCell.NO_BORDER); f2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            PdfPCell fNotice = new PdfPCell(new Paragraph("Note: This is an officially certified legal document compiled directly from biometric system nodes.", FontFactory.getFont(FontFactory.HELVETICA, 8, com.itextpdf.text.Font.ITALIC, com.itextpdf.text.BaseColor.GRAY)));
            fNotice.setBorder(PdfPCell.TOP); fNotice.setBorderColor(new com.itextpdf.text.BaseColor(241, 245, 249));
            fNotice.setColspan(2); fNotice.setPaddingTop(15f); fNotice.setPaddingBottom(5f);

            footerTable.addCell(f1); footerTable.addCell(f2);
            footerTable.addCell(fNotice);

            // 🔮 FIXED LOGIC: වැරදි ලයින් එක වෙනුවට 'writer.getDirectContent()' පාවිච්චි කළා
            footerTable.writeSelectedRows(0, -1, 36, 55, writer.getDirectContent());

            doc.close();
            return true;
        } catch (Exception ex) {
            System.out.println("PDF Compile Pipeline Bug: " + ex.getMessage());
            return false;
        }
    }

    private PdfPCell createPayslipLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.DARK_GRAY)));
        cell.setBorder(PdfPCell.NO_BORDER); cell.setPadding(5f); return cell;
    }

    private PdfPCell createPayslipValueCell(String text, int style) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10, style, new com.itextpdf.text.BaseColor(15, 23, 42))));
        cell.setBorder(PdfPCell.NO_BORDER); cell.setPadding(5f); return cell;
    }

    private PdfPCell createCleanLayoutCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, new com.itextpdf.text.BaseColor(51, 65, 85))));
        cell.setBorder(PdfPCell.BOTTOM); cell.setBorderColor(new com.itextpdf.text.BaseColor(241, 245, 249)); cell.setPadding(8f); return cell;
    }

    private PdfPCell createCleanAmountCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, new com.itextpdf.text.BaseColor(51, 65, 85))));
        cell.setBorder(PdfPCell.BOTTOM); cell.setBorderColor(new com.itextpdf.text.BaseColor(241, 245, 249)); cell.setPadding(8f); cell.setHorizontalAlignment(Element.ALIGN_RIGHT); return cell;
    }

    public void computeAndLoadPayrollSheet() {
        tableModel.setRowCount(0); 
        int monthIndex = cmbMonth.getSelectedIndex() + 1; 
        String yearSelected = cmbYear.getSelectedItem().toString();

        String query = "SELECT e.employee_id, e.full_name, p.basic_salary, p.allowances, p.ot_rate, " +
                       "COALESCE(SUM(a.ot_hours), 0) AS total_ot_hours " +
                       "FROM employees e " +
                       "LEFT JOIN payroll_details p ON e.employee_id = p.employee_id " + 
                       "LEFT JOIN attendance a ON e.employee_id = a.employee_id " +
                       "AND MONTH(a.date) = ? AND YEAR(a.date) = ? " +
                       "WHERE e.status = 'Active' " +
                       "GROUP BY e.employee_id, e.full_name, p.basic_salary, p.allowances, p.ot_rate " +
                       "ORDER BY e.employee_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, monthIndex);
            pst.setString(2, yearSelected);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String empId = rs.getString("employee_id");
                    String fullName = rs.getString("full_name");
                    double basic = rs.getDouble("basic_salary");
                    double allowances = rs.getDouble("allowances");
                    double otRate = rs.getDouble("ot_rate");
                    double totalOtHours = rs.getDouble("total_ot_hours");

                    double otEarnings = totalOtHours * otRate;
                    double grossSalary = basic + allowances + otEarnings;
                    double netSalary = grossSalary; 

                    tableModel.addRow(new Object[]{
                        empId, fullName,
                        String.format("Rs. %,.2f", basic), String.format("Rs. %,.2f", allowances),
                        String.format("%.2f hrs", totalOtHours), String.format("Rs. %,.2f", otEarnings),
                        String.format("Rs. %,.2f", grossSalary), String.format("Rs. %,.2f", netSalary)
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Payroll Engine Execution Failure: " + ex.getMessage(), "Core Error", JOptionPane.ERROR_MESSAGE);
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
                    comp.setBackground(new Color(168, 85, 247)); 
                    comp.setForeground(Color.WHITE);
                } else {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(251, 253, 255));
                    if (c == 7) {
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        setForeground(new Color(168, 85, 247));
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
        btn.setPreferredSize(new Dimension(240, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleActionMiniButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(160, 32));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void stylePopupActionBtn(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(170, 35));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}