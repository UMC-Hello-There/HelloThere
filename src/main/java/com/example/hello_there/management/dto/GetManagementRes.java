package com.example.hello_there.management.dto;

import com.example.hello_there.management.Management;
import com.example.hello_there.management.vo.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetManagementRes {
    private Long id;
    private String name;
    private String city;
    private String district;
    private String streetAddress;
    private GeoPoint location;

    public static GetManagementRes mapEntityToResponse(Management management){
        return new GetManagementRes(
                management.getId(),
                management.getName(),
                management.getCity(),
                management.getDistrict(),
                management.getStreetAddress(),
                GeoPoint.fromPoint(management.getLocation())
        );
    }
}
