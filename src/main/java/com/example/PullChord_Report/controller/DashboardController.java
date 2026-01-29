package com.example.PullChord_Report.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.PullChord_Report.entity.Z3PullchordT2Entity;
import com.example.PullChord_Report.entity.Z5PullchordTEntity;
import com.example.PullChord_Report.entity.Z7PullchordTEntity;
import com.example.PullChord_Report.entity.Z9PullchordTEntity;
import com.example.PullChord_Report.repository.Z3PullchordT2Repository;
import com.example.PullChord_Report.repository.Z5PullchordTRepository;
import com.example.PullChord_Report.repository.Z7PullchordTRepository;
import com.example.PullChord_Report.repository.Z9PullchordTRepository;

@Controller
public class DashboardController {

    @Autowired
    private Z3PullchordT2Repository z3Repository;

    @Autowired
    private Z5PullchordTRepository z5Repository;

    @Autowired
    private Z7PullchordTRepository z7Repository;

    @Autowired
    private Z9PullchordTRepository z9Repository;

    // Root path redirects to dashboard
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "Z3 Pullchord T2") String selectedTable,
            @RequestParam(required = false) String station,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false, name = "fromDateTime") String from,
            @RequestParam(required = false, name = "toDateTime") String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, // Increased default size
            @RequestParam(defaultValue = "srNo") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        long startTime = System.currentTimeMillis();

        // 1. Clean Inputs
        if (station != null && station.trim().isEmpty())
            station = null;
        if (shift != null && shift.trim().isEmpty())
            shift = null;

        if (from != null && !from.trim().isEmpty()) {
            from = from.replace("T", " ");
            if (from.length() == 16)
                from += ":00";
        } else {
            from = null;
        }

        if (to != null && !to.trim().isEmpty()) {
            to = to.replace("T", " ");
            if (to.length() == 16)
                to += ":59";
        } else {
            to = null;
        }

        // Map selectedTable to Zone for SP
        String zoneParam = "Z3";
        if (selectedTable.contains("Z5"))
            zoneParam = "Z5";
        else if (selectedTable.contains("Z7"))
            zoneParam = "Z7";
        else if (selectedTable.contains("Z9"))
            zoneParam = "Z9";

        // 2. Call Stored Procedure
        // Note: Page is 0-indexed in Spring, 1-indexed in SP. So page + 1.
        List<Object[]> spResults = z3Repository.callDowntimeSP(
                station,
                shift,
                from,
                to,
                zoneParam,
                page + 1,
                size,
                0);

        // 3. Process Results
        List<Map<String, Object>> records = new ArrayList<>();
        long totalRecords = 0;
        int totalPages = 0;

        if (spResults != null && !spResults.isEmpty()) {
            // Extract totals from first row (cols 22 and 23 added in SP)
            Object[] firstRow = spResults.get(0);
            if (firstRow.length > 23) {
                totalRecords = firstRow[22] != null ? ((Number) firstRow[22]).longValue() : 0;
                totalPages = firstRow[23] != null ? ((Number) firstRow[23]).intValue() : 1;
            } else {
                // Fallback if SP not updated yet
                totalRecords = spResults.size();
            }

            for (Object[] row : spResults) {
                Map<String, Object> item = new HashMap<>();
                // Keys must match what Frontend expects (e.g., entity properties)
                // Entity: srNo, dateTime, shift, line, zone, station, side, maintenanceCall...
                // SP: 0=Station, 1=TableName, 2=SrNo, 3=Shift, 4=Line, 5=Zone, 6=Side,
                // 7=Category
                // 8=StartTime, 9=EndTime, 10=Remark...

                item.put("station", row[0]);
                item.put("srNo", row[2]);
                item.put("shift", row[3]);
                item.put("line", row[4]);
                item.put("zone", row[5]);
                item.put("side", row[6]);

                // Map Category to Boolean calls for UI compatibility AND raw Category for
                // display
                String cat = (String) row[7];
                item.put("category", cat); // New field for display
                item.put("maintenanceCall", "Maintenance".equals(cat) ? true : false);
                item.put("materialCall", "Material".equals(cat) ? true : false);
                item.put("productionCall", "Production".equals(cat) ? true : false);
                item.put("pullCord", "PullCord".equals(cat) ? true : false);
                item.put("qualityCall", "Quality".equals(cat) ? true : false);

                item.put("dateTime", row[8]); // StartTime mapped to dateTime
                item.put("startTime", row[8]); // Explicit startTime
                item.put("endTime", row[9]);
                item.put("remark", row[10]);

                // Add new SP fields
                item.put("individualDowntime", row[13]); // IndividualFormatted
                item.put("clusterDowntime", row[21]); // FinalFormatted
                item.put("downtime", row[21]); // Keep 'downtime' for backward compatibility if needed
                item.put("durationMs", row[19]); // FinalMs

                records.add(item);
            }
        }

        // 4. Calculate KPIs from Snapshot (Accurate based on SP run)
        List<Object[]> snapshotMetrics = z3Repository.findTrendMetricsFromSnapshot();

        long totalEvents = 0;
        double totalDowntimeMs = 0;

        for (Object[] m : snapshotMetrics) {
            totalEvents += ((Number) m[1]).longValue();
            totalDowntimeMs += m[6] != null ? ((Number) m[6]).doubleValue() : 0;
        }

        // Formulas
        double availableTimeMins = (shift != null) ? 480.0
                : 1440.0 * (snapshotMetrics.isEmpty() ? 1 : snapshotMetrics.size());
        // Note: Available time is tricky with ranges. Simplified:
        // If snapshot has 1 day, 1440. If multiple, 1440 * days.
        // For accurate KPI, we typically look at the range.

        double totalDowntimeMins = totalDowntimeMs / 60000.0;
        double operatingTimeMins = Math.max(0, availableTimeMins - totalDowntimeMins);

        double uptimePercentage = 100.0 - ((totalDowntimeMins / availableTimeMins) * 100.0);
        double breakdownPercentage = (totalDowntimeMins / availableTimeMins) * 100.0;

        double mtbfHours = (totalEvents > 0) ? (operatingTimeMins / 60.0) / totalEvents : (availableTimeMins / 60.0);
        double mttrMins = (totalEvents > 0) ? totalDowntimeMins / totalEvents : 0.0;

        double oeePercentage = (operatingTimeMins / availableTimeMins) * 0.96 * 0.99 * 100.0;

        // Pass to View
        model.addAttribute("kpiAvailability", Math.round(uptimePercentage * 100.0) / 100.0);
        model.addAttribute("kpiBreakdownPct", Math.round(breakdownPercentage * 100.0) / 100.0);
        model.addAttribute("kpiOEE", Math.round(oeePercentage * 100.0) / 100.0);
        model.addAttribute("kpiMTTR", Math.round(mttrMins * 10.0) / 10.0);
        model.addAttribute("kpiMTBF", Math.round(mtbfHours * 10.0) / 10.0);
        model.addAttribute("kpiDowntime", Math.round(totalDowntimeMins * 10.0) / 10.0);
        model.addAttribute("totalEvents", totalEvents);

        // Populate Model for Table
        model.addAttribute("records", records);
        model.addAttribute("selectedTable", selectedTable);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        // Filter state
        model.addAttribute("station", station);
        model.addAttribute("shift", shift);
        model.addAttribute("fromDateTime", from);
        model.addAttribute("toDateTime", to);

        // Add chart data directly to model (optional, but robust)
        model.addAttribute("uniqueStations", z3Repository.findAllUniqueStations()); // Keep generic list for dropdowns
                                                                                    // if
        // needed

        System.out.println("Dashboard Load Time (SP Mode): " + (System.currentTimeMillis() - startTime) + "ms");

        return "KD_VECV_NewClientDemoUI";
    }

    @GetMapping("/api/kpi")
    @ResponseBody
    public Map<String, Object> getKPIData() {
        Map<String, Object> kpiData = new HashMap<>();

        // Get counts from all tables
        long z3Count = z3Repository.count();
        long z5Count = z5Repository.count();
        long z7Count = z7Repository.count();
        long z9Count = z9Repository.count();

        long totalEvents = z3Count + z5Count + z7Count + z9Count;

        kpiData.put("totalEvents", totalEvents);
        kpiData.put("z3Events", z3Count);
        kpiData.put("z5Events", z5Count);
        kpiData.put("z7Events", z7Count);
        kpiData.put("z9Events", z9Count);
        kpiData.put("totalDowntime", "N/A"); // Can be calculated if needed
        kpiData.put("avgDuration", "N/A"); // Can be calculated if needed

        return kpiData;
    }

    @GetMapping("/api/downtime")
    @ResponseBody
    public Map<String, Object> getDowntimeData(
            @RequestParam(defaultValue = "Z3") String zone,
            @RequestParam(required = false) String station,
            @RequestParam(required = false) String shift) {

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> data;

        switch (zone) {
            case "Z3":
                data = z3Repository.findAll().stream()
                        .map(this::convertZ3ToMap)
                        .collect(Collectors.toList());
                break;
            case "Z5":
                data = z5Repository.findAll().stream()
                        .map(this::convertZ5ToMap)
                        .collect(Collectors.toList());
                break;
            case "Z7":
                data = z7Repository.findAll().stream()
                        .map(this::convertZ7ToMap)
                        .collect(Collectors.toList());
                break;
            case "Z9":
                data = z9Repository.findAll().stream()
                        .map(this::convertZ9ToMap)
                        .collect(Collectors.toList());
                break;
            default:
                data = z3Repository.findAll().stream()
                        .map(this::convertZ3ToMap)
                        .collect(Collectors.toList());
        }

        response.put("data", data);
        response.put("total", data.size());

        return response;
    }

    private Map<String, Object> convertZ3ToMap(Z3PullchordT2Entity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("sr", entity.getSrNo());
        map.put("datetime", entity.getDateTime());
        map.put("shift", entity.getShift());
        map.put("line", entity.getLine());
        map.put("zone", entity.getZone());
        map.put("station", entity.getStation());
        map.put("side", entity.getSide());
        map.put("maintenanceCall", entity.getMaintenanceCall());
        map.put("materialCall", entity.getMaterialCall());
        map.put("productionCall", entity.getProductionCall());
        map.put("pullCord", entity.getPullCord());
        map.put("qualityCall", entity.getQualityCall());
        map.put("remark", entity.getRemark());
        return map;
    }

    private Map<String, Object> convertZ5ToMap(Z5PullchordTEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("sr", entity.getSrNo());
        map.put("datetime", entity.getDateTime());
        map.put("shift", entity.getShift());
        map.put("line", entity.getLine());
        map.put("zone", entity.getZone());
        map.put("station", entity.getStation());
        map.put("side", entity.getSide());
        map.put("maintenanceCall", entity.getMaintenanceCall());
        map.put("materialCall", entity.getMaterialCall());
        map.put("productionCall", entity.getProductionCall());
        map.put("pullCord", entity.getPullCord());
        map.put("qualityCall", entity.getQualityCall());
        map.put("remark", entity.getRemark());
        return map;
    }

    private Map<String, Object> convertZ7ToMap(Z7PullchordTEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("sr", entity.getSrNo());
        map.put("datetime", entity.getDateTime());
        map.put("shift", entity.getShift());
        map.put("line", entity.getLine());
        map.put("zone", entity.getZone());
        map.put("station", entity.getStation());
        map.put("side", entity.getSide());
        map.put("maintenanceCall", entity.getMaintenanceCall());
        map.put("materialCall", entity.getMaterialCall());
        map.put("productionCall", entity.getProductionCall());
        map.put("pullCord", entity.getPullCord());
        map.put("qualityCall", entity.getQualityCall());
        map.put("remark", entity.getRemark());
        return map;
    }

    private Map<String, Object> convertZ9ToMap(Z9PullchordTEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("sr", entity.getSrNo());
        map.put("datetime", entity.getDateTime());
        map.put("shift", entity.getShift());
        map.put("line", entity.getLine());
        map.put("zone", entity.getZone());
        map.put("station", entity.getStation());
        map.put("side", entity.getSide());
        map.put("maintenanceCall", entity.getMaintenanceCall());
        map.put("materialCall", entity.getMaterialCall());
        map.put("productionCall", entity.getProductionCall());
        map.put("pullCord", entity.getPullCord());
        map.put("qualityCall", entity.getQualityCall());
        map.put("remark", entity.getRemark());
        return map;
    }

    @GetMapping("/api/db-status")
    @ResponseBody
    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // Try to execute a simple query to check database connectivity
            z3Repository.count();
            status.put("connected", true);
            status.put("message", "Database connection is active");
        } catch (Exception e) {
            status.put("connected", false);
            status.put("message", "Database connection failed: " + e.getMessage());
            System.err.println("Database connection check failed: " + e.getMessage());
        }
        return status;
    }

    /**
     * Optimized Downtime Calculation using Stored Procedure
     * Location: /api/downtime-sp
     * This endpoint uses sp_CalcDowntime for 4-10x faster performance
     */
    @GetMapping("/api/downtime-sp")
    @ResponseBody
    public Map<String, Object> getDowntimeFromSP(
            @RequestParam(required = false) String station,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "Z3") String zone,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "0") int fetchAll) {

        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // Call stored procedure
            List<Object[]> results = z3Repository.callDowntimeSP(
                    station,
                    shift,
                    fromDate,
                    toDate,
                    zone,
                    page,
                    size,
                    fetchAll);

            // Convert results to list of maps for easier JSON serialization
            List<Map<String, Object>> downtimeList = results.stream().map(row -> {
                Map<String, Object> item = new HashMap<>();
                item.put("station", row[0]);
                item.put("tableName", row[1]);
                item.put("srNo", row[2]);
                item.put("shift", row[3]);
                item.put("line", row[4]);
                item.put("zone", row[5]);
                item.put("side", row[6]);
                item.put("category", row[7]);
                item.put("startTime", row[8]);
                item.put("endTime", row[9]);
                item.put("remark", row[10]);
                item.put("individualMs", row[11]);
                item.put("individualSec", row[12]);
                item.put("individualFormatted", row[13]);
                item.put("prevEnd", row[14]);
                item.put("newGroup", row[15]);
                item.put("clusterID", row[16]);
                item.put("clusterStart", row[17]);
                item.put("clusterEnd", row[18]);
                item.put("finalMs", row[19]);
                item.put("finalSec", row[20]);
                item.put("finalFormatted", row[21]);
                return item;
            }).collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;

            response.put("success", true);
            response.put("data", downtimeList);
            response.put("count", downtimeList.size());
            response.put("page", page);
            response.put("size", size);
            response.put("zone", zone);
            response.put("executionTimeMs", executionTime);
            response.put("message", "Data fetched from stored procedure in " + executionTime + "ms");

            System.out.println("SP Execution Time: " + executionTime + "ms for " + downtimeList.size() + " records");

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Failed to fetch downtime data");
            System.err.println("Stored procedure error: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Real-time Analytics API for Chart Updates
     * Location: /api/analytics
     * Provides fresh data for charts without page reload
     */
    @GetMapping("/api/analytics")
    @ResponseBody
    public Map<String, Object> getAnalytics(
            @RequestParam(required = false, defaultValue = "Z3 Pullchord T2") String selectedTable,
            @RequestParam(required = false, defaultValue = "optimized") String mode) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Fetch analytics from the Snapshot (populated by the last Dashboard/SP run)
            List<Object[]> trendData = z3Repository.findTrendMetricsFromSnapshot();
            List<Object[]> stationData = z3Repository.findTopStationsFromSnapshot();
            List<Object[]> shiftData = z3Repository.findShiftCountsFromSnapshot();
            List<Object[]> lineData = z3Repository.findLineCountsFromSnapshot();

            // Total records in the snapshot (Approximate from trend sum)
            long totalRecords = 0;
            for (Object[] row : trendData) {
                totalRecords += ((Number) row[1]).longValue();
            }

            // Process shift data
            Map<String, Object> shifts = new HashMap<>();
            List<String> shiftLabels = new ArrayList<>();
            List<Long> shiftValues = new ArrayList<>();
            for (Object[] row : shiftData) {
                shiftLabels.add(row[0] != null ? row[0].toString() : "Unknown");
                shiftValues.add(row[1] != null ? ((Number) row[1]).longValue() : 0L);
            }
            shifts.put("labels", shiftLabels);
            shifts.put("values", shiftValues);

            // Process line data
            Map<String, Object> lines = new HashMap<>();
            List<String> lineLabels = new ArrayList<>();
            List<Long> lineValues = new ArrayList<>();
            for (Object[] row : lineData) {
                lineLabels.add(row[0] != null ? row[0].toString() : "Unknown");
                lineValues.add(row[1] != null ? ((Number) row[1]).longValue() : 0L);
            }
            lines.put("labels", lineLabels);
            lines.put("values", lineValues);

            // Process station data
            Map<String, Object> stations = new HashMap<>();
            List<String> stationLabels = new ArrayList<>();
            List<Long> stationValues = new ArrayList<>();
            for (Object[] row : stationData) {
                stationLabels.add(row[0] != null ? row[0].toString() : "Unknown");
                stationValues.add(row[1] != null ? ((Number) row[1]).longValue() : 0L);
            }
            stations.put("labels", stationLabels);
            stations.put("values", stationValues);

            // Process Trend Data
            Map<String, Object> trends = new HashMap<>();
            List<String> labels = new ArrayList<>();
            List<Double> breakdownTrends = new ArrayList<>();
            List<Double> mtbfTrends = new ArrayList<>();
            List<Double> mttrTrends = new ArrayList<>();
            List<Double> uptimeTrends = new ArrayList<>();
            List<Double> lossHoursTrends = new ArrayList<>();
            List<Long> lineBreakdownTrends = new ArrayList<>();

            // Pie Chart Data
            long totalMaint = 0;
            long totalProd = 0;
            long totalMat = 0;
            long totalQual = 0;

            // Trend Data Structure from findTrendMetricsFromSnapshot:
            // [0]=Date, [1]=Total, [2]=Maint, [3]=Prod, [4]=Mat, [5]=Qual,
            // [6]=TotalDowntimeMs

            for (Object[] row : trendData) {
                String date = row[0] != null ? row[0].toString() : "Unknown";
                labels.add(date);

                long total = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                long maint = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                long prod = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                long mat = row[4] != null ? ((Number) row[4]).longValue() : 0L;
                long qual = row[5] != null ? ((Number) row[5]).longValue() : 0L;

                double totalDowntimeMs = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;

                // Aggregate for Pie Chart
                totalMaint += maint;
                totalProd += prod;
                totalMat += mat;
                totalQual += qual;

                // KPI Calculations (Daily, using Accurate Downtime)
                double availableMins = 1440.0; // 24 Hours
                double effectiveDowntimeMins = Math.min(totalDowntimeMs / 60000.0, availableMins);
                double lossHours = effectiveDowntimeMins / 60.0;

                lossHoursTrends.add(Math.round(lossHours * 100.0) / 100.0);

                // Chart 1: Breakdown %
                double breakdownPct = (effectiveDowntimeMins / availableMins) * 100.0;
                breakdownTrends.add(Math.round(breakdownPct * 100.0) / 100.0);

                // Chart 7: Uptime %
                uptimeTrends.add(Math.round((100.0 - breakdownPct) * 100.0) / 100.0);

                // Chart 2: MTBF (Hours)
                double operatingTimeHrs = (availableMins - effectiveDowntimeMins) / 60.0;
                double mtbf = total > 0 ? operatingTimeHrs / total : operatingTimeHrs;
                mtbfTrends.add(Math.round(mtbf * 100.0) / 100.0);

                // Chart 3: MTTR (Minutes)
                double mttr = total > 0 ? (effectiveDowntimeMins / total) : 0;
                mttrTrends.add(Math.round(mttr * 100.0) / 100.0);

                // Chart 5: Line Breakdowns (Count)
                lineBreakdownTrends.add(total);
            }

            // Reverse Arrays (Oldest -> Newest) because Query orders DESC
            java.util.Collections.reverse(labels);
            java.util.Collections.reverse(breakdownTrends);
            java.util.Collections.reverse(mtbfTrends);
            java.util.Collections.reverse(mttrTrends);
            java.util.Collections.reverse(uptimeTrends);
            java.util.Collections.reverse(lossHoursTrends);
            java.util.Collections.reverse(lineBreakdownTrends);

            trends.put("labels", labels);
            trends.put("breakdown", breakdownTrends);
            trends.put("mtbf", mtbfTrends);
            trends.put("mttr", mttrTrends);
            trends.put("uptime", uptimeTrends);
            trends.put("values", lossHoursTrends);
            trends.put("lineBreakdown", lineBreakdownTrends);

            trends.put("pieMaint", totalMaint);
            trends.put("pieProd", totalProd);
            trends.put("pieMat", totalMat);
            trends.put("pieQual", totalQual);

            // KPI data
            Map<String, Object> kpis = new HashMap<>();
            kpis.put("totalRecords", totalRecords);

            // Build response
            response.put("success", true);
            response.put("shifts", shifts);
            response.put("lines", lines);
            response.put("stations", stations);
            response.put("trends", trends);
            response.put("kpis", kpis);
            response.put("table", "SP_Snapshot"); // Meta info
            response.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            System.err.println("Analytics API error: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}
