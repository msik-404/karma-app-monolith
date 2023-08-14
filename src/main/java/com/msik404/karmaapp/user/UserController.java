package com.msik404.karmaapp.user;

import com.msik404.karmaapp.constraintExceptions.exception.DuplicateEmailException;
import com.msik404.karmaapp.constraintExceptions.exception.DuplicateUsernameException;
import com.msik404.karmaapp.constraintExceptions.exception.UndefinedConstraintException;
import com.msik404.karmaapp.user.dto.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;
import com.msik404.karmaapp.user.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("user/users/{userId}")
    public ResponseEntity<UserDtoWithUserPrivilege> updateWithUserPrivilege(
            @PathVariable Long userId,
            @Valid @RequestBody UserDtoWithUserPrivilege request)
            throws AccessDeniedException, DuplicateEmailException, UndefinedConstraintException, UserNotFoundException {

        return ResponseEntity.ok(userService.updateWithUserPrivilege(userId, request));
    }

    @PutMapping("admin/users/{userId}")
    public ResponseEntity<UserDtoWithAdminPrivilege> updateWithAdminPrivilege(
            @PathVariable Long userId,
            @Valid @RequestBody UserDtoWithAdminPrivilege request)
            throws DuplicateEmailException, DuplicateUsernameException, UndefinedConstraintException, UserNotFoundException {

        return ResponseEntity.ok(userService.updateWithAdminPrivilege(userId, request));
    }

}
