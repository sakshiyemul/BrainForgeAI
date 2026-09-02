package com.sakshi.brainforgeai.controller;

import com.sakshi.brainforgeai.dto.AdminAnalyticsResponse;
import com.sakshi.brainforgeai.dto.UserAnalyticsResponse;
import com.sakshi.brainforgeai.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Operation(summary = "Get user analytics", description = "Returns activity metrics, token usage, and conversation stats for the authenticated user.")
    @GetMapping("/user")
    public UserAnalyticsResponse getUserAnalytics() {
        return analyticsService.getUserAnalytics();
    }

    @Operation(summary = "Get admin analytics", description = "Returns system-wide telemetry and platform usage metrics. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public AdminAnalyticsResponse getAdminAnalytics() {
        return analyticsService.getAdminAnalytics();
    }
}
