package com.venkat.meditrack.dto;

import com.venkat.meditrack.repository.DoctorRatingSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRatingResponse {
    private Long doctorId;
    // null (not 0.00) when the doctor has no feedback yet — 0.00 would read as a bad score.
    private BigDecimal averageRating;
    private long feedbackCount;

    public static DoctorRatingResponse from(Long doctorId, DoctorRatingSummary summary) {
        if (summary == null || summary.getAverageRating() == null) {
            return DoctorRatingResponse.builder()
                    .doctorId(doctorId)
                    .averageRating(null)
                    .feedbackCount(0L)
                    .build();
        }
        return DoctorRatingResponse.builder()
                .doctorId(doctorId)
                .averageRating(round(summary.getAverageRating()))
                .feedbackCount(summary.getFeedbackCount() != null ? summary.getFeedbackCount() : 0L)
                .build();
    }

    // 2dp so JSON reads 4.33, not 4.333333333.
    public static BigDecimal round(Double average) {
        return average == null ? null : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }
}
