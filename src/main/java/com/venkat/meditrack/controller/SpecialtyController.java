package com.venkat.meditrack.controller;

import com.venkat.meditrack.dto.SpecialtyRequest;
import com.venkat.meditrack.dto.SpecialtyResponse;
import com.venkat.meditrack.service.SpecialtyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/specialties")
@RequiredArgsConstructor
@Tag(name = "Specialty Management")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping
    public List<SpecialtyResponse> all() {
        return specialtyService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpecialtyResponse create(@Valid @RequestBody SpecialtyRequest request) {
        return specialtyService.create(request);
    }
}
