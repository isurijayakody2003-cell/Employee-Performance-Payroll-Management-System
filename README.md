# Employee Performance and Payroll Management System

## Project Overview

The Employee Performance and Payroll Management System is a Java-based desktop ERP application developed to streamline employee management and HR operations at RN House Plan & Construction.

This project was developed as a group project for the HNDIT2062 ICT Project module during the first year, second semester at SLIATE (ATI Gampaha).

The system digitizes traditional HR processes by providing tools to manage employee information, attendance, leave requests, tasks, performance, payroll, and reports.

## Key Features

- **Role-Based Access Control:** Separate access privileges for administrators and staff.
- **Employee Management:** Maintain employee information and records.
- **Attendance Management:** Record and manage employee attendance.
- **Leave Management:** Manage leave requests and approval workflows.
- **Task Management:** Assign tasks and track deadlines.
- **Performance Tracking:** Monitor employee task progress and performance.
- **Payroll Processing:** Calculate salaries using attendance and overtime information.
- **EPF/ETF Deductions:** Support applicable statutory payroll deductions.
- **Report Generation:** Generate reports for administrative purposes.

## Technologies Used

- Java
- Java Swing
- MySQL
- JDBC
- NetBeans IDE

## System Architecture

The application uses Java Swing for the desktop user interface, JDBC for database connectivity, and MySQL for data storage.

- **Presentation Layer:** Java Swing GUI
- **Application Layer:** Java business logic
- **Database Layer:** MySQL database accessed through JDBC

## Database

The MySQL database stores the information required for employee management, attendance, leave processing, task tracking, and payroll operations.

The SQL database script is included in the repository as `ems_db.sql`, if applicable to the current project structure.

## Installation and Setup

### Prerequisites

- Java Development Kit (JDK)
- NetBeans IDE or another compatible Java IDE
- MySQL Server
- JDBC driver for MySQL

### Setup Instructions

1. Clone or download this repository.
2. Open the project in NetBeans IDE.
3. Start the MySQL server.
4. Create the required MySQL database.
5. Import the provided `ems_db.sql` script if it contains the database schema and initial data.
6. Configure the database connection settings in the project.
7. Ensure the MySQL JDBC driver is available.
8. Build and run the application from the IDE.

**Note:** Database names, credentials, and configuration details must match the settings used in the source code.

## Project Purpose

This academic project demonstrates the practical application of Java desktop development, database integration, HR process automation, and collaborative software development.

## Academic Information

- **Module:** HNDIT2062 – ICT Project
- **Institution:** SLIATE, ATI Gampaha
- **Project Type:** Group Project
- **Project:** Employee Performance and Payroll Management System

## Contributors

Developed collaboratively as part of the HNDIT academic program.

## License

This project was developed for academic purposes. Contact the project contributors before reusing or redistributing the source code.
