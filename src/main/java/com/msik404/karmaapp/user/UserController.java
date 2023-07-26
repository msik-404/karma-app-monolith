package com.msik404.karmaapp.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.msik404.karmaapp.user.dtos.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dtos.UserDtoWithUserPrivilege;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("user/users/{userId}")
    public ResponseEntity<UserDtoWithUserPrivilege> updateWithUserPrivilege(
            @PathVariable Long userId,
            @Valid @RequestBody UserDtoWithUserPrivilege request) {

        return ResponseEntity.ok(userService.updateWithUserPrivilege(userId, request));
    }

    @PutMapping("admin/users/{userId}")
    public ResponseEntity<UserDtoWithAdminPrivilege> updateWithAdminPrivilege(
            @PathVariable Long userId,
            @Valid @RequestBody UserDtoWithAdminPrivilege request) {

        return ResponseEntity.ok(userService.updateWithAdminPrivilege(userId, request));
    }

}
