package com.msik404.karmaappmonolith.user.repository;

import com.msik404.karmaappmonolith.exception.constraint.ConstraintExceptionsHandler;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaappmonolith.exception.constraint.strategy.ConstraintViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaappmonolith.exception.constraint.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaappmonolith.user.User;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappmonolith.user.exception.NoFieldSetException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final EntityManager entityManager;
    private final CriteriaBuilder cb;
    private final UserCriteriaUpdater userCriteriaUpdater;
    private final ConstraintExceptionsHandler constraintExceptionsHandler;
    private final ConstraintViolationExceptionErrorMessageExtractionStrategy extractionStrategy;
    private final RoundBraceErrorMassageParseStrategy parseStrategy;

    public UserRepositoryCustomImpl(
            EntityManager entityManager,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            ConstraintExceptionsHandler constraintExceptionsHandler) {

        this.entityManager = entityManager;
        this.cb = entityManager.getCriteriaBuilder();
        this.constraintExceptionsHandler = constraintExceptionsHandler;

        this.extractionStrategy = new ConstraintViolationExceptionErrorMessageExtractionStrategy();
        this.parseStrategy = new RoundBraceErrorMassageParseStrategy();
        this.userCriteriaUpdater = new UserCriteriaUpdater(bCryptPasswordEncoder);
    }

    @Override
    public int updateNonNull(long userId, @NonNull UserUpdateRequestWithUserPrivilege dto)
            throws NoFieldSetException, DuplicateEmailException, DuplicateUsernameException,
            DuplicateUnexpectedFieldException {

        var criteriaUpdate = cb.createCriteriaUpdate(User.class);
        var root = criteriaUpdate.from(User.class);

        boolean someFieldSet = userCriteriaUpdater.updateUserCriteria(dto, root, criteriaUpdate);
        if (!someFieldSet) {
            throw new NoFieldSetException();
        }
        criteriaUpdate.where(cb.equal(root.get("id"), userId));

        int rowsAffected = 0;
        try {
            rowsAffected = entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (ConstraintViolationException ex) {
            constraintExceptionsHandler.handle(ex, extractionStrategy, parseStrategy);
        }
        return rowsAffected;
    }

    @Override
    public int updateNonNull(long userId, @NonNull UserUpdateRequestWithAdminPrivilege dto)
            throws NoFieldSetException, DuplicateEmailException, DuplicateUsernameException,
            DuplicateUnexpectedFieldException {

        var criteriaUpdate = cb.createCriteriaUpdate(User.class);
        var root = criteriaUpdate.from(User.class);

        boolean someFieldSet = userCriteriaUpdater.updateAdminCriteria(dto, root, criteriaUpdate);
        if (!someFieldSet) {
            throw new NoFieldSetException();
        }
        criteriaUpdate.where(cb.equal(root.get("id"), userId));

        int rowsAffected = 0;
        try {
            rowsAffected = entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (ConstraintViolationException ex) {
            constraintExceptionsHandler.handle(ex, extractionStrategy, parseStrategy);
        }
        return rowsAffected;
    }

}
