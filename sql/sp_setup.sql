USE [VECV_Scada_DB]
GO

-- =============================================
-- Step 1: Create Snapshot Table
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Downtime_LastRun_Snapshot]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[Downtime_LastRun_Snapshot]
    (
        SnapshotID INT IDENTITY(1,1) PRIMARY KEY,
        Station NVARCHAR(200),
        TableName NVARCHAR(50),
        SrNo BIGINT,
        Shift NVARCHAR(10),
        [Line] NVARCHAR(100),
        [Zone] NVARCHAR(100),
        [Side] NVARCHAR(50),
        Category NVARCHAR(50),
        
        StartTime DATETIME2(3),
        EndTime DATETIME2(3),
        Remark NVARCHAR(4000),
        
        IndividualMs BIGINT,
        IndividualSec INT,
        IndividualFormatted VARCHAR(50),
        
        PrevEnd DATETIME2(3),
        NewGroup INT,
        ClusterID INT,
        
        ClusterStart DATETIME2(3),
        ClusterEnd DATETIME2(3),
        
        FinalMs BIGINT,
        FinalSec INT,
        FinalFormatted VARCHAR(50),
        
        CreatedAt DATETIME DEFAULT GETDATE()
    );
    
    CREATE INDEX IX_Snapshot_Station ON Downtime_LastRun_Snapshot(Station);
    CREATE INDEX IX_Snapshot_Zone ON Downtime_LastRun_Snapshot([Zone]);
    CREATE INDEX IX_Snapshot_Shift ON Downtime_LastRun_Snapshot(Shift);
    
    PRINT 'Snapshot table created successfully';
END
ELSE
BEGIN
    PRINT 'Snapshot table already exists';
END
GO

-- =============================================
-- Step 2: Create Views for Each Zone
-- =============================================

-- View for Z3
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_Z3_Pullchord_All')
    DROP VIEW vw_Z3_Pullchord_All;
GO

CREATE VIEW vw_Z3_Pullchord_All AS
SELECT 
    'Z3_Pullchord_T2' AS TableName,
    SrNo,
    CAST(Date_Time AS DATETIME2(3)) AS Date_Time,
    Shift,
    Line,
    Zone,
    Station,
    Side,
    CAST(Maintenance_Call AS BIT) AS Maintenance_Call,
    CAST(Material_Call AS BIT) AS Material_Call,
    CAST(Production_Call AS BIT) AS Production_Call,
    CAST(Pull_Cord AS BIT) AS Pull_Cord,
    CAST(Quality_Call AS BIT) AS Quality_Call,
    Remark
FROM Z3_Pullchord_T2;
GO

-- View for Z5
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_Z5_Pullchord_All')
    DROP VIEW vw_Z5_Pullchord_All;
GO

CREATE VIEW vw_Z5_Pullchord_All AS
SELECT 
    'Z5_Pullchord_T' AS TableName,
    SrNo,
    CAST(Date_Time AS DATETIME2(3)) AS Date_Time,
    Shift,
    Line,
    Zone,
    Station,
    Side,
    CAST(Maintenance_Call AS BIT) AS Maintenance_Call,
    CAST(Material_Call AS BIT) AS Material_Call,
    CAST(Production_Call AS BIT) AS Production_Call,
    CAST(Pull_Cord AS BIT) AS Pull_Cord,
    CAST(Quality_Call AS BIT) AS Quality_Call,
    Remark
FROM Z5_Pullchord_T;
GO

-- View for Z7
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_Z7_Pullchord_All')
    DROP VIEW vw_Z7_Pullchord_All;
GO

CREATE VIEW vw_Z7_Pullchord_All AS
SELECT 
    'Z7_Pullchord_T' AS TableName,
    SrNo,
    CAST(Date_Time AS DATETIME2(3)) AS Date_Time,
    Shift,
    Line,
    Zone,
    Station,
    Side,
    CAST(Maintenance_Call AS BIT) AS Maintenance_Call,
    CAST(Material_Call AS BIT) AS Material_Call,
    CAST(Production_Call AS BIT) AS Production_Call,
    CAST(Pull_Cord AS BIT) AS Pull_Cord,
    CAST(Quality_Call AS BIT) AS Quality_Call,
    Remark
FROM Z7_Pullchord_T;
GO

-- View for Z9
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_Z9_Pullchord_All')
    DROP VIEW vw_Z9_Pullchord_All;
GO

CREATE VIEW vw_Z9_Pullchord_All AS
SELECT 
    'Z9_Pullchord_T' AS TableName,
    SrNo,
    CAST(Date_Time AS DATETIME2(3)) AS Date_Time,
    Shift,
    Line,
    Zone,
    Station,
    Side,
    CAST(Maintenance_Call AS BIT) AS Maintenance_Call,
    CAST(Material_Call AS BIT) AS Material_Call,
    CAST(Production_Call AS BIT) AS Production_Call,
    CAST(Pull_Cord AS BIT) AS Pull_Cord,
    CAST(Quality_Call AS BIT) AS Quality_Call,
    Remark
FROM Z9_Pullchord_T;
GO

-- View for ALL zones combined
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_AllPullchord')
    DROP VIEW vw_AllPullchord;
GO

CREATE VIEW vw_AllPullchord AS
SELECT * FROM vw_Z3_Pullchord_All
UNION ALL
SELECT * FROM vw_Z5_Pullchord_All
UNION ALL
SELECT * FROM vw_Z7_Pullchord_All
UNION ALL
SELECT * FROM vw_Z9_Pullchord_All;
GO

PRINT 'All views created successfully';
GO
