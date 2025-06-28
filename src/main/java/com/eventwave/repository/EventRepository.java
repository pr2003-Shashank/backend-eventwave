package com.eventwave.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventwave.model.Event;

public interface EventRepository extends JpaRepository<Event, Long>{

}
