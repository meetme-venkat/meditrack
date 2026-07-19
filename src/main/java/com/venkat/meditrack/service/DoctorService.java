package com.venkat.meditrack.service;

import com.venkat.meditrack.dto.DoctorRequest;
import com.venkat.meditrack.dto.DoctorResponse;
import com.venkat.meditrack.entity.Doctor;
import com.venkat.meditrack.entity.Specialty;
import com.venkat.meditrack.exception.DuplicateResourceException;
import com.venkat.meditrack.exception.ResourceNotFoundException;
import com.venkat.meditrack.repository.DoctorRepository;
import com.venkat.meditrack.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepo;
    private final SpecialtyRepository specialtyRepo;

    public List<DoctorResponse> findAll() {
        return doctorRepo.findAll().stream().map(DoctorResponse::from).toList();
    }

    public DoctorResponse findById(Long id) {
        return DoctorResponse.from(getDoctorOrThrow(id));
    }

    public DoctorResponse create(DoctorRequest request) {
        if (doctorRepo.existsByLicenseNo(request.getLicenseNo())) {
            throw new DuplicateResourceException("Doctor licence already exists: " + request.getLicenseNo());
        }
        Specialty specialty = specialtyRepo.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Specialty not found: " + request.getSpecialtyId()));
        LocalDateTime now = LocalDateTime.now();
        Doctor doctor = Doctor.builder()
                .name(request.getName())
                .licenseNo(request.getLicenseNo())
                .consultationFee(request.getConsultationFee())
                .dailySlotCapacity(request.getDailySlotCapacity())
                .active(request.isActive())
                .specialty(specialty)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return DoctorResponse.from(doctorRepo.save(doctor));
    }

    private Doctor getDoctorOrThrow(Long id) {
        return doctorRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }
}
