package com.example.hello_there.house;

import com.example.hello_there.board.Board;
import com.example.hello_there.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name="houses")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class House {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long houseId; // 거주지 식별자

    // Geometry 타입으로 저장
    @Column(nullable = true, columnDefinition = "GEOMETRY")
    @JsonIgnore
    private Point location;

    @Column(nullable = false)
    private String city; // ex) 인천시

    @Column(nullable = false)
    private String district; // ex) 부평구

    @Column(nullable = false)
    private String streetAddress; // 도로명 주소 ex) 부평대로165번길 40

    @Column(nullable = false)
    private String numberAddress; // 지번 주소 ex) 부평1동 70-5

    @Column(nullable = false)
    private String houseName; // 아파트명

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();
}