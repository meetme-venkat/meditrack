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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private AppointmentFeedbackRepository feedbackRepo;
    @Mock
    private AppointmentRepository appointmentRepo;
    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private FeedbackService feedbackService;

    private static final Long APPOINTMENT_ID = 10L;
    private static final Long PATIENT_ID = 1L;

    private FeedbackRequest request() {
        return FeedbackRequest.builder().patientId(PATIENT_ID).rating(4).comment("Great visit").build();
    }

    private Patient patient(Long id) {
        return Patient.builder().id(id).build();
    }

    private Appointment appointment(String status, Long patientId) {
        return Appointment.builder()
                .appointmentId(APPOINTMENT_ID)
                .status(status)
                .patient(patient(patientId))
                .build();
    }

    @Test
    void should_createFeedback_when_appointmentCompletedAndFirstSubmission() {
        // Arrange
        when(appointmentRepo.findById(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment("COMPLETED", PATIENT_ID)));
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient(PATIENT_ID)));
        when(feedbackRepo.existsByAppointment_AppointmentIdAndPatient_Id(APPOINTMENT_ID, PATIENT_ID))
                .thenReturn(false);
        when(feedbackRepo.save(any(AppointmentFeedback.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        FeedbackResponse result = feedbackService.submit(APPOINTMENT_ID, request());

        // Assert
        assertThat(result).satisfies(r -> {
            assertThat(r.getRating()).isEqualTo(4);
            assertThat(r.getComment()).isEqualTo("Great visit");
            assertThat(r.getAppointmentId()).isEqualTo(APPOINTMENT_ID);
            assertThat(r.getPatientId()).isEqualTo(PATIENT_ID);
            assertThat(r.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void should_throwResourceNotFound_when_appointmentMissing() {
        // Arrange
        when(appointmentRepo.findById(APPOINTMENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> feedbackService.submit(APPOINTMENT_ID, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found");
        verify(feedbackRepo, never()).save(any());
    }

    @Test
    void should_throwResourceNotFound_when_patientMissing() {
        // Arrange
        when(appointmentRepo.findById(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment("COMPLETED", PATIENT_ID)));
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> feedbackService.submit(APPOINTMENT_ID, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found");
        verify(feedbackRepo, never()).save(any());
    }

    @Test
    void should_throwBusinessRule_when_patientDidNotAttendAppointment() {
        // Arrange — appointment belongs to a different patient
        when(appointmentRepo.findById(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment("COMPLETED", 99L)));
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient(PATIENT_ID)));

        // Act & Assert
        assertThatThrownBy(() -> feedbackService.submit(APPOINTMENT_ID, request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("did not attend");
        verify(feedbackRepo, never()).save(any());
    }

    @Test
    void should_throwBusinessRule_when_appointmentNotCompleted() {
        // Arrange
        when(appointmentRepo.findById(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment("REQUESTED", PATIENT_ID)));
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient(PATIENT_ID)));

        // Act & Assert
        assertThatThrownBy(() -> feedbackService.submit(APPOINTMENT_ID, request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("COMPLETED");
        verify(feedbackRepo, never()).save(any());
    }

    @Test
    void should_throwDuplicate_when_feedbackAlreadySubmitted() {
        // Arrange
        when(appointmentRepo.findById(APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment("COMPLETED", PATIENT_ID)));
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient(PATIENT_ID)));
        when(feedbackRepo.existsByAppointment_AppointmentIdAndPatient_Id(APPOINTMENT_ID, PATIENT_ID))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> feedbackService.submit(APPOINTMENT_ID, request()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already submitted");
        verify(feedbackRepo, never()).save(any());
    }

    @Test
    void should_throwResourceNotFound_when_noFeedbackForAppointment() {
        // Arrange
        when(appointmentRepo.existsById(APPOINTMENT_ID)).thenReturn(true);
        when(feedbackRepo.findByAppointment_AppointmentId(APPOINTMENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> feedbackService.findByAppointment(APPOINTMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No feedback");
    }
}
