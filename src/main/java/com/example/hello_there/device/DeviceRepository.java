package com.example.hello_there.device;

import com.example.hello_there.house.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    @Query("select d from Device d where d.user.id = :userId")
    Optional<Device> findDeviceByUserId(@Param("userId") Long userId);
}
