package com.anli.generalization.user.data.access.beans;

import com.anli.generalization.user.data.entities.User;
import com.anli.generalization.user.data.entities.jpa.JpaUser;
import com.anli.jta.aop.annotation.Transactional;
import java.math.BigInteger;
import java.util.List;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import static com.anli.jta.aop.propagation.TransactionPropagation.MANDATORY;
import static com.google.common.collect.Iterables.getFirst;

@Singleton
public class UserProvider extends GenericProvider<JpaUser>
        implements com.anli.generalization.user.data.access.UserProvider {

    @Override
    protected Class<JpaUser> getEntityClass() {
        return JpaUser.class;
    }

    @Override
    protected JpaUser getEntityInstance() {
        return new JpaUser();
    }

    @Override
    @Transactional(MANDATORY)
    public User create() {
        return createEntity();
    }

    @Override
    @Transactional(MANDATORY)
    public User getById(BigInteger id) {
        return getEntityById(id);
    }

    @Override
    @Transactional(MANDATORY)
    public User getByLogin(String login) {
        List<JpaUser> users = getUsersByLogin(login);
        if (users.size() > 1) {
            throw new IllegalStateException("There are " + users.size() + " users with login " + login);
        }
        return getFirst(users, null);
    }

    protected List<JpaUser> getUsersByLogin(String login) {
        CriteriaBuilder criteriaBuilder = getManager().getCriteriaBuilder();
        CriteriaQuery<JpaUser> query = criteriaBuilder.createQuery(getEntityClass());
        Root<JpaUser> root = query.from(getEntityClass());
        if (login == null) {
            query.where(criteriaBuilder.isNull(root.get("login")));
        } else {
            query.where(criteriaBuilder.equal(root.get("login"), login));
        }
        query.select(root);
        return getManager().createQuery(query).getResultList();
    }

    @Override
    @Transactional(MANDATORY)
    public boolean loginExists(String login) {
        return !getUsersByLogin(login).isEmpty();
    }

    @Override
    @Transactional(MANDATORY)
    public void remove(User user) {
        removeEntity((JpaUser) user);
    }
}
