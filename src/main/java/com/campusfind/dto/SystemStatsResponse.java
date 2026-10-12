package com.campusfind.dto;

public class SystemStatsResponse {
    private long totalReports;
    private long lostReports;
    private long foundReports;
    private long resolvedReports;
    private long totalUsers;

    public SystemStatsResponse() {}

    public SystemStatsResponse(long totalReports, long lostReports, long foundReports, long resolvedReports, long totalUsers) {
        this.totalReports = totalReports;
        this.lostReports = lostReports;
        this.foundReports = foundReports;
        this.resolvedReports = resolvedReports;
        this.totalUsers = totalUsers;
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public long getLostReports() {
        return lostReports;
    }

    public void setLostReports(long lostReports) {
        this.lostReports = lostReports;
    }

    public long getFoundReports() {
        return foundReports;
    }

    public void setFoundReports(long foundReports) {
        this.foundReports = foundReports;
    }

    public long getResolvedReports() {
        return resolvedReports;
    }

    public void setResolvedReports(long resolvedReports) {
        this.resolvedReports = resolvedReports;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
}
