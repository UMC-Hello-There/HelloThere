package com.example.hello_there.management.vo;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;

@Getter
@RequiredArgsConstructor
public class GeoPoint {
    @NonNull
    private Double lng;
    @NonNull
    private Double lat;

    public static GeoPoint fromPoint(Point point){
        return new GeoPoint(point.getX(), point.getY());
    }
}