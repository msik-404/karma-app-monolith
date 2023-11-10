package com.msik404.karmaappmonolith.user.repository;

import com.msik404.karmaappmonolith.user.User;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithUserPrivilege;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RequiredArgsConstructor
public class UserCriteriaUpdater {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean updateUserCriteria(
            @NonNull UserUpdateRequestWithUserPrivilege dto,
            @NonNull Root<User> root,
            @NonNull CriteriaUpdate<User> criteriaUpdate) {

        boolean someFieldSet = false;

        if (dto.firstName() != null) {
            someFieldSet = true;
            criteriaUpdate.set(root.get("firstName"), dto.firstName());
        }
        if (dto.lastName() != null) {
            someFieldSet = true;
            criteriaUpdate.set(root.get("lastName"), dto.lastName());
        }
        if (dto.username() != null) {
            someFieldSet = true;
            criteriaUpdate.set(root.get("username"), dto.username());
        }
        if (dto.email() != null) {
            someFieldSet = true;
            criteriaUpdate.set(root.get("email"), dto.email());
        }
        if (dto.password() != null) {
            someFieldSet = true;
            criteriaUpdate.set(root.get("password"), bCryptPasswordEncoder.encode(dto.password()));
        }

        return someFieldSet;
    }

    public boolean updateAdminCriteria(
            @NonNull UserUpdateRequestWithAdminPrivilege dto,
            @NonNull Root<User> root,
            @NonNull CriteriaUpdate<User> criteriaUpdate) {

        boolean someFieldSet = false;

        if (dto.userUpdateByUser() != null) {
            someFieldSet = updateUserCriteria(dto.userUpdateByUser(), root, criteriaUpdate);
        }
        if (dto.role() != null) {
            someFieldSet = true;
            criteriaUpdate.set(root.get("role"), dto.role());
        }

        return someFieldSet;
    }

}
