package com.anli.generalization.user.data;

import com.anli.generalization.user.data.access.UserGrantProvider;
import com.anli.generalization.user.data.access.UserGroupProvider;
import com.anli.generalization.user.data.access.UserProvider;
import com.anli.generalization.user.data.factory.JpaProviderFactory;
import com.anli.generalization.user.data.utils.UserGrantHelper;
import com.anli.generalization.user.data.utils.UserGroupHelper;
import com.anli.generalization.user.data.utils.UserHelper;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.anli.generalization.user.data.utils.CommonDeployment.getDeployment;
import static com.anli.generalization.user.data.utils.JndiUtils.getDataSource;
import static com.anli.generalization.user.data.utils.JndiUtils.getTransaction;

@RunWith(Arquillian.class)
public class UserDataRelationsTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    private DataSource dataSource;

    private UserTransaction transaction;

    private UserGrantHelper grantHelper;
    private UserGroupHelper groupHelper;
    private UserHelper userHelper;
    private UserGrantProvider grantProvider;
    private UserGroupProvider groupProvider;
    private UserProvider userProvider;

    @Before
    public void setUp() {
        dataSource = getDataSource();
        transaction = getTransaction();
        grantHelper = new UserGrantHelper(dataSource);
        groupHelper = new UserGroupHelper(dataSource);
        userHelper = new UserHelper(dataSource);
        grantProvider = JpaProviderFactory.getInstance().getUserGrantProvider();
        groupProvider = JpaProviderFactory.getInstance().getUserGroupProvider();
        userProvider = JpaProviderFactory.getInstance().getUserProvider();
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldLinkEntities() throws Exception {

    }

    @Test
    @InSequence(1)
    public void testCreate_shouldRollbackLinkage() throws Exception {

    }

    @Test
    @InSequence(2)
    public void testReading_shouldReadRelationStructure() throws Exception {

    }

    @Test
    @InSequence(3)
    public void testUpdate_shouldUpdateRelations() throws Exception {

    }

    @Test
    @InSequence(4)
    public void testUpdate_shouldRollbackLinkageUpdate() throws Exception {

    }

    @Test
    @InSequence(5)
    public void testRemove_shouldRemoveLinkage() throws Exception {

    }

    @Test
    @InSequence(6)
    public void testRemove_shouldRollbackLinkageRemoval() throws Exception {

    }
}
