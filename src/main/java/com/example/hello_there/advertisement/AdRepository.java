package com.example.hello_there.advertisement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    @Query(value = "SELECT * FROM ad WHERE district= :district ORDER BY rand() limit 1",nativeQuery = true)
    Ad findAdByRandom(@Param("district") String district);
}
