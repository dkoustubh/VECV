# Production Database Migration Guide

This guide ensures that the VECV Dashboard works correctly when connected to a new Production Database (e.g., restored from `VECVjan.bak`).

## 1. Compatibility Check
The application connects to the database expecting a specific structure. It does **not** create tables itself; it reads from existing ones.

**Required Tables (Raw Data):**
Ensure your production database contains these tables (or similar ones mapped to Views):
*   `Z3_Pullchord_T2`
*   `Z5_Pullchord_T`
*   `Z7_Pullchord_T`
*   `Z9_Pullchord_T`

## 2. Mandatory Setup Steps
When you restore a backup or switch to a live production server, the **Views** and **Stored Procedures** might be missing (as they are part of the Dashboard logic, not the PLC logic).

You **MUST** run the project SQL scripts on the new database.

### Step-by-Step Instructions:
1.  **Open SQL Server Management Studio (SSMS)**.
2.  Connect to your Production Database.
3.  **Open and Run `sp_setup.sql`**:
    *   Located in the `sql/` folder of the project.
    *   This script creates the necessary **Views** (`vw_Z3_Pullchord_All`, etc.) that normalize the data for the dashboard.
    *   *Note: This does NOT delete or modify your raw production data.*
4.  **Open and Run `sp_CalcDowntime.sql`**:
    *   Located in the `sql/` folder.
    *   This creates the logic engine for calculating reports.

## 3. Troubleshooting
**Symptom:** Dashboard loads but charts are empty.
**Cause:** The application cannot find the Views or Stored Procedure in the new database.
**Fix:** Re-run the steps in Section 2.

**Symptom:** "Invalid object name 'Z3_Pullchord_T2'".
**Cause:** Your production database has different table names than expected.
**Fix:** You may need to edit `sp_setup.sql` to point the Views to your actual table names.
