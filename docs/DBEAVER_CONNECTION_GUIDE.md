# DBeaver Connection Guide for VECV Pull Chord Report

## ✅ Current Status

Your project is now **fully configured and running**! Here's what has been set up:

- ✅ SQL Server running in Docker container on port 1433
- ✅ Database `VECV_Scada_DB` created with all required tables
- ✅ Spring Boot application running on http://localhost:8070
- ✅ Database connection successfully established

## 🔌 Connecting to MSSQL Database in DBeaver

Follow these steps to connect DBeaver to your SQL Server database:

### Step 1: Open DBeaver and Create New Connection

1. Launch **DBeaver**
2. Click on **Database** → **New Database Connection** (or click the plug icon in the toolbar)
3. In the connection wizard, select **SQL Server** (Microsoft SQL Server)
4. Click **Next**

### Step 2: Configure Connection Settings

Enter the following connection details:

#### **Main Tab:**
- **Host**: `localhost`
- **Port**: `1433`
- **Database**: `VECV_Scada_DB`
- **Authentication**: SQL Server Authentication
- **Username**: `sa`
- **Password**: `Ats1234@`

#### **Driver Properties Tab (Optional but Recommended):**
Add these properties for better compatibility:
- `encrypt`: `true`
- `trustServerCertificate`: `true`

### Step 3: Test Connection

1. Click **Test Connection** button at the bottom
2. If this is your first time connecting to SQL Server in DBeaver, it may prompt you to download the JDBC driver
   - Click **Download** and wait for it to complete
3. You should see a "Connected" message with connection details
4. Click **Finish** to save the connection

### Step 4: Explore Your Database

Once connected, you can:

1. **View Tables**: Expand the connection → `VECV_Scada_DB` → `Schemas` → `dbo` → `Tables`
   - You should see: `Z3_Pullchord_T2`, `Z5_Pullchord_T`, `Z7_Pullchord_T`, `Z9_Pullchord_T`

2. **Query Data**: Right-click on any table and select "View Data" or open a SQL Editor

3. **Run SQL Queries**: Click on **SQL Editor** → **New SQL Script** and run queries like:
   ```sql
   SELECT * FROM Z3_Pullchord_T2;
   SELECT * FROM Z5_Pullchord_T;
   SELECT * FROM Z7_Pullchord_T;
   SELECT * FROM Z9_Pullchord_T;
   ```

## 🌐 Accessing the Application

Your Spring Boot application is now running and accessible at:

- **Dashboard**: http://localhost:8070/dashboard
- **Report Viewer**: http://localhost:8070/report

## 🐳 Managing SQL Server Docker Container

### Check if SQL Server is Running:
```bash
docker ps | grep sqlserver
```

### Stop SQL Server:
```bash
docker stop sqlserver
```

### Start SQL Server (if stopped):
```bash
docker start sqlserver
```

### Remove SQL Server Container (if you need to start fresh):
```bash
docker stop sqlserver
docker rm sqlserver
```

Then recreate it with:
```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Ats1234@" \
   -p 1433:1433 --name sqlserver \
   -d mcr.microsoft.com/mssql/server:2022-latest
```

## 📊 Database Schema

Your database contains the following tables:

### Table: Z3_Pullchord_T2
- SrNo (Primary Key, Auto-increment)
- Date_Time
- Shift
- Line
- Zone
- Station
- Side
- Maintenance_Call
- Material_Call
- Production_Call
- Pull_Cord
- Quality_Call
- Remark

### Tables: Z5_Pullchord_T, Z7_Pullchord_T, Z9_Pullchord_T
- Same structure as Z3_Pullchord_T2

## 🔧 Troubleshooting

### DBeaver Connection Issues:

**Problem**: Cannot connect to SQL Server
- **Solution**: Ensure Docker container is running: `docker ps | grep sqlserver`
- If not running: `docker start sqlserver`

**Problem**: Driver not found
- **Solution**: DBeaver will prompt to download the JDBC driver automatically. Click "Download" when prompted.

**Problem**: Authentication failed
- **Solution**: Verify username is `sa` and password is `Ats1234@`

### Application Issues:

**Problem**: Port 8070 already in use
- **Solution**: Kill the process using the port:
  ```bash
  lsof -ti:8070 | xargs kill -9
  ```
  Then restart the application:
  ```bash
  ./mvnw spring-boot:run
  ```

**Problem**: Database connection refused
- **Solution**: Make sure SQL Server Docker container is running
  ```bash
  docker start sqlserver
  ```

## 📝 Quick Reference

### Connection String (for reference):
```
jdbc:sqlserver://localhost:1433;database=VECV_Scada_DB;encrypt=true;trustServerCertificate=true;
```

### Default Credentials:
- **Username**: sa
- **Password**: Ats1234@

### Application Port:
- **Backend**: 8070

---

**Note**: The SQL Server is running in a Docker container, so it will stop when you restart your computer. To start it again, run:
```bash
docker start sqlserver
```
