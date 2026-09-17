package com.restaurant.hub.repository;

import com.restaurant.hub.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
}
