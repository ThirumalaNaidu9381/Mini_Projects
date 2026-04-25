import mysql.connector
from mysql.connector import Error

# TODO: update this with your actual local mysql credentials
DB_HOST = 'localhost'
DB_USER = 'root'
DB_PASSWORD = 'Naidu@1234' # <-- put your mysql password here
DB_NAME = 'hospital_db'

def get_connection(use_db=True):
    """Helper to get a database connection"""
    try:
        conn = mysql.connector.connect(
            host=DB_HOST,
            user=DB_USER,
            password=DB_PASSWORD,
            database=DB_NAME if use_db else None
        )
        return conn
    except Error as e:
        print(f"Oops! Couldn't connect to MySQL: {e}")
        return None

def setup_database():
    print("Setting up the database... hold on a sec.")
    conn = get_connection(use_db=False)
    if not conn:
        return False
        
    cursor = conn.cursor()
    
    # recreate db just to have a fresh start every time we run this
    cursor.execute(f"DROP DATABASE IF EXISTS {DB_NAME}")
    cursor.execute(f"CREATE DATABASE {DB_NAME}")
    cursor.execute(f"USE {DB_NAME}")
    
    # 1. create patients table
    cursor.execute("""
        CREATE TABLE Patients (
            patient_id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            age INT,
            gender VARCHAR(10)
        )
    """)
    
    # 2. create doctors table
    cursor.execute("""
        CREATE TABLE Doctors (
            doctor_id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            specialization VARCHAR(100)
        )
    """)
    
    # 3. create appointments table
    cursor.execute("""
        CREATE TABLE Appointments (
            appointment_id INT AUTO_INCREMENT PRIMARY KEY,
            patient_id INT,
            doctor_id INT,
            date DATE,
            FOREIGN KEY (patient_id) REFERENCES Patients(patient_id),
            FOREIGN KEY (doctor_id) REFERENCES Doctors(doctor_id)
        )
    """)
    
    # 4. create treatments table
    # per requirements: Treatments (treatment_id, patient_id, diagnosis, cost)
    cursor.execute("""
        CREATE TABLE Treatments (
            treatment_id INT AUTO_INCREMENT PRIMARY KEY,
            patient_id INT,
            diagnosis VARCHAR(200),
            cost DECIMAL(10, 2),
            FOREIGN KEY (patient_id) REFERENCES Patients(patient_id)
        )
    """)
    
    print("Tables created! Injecting some mock data...")
    
    # inserting mock patients
    patients = [
        ('Alice Smith', 45, 'Female'),
        ('Bob Johnson', 30, 'Male'),
        ('Charlie Davis', 60, 'Male'),
        ('Diana Prince', 25, 'Female'),
        ('Evan Wright', 50, 'Male')
    ]
    cursor.executemany("INSERT INTO Patients (name, age, gender) VALUES (%s, %s, %s)", patients)
    
    # inserting doctors
    doctors = [
        ('Dr. House', 'Cardiology'),
        ('Dr. Strange', 'Neurology'),
        ('Dr. Grey', 'General Surgery')
    ]
    cursor.executemany("INSERT INTO Doctors (name, specialization) VALUES (%s, %s)", doctors)
    
    # inserting appointments
    appointments = [
        (1, 1, '2023-10-01'), # Alice -> House
        (2, 3, '2023-10-05'), # Bob -> Grey
        (3, 2, '2023-10-10'), # Charlie -> Strange
        (4, 1, '2023-11-02'), # Diana -> House
        (1, 1, '2023-11-15')  # Alice -> House again
    ]
    cursor.executemany("INSERT INTO Appointments (patient_id, doctor_id, date) VALUES (%s, %s, %s)", appointments)
    
    # inserting treatments
    treatments = [
        (1, 'Hypertension', 150.00),
        (2, 'Appendicitis', 1500.00),
        (3, 'Migraine', 200.00),
        (4, 'Routine Checkup', 75.00),
        (1, 'Follow-up Checkup', 100.00)
    ]
    cursor.executemany("INSERT INTO Treatments (patient_id, diagnosis, cost) VALUES (%s, %s, %s)", treatments)
    
    conn.commit()
    cursor.close()
    conn.close()
    print("Database is ready to go!\n")
    return True

def run_analytics():
    conn = get_connection()
    if not conn: return
    cursor = conn.cursor(dictionary=True)
    
    print("="*50)
    print("HOSPITAL ANALYTICS REPORT")
    print("="*50)
    
    # 1. Find most consulted doctors
    print("\n--- Most Consulted Doctors ---")
    query1 = """
        SELECT d.name, d.specialization, COUNT(a.appointment_id) as total_visits
        FROM Doctors d
        JOIN Appointments a ON d.doctor_id = a.doctor_id
        GROUP BY d.doctor_id
        ORDER BY total_visits DESC
    """
    cursor.execute(query1)
    for row in cursor.fetchall():
        print(f"{row['name']} ({row['specialization']}) - {row['total_visits']} visits")
        
    # 2. Calculate total revenue per month
    # Joining Treatments with Appointments by patient_id to figure out the month
    print("\n--- Total Revenue per Month ---")
    query2 = """
        SELECT DATE_FORMAT(a.date, '%Y-%m') as month, SUM(t.cost) as revenue
        FROM Treatments t
        JOIN Appointments a ON t.patient_id = a.patient_id
        GROUP BY month
        ORDER BY month
    """
    cursor.execute(query2)
    for row in cursor.fetchall():
        print(f"Month: {row['month']} | Revenue: ${row['revenue']}")
        
    # 3. Identify most common diseases
    print("\n--- Most Common Diseases ---")
    query3 = """
        SELECT diagnosis, COUNT(treatment_id) as occurrences
        FROM Treatments
        GROUP BY diagnosis
        ORDER BY occurrences DESC
    """
    cursor.execute(query3)
    for row in cursor.fetchall():
        print(f"{row['diagnosis']}: {row['occurrences']} cases")
        
    # 4. Track patient visit frequency
    print("\n--- Patient Visit Frequency ---")
    query4 = """
        SELECT p.name, COUNT(a.appointment_id) as visits
        FROM Patients p
        JOIN Appointments a ON p.patient_id = a.patient_id
        GROUP BY p.patient_id
        ORDER BY visits DESC
    """
    cursor.execute(query4)
    for row in cursor.fetchall():
        print(f"{row['name']} visited {row['visits']} times")
        
    # 5. Analyze doctor performance
    print("\n--- Doctor Performance (Revenue) ---")
    query5 = """
        SELECT d.name, SUM(t.cost) as total_revenue
        FROM Doctors d
        JOIN Appointments a ON d.doctor_id = a.doctor_id
        JOIN Treatments t ON a.patient_id = t.patient_id
        GROUP BY d.doctor_id
        ORDER BY total_revenue DESC
    """
    cursor.execute(query5)
    for row in cursor.fetchall():
        # format it nicely
        print(f"{row['name']} generated ${row['total_revenue']}")
        
    print("\n" + "="*50)
    print("Done! Hope these insights are helpful.")
    
    cursor.close()
    conn.close()

if __name__ == "__main__":
    # first setup the db, if successful, run the analytics!
    if setup_database():
        run_analytics()
