package com.msik404.karmaapp.user.repository;

import java.util.Optional;

import com.msik404.karmaapp.TestingDataCreator;
import com.msik404.karmaapp.exception.constraint.ConstraintExceptionsHandler;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaapp.exception.constraint.strategy.ConstraintViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaapp.exception.constraint.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithAdminPrivilege;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = UserRepositoryCustomImplTest.DataSourceInitializer.class)
@Import({
        BCryptPasswordEncoder.class,
        UserCriteriaUpdater.class,
        ConstraintExceptionsHandler.class,
        ConstraintViolationExceptionErrorMessageExtractionStrategy.class,
        RoundBraceErrorMassageParseStrategy.class,
})
class UserRepositoryCustomImplTest {

    private static final int testUserId = 1;
    private static final Role TEST_ROLE = Role.USER;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final UserRepository userRepository;

    private final TransactionTemplate transactionTemplate;

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:alpine");

    public static class DataSourceInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
                    "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword()
            );
        }
    }

    @Autowired
    UserRepositoryCustomImplTest(
            BCryptPasswordEncoder bCryptPasswordEncoder,
            UserRepository userRepository,
            TransactionTemplate transactionTemplate) {

        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
    }

    private User getUserForTesting(int userId) {

        final String username = TestingDataCreator.getTestingUsername(userId);

        return User.builder()
                .firstName(username)
                .lastName(username)
                .username(username)
                .email(TestingDataCreator.getTestingEmail(username))
                .password(bCryptPasswordEncoder.encode(username))
                .role(TEST_ROLE)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    @BeforeEach
    void setUp() {
        userRepository.save(getUserForTesting(testUserId));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void updateNonNull_UserIdIsOneAndDtoHasNullFields_NonNullFieldsAreUpdated() {

        // given
        final String username = TestingDataCreator.getTestingUsername(testUserId);

        final Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        final User oldUser = optionalUser.get();
        final long persistedUserId = oldUser.getId();

        final int newUserId = 2;
        final String newUsername = TestingDataCreator.getTestingUsername(newUserId);
        final String newEmail = TestingDataCreator.getTestingEmail(newUsername);
        final Role newRole = Role.MOD;

        final var dto = UserUpdateRequestWithAdminPrivilege.builder()
                .username(newUsername)
                .email(newEmail)
                .password(newUsername)
                .role(newRole)
                .build();

        // when
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus transactionStatus) {
                userRepository.updateNonNull(persistedUserId, dto);
            }
        });

        // then
        final Optional<User> optionalNewUser = userRepository.findByUsername(newUsername);

        assertTrue(optionalNewUser.isPresent());

        final User newUser = optionalNewUser.get();

        assertEquals(persistedUserId, newUser.getId());
        assertEquals(newUsername, newUser.getUsername());
        assertEquals(newEmail, newUser.getEmail());
        assertTrue(bCryptPasswordEncoder.matches(newUsername, newUser.getPassword()));
        assertEquals(newRole, newUser.getRole());

        assertEquals(oldUser.getFirstName(), newUser.getFirstName());
        assertEquals(oldUser.getLastName(), newUser.getLastName());
        assertEquals(oldUser.isAccountNonExpired(), newUser.isAccountNonExpired());
        assertEquals(oldUser.isAccountNonLocked(), newUser.isAccountNonLocked());
        assertEquals(oldUser.isCredentialsNonExpired(), newUser.isCredentialsNonExpired());
        assertEquals(oldUser.isEnabled(), newUser.isEnabled());
    }

    @Test
    void updateNonNull_UserIdIsOneAndDtoHasNullFieldsAndUsernameIsDuplicate_DuplicateUsernameExceptionIsThrown() {

        // given
        final String username = TestingDataCreator.getTestingUsername(testUserId);

        final Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        final long persistedUserId = optionalUser.get().getId();
        final int newUserId = 2;

        final User newUser = userRepository.save(getUserForTesting(newUserId));

        final var dto = UserUpdateRequestWithAdminPrivilege.builder()
                .username(newUser.getUsername())
                .build();

        // then
        assertThrows(UnexpectedRollbackException.class, () ->
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(@NonNull TransactionStatus transactionStatus) {
                        // then                                              // when
                        assertThrows(DuplicateUsernameException.class, () -> userRepository.updateNonNull(persistedUserId, dto));
                    }
                })
        );
    }

    @Test
    void updateNonNull_UserIdIsOneAndDtoHasNullFieldsAndEmailIsDuplicate_DuplicateEmailExceptionIsThrown() {

        // given
        final String username = TestingDataCreator.getTestingUsername(testUserId);
        final Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        final long persistedUserId = optionalUser.get().getId();

        final int newUserId = 2;
        final User newUser = userRepository.save(getUserForTesting(newUserId));

        final var dto = UserUpdateRequestWithAdminPrivilege.builder()
                .email(newUser.getEmail())
                .build();

        // then
        assertThrows(UnexpectedRollbackException.class, () ->
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(@NonNull TransactionStatus transactionStatus) {
                        // then                                           // when
                        assertThrows(DuplicateEmailException.class, () -> userRepository.updateNonNull(persistedUserId, dto));
                    }
                })
        );
    }
}