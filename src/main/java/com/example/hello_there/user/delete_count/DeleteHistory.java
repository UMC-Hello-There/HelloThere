package com.example.hello_there.user.delete_count;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteHistory {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deleteHistoryId;

    @Column(nullable = false)
    private String email; // 재가입 방지를 위한 것이므로, userId가 아닌 이메일을 사용
}
