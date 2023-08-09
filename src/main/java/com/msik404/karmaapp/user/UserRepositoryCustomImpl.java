package com.msik404.karmaapp.user;

import com.msik404.karmaapp.constraintExceptions.*;
import com.msik404.karmaapp.user.dto.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;
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
    @Transactional(rollbackOn = DuplicateEmailException.class)
    public void updateNonNull(long userId, UserDtoWithUserPrivilege dto)
            throws DuplicateEmailException, UndefinedConstraintException, UserNotFoundException {

        var criteriaUpdate = cb.createCriteriaUpdate(User.class);
        var root = criteriaUpdate.from(User.class);

        userCriteriaUpdater.updateUserCriteria(dto, bCryptPasswordEncoder, root, criteriaUpdate);
        if (dto instanceof UserDtoWithAdminPrivilege) {
            userCriteriaUpdater.updateAdminCriteria((UserDtoWithAdminPrivilege) dto, root, criteriaUpdate);
        }
        criteriaUpdate.where(cb.equal(root.get("id"), userId));

        try {
            int rowsAffected = entityManager.createQuery(criteriaUpdate).executeUpdate();
            if (rowsAffected == 0) {
                throw new UserNotFoundException();
            }
        } catch (ConstraintViolationException ex) {
            constraintExceptionsHandler.handle(ex, extractionStrategy, parseStrategy);
        }
    }

}
