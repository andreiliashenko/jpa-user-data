package com.anli.generalization.user.data.utils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

public class JndiUtils {

    public static DataSource getDataSource() {
        try {
            return InitialContext.doLookup("java:/jdbc/integration_testing");
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static UserTransaction getTransaction() {
        try {
            return InitialContext.doLookup("java:jboss/UserTransaction");
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
