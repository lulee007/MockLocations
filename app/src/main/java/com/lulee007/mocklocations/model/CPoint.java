package com.lulee007.mocklocations.model;

/**
 * 用于构造地图中的坐标点
 * @author lw
 * http://my.oschina.net/Thinkeryjgfn/blog/402565?fromerr=oUdHahrh
 * **/
public class CPoint {

    private double lat;// 纬度
    private double lng;// 经度

    public CPoint() {
    }

    public CPoint(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CPoint) {
            CPoint bmapCPoint = (CPoint) obj;
            return (bmapCPoint.getLng() == lng && bmapCPoint.getLat() == lat) ? true : false;
        } else {
            return false;
        }
    }

    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "Point [lat=" + lat + ", lng=" + lng + "]";
    }

}
