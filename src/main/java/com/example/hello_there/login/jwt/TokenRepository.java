package com.example.hello_there.login.jwt;

import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query("select u from User u JOIN Token t ON u.id = t.user.id WHERE t.accessToken= :accessToken")
    Optional<User> findUserByAccessToken(@Param("accessToken") String accessToken);
}
