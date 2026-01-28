USE VECV_Scada_DB;
GO

-- Helper logic inside a block since we can't create procedure easily in one batch via some tools, 
-- but actually standard SQL batching works. We will just repetitive scripts for safety.

PRINT 'Optimizing Z3_Pullchord_T2...';
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Z3_Pullchord_T2') AND name = 'Report_Date')
BEGIN
    ALTER TABLE Z3_Pullchord_T2 ADD Report_Date DATE;
    PRINT 'Column Added.';
END
GO

UPDATE Z3_Pullchord_T2 SET Report_Date = CAST(Date_Time AS DATE) WHERE Report_Date IS NULL AND Date_Time IS NOT NULL;
PRINT 'Data Updated.';
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z3_Report_Date')
BEGIN
    CREATE INDEX IDX_Z3_Report_Date ON Z3_Pullchord_T2(Report_Date);
    PRINT 'Index Created.';
END
GO


PRINT 'Optimizing Z5_Pullchord_T...';
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Z5_Pullchord_T') AND name = 'Report_Date')
BEGIN
    ALTER TABLE Z5_Pullchord_T ADD Report_Date DATE;
    PRINT 'Column Added.';
END
GO

UPDATE Z5_Pullchord_T SET Report_Date = CAST(Date_Time AS DATE) WHERE Report_Date IS NULL AND Date_Time IS NOT NULL;
PRINT 'Data Updated.';
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z5_Report_Date')
BEGIN
    CREATE INDEX IDX_Z5_Report_Date ON Z5_Pullchord_T(Report_Date);
    PRINT 'Index Created.';
END
GO


PRINT 'Optimizing Z7_Pullchord_T...';
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Z7_Pullchord_T') AND name = 'Report_Date')
BEGIN
    ALTER TABLE Z7_Pullchord_T ADD Report_Date DATE;
    PRINT 'Column Added.';
END
GO

UPDATE Z7_Pullchord_T SET Report_Date = CAST(Date_Time AS DATE) WHERE Report_Date IS NULL AND Date_Time IS NOT NULL;
PRINT 'Data Updated.';
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z7_Report_Date')
BEGIN
    CREATE INDEX IDX_Z7_Report_Date ON Z7_Pullchord_T(Report_Date);
    PRINT 'Index Created.';
END
GO


PRINT 'Optimizing Z9_Pullchord_T...';
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Z9_Pullchord_T') AND name = 'Report_Date')
BEGIN
    ALTER TABLE Z9_Pullchord_T ADD Report_Date DATE;
    PRINT 'Column Added.';
END
GO

UPDATE Z9_Pullchord_T SET Report_Date = CAST(Date_Time AS DATE) WHERE Report_Date IS NULL AND Date_Time IS NOT NULL;
PRINT 'Data Updated.';
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z9_Report_Date')
BEGIN
    CREATE INDEX IDX_Z9_Report_Date ON Z9_Pullchord_T(Report_Date);
    PRINT 'Index Created.';
END
GO

PRINT 'Optimization Complete.';
GO
