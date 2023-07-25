package com.example.hello_there.user.delete_history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeleteHistoryRepository extends JpaRepository<DeleteHistory, Long> {
    boolean existsByEmail(String email);

    @Query("select d from DeleteHistory d where d.email = :email")
    DeleteHistory findDeleteHistoryByEmail(@Param("email") String email);
}
