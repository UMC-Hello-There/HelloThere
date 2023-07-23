package com.example.hello_there.user_fee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserFeeRepository extends JpaRepository<UserFee, Long> {

    //UserFee findByHouseIdANDByFeeYearANDByFeeMonth(Long houseId, int feeYear, int feeMonth);

}
