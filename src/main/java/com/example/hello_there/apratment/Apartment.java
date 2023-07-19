package com.example.hello_there.apratment;

import com.example.hello_there.board.Board;
import com.example.hello_there.user.User;
import lombok.*;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Apartment {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long apartmentId; // 아파트의 식별자

    @Column(nullable = false)
    private String city; // ex) 인천시

    @Column(nullable = false)
    private String district; // ex) 부평구

    @Column(nullable = false)
    private String streetAddress; // 도로명 주소 ex) 부평대로165번길 40

    @Column(nullable = false)
    private String numberAddress; // 지번 주소 ex) 부평1동 70-5

    @Column(nullable = false)
    private String apartmentName; // 아파트명

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();
}
