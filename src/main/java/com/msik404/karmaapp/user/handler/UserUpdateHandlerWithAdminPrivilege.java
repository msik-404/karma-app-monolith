package com.msik404.karmaapp.user.handler;

import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.dtos.UserDtoWithAdminPrivilege;

import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserUpdateHandlerWithAdminPrivilege implements Handler<CriteriaUpdate<User>> {

    private final Root<User> root;
    private final UserDtoWithAdminPrivilege dto;

    private Handler<CriteriaUpdate<User>> nextUpdater;

    @Override
    public void setNext(Handler<CriteriaUpdate<User>> updater) {
        this.nextUpdater = updater;
    }

    @Override
    public void handle(CriteriaUpdate<User> criteriaUpdate) {

        if (dto.getRole() != null) {
            criteriaUpdate.set(root.get("role"), dto.getRole());
        }
        criteriaUpdate.set(root.get("accountNonExpired"), dto.isAccountNonExpired());
        criteriaUpdate.set(root.get("accountNonLocked"), dto.isAccountNonLocked());
        criteriaUpdate.set(root.get("credentialsNonExpired"), dto.isCredentialsNonExpired());
        criteriaUpdate.set(root.get("enabled"), dto.isEnabled());

        if (nextUpdater != null) {
            nextUpdater.handle(criteriaUpdate);
        }
    }

}
