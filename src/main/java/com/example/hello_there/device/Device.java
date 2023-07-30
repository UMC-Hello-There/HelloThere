package com.example.hello_there.device;

import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceId;

    @Column(nullable = false)
    private String token; // 파이어베이스에서 발급하는 디바이스 토큰

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void setToken(String token) {
        this.token = token;
    }
}
