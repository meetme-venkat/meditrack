package com.venkat.meditrack.service;

import com.venkat.meditrack.dto.FeedbackRequest;
import com.venkat.meditrack.dto.FeedbackResponse;
import com.venkat.meditrack.entity.Appointment;
import com.venkat.meditrack.entity.AppointmentFeedback;
import com.venkat.meditrack.entity.Patient;
import com.venkat.meditrack.exception.BusinessRuleException;
import com.venkat.meditrack.exception.DuplicateResourceException;
import com.venkat.meditrack.exception.ResourceNotFoundException;
import com.venkat.meditrack.repository.AppointmentFeedbackRepository;
import com.venkat.meditrack.repository.AppointmentRepository;
import com.venkat.meditrack.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    // Feedback is only accepted once an appointment reaches this status. Nothing in
    // the API sets it today — appointments are marked COMPLETED directly in the DB.
    private static final String COMPLETED = "COMPLETED";

    private final AppointmentFeedbackRepository feedbackRepo;
    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;

    public FeedbackResponse submit(Long appointmentId, FeedbackRequest request) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        Patient patient = patientRepo.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));

        Long appointmentPatientId = appointment.getPatient() != null ? appointment.getPatient().getId() : null;
        if (!patient.getId().equals(appointmentPatientId)) {
            throw new BusinessRuleException(
                    "Patient " + patient.getId() + " did not attend appointment " + appointmentId);
        }

        if (!COMPLETED.equals(appointment.getStatus())) {
            throw new BusinessRuleException(
                    "Feedback is only allowed on a COMPLETED appointment; appointment "
                            + appointmentId + " is " + appointment.getStatus());
        }

        if (feedbackRepo.existsByAppointment_AppointmentIdAndPatient_Id(appointmentId, patient.getId())) {
            throw new DuplicateResourceException(
                    "Feedback already submitted for appointment " + appointmentId);
        }

        AppointmentFeedback feedback = AppointmentFeedback.builder()
                .appointment(appointment)
                .patient(patient)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();
        return FeedbackResponse.from(feedbackRepo.save(feedback));
    }

    public FeedbackResponse findByAppointment(Long appointmentId) {
        if (!appointmentRepo.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Appointment not found: " + appointmentId);
        }
        return feedbackRepo.findByAppointment_AppointmentId(appointmentId)
                .map(FeedbackResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No feedback for appointment " + appointmentId));
    }
}
