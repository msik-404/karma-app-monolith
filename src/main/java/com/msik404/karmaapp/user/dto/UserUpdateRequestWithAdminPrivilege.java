package com.msik404.karmaapp.user.dto;

import com.msik404.karmaapp.user.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserUpdateRequestWithAdminPrivilege extends UserUpdateRequestWithUserPrivilege {

    @Nullable
    private Role role;

    @Nullable
    private Boolean accountNonExpired;

    @Nullable
    private Boolean accountNonLocked;

    @Nullable
    private Boolean credentialsNonExpired;

    @Nullable
    private Boolean enabled;

}

