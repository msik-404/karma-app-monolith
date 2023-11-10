package com.msik404.karmaappmonolith.user;

import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappmonolith.user.exception.NoFieldSetException;
import com.msik404.karmaappmonolith.user.exception.UserNotFoundException;
import com.msik404.karmaappmonolith.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
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

    @Transactional
    public UserUpdateRequestWithUserPrivilege updateWithUserPrivilege(
            @NonNull UserUpdateRequestWithUserPrivilege request)
            throws NoFieldSetException, DuplicateEmailException, DuplicateUsernameException,
            DuplicateUnexpectedFieldException, UserNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (long) authentication.getPrincipal();

        int rowsAffected = userRepository.updateNonNull(clientId, request);
        if (rowsAffected == 0) {
            throw new UserNotFoundException();
        }

        return request;
    }

    @Transactional
    public UserUpdateRequestWithAdminPrivilege updateWithAdminPrivilege(
            long userId,
            @NonNull UserUpdateRequestWithAdminPrivilege request)
            throws NoFieldSetException, DuplicateEmailException, DuplicateUsernameException,
            DuplicateUnexpectedFieldException, UserNotFoundException {

        int rowsAffected = userRepository.updateNonNull(userId, request);
        if (rowsAffected == 0) {
            throw new UserNotFoundException();
        }

        return request;
    }

    public User findByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

}
