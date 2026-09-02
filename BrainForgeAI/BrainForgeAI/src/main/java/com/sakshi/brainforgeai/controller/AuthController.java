package com.sakshi.brainforgeai.controller;

import com.sakshi.brainforgeai.dto.RefreshTokenRequest;
import com.sakshi.brainforgeai.dto.RefreshTokenResponse;
import com.sakshi.brainforgeai.entity.RefreshToken;
import com.sakshi.brainforgeai.security.JwtService;
import com.sakshi.brainforgeai.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new JWT access token using a valid refresh token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access token refreshed"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(
            @RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        String newAccessToken =
                jwtService.generateToken(
                        refreshToken.getUser().getEmail()
                );

        return new RefreshTokenResponse(newAccessToken);
    }

    @Operation(
            summary = "Logout",
            description = "Revokes the refresh token."
    )
    @PostMapping("/logout")
    public String logout(
            @RequestBody RefreshTokenRequest request) {

        refreshTokenService.revokeToken(
                request.getRefreshToken()
        );

        return "Logout successful";
    }
}