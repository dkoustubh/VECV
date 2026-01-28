package com.example.PullChord_Report.entity;

import java.time.LocalDateTime;

/**
 * Entity class for Downtime calculation results from sp_CalcDowntime stored
 * procedure
 * Location:
 * src/main/java/com/example/PullChord_Report/entity/DowntimeResult.java
 */
public class DowntimeResult {

    private String station;
    private String tableName;
    private Long srNo;
    private String shift;
    private String line;
    private String zone;
    private String side;
    private String category;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;

    private Long individualMs;
    private Integer individualSec;
    private String individualFormatted;

    private LocalDateTime prevEnd;
    private Integer newGroup;
    private Integer clusterID;

    private LocalDateTime clusterStart;
    private LocalDateTime clusterEnd;

    private Long finalMs;
    private Integer finalSec;
    private String finalFormatted;

    // Constructors
    public DowntimeResult() {
    }

    // Getters and Setters
    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getSrNo() {
        return srNo;
    }

    public void setSrNo(Long srNo) {
        this.srNo = srNo;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getIndividualMs() {
        return individualMs;
    }

    public void setIndividualMs(Long individualMs) {
        this.individualMs = individualMs;
    }

    public Integer getIndividualSec() {
        return individualSec;
    }

    public void setIndividualSec(Integer individualSec) {
        this.individualSec = individualSec;
    }

    public String getIndividualFormatted() {
        return individualFormatted;
    }

    public void setIndividualFormatted(String individualFormatted) {
        this.individualFormatted = individualFormatted;
    }

    public LocalDateTime getPrevEnd() {
        return prevEnd;
    }

    public void setPrevEnd(LocalDateTime prevEnd) {
        this.prevEnd = prevEnd;
    }

    public Integer getNewGroup() {
        return newGroup;
    }

    public void setNewGroup(Integer newGroup) {
        this.newGroup = newGroup;
    }

    public Integer getClusterID() {
        return clusterID;
    }

    public void setClusterID(Integer clusterID) {
        this.clusterID = clusterID;
    }

    public LocalDateTime getClusterStart() {
        return clusterStart;
    }

    public void setClusterStart(LocalDateTime clusterStart) {
        this.clusterStart = clusterStart;
    }

    public LocalDateTime getClusterEnd() {
        return clusterEnd;
    }

    public void setClusterEnd(LocalDateTime clusterEnd) {
        this.clusterEnd = clusterEnd;
    }

    public Long getFinalMs() {
        return finalMs;
    }

    public void setFinalMs(Long finalMs) {
        this.finalMs = finalMs;
    }

    public Integer getFinalSec() {
        return finalSec;
    }

    public void setFinalSec(Integer finalSec) {
        this.finalSec = finalSec;
    }

    public String getFinalFormatted() {
        return finalFormatted;
    }

    public void setFinalFormatted(String finalFormatted) {
        this.finalFormatted = finalFormatted;
    }
}
