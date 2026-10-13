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
import java.time.format.DateTimeFormatter;

public class ReportPreviewDialog extends JDialog {

    private JTable tblPreview;
    private DefaultTableModel tableModel;
    private String reportTitle;
    private int reportType;
    private String timeFilter; 
    private JButton btnDownload, btnCancel;
    private JLabel lblTotalCalculatedCost; 

    public ReportPreviewDialog(Frame parent, String reportTitle, int reportType, String timeFilter) {
        super(parent, "Management Report Print Preview Wizard", true);
        this.reportTitle = reportTitle;
        this.reportType = reportType;
        this.timeFilter = timeFilter;

        setSize(980, 680);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 15));
        getContentPane().setBackground(Color.WHITE);

        // ========================================================
        // TOP PREVIEW HEADER (Premium Corporate Dark Slate)
        // ========================================================
        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        headerPanel.setBackground(new Color(15, 23, 42)); // Slate 900
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblCompany = new JLabel("RN HOUSE PLAN & CONSTRUCTION (PVT) LTD");
        lblCompany.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCompany.setForeground(new Color(56, 189, 248)); // Sky 400 Accent

        JLabel lblDocTitle = new JLabel(reportTitle);
        lblDocTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDocTitle.setForeground(Color.WHITE);

        JLabel lblMeta = new JLabel("Authorized Executive Board Copy  |  Scope: " + timeFilter + "  |  Generated: " + LocalDate.now());
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMeta.setForeground(new Color(148, 163, 184));

        headerPanel.add(lblCompany);
        headerPanel.add(lblDocTitle);
        headerPanel.add(lblMeta);
        add(headerPanel, BorderLayout.NORTH);

        // ========================================================
        // CENTER LIVE GRID VIEW WITH AUTOMATED TOTAL MATRIX
        // ========================================================
        setupPreviewTableGrid();
        JScrollPane scrollGridWrapper = new JScrollPane(tblPreview);
        scrollGridWrapper.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollGridWrapper.getViewport().setBackground(Color.WHITE);
        
        lblTotalCalculatedCost = new JLabel("Analyzing database structures...", SwingConstants.RIGHT);
        lblTotalCalculatedCost.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalCalculatedCost.setForeground(new Color(15, 23, 42));
        lblTotalCalculatedCost.setBorder(new EmptyBorder(12, 0, 8, 10));

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(10, 25, 5, 25));
        centerWrapper.add(scrollGridWrapper, BorderLayout.CENTER);
        centerWrapper.add(lblTotalCalculatedCost, BorderLayout.SOUTH);
        add(centerWrapper, BorderLayout.CENTER);

        // ========================================================
        // BOTTOM ACTION TOOL DECK
        // ========================================================
        JPanel buttonDeck = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonDeck.setBackground(new Color(248, 250, 252));
        buttonDeck.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        btnDownload = new JButton("Download Official Certified PDF"); 
        btnCancel = new JButton("Close Window");

        styleButton(btnDownload, new Color(14, 165, 233)); 
        styleButton(btnCancel, new Color(100, 116, 139));   

        buttonDeck.add(btnCancel);
        buttonDeck.add(btnDownload);
        add(buttonDeck, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnDownload.addActionListener(e -> triggerPdfDownloadPipeline());

        loadLiveDatabaseRecords(); 
    }

    private void setupPreviewTableGrid() {
        String[] columns = (reportType == 1) ? 
            new String[]{"Emp ID", "Staff Full Name", "Basic Salary", "Allowances", "Gross Liability", "Pay Method"} :
            (reportType == 2) ? new String[]{"Leave ID", "Emp ID", "Staff Full Name", "Leave Type", "Duration Logs", "Status"} :
            new String[]{"Task ID", "Emp ID", "Task Operation Assignment", "Timeline Due", "Status"};

        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblPreview = new JTable(tableModel);
        tblPreview.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblPreview.setRowHeight(36);
        tblPreview.setShowGrid(false);
        tblPreview.setFocusable(false);
        tblPreview.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = tblPreview.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                setBorder(noFocusBorder);
                
                Object firstCellVal = table.getValueAt(r, 0);
                if (firstCellVal != null && firstCellVal.toString().startsWith("[PERIOD]")) {
                    comp.setBackground(new Color(30, 41, 59)); 
                    comp.setForeground(new Color(56, 189, 248)); 
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    setForeground(new Color(51, 65, 85));
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    
                    if (reportType == 1 && (c == 2 || c == 3 || c == 4)) {
                        setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }
                return comp;
            }
        };
        tblPreview.setDefaultRenderer(Object.class, cellRenderer);
    }

    private void loadLiveDatabaseRecords() {
        tableModel.setRowCount(0);
        double netSumLiability = 0.0;
        int activeRowsCount = 0;
        String sqlInterval = getSqlIntervalKeyword(timeFilter);
        String currentMonthYearTracker = ""; 

        String sql;
        if (reportType == 1) {
            sql = "SELECT p.employee_id, e.full_name, p.basic_salary, p.allowances, p.payment_method, j.date_joined " +
                  "FROM payroll_details p INNER JOIN employees e ON p.employee_id = e.employee_id " +
                  "INNER JOIN job_details j ON e.employee_id = j.employee_id " +
                  "WHERE e.status='Active' AND j.date_joined >= DATE_SUB(CURDATE(), " + sqlInterval + ") " +
                  "ORDER BY j.date_joined DESC"; 
        } else if (reportType == 2) {
            sql = "SELECT l.leave_id, l.employee_id, e.full_name, l.leave_type, l.start_date, l.end_date, l.status " +
                  "FROM leaves l INNER JOIN employees e ON l.employee_id = e.employee_id " +
                  "WHERE l.start_date >= DATE_SUB(CURDATE(), " + sqlInterval + ") ORDER BY l.start_date DESC";
        } else {
            sql = "SELECT task_id, employee_id, task_title, due_date, status FROM tasks " +
                  "WHERE due_date >= DATE_SUB(CURDATE(), " + sqlInterval + ") ORDER BY due_date DESC";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                activeRowsCount++;
                java.sql.Date recordDate = (reportType == 1) ? rs.getDate("date_joined") : (reportType == 2) ? rs.getDate("start_date") : rs.getDate("due_date");
                
                String recordMonthYear = "UNKNOWN PERIOD";
                if (recordDate != null) {
                    recordMonthYear = recordDate.toLocalDate().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                }

                if (!recordMonthYear.equals(currentMonthYearTracker)) {
                    currentMonthYearTracker = recordMonthYear;
                    tableModel.addRow(new Object[]{"[PERIOD] " + currentMonthYearTracker.toUpperCase(), "", "", "", "", ""});
                }

                if (reportType == 1) {
                    double base = rs.getDouble("basic_salary");
                    double allow = rs.getDouble("allowances");
                    double rowSum = base + allow; netSumLiability += rowSum;

                    tableModel.addRow(new Object[]{
                        rs.getString("employee_id"), rs.getString("full_name"),
                        String.format("%,.2f ", base), String.format("%,.2f ", allow),
                        String.format("%,.2f ", rowSum), rs.getString("payment_method")
                    });
                } else if (reportType == 2) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("leave_id"), rs.getString("employee_id"), rs.getString("full_name"),
                        rs.getString("leave_type"), rs.getDate("start_date") + " to " + rs.getDate("end_date"), rs.getString("status")
                    });
                } else if (reportType == 3) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("task_id"), rs.getString("employee_id"), rs.getString("task_title"),
                        rs.getDate("due_date").toString(), rs.getString("status")
                    });
                }
            }

            if (reportType == 1) {
                lblTotalCalculatedCost.setText(String.format("Total Period Financial Liability Sum: Rs. %,.2f  ", netSumLiability));
            } else {
                lblTotalCalculatedCost.setText("Total Filtered Record Entries: " + activeRowsCount + "  ");
            }

        } catch (Exception ex) {
            System.out.println("Preview Sync Failure: " + ex.getMessage());
        }
    }

    private String getSqlIntervalKeyword(String label) {
        if (label == null || label.isEmpty()) return "INTERVAL 1 MONTH";
        switch (label) {
            case "Last Day": return "INTERVAL 1 DAY";
            case "Last Week": return "INTERVAL 1 WEEK";
            case "Last Two Months": return "INTERVAL 2 MONTH";
            case "Last Six Months": return "INTERVAL 6 MONTH";
            case "Last Year": return "INTERVAL 1 YEAR";
            default: return "INTERVAL 1 MONTH"; 
        }
    }

    private void triggerPdfDownloadPipeline() {
        String defaultName = (reportType == 1) ? "Payroll_Master_Report" : (reportType == 2) ? "Workforce_Leaves_Report" : "Site_Tasks_Deployment_Report";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Official Certified PDF Document");
        fileChooser.setSelectedFile(new File(defaultName + "_" + LocalDate.now() + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile(); 
            boolean success = false;

            if (reportType == 1) success = buildProfessionalPayrollPdf(fileToSave.getAbsolutePath());
            else if (reportType == 2) success = buildProfessionalLeavesPdf(fileToSave.getAbsolutePath());
            else if (reportType == 3) success = buildProfessionalTasksPdf(fileToSave.getAbsolutePath());

            if (success) {
                JOptionPane.showMessageDialog(this, "Official PDF Report successfully generated and saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error compiling the PDF layout parameters.", "Compilation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ========================================================
    // 🔮 REFACTORED PDF ENGINES (Fixed: Clean Lines & Bottom Signatures Guard)
    // ========================================================
    private boolean buildProfessionalPayrollPdf(String path) {
        Document doc = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(path)); doc.open();
            
            doc.add(new Paragraph("RN HOUSE PLAN & CONSTRUCTION", FontFactory.getFont(FontFactory.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(15, 23, 42))));
            doc.add(new Paragraph("Official Corporate Management Executive Analytics", FontFactory.getFont(FontFactory.HELVETICA, 9, com.itextpdf.text.Font.ITALIC, com.itextpdf.text.BaseColor.GRAY)));
            doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------\n\n"));
            
            doc.add(new Paragraph("PAYROLL MASTER LEDGER AUDIT STATEMENT", FontFactory.getFont(FontFactory.HELVETICA, 13, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(14, 165, 233))));
            doc.add(new Paragraph("Scope Timeline: " + timeFilter + "  |  Extracted: " + LocalDate.now() + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));

            String sqlInterval = getSqlIntervalKeyword(timeFilter);
            String sql = "SELECT p.employee_id, e.full_name, p.basic_salary, p.allowances, j.date_joined " +
                         "FROM payroll_details p INNER JOIN employees e ON p.employee_id = e.employee_id " +
                         "INNER JOIN job_details j ON e.employee_id = j.employee_id " +
                         "WHERE e.status='Active' AND j.date_joined >= DATE_SUB(CURDATE(), " + sqlInterval + ") " +
                         "ORDER BY j.date_joined DESC";
            
            double netSumTotal = 0.0; String activeMonthTracker = ""; PdfPTable table = null;

            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("date_joined");
                    String rowMonth = (d != null) ? d.toLocalDate().format(DateTimeFormatter.ofPattern("MMMM yyyy")) : "UNKNOWN PERIOD";

                    if (!rowMonth.equals(activeMonthTracker)) {
                        if (table != null) { doc.add(table); doc.add(new Paragraph("\n")); } 
                        activeMonthTracker = rowMonth;
                        
                        doc.add(new Paragraph("PARTITION: " + activeMonthTracker.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA, 11, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(15, 23, 42))));
                        doc.add(new Paragraph(" "));
                        
                        table = new PdfPTable(5); table.setWidthPercentage(100);
                        table.setWidths(new float[]{1.5f, 3.5f, 2f, 2f, 2.5f});
                        
                        String[] headers = {"Emp ID", "Staff Full Name", "Basic Salary", "Allowances", "Gross Liability"};
                        for (String h : headers) {
                            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(71, 85, 105))));
                            cell.setBackgroundColor(new com.itextpdf.text.BaseColor(241, 245, 249)); 
                            cell.setBorder(PdfPCell.BOTTOM); cell.setBorderColor(new com.itextpdf.text.BaseColor(226, 232, 240));
                            cell.setPadding(8f); cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            table.addCell(cell);
                        }
                    }

                    double b = rs.getDouble("basic_salary"); double a = rs.getDouble("allowances");
                    double gross = b + a; netSumTotal += gross;

                    table.addCell(createCleanCell(rs.getString("employee_id"), Element.ALIGN_CENTER));
                    table.addCell(createCleanCell(rs.getString("full_name"), Element.ALIGN_LEFT));
                    table.addCell(createCleanCell(String.format("%,.2f", b), Element.ALIGN_RIGHT));
                    table.addCell(createCleanCell(String.format("%,.2f", a), Element.ALIGN_RIGHT));
                    table.addCell(createCleanCell(String.format("%,.2f", gross), Element.ALIGN_RIGHT));
                }
                if (table != null) doc.add(table);
            }

            doc.add(new Paragraph("\n----------------------------------------------------------------------------------------------------------------------------------"));
            doc.add(new Paragraph("NET FINANCING EXPENSE LIABILITY: Rs. " + String.format("%,.2f", netSumTotal), FontFactory.getFont(FontFactory.HELVETICA, 12, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(34, 197, 94))));
            
            // 🔮 FIXED LOGIC: Absolute Page Bottom Footer Table Registration
            injectAbsoluteBottomSignaturesBlock(writer, "Compiled By: Accounts Executive", "Authorized Validation Signatory");

            doc.close(); return true;
        } catch (Exception ex) { return false; }
    }

    private boolean buildProfessionalLeavesPdf(String path) {
        Document doc = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(path)); doc.open();
            doc.add(new Paragraph("RN HOUSE PLAN & CONSTRUCTION", FontFactory.getFont(FontFactory.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(15, 23, 42))));
            doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------\n"));
            doc.add(new Paragraph("WORKFORCE LEAVE VACATION RECORD SUMMARY", FontFactory.getFont(FontFactory.HELVETICA, 12, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(34, 197, 94))));
            doc.add(new Paragraph("Timeline Filter Scope: " + timeFilter + "  |  Date: " + LocalDate.now() + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));

            String sqlInterval = getSqlIntervalKeyword(timeFilter);
            String sql = "SELECT l.leave_id, l.employee_id, e.full_name, l.leave_type, l.start_date, l.end_date, l.status " +
                         "FROM leaves l INNER JOIN employees e ON l.employee_id = e.employee_id " +
                         "WHERE l.start_date >= DATE_SUB(CURDATE(), " + sqlInterval + ") ORDER BY l.start_date DESC";
            
            String activeMonthTracker = ""; PdfPTable table = null;
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sd = rs.getDate("start_date");
                    String rowMonth = (sd != null) ? sd.toLocalDate().format(DateTimeFormatter.ofPattern("MMMM yyyy")) : "UNKNOWN PERIOD";

                    if (!rowMonth.equals(activeMonthTracker)) {
                        if (table != null) { doc.add(table); doc.add(new Paragraph("\n")); }
                        activeMonthTracker = rowMonth;
                        doc.add(new Paragraph("PARTITION: " + activeMonthTracker.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA, 11, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(15, 23, 42))));
                        doc.add(new Paragraph(" "));
                        
                        table = new PdfPTable(5); table.setWidthPercentage(100);
                        String[] headers = {"Leave ID", "Emp ID", "Staff Full Name", "Leave Type", "Status"};
                        for (String h : headers) {
                            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(71, 85, 105))));
                            cell.setBackgroundColor(new com.itextpdf.text.BaseColor(241, 245, 249)); cell.setPadding(8f); cell.setBorder(PdfPCell.BOTTOM); cell.setBorderColor(new com.itextpdf.text.BaseColor(226, 232, 240));
                            table.addCell(cell);
                        }
                    }
                    table.addCell(createCleanCell(String.valueOf(rs.getInt("leave_id")), Element.ALIGN_CENTER));
                    table.addCell(createCleanCell(rs.getString("employee_id"), Element.ALIGN_CENTER));
                    table.addCell(createCleanCell(rs.getString("full_name"), Element.ALIGN_LEFT));
                    table.addCell(createCleanCell(rs.getString("leave_type"), Element.ALIGN_LEFT));
                    table.addCell(createCleanCell(rs.getString("status"), Element.ALIGN_CENTER));
                }
                if (table != null) doc.add(table);
            }
            
            // 🔮 FIXED LOGIC: Page Bottom Signature
            injectAbsoluteBottomSignaturesBlock(writer, "Human Resource Department Audit Desk", "Executive Director Stamp Verification");

            doc.close(); return true;
        } catch (Exception ex) { return false; }
    }

    private boolean buildProfessionalTasksPdf(String path) {
        Document doc = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(path)); doc.open();
            doc.add(new Paragraph("RN HOUSE PLAN & CONSTRUCTION", FontFactory.getFont(FontFactory.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(15, 23, 42))));
            doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------\n"));
            doc.add(new Paragraph("SITE OPERATION DISPATCH LEGISLATIVE LOGS", FontFactory.getFont(FontFactory.HELVETICA, 12, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(234, 179, 8))));
            doc.add(new Paragraph("Timeline Scope: " + timeFilter + "  |  Extraction Date: " + LocalDate.now() + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));

            String sqlInterval = getSqlIntervalKeyword(timeFilter);
            String sql = "SELECT task_id, employee_id, task_title, due_date, status FROM tasks " +
                         "WHERE due_date >= DATE_SUB(CURDATE(), " + sqlInterval + ") ORDER BY due_date DESC";
            
            String activeMonthTracker = ""; PdfPTable table = null;
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date dd = rs.getDate("due_date");
                    String rowMonth = (dd != null) ? dd.toLocalDate().format(DateTimeFormatter.ofPattern("MMMM yyyy")) : "UNKNOWN PERIOD";

                    if (!rowMonth.equals(activeMonthTracker)) {
                        if (table != null) { doc.add(table); doc.add(new Paragraph("\n")); }
                        activeMonthTracker = rowMonth;
                        doc.add(new Paragraph("PARTITION: " + activeMonthTracker.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA, 11, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(15, 23, 42))));
                        doc.add(new Paragraph(" "));
                        
                        table = new PdfPTable(5); table.setWidthPercentage(100);
                        String[] headers = {"Task ID", "Emp ID", "Task Operation Assignment", "Timeline Due", "Status"};
                        for (String h : headers) {
                            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(71, 85, 105))));
                            cell.setBackgroundColor(new com.itextpdf.text.BaseColor(241, 245, 249)); cell.setPadding(8f); cell.setBorder(PdfPCell.BOTTOM); cell.setBorderColor(new com.itextpdf.text.BaseColor(226, 232, 240));
                            table.addCell(cell);
                        }
                    }
                    table.addCell(createCleanCell(String.valueOf(rs.getInt("task_id")), Element.ALIGN_CENTER));
                    table.addCell(createCleanCell(rs.getString("employee_id"), Element.ALIGN_CENTER));
                    table.addCell(createCleanCell(rs.getString("task_title"), Element.ALIGN_LEFT));
                    table.addCell(createCleanCell(rs.getDate("due_date").toString(), Element.ALIGN_CENTER));
                    table.addCell(createCleanCell(rs.getString("status"), Element.ALIGN_CENTER));
                }
                if (table != null) doc.add(table);
            }
            
            // 🔮 FIXED LOGIC: Page Bottom Signature
            injectAbsoluteBottomSignaturesBlock(writer, "Chief Operations Dispatch Controller", "Chief Site Engineering General Director");

            doc.close(); return true;
        } catch (Exception ex) { return false; }
    }

    // 🔮 PRIVATE FIXED COMPILER CORE: ඩේටා කොච්චර තිබ්බත් අත්සන් කෑල්ල පිටුවේ පතුලටම (Absolute Bottom) සෙට් කරන මැජික් මෙතඩ් එක
    private void injectAbsoluteBottomSignaturesBlock(PdfWriter writer, String leftSignatory, String rightSignatory) throws Exception {
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setTotalWidth(525); 
        footerTable.setWidths(new float[]{5f, 5f});
        
        PdfPCell f1 = new PdfPCell(new Paragraph("-------------------------------------------------------\n" + leftSignatory, FontFactory.getFont(FontFactory.HELVETICA, 9, com.itextpdf.text.Font.BOLD)));
        f1.setBorder(PdfPCell.NO_BORDER); f1.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        PdfPCell f2 = new PdfPCell(new Paragraph("-------------------------------------------------------\n" + rightSignatory, FontFactory.getFont(FontFactory.HELVETICA, 9, com.itextpdf.text.Font.BOLD)));
        f2.setBorder(PdfPCell.NO_BORDER); f2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        PdfPCell fNotice = new PdfPCell(new Paragraph("Disclaimer Notice: This audit matrix contains verified operational assets data direct logs synchronized from secure server nodes.", FontFactory.getFont(FontFactory.HELVETICA, 8, com.itextpdf.text.Font.ITALIC, com.itextpdf.text.BaseColor.GRAY)));
        fNotice.setBorder(PdfPCell.TOP); fNotice.setBorderColor(new com.itextpdf.text.BaseColor(226, 232, 240));
        fNotice.setColspan(2); fNotice.setPaddingTop(12f); fNotice.setPaddingBottom(5f);

        footerTable.addCell(f1); footerTable.addCell(f2);
        footerTable.addCell(fNotice);

        // Forces the footer to write at coordinates (X=36, Y=50) from the absolute bottom of A4 canvas
        footerTable.writeSelectedRows(0, -1, 36, 50, writer.getDirectContent());
    }

    private PdfPCell createCleanCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, new com.itextpdf.text.BaseColor(51, 65, 85))));
        cell.setBorder(PdfPCell.BOTTOM);
        cell.setBorderColor(new com.itextpdf.text.BaseColor(241, 245, 249)); 
        cell.setPadding(8f);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(260, 42));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}