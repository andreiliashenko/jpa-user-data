package com.anli.generalization.user.data;

import com.anli.generalization.user.data.access.UserGrantProvider;
import com.anli.generalization.user.data.entities.UserGrant;
import com.anli.generalization.user.data.factory.JpaProviderFactory;
import com.anli.generalization.user.data.utils.UserGrantHelper;
import java.math.BigInteger;
import java.util.Map;
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
import static com.anli.generalization.user.data.utils.ValueFactory.bi;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
public class UserGrantTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    private DataSource dataSource;

    private UserTransaction transaction;

    private UserGrantHelper helper;

    private UserGrantProvider provider;

    @Before
    public void setUp() {
        dataSource = getDataSource();
        transaction = getTransaction();
        helper = new UserGrantHelper(dataSource);
        provider = JpaProviderFactory.getInstance().getUserGrantProvider();
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldCreateWithData() throws Exception {
        transaction.begin();

        UserGrant grant = provider.create(bi(2000000000000000001L));
        grant.setDescription("created grant");

        transaction.commit();

        Map<String, Object> grantData = helper.readGrant(bi(2000000000000000001L));

        assertEquals(bi(2000000000000000001L), grantData.get("id"));
        assertEquals("created grant", grantData.get("description"));
    }

    @Test
    @InSequence(1)
    public void testCreate_shouldRollbackCreation() throws Exception {
        transaction.begin();

        UserGrant grant = provider.create(bi(2000000000000000002L));
        grant.setDescription("rollback grant");

        transaction.rollback();

        Map<String, Object> grantData = helper.readGrant(bi(2000000000000000002L));

        assertNull(grantData);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(2)
    public void testCreate_shouldCheckNullId() throws Exception {
        transaction.begin();

        provider.create(null);

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(3)
    public void testCreate_shouldCheckLesserId() throws Exception {
        transaction.begin();

        provider.create(bi(1000000000000000345L));

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(4)
    public void testCreate_shouldCheckGreaterId() throws Exception {
        transaction.begin();

        provider.create(bi(2000000000002000000L));

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(5)
    public void testCreate_shouldCheckExistingId() throws Exception {
        helper.createGrant(2000000000000000003L, "Initial grant");

        transaction.begin();

        provider.create(bi(2000000000000000003L));

        transaction.commit();
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(3)
    public void testCreation_shouldForbidNonTransactionalCall() {
        provider.create(bi(2000000000000000004L));
    }

    @Test
    @InSequence(4)
    public void testReading_shouldReadWithoutEdit() throws Exception {
        helper.createGrant(2000000000000000005L, "Read grant");

        transaction.begin();

        UserGrant grant = provider.getById(bi(2000000000000000005L));
        BigInteger id = grant.getId();
        String description = grant.getDescription();

        transaction.commit();

        assertEquals(bi(2000000000000000005L), id);
        assertEquals("Read grant", description);
    }

    @Test
    @InSequence(5)
    public void testReading_shouldReadNull() throws Exception {
        transaction.begin();

        UserGrant grant = provider.getById(bi(2000000000000000006L));

        transaction.commit();

        assertNull(grant);
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(6)
    public void testReading_shouldForbidNonTransactionalCall() {
        helper.createGrant(2000000000000000007L, "Created grant");
        provider.getById(bi(2000000000000000007L));
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(7)
    public void testReading_shouldCheckNullId() throws Exception {
        transaction.begin();

        provider.getById(null);

        transaction.commit();
    }

    @Test
    @InSequence(8)
    public void testUpdate_shouldUpdateData() throws Exception {
        helper.createGrant(2000000000000000008L, "Initial grant");

        transaction.begin();

        UserGrant grant = provider.getById(bi(2000000000000000008L));
        grant.setDescription("Updated grant");

        transaction.commit();

        Map<String, Object> grantData = helper.readGrant(bi(2000000000000000008L));

        assertEquals(bi(2000000000000000008L), grantData.get("id"));
        assertEquals("Updated grant", grantData.get("description"));
    }

    @Test
    @InSequence(9)
    public void testUpdate_shouldRollbackUpdate() throws Exception {
        helper.createGrant(2000000000000000009L, "Initial grant");

        transaction.begin();

        UserGrant grant = provider.getById(bi(2000000000000000009L));
        grant.setDescription("Updated grant");

        transaction.rollback();

        Map<String, Object> grantData = helper.readGrant(bi(2000000000000000009L));

        assertEquals(bi(2000000000000000009L), grantData.get("id"));
        assertEquals("Initial grant", grantData.get("description"));
    }

    @Test
    @InSequence(10)
    public void testRemove_shouldRemoveData() throws Exception {
        helper.createGrant(2000000000000000010L, "Removed grant");

        transaction.begin();

        UserGrant grant = provider.getById(bi(2000000000000000010L));
        provider.remove(grant);

        transaction.commit();

        Map<String, Object> data = helper.readGrant(bi(2000000000000000010L));

        assertNull(data);
    }

    @Test
    @InSequence(12)
    public void testRemove_shouldRollbackRemove() throws Exception {
        helper.createGrant(2000000000000000011L, "Rollback remove grant");

        transaction.begin();

        UserGrant grant = provider.getById(bi(2000000000000000011L));
        provider.remove(grant);

        transaction.rollback();

        Map<String, Object> grantData = helper.readGrant(bi(2000000000000000011L));

        assertEquals(bi(2000000000000000011L), grantData.get("id"));
        assertEquals("Rollback remove grant", grantData.get("description"));
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(13)
    public void testRemove_shouldForbidNonTransactionalCall() throws Exception {
        helper.createGrant(2000000000000000012L, "Non-removed grant");

        transaction.begin();

        UserGrant grant = provider.getById(bi(2000000000000000012L));

        transaction.commit();

        provider.remove(grant);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(14)
    public void testRemove_shouldCheckNull() throws Exception {
        transaction.begin();

        provider.remove(null);

        transaction.commit();
    }
}
