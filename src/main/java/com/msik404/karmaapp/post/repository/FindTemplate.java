package com.msik404.karmaapp.post.repository;

import java.util.List;

import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

public abstract class FindTemplate<T> {

    protected EntityManager entityManager;
    protected CriteriaBuilder cb;
    protected CriteriaQuery<T> criteriaQuery;

    public FindTemplate(EntityManager entityManager, CriteriaBuilder cb, Class<T> entityClass) {

        this.entityManager = entityManager;
        this.cb = cb;
        this.criteriaQuery = cb.createQuery(entityClass);
    }

    abstract void selectMethod(CriteriaBuilder cb);

    abstract void whereMethod(CriteriaBuilder cb, CriteriaQuery<T> criteriaQuery);

    abstract void orderMethod(CriteriaBuilder cb, CriteriaQuery<T> criteriaQuery);

    protected List<T> returnMethod(EntityManager entityManager, CriteriaQuery<T> criteriaQuery, int size) {

        final int offset = 0;
        try {
            return entityManager.createQuery(criteriaQuery)
                    .setFirstResult(offset)
                    .setMaxResults(size)
                    .getResultList();
        } catch (RuntimeException ex) {
            throw new InternalServerErrorException("Could not get posts from database for some reason.");
        }
    }

    List<T> execute(int size) throws InternalServerErrorException {

        selectMethod(cb);

        whereMethod(cb, criteriaQuery);

        orderMethod(cb, criteriaQuery);

        return returnMethod(entityManager, criteriaQuery, size);
    }

}
