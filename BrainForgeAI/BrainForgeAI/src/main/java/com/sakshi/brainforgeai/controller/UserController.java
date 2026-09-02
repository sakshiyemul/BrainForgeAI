package com.sakshi.brainforgeai.controller;
import com.sakshi.brainforgeai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.sakshi.brainforgeai.entity.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.sakshi.brainforgeai.dto.LoginRequest;
import com.sakshi.brainforgeai.dto.LoginResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.sakshi.brainforgeai.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/users")
public class UserController {

        @Autowired
        private UserService userService;

        @Operation(
                summary = "Hello endpoint",
                description = "Returns a welcome message from BrainForge AI."
        )
        @ApiResponse(
                responseCode = "200",
                description = "Welcome message returned successfully"
        )

        @GetMapping("/hello")
        public String hello(){
            return "Hello Sakshi Welcome to BrainForge AI";
        }

        @Operation(
                summary = "Register a new user",
                description = "Creates a new user account."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "User registered successfully"
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Validation failed"
                )
        })

        @PostMapping("/registerUser")
        public User registerUser(@Valid @RequestBody User user) {
                user.setRole("USER");
                return userService.saveUser(user);
        }
        @Operation(
                summary = "User login",
                description = "Authenticates a user and returns a JWT token."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "Login successful"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Invalid email or password"
                )
        })

        @PostMapping("/login")
        public LoginResponse loginUser(@RequestBody LoginRequest loginRequest) {

                LoginResponse response = userService.loginUser(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                );
                return response;
        }
        @Operation(
                summary = "Test POST endpoint",
                description = "Checks whether POST requests are working."
        )
        @ApiResponse(
                responseCode = "200",
                description = "POST is working"
        )

        @PostMapping("/test")
        public String testPost() {

            return "POST is working!";
        }
        @SecurityRequirement(name = "bearerAuth")

        @Operation(
                summary = "Get all users",
                description = "Returns all registered users."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "Users retrieved successfully"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token is missing or invalid"
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - user does not have permission"
                )
        })
        @SecurityRequirement(name = "Bearer Authentication")

        @GetMapping("/all")
        public List<UserResponse> getAllUsers() {

                List<User> users = userService.getAllUsers();

                return users.stream()
                        .map(user -> new UserResponse(
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getRole()
                        ))
                        .toList();
        }

        @Operation(
                summary = "Get user by ID",
                description = "Returns a user using their unique ID."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "User found successfully"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token is missing or invalid"
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - user does not have permission"
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "User not found"
                )
        })
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/{id}")

        public UserResponse getUserById(@PathVariable Long id) {

                Optional<User> user = userService.getUserById(id);

                if (user.isEmpty()) {
                        throw new RuntimeException("User not found with id: " + id);
                }

                User u = user.get();

                return new UserResponse(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getRole()
                );
        }

        @Operation(
                summary = "Update a user",
                description = "Updates an existing user's details using their ID."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "User updated successfully"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token is missing or invalid"
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - user does not have permission"
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "User not found"
                )
        })
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping("/{id}")
        public UserResponse updateUser(
                @PathVariable Long id,
                @RequestBody User updatedUser) {

                User user = userService.updateUser(id, updatedUser);

                return new UserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole()
                );
        }

        @Operation(
                summary = "Delete a user",
                description = "Deletes an existing user using their ID."
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "User deleted successfully"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - JWT token is missing or invalid"
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - user does not have permission"
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "User not found"
                )
        })
        @SecurityRequirement(name = "Bearer Authentication")
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")

        public String deleteUser(@PathVariable Long id) {

            return userService.deleteUser(id);

        }
}
