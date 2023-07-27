package com.msik404.karmaapp.user.handler;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;

import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserUpdateHandlerWithUserPrivilege implements Handler<CriteriaUpdate<User>> {

    private final Root<User> root;
    private final UserDtoWithUserPrivilege dto;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private Handler<CriteriaUpdate<User>> nextUpdater;

    @Override
    public void setNext(Handler<CriteriaUpdate<User>> updater) {
        this.nextUpdater = updater;
    }

    @Override
    public void handle(CriteriaUpdate<User> criteriaUpdate) {

        if (dto.getFirstName() != null) {
            criteriaUpdate.set(root.get("firstName"), dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            criteriaUpdate.set(root.get("lastName"), dto.getLastName());
        }
        if (dto.getEmail() != null) {
            criteriaUpdate.set(root.get("email"), dto.getEmail());
        }
        if (dto.getPassword() != null) {
            criteriaUpdate.set(root.get("password"), bCryptPasswordEncoder.encode(dto.getPassword()));
        }

        if (nextUpdater != null) {
            nextUpdater.handle(criteriaUpdate);
        }
    }

}
