package com.msik404.karmaapp.user;

import com.msik404.karmaapp.constraintExceptions.DuplicateEmailException;
import com.msik404.karmaapp.constraintExceptions.DuplicateUsernameException;
import com.msik404.karmaapp.constraintExceptions.UndefinedConstraintException;
import com.msik404.karmaapp.user.dto.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(long id) throws UserNotFoundException {

        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User findByUsername(@NonNull String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User with that username was not found"));
    }

    public Boolean sameAsAuthenticatedUser(long id) {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();
        return userId == id;
    }

    @Transactional
    public UserDtoWithUserPrivilege updateWithUserPrivilege(
            long userId,
            @NonNull UserDtoWithUserPrivilege request)
            throws AccessDeniedException, DuplicateEmailException, DuplicateUsernameException, UndefinedConstraintException, UserNotFoundException {

        if (!sameAsAuthenticatedUser(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        int rowsAffected = userRepository.updateNonNull(userId, request);
        if (rowsAffected == 0) {
            throw new UserNotFoundException();
        }

        return request;
    }

    @Transactional
    public UserDtoWithAdminPrivilege updateWithAdminPrivilege(
            long userId,
            @NonNull UserDtoWithAdminPrivilege request)
            throws DuplicateEmailException, DuplicateUsernameException, UndefinedConstraintException, UserNotFoundException {

        int rowsAffected = userRepository.updateNonNull(userId, request);
        if (rowsAffected == 0) {
            throw new UserNotFoundException();
        }

        return request;
    }

}
