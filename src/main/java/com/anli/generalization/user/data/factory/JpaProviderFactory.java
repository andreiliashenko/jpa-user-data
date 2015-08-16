package com.anli.generalization.user.data.factory;

import com.anli.generalization.user.data.ProviderFactory;
import com.anli.generalization.user.data.access.UserGrantProvider;
import com.anli.generalization.user.data.access.UserGroupProvider;
import com.anli.generalization.user.data.access.UserProvider;
import com.anli.jpa.jta.guice.JtaPersistenceContextTypeListener;
import com.anli.jta.aop.JtaTransactionInterceptor;
import com.anli.jta.aop.annotation.Transactional;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

public class JpaProviderFactory implements ProviderFactory {

    private static final String UNIT_NAME = "generalization";
    private static final String TRANSACTION_MANAGER_JNDI_NAME = "java:/TransactionManager";

    private static final JpaProviderFactory INSTANCE = new JpaProviderFactory();

    public static JpaProviderFactory getInstance() {
        return INSTANCE;
    }

    private final Injector injector;

    private JpaProviderFactory() {
        injector = createInjector(new UserDataModule());
    }

    @Override
    public UserGrantProvider getUserGrantProvider() {
        return injector.getInstance(UserGrantProvider.class);
    }

    @Override
    public UserGroupProvider getUserGroupProvider() {
        return injector.getInstance(UserGroupProvider.class);
    }

    @Override
    public UserProvider getUserProvider() {
        return injector.getInstance(UserProvider.class);
    }

    private static class UserDataModule extends AbstractModule {

        @Override
        protected void configure() {
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(UNIT_NAME);
            bindListener(any(), new JtaPersistenceContextTypeListener(entityManagerFactory));
            bindInterceptor(any(), annotatedWith(Transactional.class),
                    new JtaTransactionInterceptor(TRANSACTION_MANAGER_JNDI_NAME));
            bind(UserGrantProvider.class)
                    .to(com.anli.generalization.user.data.access.beans.UserGrantProvider.class);
            bind(UserProvider.class)
                    .to(com.anli.generalization.user.data.access.beans.UserProvider.class);
            bind(UserGroupProvider.class)
                    .to(com.anli.generalization.user.data.access.beans.UserGroupProvider.class);
        }
    }
}
