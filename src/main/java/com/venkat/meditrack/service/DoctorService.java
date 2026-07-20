package com.venkat.meditrack.service;

import com.venkat.meditrack.dto.DoctorRatingResponse;
import com.venkat.meditrack.dto.DoctorRequest;
import com.venkat.meditrack.dto.DoctorResponse;
import com.venkat.meditrack.entity.Doctor;
import com.venkat.meditrack.entity.Specialty;
import com.venkat.meditrack.exception.DuplicateResourceException;
import com.venkat.meditrack.exception.ResourceNotFoundException;
import com.venkat.meditrack.repository.AppointmentFeedbackRepository;
import com.venkat.meditrack.repository.DoctorRatingSummary;
import com.venkat.meditrack.repository.DoctorRepository;
import com.venkat.meditrack.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepo;
    private final SpecialtyRepository specialtyRepo;
    private final AppointmentFeedbackRepository feedbackRepo;

    public List<DoctorResponse> findAll() {
        // One grouped query for every doctor's rating, then map in memory — avoids
        // an aggregate query per doctor (N+1).
        Map<Long, DoctorRatingSummary> ratings = feedbackRepo.findRatingSummaries().stream()
                .collect(java.util.stream.Collectors.toMap(
                        DoctorRatingSummary::getDoctorId, Function.identity()));
        return doctorRepo.findAll().stream()
                .map(d -> DoctorResponse.from(d, ratings.get(d.getId())))
                .toList();
    }

    public DoctorResponse findById(Long id) {
        Doctor doctor = getDoctorOrThrow(id);
        return DoctorResponse.from(doctor, feedbackRepo.findRatingSummary(id).orElse(null));
    }

    public DoctorRatingResponse findRating(Long id) {
        getDoctorOrThrow(id);
        return DoctorRatingResponse.from(id, feedbackRepo.findRatingSummary(id).orElse(null));
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
