package com.example.hello_there.management;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagementRepository extends JpaRepository<Management, Long> {


    @Query(
            value =
                    "SELECT "
                            +"d.*,"
                            +" ( 6371000 * acos( cos( radians(:lat) ) * cos( radians( ST_X(d.location) ) ) * cos( radians( ST_Y(d.location) ) - radians(:lng) ) + sin( radians(:lat) ) * sin(radians(ST_X(d.location))) ) ) AS distance"
                            +" FROM"
                            +" managements d"
                            +" HAVING distance < :radius"
            ,nativeQuery = true
    )
    List<Management> findByRadius(Double lat, Double lng, Double radius);
}
