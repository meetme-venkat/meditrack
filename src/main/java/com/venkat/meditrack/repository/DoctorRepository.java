package com.venkat.meditrack.repository;

import com.venkat.meditrack.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    boolean existsByLicenseNo(String licenseNo);
}
