package com.venkat.meditrack.service;

import com.venkat.meditrack.dto.DoctorRatingResponse;
import com.venkat.meditrack.dto.DoctorResponse;
import com.venkat.meditrack.entity.Doctor;
import com.venkat.meditrack.repository.AppointmentFeedbackRepository;
import com.venkat.meditrack.repository.DoctorRatingSummary;
import com.venkat.meditrack.repository.DoctorRepository;
import com.venkat.meditrack.repository.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepo;
    @Mock
    private SpecialtyRepository specialtyRepo;
    @Mock
    private AppointmentFeedbackRepository feedbackRepo;

    @InjectMocks
    private DoctorService doctorService;

    private static final Long DOCTOR_ID = 2L;

    private Doctor doctor(Long id) {
        return Doctor.builder().id(id).name("Dr. Test").build();
    }

    private DoctorRatingSummary summary(Long doctorId, Double avg, Long count) {
        return new DoctorRatingSummary() {
            public Long getDoctorId() { return doctorId; }
            public Double getAverageRating() { return avg; }
            public Long getFeedbackCount() { return count; }
        };
    }

    @Test
    void should_exposeRoundedAverage_when_doctorHasFeedback() {
        // Arrange — 13/3 = 4.333...
        when(doctorRepo.findById(DOCTOR_ID)).thenReturn(Optional.of(doctor(DOCTOR_ID)));
        when(feedbackRepo.findRatingSummary(DOCTOR_ID))
                .thenReturn(Optional.of(summary(DOCTOR_ID, 13.0 / 3.0, 3L)));

        // Act
        DoctorRatingResponse result = doctorService.findRating(DOCTOR_ID);

        // Assert
        assertThat(result).satisfies(r -> {
            assertThat(r.getDoctorId()).isEqualTo(DOCTOR_ID);
            assertThat(r.getAverageRating()).isEqualByComparingTo("4.33");
            assertThat(r.getFeedbackCount()).isEqualTo(3L);
        });
    }

    @Test
    void should_returnNullAverageAndZeroCount_when_doctorHasNoFeedback() {
        // Arrange
        when(doctorRepo.findById(DOCTOR_ID)).thenReturn(Optional.of(doctor(DOCTOR_ID)));
        when(feedbackRepo.findRatingSummary(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act
        DoctorRatingResponse result = doctorService.findRating(DOCTOR_ID);

        // Assert
        assertThat(result).satisfies(r -> {
            assertThat(r.getAverageRating()).isNull();
            assertThat(r.getFeedbackCount()).isZero();
        });
    }

    @Test
    void should_useSingleGroupedQuery_when_listingDoctors() {
        // Arrange — two doctors, only one has feedback
        when(doctorRepo.findAll()).thenReturn(List.of(doctor(DOCTOR_ID), doctor(3L)));
        when(feedbackRepo.findRatingSummaries())
                .thenReturn(List.of(summary(DOCTOR_ID, 5.0, 2L)));

        // Act
        List<DoctorResponse> result = doctorService.findAll();

        // Assert — one aggregate call total, guarding against N+1
        assertThat(result).satisfies(list -> {
            assertThat(list).hasSize(2);
            assertThat(list.get(0).getAverageRating()).isEqualByComparingTo("5.00");
            assertThat(list.get(0).getFeedbackCount()).isEqualTo(2L);
            assertThat(list.get(1).getAverageRating()).isNull();
            assertThat(list.get(1).getFeedbackCount()).isZero();
            verify(feedbackRepo, times(1)).findRatingSummaries();
        });
    }
}
