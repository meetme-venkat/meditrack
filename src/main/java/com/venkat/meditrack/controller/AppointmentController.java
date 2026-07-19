package com.venkat.meditrack.controller;

import com.venkat.meditrack.dto.AppointmentResponse;
import com.venkat.meditrack.dto.BookAppointmentRequest;
import com.venkat.meditrack.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management")
public class AppointmentController {

    private final BookingService bookingService;

    @GetMapping
    public List<AppointmentResponse> all() {
        return bookingService.findAll();
    }

    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse book(@Valid @RequestBody BookAppointmentRequest request) {
        return bookingService.book(request);
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
