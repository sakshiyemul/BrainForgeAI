package com.sakshi.brainforgeai;

import com.sakshi.brainforgeai.entity.*;
import com.sakshi.brainforgeai.exception.UnauthorizedActionException;
import com.sakshi.brainforgeai.repository.*;
import com.sakshi.brainforgeai.service.QuestionService;
import com.sakshi.brainforgeai.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BrainForgeAiApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UsageMetricRepository usageMetricRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.sakshi.brainforgeai.security.JwtService jwtService;

    @Test
    void contextLoads() {
    }

    @Test
    void testCascadeDeleteUser() {
        // 1. Create a user
        User user = new User();
        user.setFullName("Integration Delete Test User");
        user.setEmail("cascade_int_test_" + System.currentTimeMillis() + "@brainforge.ai");
        user.setPassword("password123");
        user.setRole("USER");
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser.getId());
        Long userId = savedUser.getId();

        // 2. Attach a question
        Question question = new Question();
        question.setQuestion("Test Question");
        question.setAnswer("Test Answer");
        question.setUser(savedUser);
        questionRepository.save(question);

        // 3. Attach a refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("test-refresh-token-" + System.currentTimeMillis());
        refreshToken.setUser(savedUser);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        // 4. Attach a conversation with a message
        Conversation conversation = new Conversation("Test Conv", savedUser);
        ChatMessage message = new ChatMessage(conversation, "user", "Hello AI");
        conversation.getMessages().add(message);
        conversationRepository.save(conversation);

        // 5. Attach a document
        DocumentEntity doc = new DocumentEntity("test.txt", "text/plain", 100L, "Sample extracted text", savedUser);
        documentRepository.save(doc);

        // 6. Attach usage metric
        UsageMetric metric = new UsageMetric(savedUser, "CHAT", 50, "Test details");
        usageMetricRepository.save(metric);

        // 7. Execute deleteUser
        String result = userService.deleteUser(userId);
        assertEquals("User deleted successfully.", result);

        // 8. Verify all records were removed cleanly
        assertTrue(userRepository.findById(userId).isEmpty());
        assertTrue(questionRepository.findByUserEmail(savedUser.getEmail()).isEmpty());
        assertTrue(conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).isEmpty());
        assertTrue(documentRepository.findByUserIdOrderByCreatedAtDesc(userId).isEmpty());
    }

    @Test
    void testAdminCredentialsUpdateAndLogin() {
        User adminUser = userRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .findFirst()
                .orElse(null);

        assertNotNull(adminUser, "Admin user must exist");
        assertEquals("admin@brainforgeai.com", adminUser.getEmail());
        assertEquals("ADMIN", adminUser.getRole());
        assertNotNull(adminUser.getPassword());
        assertTrue(adminUser.getPassword().startsWith("$2a$") || adminUser.getPassword().startsWith("$2b$"));
    }

    @Test
    void testAdminCanDeleteAnyQuestion() {
        // 1. Create a regular user and attach a question
        User regularUser = new User();
        regularUser.setFullName("Regular User For Admin Delete");
        regularUser.setEmail("reg_user_del_" + System.currentTimeMillis() + "@brainforge.ai");
        regularUser.setPassword("password123");
        regularUser.setRole("USER");
        User savedRegularUser = userService.saveUser(regularUser);

        Question question = new Question();
        question.setQuestion("Question to be deleted by admin");
        question.setAnswer("Some Answer");
        question.setUser(savedRegularUser);
        Question savedQuestion = questionRepository.save(question);
        assertNotNull(savedQuestion.getId());

        // 2. Set security context as ADMIN
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@brainforgeai.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        // 3. Admin deletes the question
        String result = questionService.deleteQuestion(savedQuestion.getId());
        assertEquals("Question deleted successfully.", result);

        // 4. Verify question is deleted from database
        assertTrue(questionRepository.findById(savedQuestion.getId()).isEmpty());

        // Cleanup
        userService.deleteUser(savedRegularUser.getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    void testUserCanDeleteOwnQuestion() {
        // 1. Create a regular user and attach a question
        User regularUser = new User();
        regularUser.setFullName("User Own Question");
        regularUser.setEmail("user_own_q_" + System.currentTimeMillis() + "@brainforge.ai");
        regularUser.setPassword("password123");
        regularUser.setRole("USER");
        User savedUser = userService.saveUser(regularUser);

        Question question = new Question();
        question.setQuestion("Own question to delete");
        question.setAnswer("Answer");
        question.setUser(savedUser);
        Question savedQuestion = questionRepository.save(question);
        assertNotNull(savedQuestion.getId());

        // 2. Set security context as the question's owner
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        savedUser.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        // 3. User deletes their own question
        String result = questionService.deleteQuestion(savedQuestion.getId());
        assertEquals("Question deleted successfully.", result);

        // 4. Verify question is deleted from database
        assertTrue(questionRepository.findById(savedQuestion.getId()).isEmpty());

        // Cleanup
        userService.deleteUser(savedUser.getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    void testUserCannotDeleteAnotherUsersQuestion() {
        // 1. Create User A with a question
        User userA = new User();
        userA.setFullName("User A");
        userA.setEmail("userA_" + System.currentTimeMillis() + "@brainforge.ai");
        userA.setPassword("password123");
        userA.setRole("USER");
        User savedUserA = userService.saveUser(userA);

        Question questionA = new Question();
        questionA.setQuestion("User A's Protected Question");
        questionA.setAnswer("User A's Secret Answer");
        questionA.setUser(savedUserA);
        Question savedQuestionA = questionRepository.save(questionA);
        assertNotNull(savedQuestionA.getId());

        // 2. Create User B
        User userB = new User();
        userB.setFullName("User B");
        userB.setEmail("userB_" + System.currentTimeMillis() + "@brainforge.ai");
        userB.setPassword("password123");
        userB.setRole("USER");
        User savedUserB = userService.saveUser(userB);

        // 3. Set security context as User B
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        savedUserB.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        // 4. User B attempts to delete User A's question -> should throw UnauthorizedActionException
        assertThrows(UnauthorizedActionException.class, () -> {
            questionService.deleteQuestion(savedQuestionA.getId());
        });

        // 5. Verify question still exists in database
        assertTrue(questionRepository.findById(savedQuestionA.getId()).isPresent());

        // Cleanup
        userService.deleteUser(savedUserA.getId());
        userService.deleteUser(savedUserB.getId());
        SecurityContextHolder.clearContext();
    }
}
