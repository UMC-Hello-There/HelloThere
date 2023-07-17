package com.example.hello_there.management;

import com.example.hello_there.management.vo.GeoPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ManagementService {
    private final ManagementRepository managementRepository;

    public List<Management> findByRadius(GeoPoint coords, Double radius) {
        List<Management> managementList = managementRepository.findByRadius(coords.getLat(), coords.getLng(), radius);

        return managementList;
    }
}
