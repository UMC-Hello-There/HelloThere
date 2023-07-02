package com.example.hello_there.user;

import com.example.hello_there.config.UserStatus;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.user.profile.Profile;
import com.example.hello_there.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Builder
@Getter
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

    @Column(nullable = false) // status는 멤버 회원가입 시에 자동으로 ACTIVE로 설정됨.
    private UserStatus status; // 유저의 활성화, 비활성화, 징계 등을 체크

//    @Column(columnDefinition = "boolean default false")
//    private boolean isSocialLogin;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Token token; // 토큰과 일대일 매핑

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile; // 프로필 사진과 일대일 매핑

//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Board> boards = new ArrayList<>();
//
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Board> comments = new ArrayList<>();
    public User createUser(String email, String password, String nickName, boolean gender, LocalDate birth){
        this.email = email;
        this.password = password;
        this.nickName= nickName;
        this.gender = gender;
        if(birth != null) {
            this.birth = birth;
        }
        this.status = UserStatus.ACTIVE;
        return this;
    }

    public void updateNickName(String nickName){
        this.nickName = nickName;
    }
    public void updateEmail(String email){
        this.email = email;
    }

//    public void updateIsSocialLogin(){
//        this.isSocialLogin = true;
//    }
}
