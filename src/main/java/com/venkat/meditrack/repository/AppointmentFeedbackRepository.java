package com.venkat.meditrack.repository;

import com.venkat.meditrack.entity.AppointmentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppointmentFeedbackRepository extends JpaRepository<AppointmentFeedback, Long> {

    boolean existsByAppointment_AppointmentIdAndPatient_Id(Long appointmentId, Long patientId);

    Optional<AppointmentFeedback> findByAppointment_AppointmentId(Long appointmentId);

    // One grouped query for every doctor — keeps the doctor list off the N+1 path.
    @Query("""
            select a.doctor.id as doctorId,
                   avg(f.rating) as averageRating,
                   count(f) as feedbackCount
            from AppointmentFeedback f
            join f.appointment a
            group by a.doctor.id
            """)
    List<DoctorRatingSummary> findRatingSummaries();

    @Query("""
            select a.doctor.id as doctorId,
                   avg(f.rating) as averageRating,
                   count(f) as feedbackCount
            from AppointmentFeedback f
            join f.appointment a
            where a.doctor.id = :doctorId
            group by a.doctor.id
            """)
    Optional<DoctorRatingSummary> findRatingSummary(@Param("doctorId") Long doctorId);
}
