package com.example.hello_there.user_fee;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserFeeRepository extends JpaRepository<UserFee, Long> {

    Optional<UserFee> findByUserIdAndHouseIdAndFeeYearAndFeeMonth(@Param("userId") Long userId, @Param("houseId") Long houseId, @Param("feeYear") int feeYear, @Param("feeMonth") int feeMonth);

    List<UserFee> findByUserIdAndHouseIdAndFeeYearAndFeeMonthLessThanEqualOrderByFeeYearDescFeeMonthDesc(@Param("userId") Long userId, @Param("houseId") Long houseId, @Param("feeYear") int feeYear, @Param("feeMonth") int feeMonth, @Param("pageable") Pageable pageable);

}
