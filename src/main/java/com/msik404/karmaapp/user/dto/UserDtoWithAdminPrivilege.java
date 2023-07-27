package com.msik404.karmaapp.user.dto;

import com.msik404.karmaapp.user.Role;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString @Setter @Getter
public class UserDtoWithAdminPrivilege extends UserDtoWithUserPrivilege {

    public UserDtoWithAdminPrivilege(String firstName, String lastName, String email, String password, Role role,
            boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled) {
        super(firstName, lastName, email, password);
        this.role = role;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
    }
    private final Role role;

    @NotNull private final boolean accountNonExpired;
    @NotNull private final boolean accountNonLocked;
    @NotNull private final boolean credentialsNonExpired;
    @NotNull private final boolean enabled;

}

