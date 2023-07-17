package com.example.hello_there.management;

import com.example.hello_there.utils.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;

@Entity(name = "managements")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Management extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Geometry 타입으로 저장
    @Column(nullable = false, columnDefinition = "GEOMETRY")
    @JsonIgnore
    private Point location;

    @Column(nullable = false)
    private String name;  //아파트명

    @Column(nullable = false)
    private String city;  //시

    @Column(nullable = false)
    private String district; //구

    @Column(name = "number_address", nullable = false)
    private String numberAddress;   //일반 주소

    @Column(name = "street_address", nullable = false)
    private String streetAddress;   //도로명 주소
}