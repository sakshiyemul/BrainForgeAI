package com.sakshi.brainforgeai.service;
import com.sakshi.brainforgeai.entity.User;
import com.sakshi.brainforgeai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import com.sakshi.brainforgeai.security.JwtService;
import com.sakshi.brainforgeai.dto.LoginResponse;
import com.sakshi.brainforgeai.exception.InvalidCredentialsException;
import com.sakshi.brainforgeai.entity.RefreshToken;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    public User saveUser(User user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
    public Optional<User> getUserById(Long id) {

        return userRepository.findById(id);

    }
    public User updateUser(Long id, User updatedUser) {

        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }

        User user = existingUser.get();

        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());

        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );
        }

        return userRepository.save(user);
    }
    @Transactional
    public String deleteUser(Long id) {

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }

        userRepository.delete(existingUser.get());

        return "User deleted successfully.";
    }
    public LoginResponse loginUser(String email, String password) {

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {

            User existingUser = user.get();

            if (passwordEncoder.matches(
                    password,
                    existingUser.getPassword())) {

                String token =
                        jwtService.generateToken(
                                existingUser.getEmail()
                        );

                RefreshToken refreshToken =
                        refreshTokenService.createRefreshToken(
                                existingUser
                        );

                return new LoginResponse(
                        existingUser.getId(),
                        existingUser.getFullName(),
                        existingUser.getEmail(),
                        existingUser.getRole(),
                        token,
                        refreshToken.getToken()
                );
            }
        }

        throw new InvalidCredentialsException(
                "Invalid email or password"
        );
    }
}
