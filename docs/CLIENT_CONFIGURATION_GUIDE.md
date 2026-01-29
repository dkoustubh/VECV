# VECV Dashboard - Client Configuration Guide

This guide explains how to configure the application with your own SQL Server credentials and run it on your machine.

## 📂 Step 1: Open the Folder
1.  Locate the folder **`pullchord report - windows`** on your computer.
2.  Open it. You should see the following files:
    *   `PullChord-Report-0.0.1-SNAPSHOT.jar`
    *   `application.properties`
    *   `RUN_ON_WINDOWS.bat`

## ⚙️ Step 2: Configure Database Credentials
You need to tell the application your specific SQL Server username and password.

1.  Right-click on the file named **`application.properties`**.
2.  Select **Open with** -> **Notepad**.
3.  Look for these two lines (usually lines 5 and 6):
    ```properties
    spring.datasource.username = ats-india
    spring.datasource.password = Kalia@6113
    ```
4.  **Edit the values**:
    *   Change `ats-india` to **your** SQL user ID.
    *   Change `Kalia@6113` to **your** SQL password.
5.  **Save the file**: Click **File** -> **Save** (or press Ctrl+S).
6.  Close Notepad.

## 🚀 Step 3: Run the Application
1.  Double-click the file named **`RUN_ON_WINDOWS.bat`**.
2.  A black window (Command Prompt) will appear.
3.  Wait for about 10-15 seconds until you see the message:
    `Started PullChordReportApplication in X.XXX seconds`
4.  D NOT close this black window. Minimizing it is fine.

## 🌐 Step 4: Open Dashboard
1.  Open your web browser (Chrome, Edge, Firefox).
2.  Type the following address in the top bar:
    **`http://localhost:8070`**
3.  The dashboard should now load with your live data.

---

## ❓ Troubleshooting

### "Connection Refused" or Database Error
*   **Cause**: The username or password in `application.properties` might be incorrect, or the SQL Server is not running.
*   **Fix**: Re-open `application.properties` and double-check exactly what you typed. Ensure there are no extra spaces.

### Window Closes Immediately
*   **Cause**: Java is likely not installed.
*   **Fix**: Open Command Prompt and type `java -version`. If it says "command not found", please install **Java 17**.

### Data is Empty
*   **Cause**: The dashboard is connected, but the `VECV_Scada_DB` database has no records.
*   **Fix**: Ensure your PLC is writing data to the `Z3_Pullchord_T2` (or similar) tables.
