# MotorPH Payroll System

## Overview
The MotorPH Payroll System is a comprehensive solution for managing employee information, attendance tracking, payroll processing, and government-mandated deduction calculations. The system provides functionality for calculating salary, benefits, overtime, and statutory deductions according to Philippine labor laws.

## System Architecture

The system is structured into the following key components:

### Core Components
- **Employee Management**: Handles employee data storage and retrieval
- **Attendance Tracking**: Processes employee time records and calculates work hours
- **Payroll Processing**: Calculates salaries, deductions, and net pay
- **Deduction Calculation**: Computes government-mandated deductions (SSS, PhilHealth, Pag-IBIG, Tax)
- **Holiday Management**: Tracks holidays and calculates holiday pay
- **Testing Framework**: Provides comprehensive testing for system components
- **Reporting System**: Generates detailed financial and attendance reports
- **UI Layer**: Provides user interface for payroll operations

### Package Structure
- `motorph`: Main package containing the application entry point
- `motorph.employee`: Employee data models and management
- `motorph.hours`: Attendance tracking and work hour calculations
- `motorph.deductions`: Statutory deduction calculations
- `motorph.holidays`: Holiday management
- `motorph.process`: Core payroll processing logic
- `motorph.reports`: Report generation
- `motorph.input`: User input handling
- `motorph.output`: Output formatting and display
- `motorph.ui`: User interface components
- `motorph.util`: Utility classes and helper functions
- `motorph.test`: Test classes for system validation

## Features

- **Employee Management**
  - Store and retrieve employee information
  - Search employees by ID or name
  - View employee details and benefits

- **Attendance Tracking**
  - Process attendance records
  - Calculate regular and overtime hours
  - Track late arrivals and undertime
  - Generate attendance reports

- **Payroll Processing**
  - Calculate gross and net pay
  - Process mid-month and end-month payrolls
  - Apply appropriate deductions based on pay period
  - Handle absences, late arrivals, and overtime

- **Statutory Compliance**
  - SSS contributions
  - PhilHealth contributions
  - Pag-IBIG Fund contributions
  - Withholding tax calculation
  - Holiday pay computation

- **Reporting**
  - Weekly Hours Report
  - Holiday Pay Report
  - Payroll Calendar
  - Attendance Summary

- **System Testing**
  - Statutory Deductions Tests
  - Payroll Processing Tests
  - Work Hours Calculator Tests
  - Holiday Pay Tests

## Getting Started

### Prerequisites
- Java 8 or higher
- CSV files for employee and attendance data

### Data Files
The system requires two CSV files in the resources directory:
1. **Employee Data CSV**: `resources/MotorPH Employee Data - Employee Details.csv`
2. **Attendance Records CSV**: `resources/MotorPH Employee Data - Attendance Record.csv`

### Running the Application
1. Compile the Java source files
2. Run the Main class: `java motorph.Main`
  - Alternatively, use the AppLauncher: `java motorph.util.AppLauncher`
3. The system will display the main menu with the following options:
  - Run Payroll System
  - Run System Tests
  - Run Reports
  - Exit
4. Select the desired option and follow the on-screen prompts

### Payroll System Menu
When running the payroll system, you'll have access to:
1. Process Payroll
2. Find Employee
3. View Payroll Calendar
4. Exit

### Reports Menu
The system provides the following reports:
1. Weekly Hours Report
2. Holiday Pay Report

### System Tests Menu
For system validation, you can run:
1. Statutory Deductions Tests
2. Payroll Processing Tests
3. Work Hours Calculator Tests

## Payroll Policies

- **Work Hours**: Regular work is 8 hours per day, 5 days a week
- **Overtime**: Hours beyond 8 hours per day (25% premium rate)
- **Late Policy**:
  - Grace period until 8:10 AM
  - Employees who arrive after 8:10 AM are marked late
  - Late employees are not eligible for overtime pay
- **Attendance Cutoff**:
  - Mid-month: 27th of previous month to 12th of current month
  - End-month: 13th to 26th of current month
- **Deduction Schedule**:
  - Mid-month: SSS, PhilHealth, Pag-IBIG
  - End-month: Withholding Tax
- **Holiday Pay**:
  - Regular holidays: 100% of daily rate
  - Special non-working holidays: 30% of daily rate

## Calculation Formulas

### Basic Pay
- Semi-monthly Rate = Monthly Salary ÷ 2

### Deductions
- Late Deduction = (Hourly Rate ÷ 60) × Late Minutes
- Undertime Deduction = (Hourly Rate ÷ 60) × Undertime Minutes
- Absence Deduction = Daily Rate × Absent Days

### Tax Calculation
Withholding tax uses progressive tax rates according to Philippine tax brackets:
- Up to ₱20,833: 0%
- Over ₱20,833 to ₱33,332: 20% of excess over ₱20,833
- Over ₱33,332 to ₱66,666: ₱2,500 + 25% of excess over ₱33,333
- Over ₱66,666 to ₱166,666: ₱10,833 + 30% of excess over ₱66,667
- Over ₱166,666 to ₱666,666: ₱40,833.33 + 32% of excess over ₱166,667
- Over ₱666,666: ₱200,833.33 + 35% of excess over ₱666,667

## Development Guidelines

### Coding Standards
- Use consistent indentation (4 spaces)
- Add JavaDoc comments for all classes and methods
- Use meaningful variable and method names
- Handle all exceptions appropriately
- Validate all inputs before processing

### Version Control
- Use descriptive commit messages
- Create feature branches for new development
- Submit pull requests for code review
- Tag releases with version numbers

### Testing
- Run the built-in test suite regularly
- Add new tests for any added functionality
- Ensure all calculations comply with labor regulations
- Validate input handling and edge cases

## Maintenance and Support

For bug reports, feature requests, or support inquiries, please contact the MotorPH IT Department.

## License

Copyright © 2025 MotorPH. All rights reserved.