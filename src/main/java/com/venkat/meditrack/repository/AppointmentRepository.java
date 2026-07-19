package com.venkat.meditrack.repository;

import com.venkat.meditrack.entity.Appointment;
import com.venkat.meditrack.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    long countByDoctorAndScheduledDate(Doctor doctor, LocalDate scheduledDate);
}
