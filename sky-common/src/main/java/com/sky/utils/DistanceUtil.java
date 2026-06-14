package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DistanceUtil {

    private static final String AK = "pTxdA2oJ3NZ3kN8xQNKa2bDPbcehC134";
    private static final String GEOCODING_URL = "https://api.map.baidu.com/geocoding/v3/";
    private static final double EARTH_RADIUS = 6371000;

    public static double[] getLngLat(String address) throws Exception {
        String urlString = GEOCODING_URL
                + "?address=" + URLEncoder.encode(address, "UTF-8")
                + "&output=json"
                + "&ak=" + AK;

        System.out.println("请求URL: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        int responseCode = connection.getResponseCode();
        System.out.println("响应码: " + responseCode);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        connection.disconnect();

        String result = buffer.toString();
        System.out.println("API返回: " + result);

        JSONObject jsonObject = JSON.parseObject(result);
        int status = jsonObject.getIntValue("status");

        if (status == 0) {
            JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
            double lng = location.getDoubleValue("lng");
            double lat = location.getDoubleValue("lat");
            System.out.println("解析成功 - 经度: " + lng + ", 纬度: " + lat);
            return new double[]{lng, lat};
        } else {
            String message = jsonObject.getString("message");
            System.err.println("地址解析失败 - status: " + status + ", message: " + message);
            throw new RuntimeException("地址解析失败: " + address + ", 错误信息: " + message);
        }
    }

    public static double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return Math.round(s * 10000) / 10000.0;
    }

    public static void main(String[] args) {
        try {
            double[] location = getLngLat("北京市海淀区上地十街10号");
            System.out.println("经度: " + location[0]);
            System.out.println("纬度: " + location[1]);

            double distance = calculateDistance(116.307622, 40.056828, 116.407526, 39.904030);
            System.out.println("距离: " + distance + " 米");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
