package com.eventwave.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventwave.model.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
