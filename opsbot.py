import os
from datetime import datetime

def generate_sample_log():
    """Generates a sample server.log file if it doesn't already exist."""
    if not os.path.exists("server.log"):
        sample_logs = [
            "2026-04-22 08:15:23 [INFO] System boot completed successfully.\n",
            "2026-04-22 08:16:01 [INFO] User admin logged in.\n",
            "2026-04-22 08:17:45 [ERROR] Failed to load configuration file.\n",
            "2026-04-22 08:20:12 [INFO] Database connection established.\n",
            "2026-04-22 08:25:34 [FAILED LOGIN] Attempt from IP 192.168.1.50\n",
            "2026-04-22 08:26:01 [FAILED LOGIN] Attempt from IP 192.168.1.50\n",
            "2026-04-22 08:28:10 [CRITICAL] Out of memory exception in worker thread.\n",
            "2026-04-22 08:30:00 [INFO] Scheduled backup started.\n",
            "2026-04-22 08:35:14 [ERROR] Timeout waiting for external API.\n",
            "2026-04-22 08:36:20 [INFO] User guest logged in.\n",
            "2026-04-22 08:40:05 [FAILED LOGIN] Attempt from IP 10.0.0.15\n",
            "2026-04-22 08:45:00 [INFO] System health check passed.\n"
        ]
        with open("server.log", "w") as file:
            file.writelines(sample_logs)
        print("Created a sample 'server.log' for demonstration purposes.")

def run_opsbot():
    print("Starting OpsBot Log Analyzer...\n")
    
    log_file = "server.log"
    
    # Ensure our sample log is present
    generate_sample_log()
    
    # Prepare today's date for the output filename
    today_str = datetime.now().strftime("%Y-%m-%d")
    alert_filename = f"security_alert_{today_str}.txt"
    
    # Dictionary to keep track of frequencies
    error_counts = {
        "ERROR": 0,
        "CRITICAL": 0,
        "FAILED LOGIN": 0
    }
    
    critical_lines = []
    
    try:
        # Step 1: File Parsing
        with open(log_file, "r") as inf:
            # Step 2: Pattern Matching
            for line in inf:
                upper_line = line.upper()
                
                # Check for our keywords and categorize
                if "ERROR" in upper_line:
                    error_counts["ERROR"] += 1
                    critical_lines.append(line)
                elif "CRITICAL" in upper_line:
                    error_counts["CRITICAL"] += 1
                    critical_lines.append(line)
                elif "FAILED LOGIN" in upper_line:
                    # Step 3: Data Structuring
                    error_counts["FAILED LOGIN"] += 1
                    critical_lines.append(line)
                    
    except FileNotFoundError:
        print(f"File '{log_file}' was not found. Please make sure it's in the directory.")
        return

    # Step 4: Report Generation
    with open(alert_filename, "w") as outf:
        outf.write(f"--- OpsBot Security Alert Summary ({today_str}) ---\n\n")
        outf.write("Incident Frequencies:\n")
        for key, val in error_counts.items():
            outf.write(f"- {key}: {val}\n")
            
        outf.write("\nDetailed Log Extracts:\n")
        outf.writelines(critical_lines)
        
    print(f"Analysis complete! Found issues: {error_counts}")
    
    # Step 5: Automation (using os module to check size)
    try:
        file_size = os.path.getsize(alert_filename)
        print(f"Success! Alert Report generated: {alert_filename} (Size: {file_size} bytes)")
    except OSError as e:
        print(f"Could not retrieve file size. Error: {e}")

if __name__ == "__main__":
    run_opsbot()
