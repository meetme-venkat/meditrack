package com.venkat.meditrack.dto;

import com.venkat.meditrack.entity.AppointmentFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public static FeedbackResponse from(AppointmentFeedback f) {
        return FeedbackResponse.builder()
                .id(f.getId())
                .appointmentId(f.getAppointment() != null ? f.getAppointment().getAppointmentId() : null)
                .patientId(f.getPatient() != null ? f.getPatient().getId() : null)
                .rating(f.getRating())
                .comment(f.getComment())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
