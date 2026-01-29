# Project Data Flow & Architecture

This document outlines the high-level architecture and data flow of the VECV PullChord Report System.

## System Architecture

The system is designed as a **3-Tier Application**:
1.  **Data Layer (MSSQL)**: Stores raw production data from PLCs and processed downtime events.
2.  **Application Layer (Spring Boot)**: Fetches data, calculates KPIs, and serves the frontend.
3.  **Presentation Layer (HTML/JS)**: Dynamic dashboard for visualization.

## Data Flowchart

```mermaid
graph TD
    %% Nodes
    PLC[PLC / Sensors] -->|Raw Data| DB[(SQL Server Database)]
    
    subgraph Data_Processing
        DB -->|Fetch Raw Logs| SP[Stored Procedure: sp_CalcDowntime]
        SP -->|Calculate Duration & Clusters| ProcessedData[Structured Downtime Data]
    end
    
    subgraph Backend_Spring_Boot
        ProcessedData -->|JDBC| Repo[Repository Layer]
        Repo -->|Entities| Service[Service/Controller Layer]
        Service -->|JSON/HTML| API[REST API / Thymeleaf]
    end
    
    subgraph Frontend_Dashboard
        API -->|Data Binding| UI[Web Dashboard]
        UI -->|User Filters| API
    end

    %% Styling
    style PLC fill:#f9f,stroke:#333,stroke-width:2px
    style DB fill:#ff9,stroke:#333,stroke-width:2px
    style UI fill:#9f9,stroke:#333,stroke-width:2px
```

## Component Details

### 1. Database (MSSQL)
*   **Tables**: `Z3_Pullchord_T2`, `Z5_Pullchord_T` (Raw Data).
*   **Stored Procedures**: 
    *   `sp_CalcDowntime`: handles complex logic to merge overlapping downtime events and calculate "Cluster Downtime".
    *   `sp_setup`: Initializes the database tables.

### 2. Backend (Java Spring Boot)
*   **Controller**: `DashboardController.java` - Handles HTTP requests and filter logic.
*   **Repository**: `Z3PullchordT2Repository.java` - Interfaces with the database.
*   **Logic**:
    *   **Live Mode**: Fetches data directly using JPA.
    *   **Optimized Mode**: Calls `sp_CalcDowntime` for high-performance reporting.

### 3. Frontend (Web)
*   **Technology**: HTML5, CSS3, JavaScript (Chart.js).
*   **Features**:
    *   Real-time polling (every 30s).
    *   Responsive Charts (Breakdown, MTBF, MTTR).
    *   Excel Download capability.
