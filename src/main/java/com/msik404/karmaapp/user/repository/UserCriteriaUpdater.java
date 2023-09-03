package com.msik404.karmaapp.user.repository;

import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithUserPrivilege;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserCriteriaUpdater {

    public void updateUserCriteria(
            @NonNull UserUpdateRequestWithUserPrivilege dto,
            @NonNull BCryptPasswordEncoder passwordEncoder,
            @NonNull Root<User> root,
            @NonNull CriteriaUpdate<User> criteriaUpdate) {

        if (dto.getFirstName() != null) {
            criteriaUpdate.set(root.get("firstName"), dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            criteriaUpdate.set(root.get("lastName"), dto.getLastName());
        }
        if (dto.getUsername() != null) {
            criteriaUpdate.set(root.get("username"), dto.getUsername());
        }
        if (dto.getEmail() != null) {
            criteriaUpdate.set(root.get("email"), dto.getEmail());
        }
        if (dto.getPassword() != null) {
            criteriaUpdate.set(root.get("password"), passwordEncoder.encode(dto.getPassword()));
        }
    }

    public void updateAdminCriteria(
            @NonNull UserUpdateRequestWithAdminPrivilege dto,
            @NonNull Root<User> root,
            @NonNull CriteriaUpdate<User> criteriaUpdate) {

        if (dto.getRole() != null) {
            criteriaUpdate.set(root.get("role"), dto.getRole());
        }
        if (dto.getAccountNonExpired() != null) {
            criteriaUpdate.set(root.get("accountNonExpired"), dto.getAccountNonExpired());
        }
        if (dto.getAccountNonLocked() != null) {
            criteriaUpdate.set(root.get("accountNonLocked"), dto.getAccountNonLocked());
        }
        if (dto.getCredentialsNonExpired() != null) {
            criteriaUpdate.set(root.get("credentialsNonExpired"), dto.getCredentialsNonExpired());
        }
        if (dto.getEnabled() != null) {
            criteriaUpdate.set(root.get("enabled"), dto.getEnabled());
        }
    }

}
