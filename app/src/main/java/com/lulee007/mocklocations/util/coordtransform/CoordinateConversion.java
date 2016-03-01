package com.lulee007.mocklocations.util.coordtransform;

import com.lulee007.mocklocations.model.CPoint;

/**
 * 地图坐标转换 google,baidu,gps
 *
 * @author lw
 * @Time 2015年4月16日18:19:16
 * http://my.oschina.net/Thinkeryjgfn/blog/402565?fromerr=oUdHahrh
 */
public class CoordinateConversion {
    private static final double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

    private static final double pi = 3.14159265358979324;
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;

    /**
     * gg_lat 纬度
     * gg_lon 经度
     * GCJ-02转换BD-09
     * Google地图经纬度转百度地图经纬度
     */
    public static CPoint google_bd_encrypt(double gg_lat, double gg_lon) {
        CPoint CPoint = new CPoint();
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        CPoint.setLat(bd_lat);
        CPoint.setLng(bd_lon);
        return CPoint;
    }

    /**
     * wgLat 纬度
     * wgLon 经度
     * BD-09转换GCJ-02
     * 百度转google
     */
    public static CPoint bd_google_encrypt(double bd_lat, double bd_lon) {
        CPoint CPoint = new CPoint();
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        CPoint.setLat(gg_lat);
        CPoint.setLng(gg_lon);
        return CPoint;
    }


    /**
     * wgLat 纬度
     * wgLon 经度
     * WGS-84 到 GCJ-02 的转换（即 GPS 加偏）
     */
    public static CPoint wgs_gcj_encrypts(double wgLat, double wgLon) {
        CPoint CPoint = new CPoint();
        if (outOfChina(wgLat, wgLon)) {
            CPoint.setLat(wgLat);
            CPoint.setLng(wgLon);
            return CPoint;
        }
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double lat = wgLat + dLat;
        double lon = wgLon + dLon;
        CPoint.setLat(lat);
        CPoint.setLng(lon);
        return CPoint;
    }

    /**
     * GCJ02 转换为 WGS84
     *
     * @param wgLon
     * @param wgLat
     * @returns {*[]}
     */
    public static CPoint gcj02_wgs84(double wgLat, double wgLon) {
        if (outOfChina(wgLat, wgLon)) {
            return new CPoint(wgLon, wgLat);
        }
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mglat = wgLat + dLat;
        double mglng = wgLon + dLon;
        return new CPoint(wgLon * 2 - mglng, wgLat * 2 - mglat);
    }

    public static CPoint bg2gps(double bd_lng, double bd_lat) {
        CPoint gcj02 = bd_google_encrypt(bd_lat, bd_lng);
        return gcj02_wgs84(gcj02.getLat(), gcj02.getLng());
    }

    private static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }
}