package com.example.hello_there.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u where u.id = :id")
    Optional<User> findUserById(@Param("id") Long id);

    Optional<User> findByEmail(String email); // // JPA 제공 메서드
    Optional<User> findByNickName(String nickname); // JPA 제공 메서드

    @Query("select count(u) from User u where u.email = :email")
    Integer findByEmailCount(@Param("email") String email);

    @Query("select u from User u where u.email = :email")
    User findUserByEmail(@Param("email") String email);

    @Query("select u from User u where u.house.houseId =:houseId")
    List<User> findUsersByHouseId(@Param("houseId") Long houseId);

    @Query("select u from User u where u.nickName = :nickName")
    List<User> findUserByNickName(@Param("nickName") String nickName);

    @Modifying
    @Query("delete from User u where u.id = :userId")
    void deleteUser(@Param("userId") Long userId);

    boolean existsByNickName(String nickName);
}