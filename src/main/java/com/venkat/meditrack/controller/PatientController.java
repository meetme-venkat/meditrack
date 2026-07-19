package com.venkat.meditrack.controller;

import com.venkat.meditrack.dto.PatientRequest;
import com.venkat.meditrack.dto.PatientResponse;
import com.venkat.meditrack.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Management")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public List<PatientResponse> all() {
        return patientService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse register(@Valid @RequestBody PatientRequest request) {
        return patientService.register(request);
    }
}
