ins# VECV SCADA Pull Chord Report Viewer

## Overview
This project is a comprehensive SCADA reporting solution designed for VECV. It visualizes Pull Chord data from the shop floor, providing real-time insights into production efficiency, maintenance calls, and quality alerts. The application fetches data from a SQL Server database, processes it via a Spring Boot backend, and displays it on a dynamic, premium dashboard.

![Status](https://img.shields.io/badge/Status-Deployment%20Ready-success)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)

## 🚀 Key Features
- **High-Performance Dashboard**: 10+ Interactive Charts (Trends, MTBF, MTTR).
- **Hybrid Data Engine**: Toggle between "Fast Mode" (Indexed, <1s) and "Legacy Mode" (Raw, ~26s).
- **Real-Time Polling**: Updates every 30 seconds automatically.
- **Multi-Line Support**: Supports Z3, Z5, Z7, and Z9 production lines.

## 📂 Project Structure
This project follows a professional deployment structure:

-   `/src`: Source code (Java Controller, Repository, HTML/JS Frontend).
-   `/docs`: Detailed documentation, manual, and architecture guides.
    -   `PROJECT_MANUAL.md`: **Start Here** - Client & User Guide.
    -   `project-flow.md`: Technical Flowchart.
-   `/sql`: Database setup and optimization scripts.
    -   `database_setup.sql`: Initial DB creation.
    -   `optimize_db.sql`: Performance tuning scripts.
-   `/target`: Compilation output.
    -   `PullChord-Report-0.0.1-SNAPSHOT.jar`: The executable JAR file (located in `Scadda-Report/target/PullChord-Report-0.0.1-SNAPSHOT.jar`).

## 📖 Quick Start (One-Click)

The easiest way to install this on a new machine is to use the automated installer.

1.  **Copy the Folder**: Copy the entire `Scadda-Report` folder to the target machine.
2.  **Run Installer**: Double-click `ONE_CLICK_SETUP.bat`.
3.  **Select Option**:
    *   **Option 1 (New Install)**: Choose this to create a fresh Database. You will be asked to set a **Password**.
        *   **Backup Restore**: If you have a `vecv.bak` file (or a folder named `vecv.bak` containing the file) in the project directory, it will be automatically restored.
    *   **Option 2 (Existing DB)**: Choose this to connect to an existing SQL Server. You will be asked for the **Hostname**, **Username**, and **Password**.
4.  **Launch**: Click the "VECV Dashboard" shortcut on your Desktop.

---

### Manual Setup (Advanced)
If you prefer manual control:

1.  **Database Setup**: Ensure SQL Server is running. Run scripts in `/sql`.
2.  **Run Application**:
    **Windows:**
    ```cmd
    mvnw.cmd spring-boot:run
    ```
    **Mac/Linux:**
    ```bash
    ./mvnw spring-boot:run
    ```

3.  Access the dashboard at: `http://localhost:8070`

---
### Optional: Create Executable (.exe)
If you prefer a professional `VECV_Dashboard_Setup.exe` file instead of the batch file:

1.  Double-click `BUILD_SETUP.bat` inside the folder.
2.  It will create `VECV_Dashboard_Setup.exe`.
3.  You can now delete `Setup.cs` and `BUILD_SETUP.bat` and share the folder with the `.exe`.

---
## 📚 Documentation
For a detailed explanation of how the data flows from the PLC/SQL to the charts, please refer to the **[Client Manual](docs/PROJECT_MANUAL.md)**.
