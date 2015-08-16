package com.anli.generalization.user.data.access.beans;

import java.math.BigInteger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class GenericProvider<E> {

    @PersistenceContext(unitName = "generalization")
    protected EntityManager manager;

    protected EntityManager getManager() {
        return manager;
    }

    protected abstract Class<E> getEntityClass();

    protected abstract E getEntityInstance();

    protected E createEntity() {
        E entity = getEntityInstance();
        getManager().persist(entity);
        return entity;
    }

    protected E getEntityById(BigInteger id) {
        return getManager().find(getEntityClass(), id);
    }

    protected void removeEntity(E entity) {
        getManager().remove(entity);
    }
}
