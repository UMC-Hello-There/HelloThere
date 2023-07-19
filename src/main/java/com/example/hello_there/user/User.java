package com.example.hello_there.user;

import com.example.hello_there.apratment.Apartment;
import com.example.hello_there.board.Board;
import com.example.hello_there.user_chatroom.UserChatRoom;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.user.profile.Profile;
import com.example.hello_there.utils.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseTimeEntity {
    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 멤버의 식별자

    @Column(nullable = false)
    private String email; // 이메일로 로그인

    @Column(nullable = true) // 소셜로그인의 경우 null. 일반로그인일 경우 null이면 예외 호출
    private String password;

    @Column(nullable = false)
    private String nickName; // 유저의 닉네임

    @Column(nullable = false)
    private boolean gender; // 유저의 성별

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isManager; // 관리자 여부 체크

    @Column(nullable = true)
    private LocalDate birth; // 유저의 생년월일을 yyyy-mm-dd 형식으로 표현
    private String signupPurpose; // 가입 목적

    @Column(nullable = false) // status는 멤버 회원가입 시에 자동으로 ACTIVE로 설정됨.
    @Enumerated(EnumType.STRING)
    private UserStatus status; // 유저의 활성화, 비활성화, 징계 등을 체크

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Token token; // 토큰과 일대일 매핑

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile; // 프로필 사진과 일대일 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment; // 아파트와 일대다 매핑

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChatRoom> userChatRooms = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> comments = new ArrayList<>();
    public User createUser(String email, String password, String nickName, String signupPurpose, Apartment apartment) {
        this.email = email;
        this.password = password;
        this.nickName= nickName;
        this.signupPurpose = signupPurpose;
        this.status = UserStatus.ACTIVE;
        this.apartment = apartment;
        return this;
    }
}
