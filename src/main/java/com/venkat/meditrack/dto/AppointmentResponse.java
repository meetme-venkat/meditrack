package com.venkat.meditrack.dto;

import com.venkat.meditrack.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long appointmentId;
    private String appointmentNo;
    private Long patientId;
    private Long doctorId;
    private String status;
    private LocalDate scheduledDate;
    private BigDecimal totalAmount;

    public static AppointmentResponse from(Appointment a) {
        return AppointmentResponse.builder()
                .appointmentId(a.getAppointmentId())
                .appointmentNo(a.getAppointmentNo())
                .patientId(a.getPatient() != null ? a.getPatient().getId() : null)
                .doctorId(a.getDoctor() != null ? a.getDoctor().getId() : null)
                .status(a.getStatus())
                .scheduledDate(a.getScheduledDate())
                .totalAmount(a.getTotalAmount())
                .build();
    }
}
