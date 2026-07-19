package com.venkat.meditrack.dto;

import com.venkat.meditrack.entity.Doctor;
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

    public static DoctorResponse from(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .licenseNo(d.getLicenseNo())
                .consultationFee(d.getConsultationFee())
                .dailySlotCapacity(d.getDailySlotCapacity())
                .active(d.isActive())
                .specialtyId(d.getSpecialty() != null ? d.getSpecialty().getId() : null)
                .specialtyName(d.getSpecialty() != null ? d.getSpecialty().getName() : null)
                .build();
    }
}
