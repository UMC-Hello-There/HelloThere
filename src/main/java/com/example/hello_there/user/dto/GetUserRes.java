package com.example.hello_there.user.dto;

import com.example.hello_there.apratment.Apartment;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetUserRes {
    private Long userId;
    private String email;
    private String nickName;
    private String signupPurpose;
    private String address;
    private UserStatus status;

    public GetUserRes(User user){
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickName = user.getNickName();
        this.signupPurpose = user.getSignupPurpose();
        if (user.getApartment() != null) {
            this.address = user.getApartment().getCity() + " " +
                    user.getApartment().getDistrict() + " " +
                    user.getApartment().getApartmentName();
        } else {
            this.address = "";
        }
        this.status = user.getStatus();
    }
}