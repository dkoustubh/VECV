package com.example.PullChord_Report.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PullChord_Report.entity.Z3PullchordT2Entity;;

public interface Z3PullchordT2Repository extends JpaRepository<Z3PullchordT2Entity, Integer> {

       @Query(value = "SELECT * FROM Z3_Pullchord_T2 WHERE date_time BETWEEN ?1 AND ?2", nativeQuery = true)
       Page<Z3PullchordT2Entity> findFiltered(String fromDateTime, String toDateTime, Pageable pageable);

       @Query(value = "SELECT * FROM Z3_Pullchord_T2 WHERE date_time BETWEEN ?1 AND ?2 AND shift = ?3 ", nativeQuery = true)
       Page<Z3PullchordT2Entity> findFilteredWithShift(String fromDateTime, String toDateTime, String shiftName,
                     Pageable pageable);

       @Query("SELECT s FROM Z3PullchordT2Entity s WHERE s.dateTime BETWEEN :from AND :to")
       List<Z3PullchordT2Entity> findFiltered(@Param("from") String from, @Param("to") String to);

       // With shift
       @Query("SELECT s FROM Z3PullchordT2Entity s WHERE s.dateTime BETWEEN :from AND :to AND s.shift = :shift")
       List<Z3PullchordT2Entity> findFilteredWithShift(@Param("from") String from, @Param("to") String to,
                     @Param("shift") String shiftName);

       @Query(value = "SELECT * FROM Z3_Pullchord_T2 WHERE station = :station", countQuery = "SELECT count(*) FROM Z3_Pullchord_T2 WHERE station = :station", nativeQuery = true)
       Page<Z3PullchordT2Entity> findByStation(@Param("station") String station, Pageable pageable);

       @Query(value = "SELECT * FROM Z3_Pullchord_T2 WHERE station = :station AND shift = :shift", countQuery = "SELECT count(*) FROM Z3_Pullchord_T2 WHERE station = :station AND shift = :shift", nativeQuery = true)
       Page<Z3PullchordT2Entity> findByStationAndShift(@Param("station") String station,
                     @Param("shift") String shiftName,
                     Pageable pageable);

       @Query("SELECT s FROM Z3PullchordT2Entity s WHERE s.station = :station")
       List<Z3PullchordT2Entity> findByStation(@Param("station") String station);

       @Query(value = "SELECT DISTINCT station FROM Z3_Pullchord_T2 ORDER BY station", nativeQuery = true)
       List<String> findAllUniqueStations();

       // Analytics Queries
       @Query(value = "SELECT TOP 5 station, COUNT(*) as count FROM Z3_Pullchord_T2 GROUP BY station ORDER BY count DESC", nativeQuery = true)
       List<Object[]> findTopStations();

       @Query(value = "SELECT line, COUNT(*) as count FROM Z3_Pullchord_T2 GROUP BY line", nativeQuery = true)
       List<Object[]> findLineCounts();

       @Query(value = "SELECT shift, COUNT(*) as count FROM Z3_Pullchord_T2 GROUP BY shift", nativeQuery = true)
       List<Object[]> findShiftCounts();

       @Query("SELECT e FROM Z3PullchordT2Entity e WHERE " +
                     "(:station IS NULL OR :station = '' OR e.station = :station) AND " +
                     "(:shift IS NULL OR :shift = '' OR e.shift = :shift) AND " +
                     "(:from IS NULL OR :from = '' OR e.dateTime >= :from) AND " +
                     "(:to IS NULL OR :to = '' OR e.dateTime <= :to)")
       Page<Z3PullchordT2Entity> searchReports(
                     @Param("station") String station,
                     @Param("shift") String shift,
                     @Param("from") String from,
                     @Param("to") String to,
                     Pageable pageable);

       @Query(value = "SELECT TOP 7 Report_Date as date_val, COUNT(*) as count FROM Z3_Pullchord_T2 GROUP BY Report_Date ORDER BY Report_Date DESC", nativeQuery = true)
       List<Object[]> findDailyTrend();

       @Query(value = "SELECT TOP 30 Report_Date as date_val, COUNT(*) as total_count, " +
                     "SUM(CASE WHEN Maintenance_Call = '1' THEN 1 ELSE 0 END) as maint_count, " +
                     "SUM(CASE WHEN Production_Call = '1' THEN 1 ELSE 0 END) as prod_count, " +
                     "SUM(CASE WHEN Material_Call = '1' THEN 1 ELSE 0 END) as mat_count, " +
                     "SUM(CASE WHEN Quality_Call = '1' THEN 1 ELSE 0 END) as qual_count " +
                     "FROM Z3_Pullchord_T2 GROUP BY Report_Date ORDER BY Report_Date DESC", nativeQuery = true)
       List<Object[]> findTrendMetrics();

       // Legacy Queries (Slow)
       @Query(value = "SELECT TOP 7 CAST(date_time AS DATE) as date_val, COUNT(*) as count FROM Z3_Pullchord_T2 GROUP BY CAST(date_time AS DATE) ORDER BY date_val DESC", nativeQuery = true)
       List<Object[]> findDailyTrendLegacy();

       @Query(value = "SELECT TOP 30 CAST(date_time AS DATE) as date_val, COUNT(*) as total_count, " +
                     "SUM(CASE WHEN Maintenance_Call = '1' THEN 1 ELSE 0 END) as maint_count, " +
                     "SUM(CASE WHEN Production_Call = '1' THEN 1 ELSE 0 END) as prod_count, " +
                     "SUM(CASE WHEN Material_Call = '1' THEN 1 ELSE 0 END) as mat_count, " +
                     "SUM(CASE WHEN Quality_Call = '1' THEN 1 ELSE 0 END) as qual_count " +
                     "FROM Z3_Pullchord_T2 GROUP BY CAST(date_time AS DATE) ORDER BY date_val DESC", nativeQuery = true)
       List<Object[]> findTrendMetricsLegacy();

       // Stored Procedure for Optimized Downtime Calculation
       @Query(value = "EXEC sp_CalcDowntime " +
                     "@Stations = :stations, " +
                     "@Shift = :shift, " +
                     "@FromDateStr = :fromDate, " +
                     "@ToDateStr = :toDate, " +
                     "@ZoneOrTable = :zone, " +
                     "@PageNumber = :pageNumber, " +
                     "@PageSize = :pageSize, " +
                     "@FetchAll = :fetchAll", nativeQuery = true)
       List<Object[]> callDowntimeSP(
                     @Param("stations") String stations,
                     @Param("shift") String shift,
                     @Param("fromDate") String fromDate,
                     @Param("toDate") String toDate,
                     @Param("zone") String zone,
                     @Param("pageNumber") Integer pageNumber,
                     @Param("pageSize") Integer pageSize,
                     @Param("fetchAll") Integer fetchAll);

       // Metrics from Snapshot (for graphs based on SP calculation)
       @Query(value = "SELECT CAST(StartTime AS DATE) as date_val, COUNT(*) as total_count, " +
                     "SUM(CASE WHEN Category = 'Maintenance' THEN 1 ELSE 0 END) as maint_count, " +
                     "SUM(CASE WHEN Category = 'Production' THEN 1 ELSE 0 END) as prod_count, " +
                     "SUM(CASE WHEN Category = 'Material' THEN 1 ELSE 0 END) as mat_count, " +
                     "SUM(CASE WHEN Category = 'Quality' THEN 1 ELSE 0 END) as qual_count, " +
                     "SUM(FinalMs) as total_downtime_ms " +
                     "FROM Downtime_LastRun_Snapshot GROUP BY CAST(StartTime AS DATE) ORDER BY date_val DESC", nativeQuery = true)
       List<Object[]> findTrendMetricsFromSnapshot();

       @Query(value = "SELECT Shift, COUNT(*) as count FROM Downtime_LastRun_Snapshot GROUP BY Shift", nativeQuery = true)
       List<Object[]> findShiftCountsFromSnapshot();

       @Query(value = "SELECT Line, COUNT(*) as count FROM Downtime_LastRun_Snapshot GROUP BY Line", nativeQuery = true)
       List<Object[]> findLineCountsFromSnapshot();

       @Query(value = "SELECT TOP 5 Station, COUNT(*) as count FROM Downtime_LastRun_Snapshot GROUP BY Station ORDER BY count DESC", nativeQuery = true)
       List<Object[]> findTopStationsFromSnapshot();

}