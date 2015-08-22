package com.anli.generalization.user.data;

import com.anli.generalization.user.data.access.UserProvider;
import com.anli.generalization.user.data.entities.User;
import com.anli.generalization.user.data.factory.JpaProviderFactory;
import com.anli.generalization.user.data.utils.UserHelper;
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

import static com.anli.generalization.user.data.encryption.PasswordHasher.hash;
import static com.anli.generalization.user.data.utils.CommonDeployment.getDeployment;
import static com.anli.generalization.user.data.utils.JndiUtils.getDataSource;
import static com.anli.generalization.user.data.utils.JndiUtils.getTransaction;
import static com.anli.generalization.user.data.utils.ValueFactory.bi;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class UserTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    private DataSource dataSource;

    private UserTransaction transaction;

    private UserHelper helper;

    private UserProvider provider;

    @Before
    public void setUp() {
        dataSource = getDataSource();
        transaction = getTransaction();
        helper = new UserHelper(dataSource);
        provider = JpaProviderFactory.getInstance().getUserProvider();
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldCreateWithData() throws Exception {
        transaction.begin();

        User user = provider.create();
        BigInteger id = user.getId();
        user.setName("Created user");
        user.setLogin("crus");
        user.setPassword("cruspass");

        transaction.commit();

        Map<String, Object> userData = helper.readUser(id);
        String passwordHash = hash("cruspass");
        assertEquals(128, passwordHash.length());
        assertEquals(id, userData.get("id"));
        assertEquals("Created user", userData.get("name"));
        assertEquals("crus", userData.get("login"));
        assertEquals(passwordHash, userData.get("passwordHash"));
    }

    @Test
    @InSequence(1)
    public void testCreate_shouldRollbackCreation() throws Exception {
        transaction.begin();

        User user = provider.create();
        BigInteger id = user.getId();
        user.setName("Rollback user");
        user.setLogin("rollus");
        user.setPassword("rollpass");

        transaction.rollback();

        assertNotNull(id);
        Map<String, Object> grantData = helper.readUser(id);

        assertNull(grantData);
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(2)
    public void testCreation_shouldForbidNonTransactionalCall() {
        provider.create();
    }

    @Test
    @InSequence(3)
    public void testReading_shouldReadWithoutEdit() throws Exception {
        helper.createUser(2001, "Read User", "ruser", hash("readpass"));

        transaction.begin();

        User user = provider.getById(bi(2001));
        BigInteger id = user.getId();
        String name = user.getName();
        String login = user.getLogin();
        boolean correctCheck = user.checkPassword("readpass");
        boolean incorrectCheck = user.checkPassword("redpass");

        transaction.commit();

        assertEquals(bi(2001), id);
        assertEquals("Read User", name);
        assertEquals("ruser", login);
        assertTrue(correctCheck);
        assertFalse(incorrectCheck);
    }

    @Test
    @InSequence(4)
    public void testReading_shouldReadNull() throws Exception {
        transaction.begin();

        User user = provider.getById(bi(2016));

        transaction.commit();

        assertNull(user);
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(5)
    public void testReading_shouldForbidNonTransactionalCall() {
        helper.createUser(2002, "NonTx User", "nontxuser", hash("nontxpass"));
        provider.getById(bi(2002));
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(6)
    public void testReading_shouldCheckNullId() throws Exception {
        transaction.begin();

        provider.getById(null);

        transaction.commit();
    }

    @Test
    @InSequence(7)
    public void testGetByLogin_shouldFindUnique() throws Exception {
        helper.createUser(2003, "Found", "found", hash("foundpass"));
        helper.createUser(2004, "Not Found", "notfound", hash("notfoundpass"));

        transaction.begin();

        User user = provider.getByLogin("found");

        assertNotNull(user);

        BigInteger id = user.getId();
        String name = user.getName();
        String login = user.getLogin();
        boolean passwordCheck = user.checkPassword("foundpass");

        transaction.commit();

        assertEquals(bi(2003), id);
        assertEquals("Found", name);
        assertEquals("found", login);
        assertTrue(passwordCheck);
    }

    @Test
    @InSequence(8)
    public void testGetByLogin_shouldNotFindAnything() throws Exception {
        helper.createUser(2005, "Not Found 1", "notfound1", hash("2005pass"));
        helper.createUser(2006, "Not Found 2", "notfound2", hash("2006pass"));

        transaction.begin();

        User user = provider.getByLogin("anotherfound");

        assertNull(user);

        transaction.commit();
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(9)
    public void testGetByLogin_shouldCheckNonUniqueness() throws Exception {
        helper.createUser(2007, "Found 1", "nonunique", hash("2007pass"));
        helper.createUser(2008, "Found 2", "nonunique", hash("2008pass"));

        transaction.begin();

        provider.getByLogin("nonunique");

        transaction.commit();
    }

    @Test
    @InSequence(10)
    public void testLoginExists_shouldReturnTrue() throws Exception {
        helper.createUser(2009, "Existing User", "existing", hash("2009pass"));
        helper.createUser(2010, "Dubbing User", "existing", hash("2010pass"));

        transaction.begin();

        boolean exists = provider.loginExists("existing");

        transaction.commit();

        assertTrue(exists);
    }

    @Test
    @InSequence(11)
    public void testLoginExists_shouldReturnFalse() throws Exception {

        transaction.begin();

        boolean exists = provider.loginExists("nonexisting");

        transaction.commit();

        assertFalse(exists);
    }

    @Test
    @InSequence(12)
    public void testUpdate_shouldUpdateData() throws Exception {
        helper.createUser(2011, "Initial User", "inuser", hash("inpass"));

        transaction.begin();

        User user = provider.getById(bi(2011));
        user.setName("Updated User");
        user.setLogin("upuser");
        user.setPassword("uppass");

        transaction.commit();

        Map<String, Object> userData = helper.readUser(bi(2011));

        assertEquals(bi(2011), userData.get("id"));
        assertEquals("Updated User", userData.get("name"));
        assertEquals("upuser", userData.get("login"));
        assertEquals(hash("uppass"), userData.get("passwordHash"));
    }

    @Test
    @InSequence(13)
    public void testUpdate_shouldRollbackUpdate() throws Exception {
        helper.createUser(2012, "Initial User", "inuser", hash("inpass"));

        transaction.begin();

        User user = provider.getById(bi(2012));
        user.setName("Updated User");
        user.setLogin("upuser");
        user.setPassword("uppass");

        transaction.rollback();

        Map<String, Object> userData = helper.readUser(bi(2012));

        assertEquals(bi(2012), userData.get("id"));
        assertEquals("Initial User", userData.get("name"));
        assertEquals("inuser", userData.get("login"));
        assertEquals(hash("inpass"), userData.get("passwordHash"));
    }

    @Test
    @InSequence(14)
    public void testRemove_shouldRemoveData() throws Exception {
        helper.createUser(2013, "Removed User", "remuser", hash("rempass"));

        transaction.begin();

        User user = provider.getById(bi(2013));
        provider.remove(user);

        transaction.commit();

        Map<String, Object> data = helper.readUser(bi(2013));

        assertNull(data);
    }

    @Test
    @InSequence(15)
    public void testRemove_shouldRollbackRemove() throws Exception {
        helper.createUser(2014, "RollRem User", "rollremuser", hash("rollrempass"));

        transaction.begin();

        User user = provider.getById(bi(2014));
        provider.remove(user);

        transaction.rollback();

        Map<String, Object> userData = helper.readUser(bi(2014));

        assertEquals(bi(2014), userData.get("id"));
        assertEquals("RollRem User", userData.get("name"));
        assertEquals("rollremuser", userData.get("login"));
        assertEquals(hash("rollrempass"), userData.get("passwordHash"));
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(16)
    public void testRemove_shouldForbidNonTransactionalCall() throws Exception {
        helper.createUser(2015, "NonRemUser", "nonremuser", "nonrempass");

        transaction.begin();

        User user = provider.getById(bi(2015));

        transaction.commit();

        provider.remove(user);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(17)
    public void testRemove_shouldCheckNull() throws Exception {
        transaction.begin();

        provider.remove(null);

        transaction.commit();
    }
}
