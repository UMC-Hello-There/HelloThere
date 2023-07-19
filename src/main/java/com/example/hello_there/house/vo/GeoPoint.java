package com.example.hello_there.house.vo;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GeoPoint {
    @NonNull
    private Double lng; //경도
    @NonNull
    private Double lat; //위도

    public static Point toPoint(GeoPoint geoPoint){
        return new Point(new Coordinate(geoPoint.lng,geoPoint.lat), new PrecisionModel(), 4326);
    }
    public static GeoPoint fromPoint(Point point){
        return new GeoPoint(point.getX(), point.getY());
    }

    /*
     * [GeoPoint] String 좌표를 입력 받아 GeoPoint 반환
     */
    public static GeoPoint fromString(String lng, String lat){
        return new GeoPoint(Double.parseDouble(lng), Double.parseDouble(lat));
    }

}