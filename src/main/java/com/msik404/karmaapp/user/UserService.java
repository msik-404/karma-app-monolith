package com.msik404.karmaapp.user;

import com.msik404.karmaapp.constraintExceptions.DuplicateEmailException;
import com.msik404.karmaapp.user.dto.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(long id) throws UserNotFoundException {

        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User findByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User with that username was not found"));
    }

    public Boolean sameAsAuthenticatedUser(long id) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (long) authentication.getPrincipal();
        return userId == id;
    }

    public UserDtoWithUserPrivilege updateWithUserPrivilege(
            long userId,
            @Nonnull UserDtoWithUserPrivilege request)
            throws AccessDeniedException, DuplicateEmailException, UserNotFoundException {

        if (!sameAsAuthenticatedUser(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        userRepository.updateNonNull(userId, request);

        return request;
    }

    public UserDtoWithAdminPrivilege updateWithAdminPrivilege(
            long userId,
            @Nonnull UserDtoWithAdminPrivilege request)
            throws DuplicateEmailException, UserNotFoundException {

        userRepository.updateNonNull(userId, request);

        return request;
    }

}
