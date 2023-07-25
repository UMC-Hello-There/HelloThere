package com.example.hello_there.user_fee;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user_fee.dto.PatchUserFeeReq;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.example.hello_there.exception.BaseResponseStatus.NONE_EXIST_COMMENT;
import static com.example.hello_there.exception.BaseResponseStatus.NONE_EXIST_USERFEE;

@Service
@AllArgsConstructor
public class UserFeeService {
    private final UserFeeRepository userFeeRepository;
    private final UserRepository userRepository;

    /*
     * [UserFee] 입력받은 정보의 관리비 반환
     * 해당 년월의 관리비가 존재하지 않을 경우 데이터를 생성하여 반환해줌
     */
    public UserFee getUserFeeCurrent(Long userId, int feeYear, int feeMonth){
        User user = userRepository.findById(userId).get();
        Long houseId = user.getHouse().getHouseId();
        Optional<UserFee> userFee = userFeeRepository.findByUserIdAndHouseIdAndFeeYearAndFeeMonth(userId, houseId, feeYear, feeMonth);
        return userFee.orElseGet(() -> createDefaultUserFee(userId, houseId, feeYear, feeMonth));
    }

    public UserFee createDefaultUserFee(Long userId, Long houseId, int feeYear, int feeMonth){
        UserFee userFee = UserFee.builder()
                .userId(userId)
                .houseId(houseId)
                .feeYear(feeYear)
                .feeMonth(feeMonth)
                .build();
        return userFeeRepository.saveAndFlush(userFee);
    }

    /*
     * [List<UserFee>] 입력받은 정보의 최근 3개월 관리비 반환
     */
    public List<UserFee> getUserFeeCustom(Long userId, int feeYear, int feeMonth) {
        User user = userRepository.findById(userId).get();
        Long houseId = user.getHouse().getHouseId();
        Pageable pageable = PageRequest.of(0, 3);
        getUserFeeCurrent(userId, feeYear, feeMonth);    //해당 년월의 관리비가 존재하지 않을 경우 데이터를 생성
        return userFeeRepository.findByUserIdAndHouseIdAndFeeYearAndFeeMonthLessThanEqualOrderByFeeYearDescFeeMonthDesc(userId, houseId, feeYear, feeMonth, pageable);
    }

    public UserFee updateUserFee(Long id, PatchUserFeeReq patchUserFeeReq) {
        UserFee userFee = userFeeRepository.findById(id).orElseThrow(()-> new BaseException(NONE_EXIST_USERFEE));
        return userFeeRepository.save(patchUserFeeReq.updateEntity(userFee));
    }
}
