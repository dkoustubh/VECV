# VECV SCADA Pull Chord Report Viewer
===================================

![Status](https://img.shields.io/badge/Status-Live%20Production-success)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)
![MSSQL](https://img.shields.io/badge/Database-SQL%20Server-blue)

## 📌 Project Overview
This is a **mission-critical SCADA reporting system** developed for **VE Commercial Vehicles (VECV)**. It continuously monitors production lines, captures "Pull Chord" events (stops initiated by operators), and visualizes downtime data in real-time.

**Key Goals:**
*   Minimizing downtime response time.
*   Analyzing MTBF (Mean Time Between Failures) and MTTR (Mean Time To Repair).
*   Providing actionable insights for Maintenance, Production, and Quality teams.

## 🚀 Key Features
*   **Real-Time Dashboard**: 10+ Interactive Charts updated every 30 seconds.
*   **Smart Clustering**: Automatically groups micro-stoppages into single logical events.
*   **Hybrid Engine**: Toggles between "Live Mode" (Direct DB) and "Fast Mode" (Stored Procedure optimized).
*   **Excel Export**: One-click report generation for offline analysis.
*   **Multi-Line Support**: Logic covers Z3, Z5, Z7, and Z9 production zones.

## 📚 Documentation
Detailed technical documentation is available:
*   **[Project Flow & Data Diagram](docs/PROJECT_FLOW.md)**: Visual architecture of how data moves from PLC to UI.
*   **[Technical Architecture](docs/ARCHITECTURE.md)**: Tech stack, code structure, and module details.
*   **[User Manual](docs/PROJECT_MANUAL.md)**: Guide for end-users.

## 🛠️ Installation & Setup

### 1. Windows Production (Recommended)
We provide pre-configured deployment folders for specific machines.
*   **Standard Windows**: Use the `pullchord report - windows` folder.
*   **Vaibhav's Machine**: Use the `pullchord report - Vaibhav Machine` folder.

**Steps:**
1.  Copy the respective folder to the target machine.
2.  Ensure **Java 17** is installed.
3.  Double-click **`RUN_ON_WINDOWS.bat`**.
4.  Access at `http://localhost:8070`.

### 2. Manual Developer Setup (Mac/Linux)
```bash
# 1. Clone Repository
git clone https://github.com/dkoustubh/VECV.git

# 2. Configure Database
# Edit src/main/resources/application.properties

# 3. Run Application
./mvnw spring-boot:run
```

## 📂 Project Structure
*   `/src`: Java Source Code & HTML Templates.
*   `/sql`: Database schema and Stored Procedures (`sp_CalcDowntime`).
*   `/docs`: Architecture and Flowcharts.
*   `/target`: Compiled JAR files.

## 🏗️ Version History
*   **v2.0**: Massive Performance Update.
    *   Added Stored Procedure for server-side processing.
    *   Fixed "Station" dropdown missing data issue.
    *   Added Windows deployment scripts.
*   **v1.0**: Initial Release.

---
**Developed by ATS India for VECV**
