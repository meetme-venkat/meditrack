package com.venkat.meditrack.controller;

import com.venkat.meditrack.dto.DoctorRequest;
import com.venkat.meditrack.dto.DoctorResponse;
import com.venkat.meditrack.service.DoctorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Management")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public List<DoctorResponse> all() {
        return doctorService.findAll();
    }

    @GetMapping("/{id}")
    public DoctorResponse get(@PathVariable Long id) {
        return doctorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse create(@Valid @RequestBody DoctorRequest request) {
        return doctorService.create(request);
    }
}
