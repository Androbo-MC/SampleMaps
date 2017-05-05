package com.example.sample.samplemaps;

import java.io.Serializable;
import java.util.ArrayList;

public class DisplayModel implements Serializable {

    private ArrayList<SearchResultModel> detailList = null;
    private String title = null;
    private double aveTime = 0;
    private double aveTrans = 0;
    private double aveCost = 0;

    public ArrayList<SearchResultModel> getDetailList() {

        return this.detailList;
    }

    public void setDetailList(ArrayList<SearchResultModel> detailList) {

        this.detailList = detailList;
    }

    public String getTitle() {

        return this.title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public double getAveTime() {

        return this.aveTime;
    }

    public void setAveTime(double aveTime) {

        this.aveTime = aveTime;
    }

    public double getAveTrans() {

        return this.aveTrans;
    }

    public void setAveTrans(double aveTrans) {

        this.aveTrans = aveTrans;
    }

    public double getAveCost() {

        return this.aveCost;
    }

    public void setAveCost(double aveCost) {

        this.aveCost = aveCost;
    }
}
