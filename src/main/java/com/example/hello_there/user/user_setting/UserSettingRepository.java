package com.example.hello_there.user.user_setting;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    UserSetting findByUserId(Long userId);
}
