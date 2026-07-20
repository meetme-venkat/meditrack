package com.venkat.meditrack.repository;

// Aggregate projection for per-doctor rating stats. Populated by the grouped
// queries in AppointmentFeedbackRepository (one query for the whole list — no N+1).
public interface DoctorRatingSummary {
    Long getDoctorId();
    Double getAverageRating();
    Long getFeedbackCount();
}
