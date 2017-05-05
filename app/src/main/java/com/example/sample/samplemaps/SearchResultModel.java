package com.example.sample.samplemaps;

import java.io.Serializable;

public class SearchResultModel implements Serializable {

    private String stationNameFrom = null;
    private String stationNameTo = null;
    private String fastestTime = null;
    private String transfer = null;
    private String cost = null;

    public String getStationNameFrom() {

        return this.stationNameFrom;
    }

    public void setStationNameFrom(String stationNameFrom) {

        this.stationNameFrom = stationNameFrom;
    }

    public String getStationNameTo() {

        return this.stationNameTo;
    }

    public void setStationNameTo(String stationNameTo) {

        this.stationNameTo = stationNameTo;
    }

    public String getFastestTime() {

        return this.fastestTime;
    }

    public void setFastestTime(String fastestTime) {

        this.fastestTime = fastestTime;
    }

    public String getTransfer() {

        return this.transfer;
    }

    public void setTransfer(String transfer) {

        this.transfer = transfer;
    }

    public String getCost() {

        return this.cost;
    }

    public void setCost(String cost) {

        this.cost = cost;
    }
}
