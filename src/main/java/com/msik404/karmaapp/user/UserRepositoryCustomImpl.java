package com.msik404.karmaapp.user;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import com.msik404.karmaapp.uniqueConstraintExceptions.DuplicateEmailException;
import com.msik404.karmaapp.user.dto.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;

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

    private final UserCriteriaUpdater userCriteriaUpdater;

    public UserRepositoryCustomImpl(
            EntityManager entityManager,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            UserCriteriaUpdater userCriteriaUpdater) {

        this.entityManager = entityManager;
        this.cb = entityManager.getCriteriaBuilder();
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userCriteriaUpdater = userCriteriaUpdater;
    }

    // TODO: chain of responsibility here doesn't make really sense
    @Override
    @Transactional(rollbackOn = DuplicateEmailException.class)
    public void updateNonNull(Long userId, UserDtoWithUserPrivilege dto)
            throws DuplicateEmailException {

        CriteriaUpdate<User> criteriaUpdate = cb.createCriteriaUpdate(User.class);
        Root<User> root = criteriaUpdate.from(User.class);

        userCriteriaUpdater.updateUserCriteria(dto, bCryptPasswordEncoder, root, criteriaUpdate);
        criteriaUpdate.where(cb.equal(root.get("id"), userId));

        try {
            entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (ConstraintViolationException ex) {
            // TODO: Handle username unique constraint exception
            throw new DuplicateEmailException();
        }
    }

    @Override
    @Transactional(rollbackOn = DuplicateEmailException.class)
    public void updateNonNull(Long userId, UserDtoWithAdminPrivilege dto)
            throws DuplicateEmailException {

        CriteriaUpdate<User> criteriaUpdate = cb.createCriteriaUpdate(User.class);
        Root<User> root = criteriaUpdate.from(User.class);

        userCriteriaUpdater.updateUserCriteria(dto, bCryptPasswordEncoder, root, criteriaUpdate);
        userCriteriaUpdater.updateAdminCriteria(dto, root, criteriaUpdate);
        criteriaUpdate.where(cb.equal(root.get("id"), userId));

        try {
            entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (ConstraintViolationException ex) {
            // TODO: Handle username unique constraint exception
            throw new DuplicateEmailException();
        }
    }

}
