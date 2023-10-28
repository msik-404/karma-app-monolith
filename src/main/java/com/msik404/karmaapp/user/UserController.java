package com.msik404.karmaapp.user;

import com.msik404.karmaapp.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaapp.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaapp.user.exception.NoFieldSetException;
import com.msik404.karmaapp.user.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("user/users")
    public ResponseEntity<UserUpdateRequestWithUserPrivilege> updateWithUserPrivilege(
            @Valid @RequestBody UserUpdateRequestWithUserPrivilege request)
            throws NoFieldSetException, DuplicateEmailException,
            DuplicateUnexpectedFieldException, UserNotFoundException {

        return ResponseEntity.ok(userService.updateWithUserPrivilege(request));
    }

    @PutMapping("admin/users/{userId}")
    public ResponseEntity<UserUpdateRequestWithAdminPrivilege> updateWithAdminPrivilege(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestWithAdminPrivilege request)
            throws DuplicateEmailException, NoFieldSetException, DuplicateUsernameException,
            DuplicateUnexpectedFieldException, UserNotFoundException {

        return ResponseEntity.ok(userService.updateWithAdminPrivilege(userId, request));
    }

}
