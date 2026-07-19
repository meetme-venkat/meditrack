package com.venkat.meditrack.controller;

import com.venkat.meditrack.dto.AppointmentResponse;
import com.venkat.meditrack.dto.BookAppointmentRequest;
import com.venkat.meditrack.exception.ResourceNotFoundException;
import com.venkat.meditrack.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Boot 4 removed @MockBean; @MockitoBean is its replacement.
@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void should_return201WithBody_when_bookingSucceeds() throws Exception {
        // Arrange
        AppointmentResponse response = AppointmentResponse.builder()
                .appointmentId(100L).appointmentNo("APT-ABCD1234").patientId(1L).doctorId(2L)
                .status("REQUESTED").scheduledDate(LocalDate.of(2099, 8, 1))
                .totalAmount(new BigDecimal("600.00")).build();
        when(bookingService.book(any(BookAppointmentRequest.class))).thenReturn(response);
        String body = "{\"patientId\":1,\"doctorId\":2,\"scheduledDate\":\"2099-08-01\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.appointmentNo").value("APT-ABCD1234"));
    }

    @Test
    void should_return400_when_bookingRequestInvalid() throws Exception {
        // Arrange
        String emptyBody = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void should_return404_when_cancelTargetMissing() throws Exception {
        // Arrange
        when(bookingService.cancel(any(Long.class)))
                .thenThrow(new ResourceNotFoundException("Appointment not found: 99"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments/99/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
