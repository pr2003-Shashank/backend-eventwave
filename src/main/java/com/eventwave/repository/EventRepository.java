package com.eventwave.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eventwave.model.Event;

public interface EventRepository extends JpaRepository<Event, Long>{
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT e FROM Event e WHERE e.id = :eventId")
	Optional<Event> findByIdWithLock(@Param("eventId") Long eventId);
}
