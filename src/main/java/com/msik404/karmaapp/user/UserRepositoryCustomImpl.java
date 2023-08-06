package com.msik404.karmaapp.user;

import com.msik404.karmaapp.constraintExceptions.ConstraintExceptionsHandler;
import com.msik404.karmaapp.constraintExceptions.ConstraintViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaapp.constraintExceptions.DuplicateEmailException;
import com.msik404.karmaapp.constraintExceptions.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaapp.user.dto.UserDtoWithAdminPrivilege;
import com.msik404.karmaapp.user.dto.UserDtoWithUserPrivilege;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
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
    public void updateNonNull(Long userId, UserDtoWithUserPrivilege dto)
            throws DuplicateEmailException, UserNotFoundException {

        CriteriaUpdate<User> criteriaUpdate = cb.createCriteriaUpdate(User.class);
        Root<User> root = criteriaUpdate.from(User.class);

        userCriteriaUpdater.updateUserCriteria(dto, bCryptPasswordEncoder, root, criteriaUpdate);
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

    @Override
    @Transactional(rollbackOn = DuplicateEmailException.class)
    public void updateNonNull(Long userId, UserDtoWithAdminPrivilege dto)
            throws DuplicateEmailException, UserNotFoundException {

        CriteriaUpdate<User> criteriaUpdate = cb.createCriteriaUpdate(User.class);
        Root<User> root = criteriaUpdate.from(User.class);

        userCriteriaUpdater.updateUserCriteria(dto, bCryptPasswordEncoder, root, criteriaUpdate);
        userCriteriaUpdater.updateAdminCriteria(dto, root, criteriaUpdate);
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
