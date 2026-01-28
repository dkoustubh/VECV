# File Structure Map

This document describes the purpose of every folder and key file in this project. Use this as a map to navigate the codebase.

## 📂 Root Directory
- **`README.md`**: The main entry point. Contains a high-level summary and quick start guide.
- **`pom.xml`**: The Maven configuration file. Lists all project dependencies (Spring Boot, SQL Server Driver, etc.).
- **`mvnw` / `mvnw.cmd`**: Maven Wrapper scripts. These allow you to run the project without manually installing Maven.

## 📂 /src (Source Code)
The heart of the application. It contains the Java backend and HTML frontend.

- **`src/main/resources/application.properties`**: Configuration file. Contains database connection details (URL, Username, Password) and server port (8070).
- **`src/main/resources/templates/KD_VECV_NewClientDemoUI.html`**: The main Frontend logic. This SINGLE file contains:
    -   **HTML**: Structure of the Dashboard (Filters, Charts Grid).
    -   **CSS**: Styling (Dark Theme, Glassmorphism, Responsive Grid).
    -   **JavaScript**: Logic for fetching data, updating Chart.js charts, and handling the "Fast v/s Legacy" toggle.

- **`src/main/java/com/example/PullChord_Report/`**: Java Backend Code.
    -   **`PullChordReportApplication.java`**: The main entry point to start the Spring Boot server.
    -   **📂 /controller/**:
        -   **`DashboardController.java`**: The "Brain". It receives requests from the Frontend (`/api/analytics`), asks the Repositories for data, calculates KPIs, and sends the JSON response.
    -   **📂 /repository/**:
        -   **`Z3PullchordT2Repository.java`** (and Z5, Z7, Z9): The "Data Connectors". These files contain the SQL queries.
            -   `findTrendMetrics()`: The Optimized Query (Fast Mode).
            -   `findTrendMetricsLegacy()`: The Original Query (Slow Mode).
    -   **📂 /entity/**:
        -   **`Z3PullchordT2.java`**: Maps the SQL Table columns (`Date_Time`, `Station`, `Report_Date`) to Java Objects.

## 📂 /docs (Documentation)
Manuals and guides for users and developers.

-   **`PROJECT_MANUAL.md`**: **Client User Guide**. Explains how to install, run, and read the dashboard.
-   **`project-flow.md`**: Technical architectural diagram showing data flow.
-   **`DBEAVER_CONNECTION_GUIDE.md`**: Instructions for connecting to the database using DBeaver.

## 📂 /sql (Database Scripts)
Scripts to create and optimize the SQL Server database.

-   **`database_setup.sql`**: Creates the `VECV_Scada_DB` and the 4 main tables (`Z3`, `Z5`, `Z7`, `Z9`).
-   **`optimize_db.sql`**: Adds the `Report_Date` column to tables for faster trend searching.
-   **`optimize_dimensions.sql`**: Adds Indexes to `Station`, `Shift`, and `Line` columns for instant Pie Chart loading.

## 📂 /scripts (Utilities)
Helper scripts for maintenance.

-   **`start-project.sh`**: A simple script to launch the application on Linux/Mac.
