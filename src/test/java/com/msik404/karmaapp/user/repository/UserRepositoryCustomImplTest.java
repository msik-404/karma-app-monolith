package com.msik404.karmaapp.user.repository;

import java.util.Optional;

import com.msik404.karmaapp.TestingDataCreator;
import com.msik404.karmaapp.exception.constraint.ConstraintExceptionsHandler;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaapp.user.exception.NoFieldSetException;
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
        ConstraintExceptionsHandler.class
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
            TransactionTemplate transactionTemplate
    ) {

        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
    }

    private User getUserForTesting(int userId) {

        final String username = TestingDataCreator.getTestingUsername(userId);

        return new User(
                username,
                TestingDataCreator.getTestingEmail(username),
                bCryptPasswordEncoder.encode(username),
                TEST_ROLE,
                username,
                username
        );
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
        String username = TestingDataCreator.getTestingUsername(testUserId);

        Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        User oldUser = optionalUser.get();
        long persistedUserId = oldUser.getId();

        int newUserId = 2;
        String newUsername = TestingDataCreator.getTestingUsername(newUserId);
        String newEmail = TestingDataCreator.getTestingEmail(newUsername);
        String newPassword = newUsername;
        Role newRole = Role.MOD;

        var userUpdateWithUserPrivilege = new UserUpdateRequestWithUserPrivilege(
                null,
                null,
                newUsername,
                newEmail,
                newPassword
        );

        var dto = new UserUpdateRequestWithAdminPrivilege(userUpdateWithUserPrivilege, newRole);

        // when
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus transactionStatus) {
                userRepository.updateNonNull(persistedUserId, dto);
            }
        });

        // then
        Optional<User> optionalNewUser = userRepository.findByUsername(newUsername);

        assertTrue(optionalNewUser.isPresent());

        User newUser = optionalNewUser.get();

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
        String username = TestingDataCreator.getTestingUsername(testUserId);

        Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        long persistedUserId = optionalUser.get().getId();
        int newUserId = 2;

        User newUser = userRepository.save(getUserForTesting(newUserId));

        var dto = new UserUpdateRequestWithUserPrivilege(
                null,
                null,
                newUser.getUsername(),
                null,
                null
        );

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
        String username = TestingDataCreator.getTestingUsername(testUserId);
        Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        long persistedUserId = optionalUser.get().getId();

        int newUserId = 2;
        User newUser = userRepository.save(getUserForTesting(newUserId));

        var dto = new UserUpdateRequestWithUserPrivilege(
                null,
                null,
                null,
                newUser.getEmail(),
                null
        );

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

    @Test
    void updateNonNull_UpdateDtoHasOnlyNullField_NoFieldExceptionThrown() {

        // given
        String username = TestingDataCreator.getTestingUsername(testUserId);
        Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        long persistedUserId = optionalUser.get().getId();

        var dto = new UserUpdateRequestWithAdminPrivilege(
                new UserUpdateRequestWithUserPrivilege(null, null, null, null, null),
                null
        );

        // then
        assertDoesNotThrow(() ->
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        // then                                       // when
                        assertThrows(NoFieldSetException.class, () -> userRepository.updateNonNull(persistedUserId, dto));
                    }
                })
        );
    }

}