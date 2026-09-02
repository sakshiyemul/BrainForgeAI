package com.sakshi.brainforgeai.repository;

import com.sakshi.brainforgeai.entity.UsageMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UsageMetricRepository extends JpaRepository<UsageMetric, Long> {

    List<UsageMetric> findByUserEmailOrderByTimestampDesc(String email);

    long countByUserEmail(String email);

    @Query("SELECT COALESCE(SUM(u.tokensEstimated), 0) FROM UsageMetric u WHERE u.user.email = :email")
    long sumTokensByUserEmail(String email);

    @Query("SELECT COALESCE(SUM(u.tokensEstimated), 0) FROM UsageMetric u")
    long sumAllTokens();
}
