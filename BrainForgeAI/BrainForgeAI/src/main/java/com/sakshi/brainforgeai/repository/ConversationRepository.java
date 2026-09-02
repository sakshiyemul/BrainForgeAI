package com.sakshi.brainforgeai.repository;

import com.sakshi.brainforgeai.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserEmailOrderByUpdatedAtDesc(String email);

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    void deleteByIdAndUserEmail(Long id, String email);
}
