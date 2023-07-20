package com.example.hello_there.login.dto;

import com.example.hello_there.user.User;
import com.example.hello_there.user.dto.GetUserRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AssertionDTO {
    private JwtResponseDTO.TokenInfo tokenInfo;
    private String msg;
}
