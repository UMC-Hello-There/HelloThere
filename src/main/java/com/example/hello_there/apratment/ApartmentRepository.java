package com.example.hello_there.apratment;

import com.example.hello_there.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    @Query("select a from Apartment a where a.city = :city and a.district =:district and a.apartmentName =:apartmentName")
    Optional<Apartment> findApartment(@Param("city") String city, @Param("district") String district, @Param("apartmentName") String apartmentName);
}