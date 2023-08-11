package com.example.hello_there.house.dto;

import com.example.hello_there.house.House;
import com.example.hello_there.house.vo.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetHouseRes {
    private Long houseId;
    private String houseName;
    private String city;
    private String district;
    private String streetAddress;
    private GeoPoint location;

    public static GetHouseRes mapEntityToResponse(House house){
        return new GetHouseRes(
                house.getHouseId(),
                house.getHouseName(),
                house.getCity(),
                house.getDistrict(),
                house.getStreetAddress(),
                GeoPoint.fromPoint(house.getLocation())
        );
    }
}
