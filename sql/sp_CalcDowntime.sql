USE [VECV_Scada_DB]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_identifier ON
GO

/********************************************************************************************
    Stored Procedure: sp_CalcDowntime
    Purpose        : Calculates individual + final merged downtime based on filtered data.
                     Supports zone-based view selection, station filtering, shift filtering,
                     default 06:30 → next day 06:29:59 date logic, event-pairing, cluster
                     merging with 100ms tolerance, and pagination.

    CLEAN, OPTIMIZED, FULL PRODUCTION VERSION
*********************************************************************************************/

CREATE OR ALTER PROCEDURE [dbo].[sp_CalcDowntime]
(
    @Stations        NVARCHAR(200) = NULL,     -- NULL or 'All Stations' = all stations
    @Shift           NVARCHAR(10)  = NULL,     -- A / B / C or NULL
    @FromDateStr     NVARCHAR(50)  = NULL,
    @ToDateStr       NVARCHAR(50)  = NULL,
    @ZoneOrTable     NVARCHAR(20)  = NULL,     -- Z3 / Z5 / Z7 / Z9 or NULL = all zones
    @PageNumber      INT = 1,
    @PageSize        INT = 5000,
    @GapMillis       INT = 100,                 -- 100 ms gap/simultaneity threshold
    @FetchAll        BIT = 0                   -- NEW FLAG BIT FOR EXCEL OPTION
)
AS
BEGIN
    SET NOCOUNT ON;

    /**************************************************************************
        1. DATE LOGIC (clean + safe 06:30 → next day 06:29:59)
    **************************************************************************/
    DECLARE @FromDT DATETIME = TRY_CAST(@FromDateStr AS DATETIME);
    DECLARE @ToDT   DATETIME = TRY_CAST(@ToDateStr   AS DATETIME);

    DECLARE @Today DATETIME      = CAST(CAST(GETDATE() AS DATE) AS DATETIME);
    DECLARE @Today630        DATETIME = DATEADD(MINUTE, 390, @Today);      -- today 06:30
    DECLARE @Tomorrow630     DATETIME = DATEADD(DAY, 1, @Today630);        -- tomorrow 06:30
    DECLARE @Tomorrow629     DATETIME = DATEADD(MILLISECOND, -3, @Tomorrow630); -- 06:29:59.997

    IF (@FromDT IS NULL AND @ToDT IS NULL)
    BEGIN
        SET @FromDT = DATEADD(DAY, -1000, @Today630); -- Default to last ~3 years to show all data
        SET @ToDT   = @Tomorrow629;
    END
    ELSE IF (@FromDT IS NOT NULL AND @ToDT IS NULL)
    BEGIN
        SET @ToDT   = @Tomorrow629;
    END
    ELSE IF (@FromDT IS NULL AND @ToDT IS NOT NULL)
    BEGIN
        SET @FromDT = DATEADD(HOUR, -24, @ToDT);
    END


    /**************************************************************************
        2. Select correct SOURCE VIEW based on zone
    **************************************************************************/
    DECLARE @SourceView NVARCHAR(100);

    IF @ZoneOrTable = 'TEST' SET @SourceView = 'Test_Pullchord_T';
    ELSE IF @ZoneOrTable = 'Z3' SET @SourceView = 'vw_Z3_Pullchord_All';
    ELSE IF @ZoneOrTable = 'Z5' SET @SourceView = 'vw_Z5_Pullchord_All';
    ELSE IF @ZoneOrTable = 'Z7' SET @SourceView = 'vw_Z7_Pullchord_All';
    ELSE IF @ZoneOrTable = 'Z9' SET @SourceView = 'vw_Z9_Pullchord_All';
    ELSE IF @ZoneOrTable = 'All Zones' SET @SourceView = 'vw_AllPullchord';   -- ALL zones
    ELSE SET @SourceView = 'vw_Z3_Pullchord_All';


    /**************************************************************************
        3. SNAPSHOT TABLE (#Snap)
    **************************************************************************/
    CREATE TABLE #Snap
    (
        RowID INT IDENTITY PRIMARY KEY,
        TableName NVARCHAR(50),
        SrNo BIGINT,
        Date_Time DATETIME2(3),
        Shift NVARCHAR(10),
        [Line] NVARCHAR(100),
        [Zone] NVARCHAR(100),
        Station NVARCHAR(200),
        [Side] NVARCHAR(50),
        Maintenance_Call BIT,
        Material_Call BIT,
        Production_Call BIT,
        Pull_Cord BIT,
        Quality_Call BIT,
        Remark NVARCHAR(4000)
    );


    /**************************************************************************
        4. Load SNAPSHOT data using dynamic SQL (correct source view)
           Station logic:
               NULL or 'All Stations' → no filtering
               otherwise filter by specific station
    **************************************************************************/
    DECLARE @SQL NVARCHAR(MAX) = '
        INSERT INTO #Snap
        SELECT
            TableName, SrNo, Date_Time, Shift, [Line], [Zone], Station, [Side],
            CAST(Maintenance_Call AS BIT),
            CAST(Material_Call    AS BIT),
            CAST(Production_Call  AS BIT),
            CAST(Pull_Cord        AS BIT),
            CAST(Quality_Call     AS BIT),
            Remark
        FROM ' + @SourceView + '
        WHERE
            Date_Time >= @FromDT
            AND Date_Time <= @ToDT
            AND (
        @Shift IS NULL
        OR @Shift = ''All Shifts''
        OR Shift = @Shift
    )

            AND (
                    @Stations IS NULL
                    OR @Stations = ''All Stations''
                    OR Station = @Stations
                )
        ORDER BY Date_Time, SrNo;
    ';

    EXEC sp_executesql @SQL,
        N'@FromDT DATETIME, @ToDT DATETIME, @Shift NVARCHAR(10), @Stations NVARCHAR(200)',
        @FromDT, @ToDT, @Shift, @Stations;


    /**************************************************************************
        5. DETECT START (0→1) AND END (1→0) EVENTS
    **************************************************************************/
    ;WITH Prev AS
    (
        SELECT *,
            LAG(Maintenance_Call) OVER (PARTITION BY Station ORDER BY Date_Time, RowID) AS Prev_M,
            LAG(Material_Call)    OVER (PARTITION BY Station ORDER BY Date_Time, RowID) AS Prev_Mat,
            LAG(Production_Call)  OVER (PARTITION BY Station ORDER BY Date_Time, RowID) AS Prev_P,
            LAG(Pull_Cord)        OVER (PARTITION BY Station ORDER BY Date_Time, RowID) AS Prev_PC,
            LAG(Quality_Call)     OVER (PARTITION BY Station ORDER BY Date_Time, RowID) AS Prev_Q
        FROM #Snap
    ),

    Starts AS (
        SELECT Station, TableName, SrNo, Date_Time AS StartTime, 'Maintenance' AS Category,
               Remark, Shift, [Line], [Zone], [Side]
        FROM Prev WHERE Maintenance_Call = 1 AND (Prev_M = 0 OR Prev_M IS NULL)
        UNION ALL
        SELECT Station, TableName, SrNo, Date_Time, 'Material', Remark, Shift, [Line], [Zone], [Side]
        FROM Prev WHERE Material_Call = 1 AND (Prev_Mat = 0 OR Prev_Mat IS NULL)
        UNION ALL
        SELECT Station, TableName, SrNo, Date_Time, 'Production', Remark, Shift, [Line], [Zone], [Side]
        FROM Prev WHERE Production_Call = 1 AND (Prev_P = 0 OR Prev_P IS NULL)
        UNION ALL
        SELECT Station, TableName, SrNo, Date_Time, 'PullCord', Remark, Shift, [Line], [Zone], [Side]
        FROM Prev WHERE Pull_Cord = 1 AND (Prev_PC = 0 OR Prev_PC IS NULL)
        UNION ALL
        SELECT Station, TableName, SrNo, Date_Time, 'Quality', Remark, Shift, [Line], [Zone], [Side]
        FROM Prev WHERE Quality_Call = 1 AND (Prev_Q = 0 OR Prev_Q IS NULL)
    ),

    Ends AS (
        SELECT Station, Date_Time AS EndTime, 'Maintenance' AS Category
        FROM Prev WHERE Maintenance_Call = 0 AND Prev_M = 1
        UNION ALL
        SELECT Station, Date_Time, 'Material' FROM Prev WHERE Material_Call = 0 AND Prev_Mat = 1
        UNION ALL
        SELECT Station, Date_Time, 'Production' FROM Prev WHERE Production_Call = 0 AND Prev_P = 1
        UNION ALL
        SELECT Station, Date_Time, 'PullCord' FROM Prev WHERE Pull_Cord = 0 AND Prev_PC = 1
        UNION ALL
        SELECT Station, Date_Time, 'Quality' FROM Prev WHERE Quality_Call = 0 AND Prev_Q = 1
    ),

    StartsRN AS (
        SELECT *, ROW_NUMBER() OVER (PARTITION BY Station, Category ORDER BY StartTime, SrNo) rn
        FROM Starts
    ),
    EndsRN AS (
        SELECT *, ROW_NUMBER() OVER (PARTITION BY Station, Category ORDER  BY EndTime) rn
        FROM Ends
    )

    SELECT
        s.Station, s.TableName, s.SrNo, s.Shift, s.[Line], s.[Zone], s.[Side],
        s.Category, s.StartTime,
        COALESCE(e.EndTime, @ToDT) AS EndTime,
        s.Remark
    INTO #Events
    FROM StartsRN s
    LEFT JOIN EndsRN e
        ON s.Station = e.Station AND s.Category = e.Category AND s.rn = e.rn
    ORDER BY s.StartTime;


    /**************************************************************************
        6. INDIVIDUAL DOWNTIME
    **************************************************************************/
    ALTER TABLE #Events ADD
        IndividualMs BIGINT,
        IndividualSec INT;

    UPDATE #Events
    SET IndividualMs = DATEDIFF_BIG(MILLISECOND, StartTime, EndTime),
        IndividualSec = DATEDIFF_BIG(SECOND, StartTime, EndTime);


    /**************************************************************************
        7. CLUSTER MERGE LOGIC (overlap + <100ms gap)
    **************************************************************************/
    ;WITH Ord AS (
        SELECT *, ROW_NUMBER() OVER (ORDER BY StartTime, EndTime, Station) AS OrdID
        FROM #Events
    ),
    PrevMax AS (
        SELECT *,
         MAX(EndTime) OVER (ORDER BY StartTime, EndTime ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING)
         AS PrevEnd
        FROM Ord
    ),
    Flags AS (
        SELECT *,
            CASE
                WHEN PrevEnd IS NULL THEN 1
                WHEN StartTime > DATEADD(MILLISECOND, @GapMillis, PrevEnd) THEN 1
                ELSE 0
            END AS NewGroup
        FROM PrevMax
    ),
    Groups AS (
        SELECT *,
            SUM(NewGroup) OVER (ORDER BY StartTime, EndTime) AS ClusterID
        FROM Flags
    ),
    Bounds AS (
        SELECT ClusterID, MIN(StartTime) AS ClusterStart, MAX(EndTime) AS ClusterEnd
        FROM Groups
        GROUP BY ClusterID
    )

    SELECT 
        g.*, b.ClusterStart, b.ClusterEnd,
        DATEDIFF_BIG(MILLISECOND, b.ClusterStart, b.ClusterEnd) AS FinalMs,
        DATEDIFF_BIG(SECOND, b.ClusterStart, b.ClusterEnd) AS FinalSec
    INTO #Final
    FROM Groups g
    JOIN Bounds b ON g.ClusterID = b.ClusterID
    ORDER BY g.StartTime;

    /********************************************************************************************
    SNAPSHOT PERSISTENCE (SILENT, FULL DATASET)
    Purpose: Stores the FULL unpaginated result of this run for ad-hoc KPI analysis.
    This does NOT affect UI output or pagination.
********************************************************************************************/

-- Clear previous snapshot
TRUNCATE TABLE dbo.Downtime_LastRun_Snapshot;

-- Persist FULL final dataset (no pagination applied)
INSERT INTO dbo.Downtime_LastRun_Snapshot
(
    Station,
    TableName,
    SrNo,
    Shift,
    [Line],
    [Zone],
    [Side],
    Category,

    StartTime,
    EndTime,
    Remark,

    IndividualMs,
    IndividualSec,
    IndividualFormatted,

    PrevEnd,
    NewGroup,
    ClusterID,

    ClusterStart,
    ClusterEnd,

    FinalMs,
    FinalSec,
    FinalFormatted
)
SELECT
    f.Station,
    f.TableName,
    f.SrNo,
    f.Shift,
    f.[Line],
    f.[Zone],
    f.[Side],
    f.Category,

    f.StartTime,
    f.EndTime,
    f.Remark,

    f.IndividualMs,
    f.IndividualSec,

    -- IndividualFormatted (same logic as final SELECT)
    CAST(f.IndividualMs / 3600000 AS VARCHAR(20)) + ':' +
        RIGHT('00' + CAST((f.IndividualMs % 3600000) / 60000 AS VARCHAR(2)), 2) + ':' +
        RIGHT('00' + CAST((f.IndividualMs % 60000) / 1000 AS VARCHAR(2)), 2),

    f.PrevEnd,
    f.NewGroup,
    f.ClusterID,

    f.ClusterStart,
    f.ClusterEnd,

    f.FinalMs,
    f.FinalSec,

    -- FinalFormatted (same logic as final SELECT)
    CAST(f.FinalMs / 3600000 AS VARCHAR(20)) + ':' +
        RIGHT('00' + CAST((f.FinalMs % 3600000) / 60000 AS VARCHAR(2)), 2) + ':' +
        RIGHT('00' + CAST((f.FinalMs % 60000) / 1000 AS VARCHAR(2)), 2)
FROM #Final f;


/**************************************************************************
    8. PAGINATION + TOTAL PAGES (UI FRIENDLY)
**************************************************************************/
DECLARE @TotalRows BIGINT = (SELECT COUNT(*) FROM #Final);
DECLARE @Offset INT = (@PageNumber - 1) * @PageSize;

-- Calculate total pages
DECLARE @TotalPages INT =
    CASE 
        WHEN @TotalRows = 0 THEN 1
        ELSE CEILING(1.0 * @TotalRows / @PageSize)
    END;

-- Return paginated rows (unchanged, your exact formatting kept)
SELECT
    f.Station,
    f.TableName,
    f.SrNo,
    f.Shift,
    f.[Line],
    f.[Zone],
    f.[Side],
    f.Category,
    f.StartTime,
    f.EndTime,
    f.Remark,
    f.IndividualMs,
    f.IndividualSec,

    -- Your existing INDIVIDUAL multi-day SAFE format:
    CAST(f.IndividualMs / 3600000 AS VARCHAR(20)) + ':' +
        RIGHT('00' + CAST((f.IndividualMs % 3600000) / 60000 AS VARCHAR(2)), 2) + ':' +
        RIGHT('00' + CAST((f.IndividualMs % 60000) / 1000 AS VARCHAR(2)), 2)
        AS IndividualFormatted,

    f.PrevEnd,
    f.NewGroup,
    f.ClusterID,
    f.ClusterStart,
    f.ClusterEnd,
    f.FinalMs,
    f.FinalSec,

    -- Your existing FINAL multi-day SAFE format:
    CAST(f.FinalMs / 3600000 AS VARCHAR(20)) + ':' +
        RIGHT('00' + CAST((f.FinalMs % 3600000) / 60000 AS VARCHAR(2)), 2) + ':' +
        RIGHT('00' + CAST((f.FinalMs % 60000) / 1000 AS VARCHAR(2)), 2)
        AS FinalFormatted,
    
    @TotalRows AS TotalRows,
    @TotalPages AS TotalPages

FROM #Final f
WHERE 
    (@FetchAll = 1)  -- Excel export → return EVERYTHING
    OR
    (f.OrdID > @Offset AND f.OrdID <= @Offset + @PageSize)

ORDER BY f.OrdID;

END;
GO

PRINT 'Stored procedure sp_CalcDowntime created successfully';
GO
