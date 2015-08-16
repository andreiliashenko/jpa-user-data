package com.anli.generalization.user.data.access.beans;

import com.anli.generalization.user.data.entities.UserGroup;
import com.anli.generalization.user.data.entities.jpa.JpaUserGroup;
import com.anli.jta.aop.annotation.Transactional;
import java.math.BigInteger;
import javax.inject.Singleton;

import static com.anli.jta.aop.propagation.TransactionPropagation.MANDATORY;

@Singleton
public class UserGroupProvider extends GenericProvider<JpaUserGroup>
        implements com.anli.generalization.user.data.access.UserGroupProvider {

    @Override
    protected Class<JpaUserGroup> getEntityClass() {
        return JpaUserGroup.class;
    }

    @Override
    protected JpaUserGroup getEntityInstance() {
        return new JpaUserGroup();
    }

    @Override
    @Transactional(MANDATORY)
    public UserGroup create() {
        return createEntity();
    }

    @Override
    @Transactional(MANDATORY)
    public UserGroup getById(BigInteger id) {
        return getEntityById(id);
    }

    @Override
    @Transactional(MANDATORY)
    public void remove(UserGroup group) {
        removeEntity((JpaUserGroup) group);
    }
}
