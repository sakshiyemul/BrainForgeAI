package com.sakshi.brainforgeai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    @Operation(
            summary = "Admin dashboard",
            description = "Returns the admin dashboard welcome message."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin dashboard accessed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role is required"
            )
    })
    @GetMapping("/dashboard")
    public String dashboard() {
        return "Welcome to Admin Dashboard";
    }
}