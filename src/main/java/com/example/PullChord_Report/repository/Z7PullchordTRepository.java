package com.example.PullChord_Report.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PullChord_Report.entity.Z5PullchordTEntity;
import com.example.PullChord_Report.entity.Z7PullchordTEntity;

public interface Z7PullchordTRepository extends JpaRepository<Z7PullchordTEntity, Integer> {

        @Query("SELECT s FROM Z7PullchordTEntity s WHERE s.dateTime BETWEEN :from AND :to")
        List<Z7PullchordTEntity> findFiltered(@Param("from") String from, @Param("to") String to);

        // With shift
        @Query("SELECT s FROM Z7PullchordTEntity s WHERE s.dateTime BETWEEN :from AND :to AND s.shift = :shift AND s.station = :station")
        List<Z7PullchordTEntity> findFilteredWithShiftAndStation(
                        @Param("from") String from,
                        @Param("to") String to,
                        @Param("shift") String shift,
                        @Param("station") String station);

        // @Query("SELECT t FROM Z7PullchordTEntity t WHERE t.dateTime BETWEEN
        // :fromDateTime AND :toDateTime")
        // Page<Z7PullchordTEntity> findFiltered(@Param("fromDateTime") String
        // fromDateTime,
        // @Param("toDateTime") String toDateTime,
        // Pageable pageable);

        @Query("""
                        SELECT t FROM Z7PullchordTEntity t
                         WHERE t.dateTime BETWEEN :from AND :to
                           AND (:shift   IS NULL OR t.shift   = :shift)
                           AND (:station IS NULL OR t.station = :station)
                        """)
        Page<Z7PullchordTEntity> findFiltered(
                        @Param("from") String from,
                        @Param("to") String to,
                        @Param("shift") String shift,
                        @Param("station") String station,
                        Pageable pageable);

        @Query("SELECT t FROM Z7PullchordTEntity t WHERE t.dateTime BETWEEN :fromDateTime AND :toDateTime AND t.shift = :shift AND t.station = :station")
        Page<Z7PullchordTEntity> findByFromDateTimeBetweenAndShiftAndStation(
                        String from, String to, String shift, String station, Pageable pageable);

        @Query("SELECT e FROM Z7PullchordTEntity e WHERE " +
                        "(:station IS NULL OR :station = '' OR e.station = :station) AND " +
                        "(:shift IS NULL OR :shift = '' OR e.shift = :shift) AND " +
                        "(:from IS NULL OR :from = '' OR e.dateTime >= :from) AND " +
                        "(:to IS NULL OR :to = '' OR e.dateTime <= :to)")
        Page<Z7PullchordTEntity> searchReports(
                        @Param("station") String station,
                        @Param("shift") String shift,
                        @Param("from") String from,
                        @Param("to") String to,
                        Pageable pageable);

        // Analytics Queries
        @Query(value = "SELECT TOP 5 station, COUNT(*) as count FROM Z7_Pullchord_T GROUP BY station ORDER BY count DESC", nativeQuery = true)
        List<Object[]> findTopStations();

        @Query(value = "SELECT line, COUNT(*) as count FROM Z7_Pullchord_T GROUP BY line", nativeQuery = true)
        List<Object[]> findLineCounts();

        @Query(value = "SELECT shift, COUNT(*) as count FROM Z7_Pullchord_T GROUP BY shift", nativeQuery = true)
        List<Object[]> findShiftCounts();

        @Query(value = "SELECT TOP 7 Report_Date as date_val, COUNT(*) as count FROM Z7_Pullchord_T GROUP BY Report_Date ORDER BY Report_Date DESC", nativeQuery = true)
        List<Object[]> findDailyTrend();

        @Query(value = "SELECT TOP 30 Report_Date as date_val, COUNT(*) as total_count, " +
                        "SUM(CASE WHEN Maintenance_Call = '1' THEN 1 ELSE 0 END) as maint_count, " +
                        "SUM(CASE WHEN Production_Call = '1' THEN 1 ELSE 0 END) as prod_count, " +
                        "SUM(CASE WHEN Material_Call = '1' THEN 1 ELSE 0 END) as mat_count, " +
                        "SUM(CASE WHEN Quality_Call = '1' THEN 1 ELSE 0 END) as qual_count " +
                        "FROM Z7_Pullchord_T GROUP BY Report_Date ORDER BY Report_Date DESC", nativeQuery = true)
        List<Object[]> findTrendMetrics();

        // Legacy Queries
        @Query(value = "SELECT TOP 7 CAST(date_time AS DATE) as date_val, COUNT(*) as count FROM Z7_Pullchord_T GROUP BY CAST(date_time AS DATE) ORDER BY date_val DESC", nativeQuery = true)
        List<Object[]> findDailyTrendLegacy();

        @Query(value = "SELECT TOP 30 CAST(date_time AS DATE) as date_val, COUNT(*) as total_count, " +
                        "SUM(CASE WHEN Maintenance_Call = '1' THEN 1 ELSE 0 END) as maint_count, " +
                        "SUM(CASE WHEN Production_Call = '1' THEN 1 ELSE 0 END) as prod_count, " +
                        "SUM(CASE WHEN Material_Call = '1' THEN 1 ELSE 0 END) as mat_count, " +
                        "SUM(CASE WHEN Quality_Call = '1' THEN 1 ELSE 0 END) as qual_count " +
                        "FROM Z7_Pullchord_T GROUP BY CAST(date_time AS DATE) ORDER BY date_val DESC", nativeQuery = true)
        List<Object[]> findTrendMetricsLegacy();

}