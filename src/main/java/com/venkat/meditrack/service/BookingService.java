package com.venkat.meditrack.service;

import com.venkat.meditrack.dto.AppointmentResponse;
import com.venkat.meditrack.dto.BookAppointmentRequest;
import com.venkat.meditrack.entity.Appointment;
import com.venkat.meditrack.entity.Doctor;
import com.venkat.meditrack.entity.Patient;
import com.venkat.meditrack.exception.BusinessRuleException;
import com.venkat.meditrack.exception.ResourceNotFoundException;
import com.venkat.meditrack.repository.AppointmentRepository;
import com.venkat.meditrack.repository.DoctorRepository;
import com.venkat.meditrack.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Booking/cancellation business logic. Named BookingService (not AppointmentService)
// — AppointmentService is the line-item ENTITY, not a Spring service.
@Service
@RequiredArgsConstructor
public class BookingService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;

    public List<AppointmentResponse> findAll() {
        return appointmentRepo.findAll().stream().map(AppointmentResponse::from).toList();
    }

    public AppointmentResponse findById(Long id) {
        return AppointmentResponse.from(getAppointmentOrThrow(id));
    }

    public AppointmentResponse book(BookAppointmentRequest request) {
        Patient patient = patientRepo.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found: " + request.getPatientId()));
        Doctor doctor = doctorRepo.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found: " + request.getDoctorId()));

        LocalDate scheduledDate = request.getScheduledDate();
        long booked = appointmentRepo.countByDoctorAndScheduledDate(doctor, scheduledDate);
        if (booked >= doctor.getDailySlotCapacity()) {
            throw new BusinessRuleException(
                    "No slots available for doctor " + doctor.getId() + " on " + scheduledDate);
        }

        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = Appointment.builder()
                .appointmentNo("APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .patient(patient)
                .doctor(doctor)
                .status("REQUESTED")
                .scheduledDate(scheduledDate)
                .totalAmount(doctor.getConsultationFee())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return AppointmentResponse.from(appointmentRepo.save(appointment));
    }

    public AppointmentResponse cancel(Long appointmentId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        appointment.setStatus("CANCELLED");
        appointment.setUpdatedAt(LocalDateTime.now());
        return AppointmentResponse.from(appointmentRepo.save(appointment));
    }

    private Appointment getAppointmentOrThrow(Long id) {
        return appointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }
}
