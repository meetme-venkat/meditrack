package com.venkat.meditrack.repository;

import com.venkat.meditrack.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
}
