package employeemanagementsystem;

import ui.LoginForm; // Import the LoginForm from ui package
import javax.swing.SwingUtilities;

public class EmployeeManagementSystem {

    public static void main(String[] args) {
        // Launch the LoginForm when the project runs
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
    
}