package com.sakshi.brainforgeai.repository;

import com.sakshi.brainforgeai.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    List<DocumentEntity> findByUserEmailOrderByCreatedAtDesc(String email);

    List<DocumentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserEmail(String email);
}
