package com.venkat.meditrack.dto;

import com.venkat.meditrack.entity.Doctor;
import com.venkat.meditrack.repository.DoctorRatingSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String name;
    private String licenseNo;
    private BigDecimal consultationFee;
    private int dailySlotCapacity;
    private boolean active;
    private Long specialtyId;
    private String specialtyName;
    // null until the doctor has feedback; see DoctorRatingResponse.
    private BigDecimal averageRating;
    private long feedbackCount;

    public static DoctorResponse from(Doctor d) {
        return from(d, null);
    }

    public static DoctorResponse from(Doctor d, DoctorRatingSummary rating) {
        boolean hasRating = rating != null && rating.getAverageRating() != null;
        return DoctorResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .licenseNo(d.getLicenseNo())
                .consultationFee(d.getConsultationFee())
                .dailySlotCapacity(d.getDailySlotCapacity())
                .active(d.isActive())
                .specialtyId(d.getSpecialty() != null ? d.getSpecialty().getId() : null)
                .specialtyName(d.getSpecialty() != null ? d.getSpecialty().getName() : null)
                .averageRating(hasRating ? DoctorRatingResponse.round(rating.getAverageRating()) : null)
                .feedbackCount(hasRating && rating.getFeedbackCount() != null ? rating.getFeedbackCount() : 0L)
                .build();
    }
}
