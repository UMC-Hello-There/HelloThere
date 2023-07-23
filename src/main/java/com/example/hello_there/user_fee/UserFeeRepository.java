package com.example.hello_there.user_fee;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserFeeRepository extends JpaRepository<UserFee, Long> {

    UserFee findByHouseIdAndFeeYearAndFeeMonth(@Param("houseId") Long houseId, @Param("feeYear") int feeYear, @Param("feeMonth") int feeMonth);

}
