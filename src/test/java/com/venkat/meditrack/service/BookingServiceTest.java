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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private AppointmentRepository appointmentRepo;
    @Mock
    private DoctorRepository doctorRepo;
    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private BookingService bookingService;

    private static final Long PATIENT_ID = 1L;
    private static final Long DOCTOR_ID = 2L;
    private static final LocalDate DATE = LocalDate.of(2099, 8, 1);

    private BookAppointmentRequest bookingRequest() {
        return BookAppointmentRequest.builder()
                .patientId(PATIENT_ID)
                .doctorId(DOCTOR_ID)
                .scheduledDate(DATE)
                .build();
    }

    private Doctor doctorWithCapacity(int capacity) {
        return Doctor.builder()
                .id(DOCTOR_ID)
                .dailySlotCapacity(capacity)
                .consultationFee(new BigDecimal("600.00"))
                .build();
    }

    @Test
    void should_throwResourceNotFound_when_patientMissing() {
        // Arrange
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.book(bookingRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found");
        verify(appointmentRepo, never()).save(any());
    }

    @Test
    void should_throwResourceNotFound_when_doctorMissing() {
        // Arrange
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(new Patient()));
        when(doctorRepo.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.book(bookingRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found");
        verify(appointmentRepo, never()).save(any());
    }

    @Test
    void should_persistRequestedAppointment_when_slotsAvailable() {
        // Arrange
        Patient patient = Patient.builder().id(PATIENT_ID).build();
        Doctor doctor = doctorWithCapacity(8);
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(doctorRepo.findById(DOCTOR_ID)).thenReturn(Optional.of(doctor));
        when(appointmentRepo.countByDoctorAndScheduledDate(doctor, DATE)).thenReturn(2L);
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AppointmentResponse result = bookingService.book(bookingRequest());

        // Assert
        assertThat(result).satisfies(r -> {
            assertThat(r.getStatus()).isEqualTo("REQUESTED");
            assertThat(r.getPatientId()).isEqualTo(PATIENT_ID);
            assertThat(r.getDoctorId()).isEqualTo(DOCTOR_ID);
            assertThat(r.getScheduledDate()).isEqualTo(DATE);
            assertThat(r.getAppointmentNo()).startsWith("APT-");
            assertThat(r.getTotalAmount()).isEqualByComparingTo("600.00");
        });
    }

    @Test
    void should_throwBusinessRule_when_doctorFullyBooked() {
        // Arrange
        Doctor doctor = doctorWithCapacity(5);
        when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(new Patient()));
        when(doctorRepo.findById(DOCTOR_ID)).thenReturn(Optional.of(doctor));
        when(appointmentRepo.countByDoctorAndScheduledDate(doctor, DATE)).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> bookingService.book(bookingRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No slots available");
        verify(appointmentRepo, never()).save(any());
    }

    @Test
    void should_setStatusCancelled_when_appointmentExists() {
        // Arrange
        Appointment appointment = Appointment.builder().status("REQUESTED").build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AppointmentResponse result = bookingService.cancel(10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void should_throwResourceNotFound_when_cancellingMissingAppointment() {
        // Arrange
        when(appointmentRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.cancel(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found");
        verify(appointmentRepo, never()).save(any());
    }
}
