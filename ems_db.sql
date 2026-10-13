-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 17, 2026 at 10:19 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ems_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

CREATE TABLE `attendance` (
  `attendance_id` int(11) NOT NULL,
  `employee_id` varchar(20) DEFAULT NULL,
  `date` date NOT NULL,
  `check_in` time DEFAULT NULL,
  `check_out` time DEFAULT NULL,
  `working_hours` decimal(4,2) DEFAULT NULL,
  `leave_balance` int(11) DEFAULT NULL,
  `attendance_status` varchar(15) DEFAULT NULL,
  `ot_hours` decimal(4,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`attendance_id`, `employee_id`, `date`, `check_in`, `check_out`, `working_hours`, `leave_balance`, `attendance_status`, `ot_hours`) VALUES
(2, 'EMP001', '2026-07-15', '15:33:04', NULL, NULL, NULL, 'Present', NULL),
(3, 'EMP007', '2026-07-16', '10:27:30', '14:49:23', 4.35, NULL, 'Present', 0.00),
(4, 'EMP001', '2026-07-17', '09:14:02', '09:14:17', 0.00, NULL, 'Present', 0.00),
(5, 'EMP007', '2026-07-17', '09:23:00', NULL, NULL, NULL, 'Present', NULL),
(6, 'EMP008', '2026-07-17', '09:53:04', '09:53:47', 0.00, NULL, 'Present', 0.00),
(7, 'EMP008', '2026-07-17', '10:48:22', '10:48:33', 0.00, NULL, 'Present', 0.00),
(8, 'EMP006', '2026-07-17', '10:57:55', '10:58:04', 0.00, NULL, 'Present', 0.00);

-- --------------------------------------------------------

--
-- Table structure for table `education_skills`
--

CREATE TABLE `education_skills` (
  `id` int(11) NOT NULL,
  `employee_id` varchar(20) DEFAULT NULL,
  `qualifications` text DEFAULT NULL,
  `certifications` text DEFAULT NULL,
  `skills` text DEFAULT NULL,
  `work_experience` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `education_skills`
--

INSERT INTO `education_skills` (`id`, `employee_id`, `qualifications`, `certifications`, `skills`, `work_experience`) VALUES
(2, 'EMP006', 'Diploma in IT', '-', 'IT', '-'),
(3, 'EMP007', 'HNDIT', 'Data science', 'communication', '1 year'),
(4, 'EMP008', 'A/L Tech', '-', '-', '-'),
(5, 'EMP009', 'HNDIT', 'CICSCO', 'Certificate', '-'),
(6, 'EMP010', 'HND', '-', '-', '-'),
(7, 'EMP011', 'Diploma', '-', '-', '-');

-- --------------------------------------------------------

--
-- Table structure for table `employees`
--

CREATE TABLE `employees` (
  `employee_id` varchar(20) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `nic_passport` varchar(20) NOT NULL,
  `date_of_birth` date NOT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `marital_status` varchar(15) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `email` varchar(50) NOT NULL,
  `emergency_contact` varchar(100) DEFAULT NULL,
  `status` varchar(15) DEFAULT 'Active',
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employees`
--

INSERT INTO `employees` (`employee_id`, `full_name`, `nic_passport`, `date_of_birth`, `gender`, `marital_status`, `address`, `phone`, `email`, `emergency_contact`, `status`, `username`, `password`) VALUES
('EMP001', 'Kamal Perera', '951234567V', '1995-04-10', 'Male', 'Married', 'No. 45, Kandy Rd, Colombo', '0771234576', 'kamal@rnhouse.com', 'Nisha Perera - 0774445556', 'Active', 'EMP001', '951234567V'),
('EMP002', 'Nimal Silva', '987654321V', '1998-08-20', 'Male', 'Single', 'No. 12, Galle Rd, Kalutara', '0712223334', 'nimal@rnhouse.com', 'S. Silva - 0715556667', 'Active', 'nimals', '0000'),
('EMP003', 'Sayuru Bandara', '33243534v', '2000-09-09', 'Male', 'Single', 'dffds', '1234567890', 'gdhdh@ghd.com', '12333444', 'Active', 'EMP003', '33243534v'),
('EMP006', 'Isuri Jayakody', '20034567823', '2003-04-20', 'Female', 'Single', '23/2, Nittabuwa', '0772345654', 'isuri1@gmail.com', 'Kasuni-0786234543', 'Active', 'Isuri Jayakody', '20034567823'),
('EMP007', 'Oshen Wickramarathne', '20035478123', '2003-05-10', 'Male', 'Married', '20/E, IHALAYAGODA, GAMPAHA', '0767473360', 'oshenwickramarathne@gmail.com', '0701900632', 'Active', 'oshen', '200313112106'),
('EMP008', 'Supun Nadeera', '200317100850', '2003-06-19', 'Male', 'Single', '123, Kiridiwala', '0761669517', 'supunnadeera19@gmail.com', '', 'Active', 'Supun Nadeera', '200317100850'),
('EMP009', 'Hasini Nethmini', '200376412655', '2003-09-20', 'Female', 'Divorced', 'No12/Ganemulla', '0701352955', 'hasini@gmail.com', '0745654735', 'Active', 'Hasini Nethmini', '200376412655'),
('EMP010', 'Hirushi Sandamali Perera', '200175200070', '2001-09-08', 'Female', 'Single', '99/D,Kalahugoda,Minuwangoda', '0761018226', 'hirushisandamaliperera@gmail.com', '0775147260', 'Active', 'Hirushi Sandamali Perera', '200175200070'),
('EMP011', 'Hashen Kavishka', '200323810148', '2003-08-25', 'Male', 'Single', '181, Ihalayagoda', '0771266157', 'hashen1@gmail.com', '0759153818', 'Active', 'Hashen Kavishka', '200323810148');

-- --------------------------------------------------------

--
-- Table structure for table `job_details`
--

CREATE TABLE `job_details` (
  `employee_id` varchar(20) NOT NULL,
  `job_title` varchar(50) DEFAULT NULL,
  `department` varchar(50) DEFAULT NULL,
  `employee_type` varchar(20) DEFAULT NULL,
  `date_joined` date NOT NULL,
  `work_location` varchar(50) DEFAULT NULL,
  `supervisor` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `job_details`
--

INSERT INTO `job_details` (`employee_id`, `job_title`, `department`, `employee_type`, `date_joined`, `work_location`, `supervisor`) VALUES
('EMP001', 'HR Manager', 'Administration', 'Full-time', '2026-01-15', 'Head Office', 'Director'),
('EMP002', 'Site Supervisor', 'Site Operations', 'Contract', '2026-02-01', 'Galle Project Site', 'Project Manager'),
('EMP003', 'Manager', 'Administration', 'Full-time', '2026-06-21', 'eff', 'fff'),
('EMP006', 'HR Manager', 'HR', 'Full-time', '2026-07-11', 'Main office', 'Nimal Gunasekara'),
('EMP007', 'Manager', 'Administration', 'Full-time', '2026-07-12', 'gampaha', 'isuri madam'),
('EMP008', 'Electrician', 'Site Operations', 'Full-time', '2026-07-17', 'site', 'Isuri Abilashini'),
('EMP009', 'HR Manager', 'HR', 'Full-time', '2026-07-17', 'Ganemulla', '-'),
('EMP010', 'Mason', 'Site Operations', 'Full-time', '2026-07-17', 'Katunayake', ''),
('EMP011', 'HR Manager', 'HR', 'Full-time', '2026-07-17', 'site', 'Nimal');

-- --------------------------------------------------------

--
-- Table structure for table `leaves`
--

CREATE TABLE `leaves` (
  `leave_id` int(11) NOT NULL,
  `employee_id` varchar(50) NOT NULL,
  `leave_type` varchar(100) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `reason` text DEFAULT NULL,
  `status` varchar(50) DEFAULT 'Pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `leaves`
--

INSERT INTO `leaves` (`leave_id`, `employee_id`, `leave_type`, `start_date`, `end_date`, `reason`, `status`) VALUES
(3, 'EMP007', 'Medical Leave', '2026-08-01', '2026-08-02', 'sick', 'Approved'),
(4, 'EMP002', 'Casual Leave', '2026-07-30', '2026-07-31', 'go to hostipal', 'Rejected'),
(5, 'EMP002', 'Annual Leave', '2026-08-03', '2026-08-05', 'temple', 'Rejected'),
(6, 'EMP007', 'Medical Leave', '2026-08-16', '2026-08-21', 'sick', 'Pending');

-- --------------------------------------------------------

--
-- Table structure for table `payroll_details`
--

CREATE TABLE `payroll_details` (
  `employee_id` varchar(20) NOT NULL,
  `basic_salary` decimal(10,2) DEFAULT NULL,
  `allowances` decimal(10,2) DEFAULT NULL,
  `bank_name` varchar(100) DEFAULT NULL,
  `account_number` varchar(50) DEFAULT NULL,
  `epf_etf_number` varchar(20) DEFAULT NULL,
  `ot_rate` decimal(10,2) DEFAULT NULL,
  `payment_method` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payroll_details`
--

INSERT INTO `payroll_details` (`employee_id`, `basic_salary`, `allowances`, `bank_name`, `account_number`, `epf_etf_number`, `ot_rate`, `payment_method`) VALUES
('EMP001', 85000.00, 10000.00, 'Peoples Bank', '123456789012', 'EPF-12345', 500.00, 'Bank Transfer'),
('EMP002', 55000.00, 15000.00, 'BOC', '9876543210', 'EPF-67890', 400.00, 'Bank Transfer'),
('EMP003', 2345.00, 2.00, 'boc', '3323323', 'fr', 2.00, 'Bank Transfer'),
('EMP006', 25000.00, 10000.00, 'BOC', '1234567891011234', 'NE2343', 1000.00, 'Bank Transfer'),
('EMP007', 50000.00, 400000.00, 'boc', '3201479005892556', '0244', 5000.00, 'Bank Transfer'),
('EMP008', 20000.00, 15000.00, 'NSB', '803025896523', 'E12354', 200.00, 'Bank Transfer'),
('EMP009', 15000.00, 10000.00, 'BOC', '200168596714', '0244', 1000.00, 'Bank Transfer'),
('EMP010', 60000.00, 7000.00, 'NSB', '25000702050', '78600', 150.00, 'Bank Transfer'),
('EMP011', 25000.00, 15000.00, 'BOC', '2548695326587544', 'E-0234', 175.00, 'Bank Transfer');

-- --------------------------------------------------------

--
-- Table structure for table `performance`
--

CREATE TABLE `performance` (
  `review_id` int(11) NOT NULL,
  `employee_id` varchar(20) DEFAULT NULL,
  `performance_reviews` text DEFAULT NULL,
  `promotions` text DEFAULT NULL,
  `warnings_records` text DEFAULT NULL,
  `achievements` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tasks`
--

CREATE TABLE `tasks` (
  `task_id` int(11) NOT NULL,
  `employee_id` varchar(20) DEFAULT NULL,
  `task_title` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `assigned_date` date NOT NULL,
  `due_date` date DEFAULT NULL,
  `status` enum('Pending','In Progress','Completed') DEFAULT 'Pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tasks`
--

INSERT INTO `tasks` (`task_id`, `employee_id`, `task_title`, `description`, `assigned_date`, `due_date`, `status`) VALUES
(1, 'EMP002', 'Foundation Concrete - Site A', 'Supervise the foundation concrete mixing and casting layout at Piliyandala site.', '2026-06-21', '2026-06-25', 'Completed'),
(2, 'EMP002', 'faaaaa', NULL, '2026-07-16', '2026-07-28', 'Pending'),
(3, 'EMP007', 'test', NULL, '2026-07-17', '2026-07-24', 'Completed'),
(5, 'EMP006', 'Document', NULL, '2026-07-17', '2026-07-25', 'Completed'),
(6, 'EMP007', 'SRS', NULL, '2026-07-17', '2026-07-25', 'Completed'),
(7, 'EMP007', 'SRR', NULL, '2026-07-17', '2026-07-26', 'In Progress');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`attendance_id`),
  ADD KEY `employee_id` (`employee_id`);

--
-- Indexes for table `education_skills`
--
ALTER TABLE `education_skills`
  ADD PRIMARY KEY (`id`),
  ADD KEY `employee_id` (`employee_id`);

--
-- Indexes for table `employees`
--
ALTER TABLE `employees`
  ADD PRIMARY KEY (`employee_id`),
  ADD UNIQUE KEY `nic_passport` (`nic_passport`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `job_details`
--
ALTER TABLE `job_details`
  ADD PRIMARY KEY (`employee_id`);

--
-- Indexes for table `leaves`
--
ALTER TABLE `leaves`
  ADD PRIMARY KEY (`leave_id`),
  ADD KEY `employee_id` (`employee_id`);

--
-- Indexes for table `payroll_details`
--
ALTER TABLE `payroll_details`
  ADD PRIMARY KEY (`employee_id`);

--
-- Indexes for table `performance`
--
ALTER TABLE `performance`
  ADD PRIMARY KEY (`review_id`),
  ADD KEY `employee_id` (`employee_id`);

--
-- Indexes for table `tasks`
--
ALTER TABLE `tasks`
  ADD PRIMARY KEY (`task_id`),
  ADD KEY `employee_id` (`employee_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `attendance`
--
ALTER TABLE `attendance`
  MODIFY `attendance_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `education_skills`
--
ALTER TABLE `education_skills`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `leaves`
--
ALTER TABLE `leaves`
  MODIFY `leave_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `performance`
--
ALTER TABLE `performance`
  MODIFY `review_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `tasks`
--
ALTER TABLE `tasks`
  MODIFY `task_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `attendance_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`) ON DELETE CASCADE;

--
-- Constraints for table `education_skills`
--
ALTER TABLE `education_skills`
  ADD CONSTRAINT `education_skills_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`) ON DELETE CASCADE;

--
-- Constraints for table `job_details`
--
ALTER TABLE `job_details`
  ADD CONSTRAINT `job_details_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`) ON DELETE CASCADE;

--
-- Constraints for table `leaves`
--
ALTER TABLE `leaves`
  ADD CONSTRAINT `leaves_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`) ON DELETE CASCADE;

--
-- Constraints for table `payroll_details`
--
ALTER TABLE `payroll_details`
  ADD CONSTRAINT `payroll_details_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`) ON DELETE CASCADE;

--
-- Constraints for table `performance`
--
ALTER TABLE `performance`
  ADD CONSTRAINT `performance_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`) ON DELETE CASCADE;

--
-- Constraints for table `tasks`
--
ALTER TABLE `tasks`
  ADD CONSTRAINT `tasks_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
