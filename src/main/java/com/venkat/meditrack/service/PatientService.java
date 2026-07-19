package com.venkat.meditrack.service;

import com.venkat.meditrack.dto.PatientRequest;
import com.venkat.meditrack.dto.PatientResponse;
import com.venkat.meditrack.entity.Patient;
import com.venkat.meditrack.exception.DuplicateResourceException;
import com.venkat.meditrack.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepo;

    public List<PatientResponse> findAll() {
        return patientRepo.findAll().stream().map(PatientResponse::from).toList();
    }

    public PatientResponse register(PatientRequest request) {
        if (patientRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Patient email already exists: " + request.getEmail());
        }
        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .createdAt(LocalDateTime.now())
                .build();
        return PatientResponse.from(patientRepo.save(patient));
    }
}
