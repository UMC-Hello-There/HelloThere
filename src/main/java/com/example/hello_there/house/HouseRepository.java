package com.example.hello_there.house;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HouseRepository extends JpaRepository<House, Long> {
    @Query("select a from houses a where a.city = :city and a.district =:district and a.houseName =:houseName")
    Optional<House> findHouse(@Param("city") String city, @Param("district") String district, @Param("houseName") String houseName);

    @Query("select a from houses a where a.houseId = :houseId")
    Optional<House> findHouseById(@Param("houseId") Long houseId);

    @Query("select a from houses a where a.city = :city and a.district =:district and a.houseName =:name")
    Optional<House> findProperty(@Param("city") String city, @Param("district") String district, @Param("name") String name);

    @Query(
            value =
                    "SELECT "
                            +"d.*,"
                            +" ( 6371000 * acos( cos( radians(:lat) ) * cos( radians( ST_X(d.location) ) ) * cos( radians( ST_Y(d.location) ) - radians(:lng) ) + sin( radians(:lat) ) * sin(radians(ST_X(d.location))) ) ) AS distance"
                            +" FROM"
                            +" houses d"
                            +" HAVING distance < :radius"
            ,nativeQuery = true
    )
    List<House> findByRadius(@Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius);
}