USE VECV_Scada_DB;
GO

PRINT 'Indexing Dimensions for Z3_Pullchord_T2...';
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z3_Station') CREATE INDEX IDX_Z3_Station ON Z3_Pullchord_T2(Station);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z3_Shift') CREATE INDEX IDX_Z3_Shift ON Z3_Pullchord_T2(Shift);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z3_Line') CREATE INDEX IDX_Z3_Line ON Z3_Pullchord_T2(Line);
GO

PRINT 'Indexing Dimensions for Z5_Pullchord_T...';
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z5_Station') CREATE INDEX IDX_Z5_Station ON Z5_Pullchord_T(Station);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z5_Shift') CREATE INDEX IDX_Z5_Shift ON Z5_Pullchord_T(Shift);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z5_Line') CREATE INDEX IDX_Z5_Line ON Z5_Pullchord_T(Line);
GO

PRINT 'Indexing Dimensions for Z7_Pullchord_T...';
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z7_Station') CREATE INDEX IDX_Z7_Station ON Z7_Pullchord_T(Station);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z7_Shift') CREATE INDEX IDX_Z7_Shift ON Z7_Pullchord_T(Shift);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z7_Line') CREATE INDEX IDX_Z7_Line ON Z7_Pullchord_T(Line);
GO

PRINT 'Indexing Dimensions for Z9_Pullchord_T...';
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z9_Station') CREATE INDEX IDX_Z9_Station ON Z9_Pullchord_T(Station);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z9_Shift') CREATE INDEX IDX_Z9_Shift ON Z9_Pullchord_T(Shift);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Z9_Line') CREATE INDEX IDX_Z9_Line ON Z9_Pullchord_T(Line);
GO

PRINT 'Dimension Indexing Complete.';
GO
