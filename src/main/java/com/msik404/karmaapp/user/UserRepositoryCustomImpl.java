package com.msik404.karmaapp.user;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import com.msik404.karmaapp.auth.DuplicateEmailException;
import com.msik404.karmaapp.user.dto.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;
import com.msik404.karmaapp.user.handler.UserUpdateHandlerWithAdminPrivilege;
import com.msik404.karmaapp.user.handler.UserUpdateHandlerWithUserPrivilege;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final EntityManager entityManager;
    private final CriteriaBuilder cb;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserRepositoryCustomImpl(EntityManager entityManager,
            BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.entityManager = entityManager;
        this.cb = entityManager.getCriteriaBuilder();
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    @Transactional(rollbackOn = DuplicateEmailException.class)
    public void updateNonNull(Long userId, UserDtoWithUserPrivilege dto)
            throws DuplicateEmailException {

        CriteriaUpdate<User> criteriaUpdate = cb.createCriteriaUpdate(User.class);
        Root<User> root = criteriaUpdate.from(User.class);

        var handler = new UserUpdateHandlerWithUserPrivilege(root, dto, bCryptPasswordEncoder);
        handler.handle(criteriaUpdate);

        criteriaUpdate.where(cb.equal(root.get("id"), userId));

        try {
            entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (ConstraintViolationException ex) {
            throw new DuplicateEmailException();
        }
    }

    @Override
    @Transactional(rollbackOn = DuplicateEmailException.class)
    public void updateNonNull(Long userId, UserDtoWithAdminPrivilege dto)
            throws DuplicateEmailException {

        CriteriaUpdate<User> criteriaUpdate = cb.createCriteriaUpdate(User.class);
        Root<User> root = criteriaUpdate.from(User.class);

        var handler = new UserUpdateHandlerWithUserPrivilege(root, dto, bCryptPasswordEncoder);
        handler.setNext(new UserUpdateHandlerWithAdminPrivilege(root, dto));
        handler.handle(criteriaUpdate);

        criteriaUpdate.where(cb.equal(root.get("id"), userId));

        try {
            entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (ConstraintViolationException ex) {
            throw new DuplicateEmailException();
        }
    }

}
