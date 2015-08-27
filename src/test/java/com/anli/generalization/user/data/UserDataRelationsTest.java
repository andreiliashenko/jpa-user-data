package com.anli.generalization.user.data;

import com.anli.generalization.user.data.access.UserGrantProvider;
import com.anli.generalization.user.data.access.UserGroupProvider;
import com.anli.generalization.user.data.access.UserProvider;
import com.anli.generalization.user.data.entities.User;
import com.anli.generalization.user.data.entities.UserGrant;
import com.anli.generalization.user.data.entities.UserGroup;
import com.anli.generalization.user.data.factory.JpaProviderFactory;
import com.anli.generalization.user.data.utils.UserGrantHelper;
import com.anli.generalization.user.data.utils.UserGroupHelper;
import com.anli.generalization.user.data.utils.UserHelper;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
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
import static com.anli.generalization.user.data.utils.ValueFactory.sequenceGrants;
import static com.anli.generalization.user.data.utils.ValueFactory.sequenceGroups;
import static com.anli.generalization.user.data.utils.ValueFactory.sequenceUsers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
    public void testReading_shouldReadGroupsToGrantsRelations() throws Exception {
        grantHelper.createGrant(2201, "Grant A");
        grantHelper.createGrant(2202, "Grant B");
        grantHelper.createGrant(2203, "Grant C");
        groupHelper.createGroup(2204, "Group A", null);
        groupHelper.createGroup(2205, "Group B", null);
        groupHelper.createGroup(2206, "Group C", null);
        grantHelper.linkGrantsToGroup(2204, 2201, 2202);
        grantHelper.linkGrantsToGroup(2205, 2202);
        grantHelper.linkGrantsToGroup(2206, 2201, 2202, 2203);

        transaction.begin();

        UserGrant grantA = grantProvider.getById(bi(2201));
        UserGrant grantB = grantProvider.getById(bi(2202));
        UserGrant grantC = grantProvider.getById(bi(2203));
        UserGroup groupA = groupProvider.getById(bi(2204));
        UserGroup groupB = groupProvider.getById(bi(2205));
        UserGroup groupC = groupProvider.getById(bi(2206));

        Collection<UserGrant> groupAGrants = groupA.getGrants();
        Collection<UserGrant> groupBGrants = groupB.getGrants();
        Collection<UserGrant> groupCGrants = groupC.getGrants();

        assertEquals(2, groupAGrants.size());
        assertEquals(1, groupBGrants.size());
        assertEquals(3, groupCGrants.size());

        Iterator<UserGrant> aIter = sequenceGrants(groupAGrants, bi(2201), bi(2202)).iterator();
        UserGrant grantAa = aIter.next();
        UserGrant grantAb = aIter.next();
        UserGrant grantBb = groupBGrants.iterator().next();
        Iterator<UserGrant> cIter = sequenceGrants(groupCGrants, bi(2201),
                bi(2202), bi(2203)).iterator();
        UserGrant grantCa = cIter.next();
        UserGrant grantCb = cIter.next();
        UserGrant grantCc = cIter.next();

        assertSame(grantA, grantAa);
        assertSame(grantB, grantAb);
        assertSame(grantB, grantBb);
        assertSame(grantA, grantCa);
        assertSame(grantB, grantCb);
        assertSame(grantC, grantCc);
    }

    @Test
    @InSequence(1)
    public void testReading_shouldCollectHierarchicalGrants() throws Exception {
        grantHelper.createGrant(2207, "Grant A");
        grantHelper.createGrant(2208, "Grant B");
        grantHelper.createGrant(2209, "Grant C");
        groupHelper.createGroup(2210, "Group A", null);
        groupHelper.createGroup(2211, "Group B", bi(2210));
        grantHelper.linkGrantsToGroup(2210, 2207, 2208);
        grantHelper.linkGrantsToGroup(2211, 2208, 2209);

        transaction.begin();

        UserGrant grantA = grantProvider.getById(bi(2207));
        UserGrant grantB = grantProvider.getById(bi(2208));
        UserGrant grantC = grantProvider.getById(bi(2209));
        UserGroup groupB = groupProvider.getById(bi(2211));

        Collection<UserGrant> explicitGrants = groupB.getGrants(false);
        Collection<UserGrant> allGrants = groupB.getGrants(true);

        assertEquals(2, explicitGrants.size());
        assertEquals(3, allGrants.size());

        Iterator<UserGrant> exIter = sequenceGrants(explicitGrants, bi(2208),
                bi(2209)).iterator();
        UserGrant grantExB = exIter.next();
        UserGrant grantExC = exIter.next();
        Iterator<UserGrant> allIter = sequenceGrants(allGrants, bi(2207),
                bi(2208), bi(2209)).iterator();
        UserGrant grantAllA = allIter.next();
        UserGrant grantAllB = allIter.next();
        UserGrant grantAllC = allIter.next();

        assertSame(grantB, grantExB);
        assertSame(grantC, grantExC);
        assertSame(grantA, grantAllA);
        assertSame(grantB, grantAllB);
        assertSame(grantC, grantAllC);
    }

    @Test
    @InSequence(2)
    public void testReading_shouldReadGroupsToUsersRelations() throws Exception {
        userHelper.createUser(2212, "User A", "", "");
        userHelper.createUser(2213, "User B", "", "");
        userHelper.createUser(2214, "User C", "", "");
        groupHelper.createGroup(2215, "Group A", null);
        groupHelper.createGroup(2216, "Group B", null);
        groupHelper.createGroup(2217, "Group C", null);
        userHelper.linkUsersToGroup(2215, 2212);
        userHelper.linkUsersToGroup(2216, 2213, 2214);
        userHelper.linkUsersToGroup(2217, 2212, 2214);

        transaction.begin();

        User userA = userProvider.getById(bi(2212));
        User userB = userProvider.getById(bi(2213));
        User userC = userProvider.getById(bi(2214));
        UserGroup groupA = groupProvider.getById(bi(2215));
        UserGroup groupB = groupProvider.getById(bi(2216));
        UserGroup groupC = groupProvider.getById(bi(2217));

        Collection<User> groupAUsers = groupA.getUsers();
        Collection<User> groupBUsers = groupB.getUsers();
        Collection<User> groupCUsers = groupC.getUsers();
        Collection<UserGroup> userAGroups = userA.getUserGroups();
        Collection<UserGroup> userBGroups = userB.getUserGroups();
        Collection<UserGroup> userCGroups = userC.getUserGroups();

        assertEquals(1, groupAUsers.size());
        assertEquals(2, groupBUsers.size());
        assertEquals(2, groupCUsers.size());
        assertEquals(2, userAGroups.size());
        assertEquals(1, userBGroups.size());
        assertEquals(2, userCGroups.size());

        User userAa = groupAUsers.iterator().next();
        Iterator<User> groupBIter = sequenceUsers(groupBUsers, bi(2213), bi(2214)).iterator();
        User userBb = groupBIter.next();
        User userBc = groupBIter.next();
        Iterator<User> groupCIter = sequenceUsers(groupCUsers, bi(2212), bi(2214)).iterator();
        User userCa = groupCIter.next();
        User userCc = groupCIter.next();

        Iterator<UserGroup> userAIter = sequenceGroups(userAGroups, bi(2215),
                bi(2217)).iterator();
        UserGroup groupAa = userAIter.next();
        UserGroup groupAc = userAIter.next();
        UserGroup groupBb = userBGroups.iterator().next();
        Iterator<UserGroup> userCIter = sequenceGroups(userCGroups, bi(2216),
                bi(2217)).iterator();
        UserGroup groupCb = userCIter.next();
        UserGroup groupCc = userCIter.next();

        assertSame(userA, userAa);
        assertSame(userB, userBb);
        assertSame(userC, userBc);
        assertSame(userA, userCa);
        assertSame(userC, userCc);

        assertSame(groupA, groupAa);
        assertSame(groupC, groupAc);
        assertSame(groupB, groupBb);
        assertSame(groupB, groupCb);
        assertSame(groupC, groupCc);
    }

    @Test
    @InSequence(3)
    public void testUpdate_shouldUpdateGroupsToGrantsRelations() throws Exception {
        grantHelper.createGrant(2218, "Grant A");
        grantHelper.createGrant(2219, "Grant B");
        grantHelper.createGrant(2220, "Grant C");
        groupHelper.createGroup(2221, "Group A", null);
        groupHelper.createGroup(2222, "Group B", null);
        grantHelper.linkGrantsToGroup(2221, 2218, 2219);
        grantHelper.linkGrantsToGroup(2222, 2219);

        transaction.begin();

        UserGrant grantA = grantProvider.getById(bi(2218));
        UserGrant grantB = grantProvider.getById(bi(2219));
        UserGrant grantC = grantProvider.getById(bi(2220));
        UserGroup groupA = groupProvider.getById(bi(2221));
        UserGroup groupB = groupProvider.getById(bi(2222));

        Collection<UserGrant> groupAGrants = groupA.getGrants();
        Collection<UserGrant> groupBGrants = groupB.getGrants();

        groupAGrants.remove(grantA);
        groupAGrants.add(grantC);
        groupBGrants.remove(grantB);

        transaction.commit();

        Collection<BigInteger> aGrants = grantHelper.readGrantsByGroup(bi(2221));
        Collection<BigInteger> bGrants = grantHelper.readGrantsByGroup(bi(2222));
        Collection<BigInteger> aGroups = groupHelper.readGroupsByGrant(bi(2218));
        Collection<BigInteger> bGroups = groupHelper.readGroupsByGrant(bi(2219));
        Collection<BigInteger> cGroups = groupHelper.readGroupsByGrant(bi(2220));

        assertEquals(2, aGrants.size());
        assertTrue(bGrants.isEmpty());
        assertTrue(aGroups.isEmpty());
        assertEquals(1, bGroups.size());
        assertEquals(1, cGroups.size());

        assertTrue(aGrants.contains(bi(2219)));
        assertTrue(aGrants.contains(bi(2220)));
        assertEquals(bi(2221), bGroups.iterator().next());
        assertEquals(bi(2221), cGroups.iterator().next());

    }

    @Test
    @InSequence(4)
    public void testUpdate_shouldUpdateGroupsToUsersRelations() throws Exception {
        userHelper.createUser(2223, "User A", "", "");
        userHelper.createUser(2224, "User B", "", "");
        userHelper.createUser(2225, "User C", "", "");
        groupHelper.createGroup(2226, "Group A", null);
        groupHelper.createGroup(2227, "Group B", null);
        groupHelper.createGroup(2228, "Group C", null);
        userHelper.linkUsersToGroup(2226, 2223);
        userHelper.linkUsersToGroup(2227, 2224, 2225);
        userHelper.linkUsersToGroup(2228, 2224);

        transaction.begin();

        User userA = userProvider.getById(bi(2223));
        User userB = userProvider.getById(bi(2224));
        User userC = userProvider.getById(bi(2225));
        UserGroup groupA = groupProvider.getById(bi(2226));
        UserGroup groupB = groupProvider.getById(bi(2227));
        UserGroup groupC = groupProvider.getById(bi(2228));

        groupA.removeUser(userA);
        groupB.removeUser(userC);
        groupC.addUser(userC);

        Collection<User> aUsers = groupA.getUsers();
        Collection<User> bUsers = groupB.getUsers();
        Collection<User> cUsers = groupC.getUsers();
        Collection<UserGroup> aGroups = userA.getUserGroups();
        Collection<UserGroup> bGroups = userB.getUserGroups();
        Collection<UserGroup> cGroups = userC.getUserGroups();

        assertTrue(aUsers.isEmpty());
        assertEquals(1, bUsers.size());
        assertEquals(2, cUsers.size());
        assertTrue(aGroups.isEmpty());
        assertEquals(2, bGroups.size());
        assertEquals(1, cGroups.size());

        assertSame(userB, bUsers.iterator().next());
        Iterator<User> cUsersIter = sequenceUsers(cUsers, bi(2224), bi(2225)).iterator();
        assertSame(userB, cUsersIter.next());
        assertSame(userC, cUsersIter.next());
        Iterator<UserGroup> bGroupsIter = sequenceGroups(bGroups, bi(2227), bi(2228)).iterator();
        assertSame(groupB, bGroupsIter.next());
        assertSame(groupC, bGroupsIter.next());
        assertSame(groupC, cGroups.iterator().next());

        transaction.commit();

        Collection<BigInteger> userAGroups = groupHelper.readGroupsByUser(bi(2223));
        Collection<BigInteger> userBGroups = groupHelper.readGroupsByUser(bi(2224));
        Collection<BigInteger> userCGroups = groupHelper.readGroupsByUser(bi(2225));
        Collection<BigInteger> groupAUsers = userHelper.readUsersByGroup(bi(2226));
        Collection<BigInteger> groupBUsers = userHelper.readUsersByGroup(bi(2227));
        Collection<BigInteger> groupCUsers = userHelper.readUsersByGroup(bi(2228));

        assertTrue(userAGroups.isEmpty());
        assertEquals(2, userBGroups.size());
        assertEquals(1, userCGroups.size());
        assertTrue(groupAUsers.isEmpty());
        assertEquals(1, groupBUsers.size());
        assertEquals(2, groupCUsers.size());

        assertTrue(userBGroups.contains(bi(2227)));
        assertTrue(userBGroups.contains(bi(2228)));
        assertEquals(bi(2228), userCGroups.iterator().next());

        assertEquals(bi(2224), groupBUsers.iterator().next());
        assertTrue(groupCUsers.contains(bi(2224)));
        assertTrue(groupCUsers.contains(bi(2225)));
    }

    @Test
    @InSequence(5)
    public void testRemove_shouldRemoveLinkage() throws Exception {
        grantHelper.createGrant(2229, "Grant A");
        grantHelper.createGrant(2230, "Grant B");
        grantHelper.createGrant(2231, "Grant C");
        userHelper.createUser(2232, "User A", "", "");
        userHelper.createUser(2233, "User B", "", "");
        userHelper.createUser(2234, "User C", "", "");
        groupHelper.createGroup(2235, "Group A", null);
        groupHelper.createGroup(2236, "Group B", null);
        groupHelper.createGroup(2237, "Group C", null);
        userHelper.linkUsersToGroup(2235, 2232);
        userHelper.linkUsersToGroup(2236, 2233);
        userHelper.linkUsersToGroup(2237, 2233, 2234);
        grantHelper.linkGrantsToGroup(2235, 2229, 2230);
        grantHelper.linkGrantsToGroup(2236, 2230);
        grantHelper.linkGrantsToGroup(2237, 2231);
        // remove user b, group a, grant c
        transaction.begin();

        UserGrant grantC = grantProvider.getById(bi(2231));
        User userB = userProvider.getById(bi(2233));
        UserGroup groupA = groupProvider.getById(bi(2235));
        grantProvider.remove(grantC);
        userProvider.remove(userB);
        groupProvider.remove(groupA);

        transaction.commit();

        assertNotNull(grantHelper.readGrant(bi(2229)));
        assertNotNull(grantHelper.readGrant(bi(2230)));
        assertNull(grantHelper.readGrant(bi(2231)));
        assertNotNull(userHelper.readUser(bi(2232)));
        assertNull(userHelper.readUser(bi(2233)));
        assertNotNull(userHelper.readUser(bi(2234)));
        assertNull(groupHelper.readGroup(bi(2235)));
        assertNotNull(groupHelper.readGroup(bi(2236)));
        assertNotNull(groupHelper.readGroup(bi(2237)));

        Collection<BigInteger> groupAGrants = grantHelper.readGrantsByGroup(bi(2235));
        Collection<BigInteger> groupBGrants = grantHelper.readGrantsByGroup(bi(2236));
        Collection<BigInteger> groupCGrants = grantHelper.readGrantsByGroup(bi(2237));

        assertTrue(groupAGrants.isEmpty());
        assertEquals(1, groupBGrants.size());
        assertTrue(groupCGrants.isEmpty());
        assertEquals(bi(2230), groupBGrants.iterator().next());

        Collection<BigInteger> grantAGroups = groupHelper.readGroupsByGrant(bi(2229));
        Collection<BigInteger> grantBGroups = groupHelper.readGroupsByGrant(bi(2230));
        Collection<BigInteger> grantCGroups = groupHelper.readGroupsByGrant(bi(2231));

        assertTrue(grantAGroups.isEmpty());
        assertEquals(1, grantBGroups.size());
        assertTrue(grantCGroups.isEmpty());
        assertEquals(bi(2236), grantBGroups.iterator().next());

        Collection<BigInteger> userAGroups = groupHelper.readGroupsByUser(bi(2232));
        Collection<BigInteger> userBGroups = groupHelper.readGroupsByUser(bi(2233));
        Collection<BigInteger> userCGroups = groupHelper.readGroupsByUser(bi(2234));

        assertTrue(userAGroups.isEmpty());
        assertTrue(userBGroups.isEmpty());
        assertEquals(1, userCGroups.size());
        assertEquals(bi(2237), userCGroups.iterator().next());

        Collection<BigInteger> groupAUsers = userHelper.readUsersByGroup(bi(2235));
        Collection<BigInteger> groupBUsers = userHelper.readUsersByGroup(bi(2236));
        Collection<BigInteger> groupCUsers = userHelper.readUsersByGroup(bi(2237));

        assertTrue(groupAUsers.isEmpty());
        assertTrue(groupBUsers.isEmpty());
        assertEquals(1, groupCUsers.size());
        assertEquals(bi(2234), groupCUsers.iterator().next());
    }
}
