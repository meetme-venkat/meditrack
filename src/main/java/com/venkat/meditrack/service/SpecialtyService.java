package com.venkat.meditrack.service;

import com.venkat.meditrack.dto.SpecialtyRequest;
import com.venkat.meditrack.dto.SpecialtyResponse;
import com.venkat.meditrack.entity.Specialty;
import com.venkat.meditrack.exception.DuplicateResourceException;
import com.venkat.meditrack.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepo;

    public List<SpecialtyResponse> findAll() {
        return specialtyRepo.findAll().stream().map(SpecialtyResponse::from).toList();
    }

    public SpecialtyResponse create(SpecialtyRequest request) {
        if (specialtyRepo.existsByName(request.getName())) {
            throw new DuplicateResourceException("Specialty name already exists: " + request.getName());
        }
        if (specialtyRepo.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Specialty slug already exists: " + request.getSlug());
        }
        LocalDateTime now = LocalDateTime.now();
        Specialty specialty = Specialty.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return SpecialtyResponse.from(specialtyRepo.save(specialty));
    }
}
