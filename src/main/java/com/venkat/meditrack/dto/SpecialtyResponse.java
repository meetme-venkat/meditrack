package com.venkat.meditrack.dto;

import com.venkat.meditrack.entity.Specialty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;

    public static SpecialtyResponse from(Specialty s) {
        return SpecialtyResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .slug(s.getSlug())
                .description(s.getDescription())
                .build();
    }
}
