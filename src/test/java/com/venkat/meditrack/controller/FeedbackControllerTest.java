package com.venkat.meditrack.controller;

import com.venkat.meditrack.dto.FeedbackRequest;
import com.venkat.meditrack.dto.FeedbackResponse;
import com.venkat.meditrack.exception.DuplicateResourceException;
import com.venkat.meditrack.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedbackController.class)
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedbackService feedbackService;

    @Test
    void should_return201WithBody_when_feedbackAccepted() throws Exception {
        // Arrange
        FeedbackResponse response = FeedbackResponse.builder()
                .id(7L).appointmentId(10L).patientId(1L).rating(4)
                .comment("Great visit").createdAt(LocalDateTime.now()).build();
        when(feedbackService.submit(eq(10L), any(FeedbackRequest.class))).thenReturn(response);
        String body = "{\"patientId\":1,\"rating\":4,\"comment\":\"Great visit\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments/10/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Great visit"));
    }

    @Test
    void should_return400_when_ratingOutOfRange() throws Exception {
        // Arrange
        String body = "{\"patientId\":1,\"rating\":6}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments/10/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("rating"));
    }

    @Test
    void should_return409_when_feedbackAlreadySubmitted() throws Exception {
        // Arrange
        when(feedbackService.submit(eq(10L), any(FeedbackRequest.class)))
                .thenThrow(new DuplicateResourceException("Feedback already submitted for appointment 10"));
        String body = "{\"patientId\":1,\"rating\":4}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments/10/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
