package com.venkat.meditrack.controller;

import com.venkat.meditrack.dto.FeedbackRequest;
import com.venkat.meditrack.dto.FeedbackResponse;
import com.venkat.meditrack.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments/{appointmentId}/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback Management")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse submit(@PathVariable Long appointmentId,
                                   @Valid @RequestBody FeedbackRequest request) {
        return feedbackService.submit(appointmentId, request);
    }

    @GetMapping
    public FeedbackResponse get(@PathVariable Long appointmentId) {
        return feedbackService.findByAppointment(appointmentId);
    }
}
