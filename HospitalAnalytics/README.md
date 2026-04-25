# Hospital Analytics System

Welcome to the Hospital Analytics System! This is a Python-based application that interfaces with a MySQL database to manage and analyze hospital data. It's designed to simulate a real-world healthcare database, automatically setting up the necessary schema, populating it with realistic mock data, and executing key analytical queries to generate actionable insights.

Whether you're looking at revenue per month, doctor performance, or tracking disease prevalence, this script handles the heavy lifting of database management and reporting in one go.

## Features

- **Automated Database Initialization**: Automatically creates the `hospital_db` schema from scratch, ensuring a clean slate every time it runs.
- **Relational Data Modeling**: Sets up normalized tables for `Patients`, `Doctors`, `Appointments`, and `Treatments`.
- **Mock Data Injection**: Seeds the database with sample records to immediately demonstrate functionality.
- **Analytics & Reporting**: Runs complex SQL queries to extract meaningful insights, including:
  - Most consulted doctors
  - Total revenue generated per month
  - Most common diseases and diagnoses
  - Patient visit frequency
  - Doctor performance based on revenue generation

## Tech Stack

- **Python 3.x**: The core logic and script execution.
- **MySQL**: The relational database management system used for storing hospital data.
- **mysql-connector-python**: The official MySQL driver for Python to handle database connections and queries.

## Prerequisites

Before running the project, make sure you have the following installed on your machine:
- Python 3.6 or higher
- A local MySQL Server running

## Installation & Setup

1. **Clone or Download the Repository**
   Make sure all project files are in your local directory.

2. **Install Dependencies**
   Open your terminal and install the required Python MySQL connector:
   ```bash
   pip install mysql-connector-python
   ```

3. **Configure Database Credentials**
   Open `app.py` in your preferred text editor and update the MySQL connection variables (around line 7) to match your local database credentials:
   ```python
   DB_HOST = 'localhost'
   DB_USER = 'root'
   DB_PASSWORD = 'YourActualPasswordHere' # Update this line
   DB_NAME = 'hospital_db'
   ```

## Usage

Simply run the script from your terminal. It will handle the database creation, data insertion, and report generation sequentially.

```bash
python app.py
```

*Tip: If you want to save the analytics report to a file for later review, you can redirect the output:*
```bash
python app.py > hospital_analytics_output.txt
```

## Database Schema Overview

The database (`hospital_db`) consists of four primary tables:
- **Patients**: Stores demographic info (`patient_id`, `name`, `age`, `gender`).
- **Doctors**: Stores doctor details (`doctor_id`, `name`, `specialization`).
- **Appointments**: Tracks visits (`appointment_id`, `patient_id`, `doctor_id`, `date`).
- **Treatments**: Records medical procedures and costs (`treatment_id`, `patient_id`, `diagnosis`, `cost`).

