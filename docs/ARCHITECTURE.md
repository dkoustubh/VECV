# Technical Architecture Guide

## Tech Stack
*   **Language**: Java 17
*   **Framework**: Spring Boot 3.3.11
*   **Build Tool**: Maven
*   **Database**: Microsoft SQL Server 2012+
*   **Frontend**: Thymeleaf, Vanilla JS, Chart.js
*   **CSS Framework**: Custom CSS (Responsive)

## Directory Structure
```
Scadda-Report/
├── src/main/java/com/example/PullChord_Report
│   ├── controller/      # API & View Controllers
│   ├── entity/          # JPA Entities (Database Tables)
│   ├── repository/      # Data Access Layer
│   └── PullChordReportApplication.java  # Main Entry Point
├── src/main/resources
│   ├── templates/       # HTML Views (Thymeleaf)
│   └── application.properties # Config (Port, DB Credentials)
├── sql/                 # SQL Scripts (Schema, Stored Procedures)
├── docs/                # Project Documentation
└── target/              # Compiled Output (JAR file)
```

## Key Modules

### 1. Station Management
*   **Problem**: Stations were hardcoded.
*   **Solution**: Implemented `findAllUniqueStations()` in Repository to dynamically fetch specific stations present in the database log, ensuring the dropdown menu is always accurate.

### 2. Downtime Calculation Engine
*   **Challenge**: Raw PLC logs can generate thousands of micro-events for a single breakdown.
*   **Solution**: 
    *   **Clustering**: Consecutive events (within 1 minute) are grouped into a single "Downtime Cluster".
    *   **Procedure**: `sp_CalcDowntime` handles this logic on the DB server to minimize latency.

### 3. Deployment
*   The project builds into a single executable JAR: `PullChord-Report-0.0.1-SNAPSHOT.jar`.
*   It supports external configuration via `application.properties` for easy migration between environments (Dev vs. Prod).
