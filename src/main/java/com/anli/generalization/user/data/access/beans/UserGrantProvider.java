package com.anli.generalization.user.data.access.beans;

import com.anli.generalization.user.data.entities.UserGrant;
import com.anli.generalization.user.data.entities.jpa.JpaUserGrant;
import com.anli.jta.aop.annotation.Transactional;
import java.math.BigInteger;
import javax.inject.Singleton;

import static com.anli.generalization.user.data.entities.UserGrant.MAX_GRANT_ID;
import static com.anli.generalization.user.data.entities.UserGrant.MIN_GRANT_ID;
import static com.anli.jta.aop.propagation.TransactionPropagation.MANDATORY;
import static com.google.common.base.Preconditions.checkArgument;

@Singleton
public class UserGrantProvider extends GenericProvider<JpaUserGrant>
        implements com.anli.generalization.user.data.access.UserGrantProvider {

    @Override
    @Transactional(MANDATORY)
    public UserGrant create(BigInteger id) {
        checkArgument(id != null, "User Grant id can not be null");
        checkArgument(MIN_GRANT_ID.compareTo(id) <= 0,
                "User Grant id is less than minimum " + MIN_GRANT_ID);
        checkArgument(MAX_GRANT_ID.compareTo(id) >= 0,
                "User Grant id is greater than maximum " + MIN_GRANT_ID);
        checkArgument(getEntityById(id) == null, "User Grant with id = " + id + " already exists");
        JpaUserGrant grant = getEntityInstance();
        grant.setId(id);
        getManager().persist(grant);
        return grant;
    }

    @Override
    @Transactional(MANDATORY)
    public UserGrant getById(BigInteger id) {
        return getEntityById(id);
    }

    @Override
    @Transactional(MANDATORY)
    public void remove(UserGrant grant) {
        removeEntity((JpaUserGrant) grant);
    }

    @Override
    protected JpaUserGrant getEntityInstance() {
        return new JpaUserGrant();
    }

    @Override
    protected Class<JpaUserGrant> getEntityClass() {
        return JpaUserGrant.class;
    }
}
