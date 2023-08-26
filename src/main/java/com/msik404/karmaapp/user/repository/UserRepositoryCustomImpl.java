package com.msik404.karmaapp.user.repository;

import com.msik404.karmaapp.constraintExceptions.ConstraintExceptionsHandler;
import com.msik404.karmaapp.constraintExceptions.exception.DuplicateEmailException;
import com.msik404.karmaapp.constraintExceptions.exception.DuplicateUsernameException;
import com.msik404.karmaapp.constraintExceptions.exception.UndefinedConstraintException;
import com.msik404.karmaapp.constraintExceptions.strategy.ConstraintViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaapp.constraintExceptions.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserUpdateRequestWithUserPrivilege;
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
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserCriteriaUpdater userCriteriaUpdater;
    private final ConstraintExceptionsHandler constraintExceptionsHandler;
    private final ConstraintViolationExceptionErrorMessageExtractionStrategy extractionStrategy;
    private final RoundBraceErrorMassageParseStrategy parseStrategy;

    public UserRepositoryCustomImpl(
            EntityManager entityManager,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            UserCriteriaUpdater userCriteriaUpdater,
            ConstraintExceptionsHandler constraintExceptionsHandler,
            ConstraintViolationExceptionErrorMessageExtractionStrategy extractionStrategy,
            RoundBraceErrorMassageParseStrategy parseStrategy) {

        this.entityManager = entityManager;
        this.cb = entityManager.getCriteriaBuilder();
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userCriteriaUpdater = userCriteriaUpdater;
        this.constraintExceptionsHandler = constraintExceptionsHandler;
        this.extractionStrategy = extractionStrategy;
        this.parseStrategy = parseStrategy;
    }

    @Override
    public int updateNonNull(long userId, @NonNull UserUpdateRequestWithUserPrivilege dto)
            throws DuplicateEmailException, DuplicateUsernameException, UndefinedConstraintException {

        var criteriaUpdate = cb.createCriteriaUpdate(User.class);
        var root = criteriaUpdate.from(User.class);

        userCriteriaUpdater.updateUserCriteria(dto, bCryptPasswordEncoder, root, criteriaUpdate);
        if (dto instanceof UserUpdateRequestWithAdminPrivilege) {
            userCriteriaUpdater.updateAdminCriteria((UserUpdateRequestWithAdminPrivilege) dto, root, criteriaUpdate);
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
