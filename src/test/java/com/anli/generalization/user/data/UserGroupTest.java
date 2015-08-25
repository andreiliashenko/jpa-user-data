package com.anli.generalization.user.data;

import com.anli.generalization.user.data.access.UserGroupProvider;
import com.anli.generalization.user.data.entities.UserGroup;
import com.anli.generalization.user.data.factory.JpaProviderFactory;
import com.anli.generalization.user.data.utils.UserGroupHelper;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class UserGroupTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    private DataSource dataSource;

    private UserTransaction transaction;

    private UserGroupHelper helper;

    private UserGroupProvider provider;

    @Before
    public void setUp() {
        dataSource = getDataSource();
        transaction = getTransaction();
        helper = new UserGroupHelper(dataSource);
        provider = JpaProviderFactory.getInstance().getUserGroupProvider();
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldCreateWithData() throws Exception {
        transaction.begin();

        UserGroup root = provider.create();
        BigInteger rootId = root.getId();
        root.setName("Root");
        UserGroup groupA = provider.create();
        BigInteger groupAId = groupA.getId();
        groupA.setName("Group A");
        UserGroup groupB = provider.create();
        BigInteger groupBId = groupB.getId();
        groupB.setName("Group B");

        groupA.setParent(root);
        groupB.setParent(root);

        Collection<UserGroup> groups = root.getChildren();

        assertEquals(2, groups.size());
        assertTrue(groups.contains(groupA));
        assertTrue(groups.contains(groupB));

        transaction.commit();

        assertNotNull(rootId);
        assertNotNull(groupAId);
        assertNotNull(groupBId);

        Map<String, Object> rootData = helper.readGroup(rootId);
        Map<String, Object> groupAData = helper.readGroup(groupAId);
        Map<String, Object> groupBData = helper.readGroup(groupBId);
        Collection<BigInteger> rootChildren = helper.readGroupsByParent(rootId);

        assertEquals(rootId, rootData.get("id"));
        assertEquals("Root", rootData.get("name"));
        assertNull(rootData.get("parent"));
        assertEquals(groupAId, groupAData.get("id"));
        assertEquals("Group A", groupAData.get("name"));
        assertEquals(rootId, groupAData.get("parent"));
        assertEquals(groupBId, groupBData.get("id"));
        assertEquals("Group B", groupBData.get("name"));
        assertEquals(rootId, groupBData.get("parent"));
        assertEquals(2, rootChildren.size());
        assertTrue(rootChildren.contains(groupAId));
        assertTrue(rootChildren.contains(groupBId));
    }

    @Test
    @InSequence(1)
    public void testCreate_shouldRollbackCreation() throws Exception {
        transaction.begin();

        UserGroup root = provider.create();
        root.setName("Rollback Root");
        UserGroup groupA = provider.create();
        groupA.setName("Rollback A");
        groupA.setParent(root);
        BigInteger rootId = root.getId();
        BigInteger groupAId = root.getId();

        transaction.rollback();

        Map<String, Object> rootData = helper.readGroup(rootId);
        Map<String, Object> groupAData = helper.readGroup(groupAId);

        assertNull(rootData);
        assertNull(groupAData);
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(2)
    public void testCreation_shouldForbidNonTransactionalCall() {
        provider.create();
    }

    @Test
    @InSequence(3)
    public void testReading_shouldReadHierarchy() throws Exception {
        helper.createGroup(2100, "Root", null);
        helper.createGroup(2101, "Group A", bi(2100));
        helper.createGroup(2102, "Group B", bi(2100));

        transaction.begin();

        UserGroup root = provider.getById(bi(2100));
        BigInteger rootId = root.getId();
        String rootName = root.getName();
        Collection<UserGroup> rootChildren = root.getChildren();
        UserGroup rootParent = root.getParent();

        assertEquals(2, rootChildren.size());
        assertNull(rootParent);

        Iterator<UserGroup> groupIter = rootChildren.iterator();
        UserGroup groupA = groupIter.next();
        UserGroup groupB = groupIter.next();
        if (bi(2102).equals(groupA.getId())) {
            UserGroup oldA = groupA;
            groupA = groupB;
            groupB = oldA;
        }

        BigInteger groupAId = groupA.getId();
        String groupAName = groupA.getName();
        UserGroup groupAParent = groupA.getParent();
        BigInteger groupBId = groupB.getId();
        String groupBName = groupB.getName();
        UserGroup groupBParent = groupB.getParent();

        assertSame(root, groupAParent);
        assertSame(root, groupBParent);

        transaction.commit();

        assertEquals(bi(2100), rootId);
        assertEquals("Root", rootName);
        assertEquals(bi(2101), groupAId);
        assertEquals("Group A", groupAName);
        assertEquals(bi(2102), groupBId);
        assertEquals("Group B", groupBName);
    }

    @Test
    @InSequence(4)
    public void testReading_shouldReadNull() throws Exception {
        transaction.begin();

        UserGroup group = provider.getById(bi(2103));

        transaction.commit();

        assertNull(group);
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(5)
    public void testReading_shouldForbidNonTransactionalCall() {
        helper.createGroup(2104, "Forbidden", null);
        provider.getById(bi(2104));
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
    public void testUpdate_shouldUpdateDataAndHierarchy() throws Exception {
        helper.createGroup(1205, "Initial Root", null);
        helper.createGroup(1206, "Initial Group A", bi(1205));
        helper.createGroup(1207, "Initial Group B", bi(1206));

        transaction.begin();

        UserGroup root = provider.getById(bi(1205));
        root.setName("Updated Root");
        UserGroup groupA = root.getChildren().iterator().next();
        groupA.setName("Updated Group A");
        UserGroup groupB = groupA.getChildren().iterator().next();
        groupB.setName("Updated Group B");
        groupB.setParent(root);

        transaction.commit();

        Map<String, Object> rootData = helper.readGroup(bi(1205));
        Map<String, Object> groupAData = helper.readGroup(bi(1206));
        Map<String, Object> groupBData = helper.readGroup(bi(1207));
        Collection<BigInteger> rootChildren = helper.readGroupsByParent(bi(1205));
        Collection<BigInteger> groupAChildren = helper.readGroupsByParent(bi(1206));
        Collection<BigInteger> groupBChildren = helper.readGroupsByParent(bi(1207));

        assertEquals(bi(1205), rootData.get("id"));
        assertEquals("Updated Root", rootData.get("name"));
        assertNull(rootData.get("parent"));
        assertEquals(bi(1206), groupAData.get("id"));
        assertEquals("Updated Group A", groupAData.get("name"));
        assertEquals(bi(1205), groupAData.get("parent"));
        assertEquals(bi(1207), groupBData.get("id"));
        assertEquals("Updated Group B", groupBData.get("name"));
        assertEquals(bi(1205), groupBData.get("parent"));
        assertEquals(2, rootChildren.size());
        assertTrue(rootChildren.contains(bi(1206)));
        assertTrue(rootChildren.contains(bi(1207)));
        assertTrue(groupAChildren.isEmpty());
        assertTrue(groupBChildren.isEmpty());
    }

    @Test
    @InSequence(8)
    public void testUpdate_shouldRollbackUpdate() throws Exception {
        helper.createGroup(1208, "Initial Root", null);
        helper.createGroup(1209, "Initial Group A", bi(1208));
        helper.createGroup(1210, "Initial Group B", bi(1209));

        transaction.begin();

        UserGroup root = provider.getById(bi(1208));
        root.setName("Updated Root");
        UserGroup groupA = root.getChildren().iterator().next();
        groupA.setName("Updated Group A");
        UserGroup groupB = groupA.getChildren().iterator().next();
        groupB.setName("Updated Group B");
        groupB.setParent(root);

        transaction.rollback();

        Map<String, Object> rootData = helper.readGroup(bi(1208));
        Map<String, Object> groupAData = helper.readGroup(bi(1209));
        Map<String, Object> groupBData = helper.readGroup(bi(1210));
        Collection<BigInteger> rootChildren = helper.readGroupsByParent(bi(1208));
        Collection<BigInteger> groupAChildren = helper.readGroupsByParent(bi(1209));
        Collection<BigInteger> groupBChildren = helper.readGroupsByParent(bi(1210));

        assertEquals(bi(1208), rootData.get("id"));
        assertEquals("Initial Root", rootData.get("name"));
        assertNull(rootData.get("parent"));
        assertEquals(bi(1209), groupAData.get("id"));
        assertEquals("Initial Group A", groupAData.get("name"));
        assertEquals(bi(1208), groupAData.get("parent"));
        assertEquals(bi(1210), groupBData.get("id"));
        assertEquals("Initial Group B", groupBData.get("name"));
        assertEquals(bi(1209), groupBData.get("parent"));
        assertEquals(1, rootChildren.size());
        assertEquals(bi(1209), rootChildren.iterator().next());
        assertEquals(1, groupAChildren.size());
        assertEquals(bi(1210), groupAChildren.iterator().next());
        assertTrue(groupBChildren.isEmpty());
    }

    @Test
    @InSequence(9)
    public void testRemove_shouldRemoveData() throws Exception {
        helper.createGroup(1211, "Root", null);
        helper.createGroup(1212, "Group A", bi(1211));
        helper.createGroup(1213, "Group B", bi(1212));

        transaction.begin();

        UserGroup root = provider.getById(bi(1211));
        UserGroup groupA = root.getChildren().iterator().next();
        UserGroup groupB = groupA.getChildren().iterator().next();
        provider.remove(groupB.getParent());

        transaction.commit();

        Map<String, Object> rootData = helper.readGroup(bi(1211));
        Map<String, Object> groupAData = helper.readGroup(bi(1212));
        Map<String, Object> groupBData = helper.readGroup(bi(1213));
        Collection<BigInteger> rootChildren = helper.readGroupsByParent(bi(1211));
        Collection<BigInteger> groupAChildren = helper.readGroupsByParent(bi(1212));
        Collection<BigInteger> groupBChildren = helper.readGroupsByParent(bi(1213));

        assertEquals(bi(1211), rootData.get("id"));
        assertEquals("Root", rootData.get("name"));
        assertNull(rootData.get("parent"));
        assertNull(groupAData);
        assertEquals(bi(1213), groupBData.get("id"));
        assertEquals("Group B", groupBData.get("name"));
        assertNull(groupBData.get("parent"));
        assertTrue(rootChildren.isEmpty());
        assertTrue(groupAChildren.isEmpty());
        assertTrue(groupBChildren.isEmpty());
    }

    @Test
    @InSequence(10)
    public void testRemove_shouldRollbackRemove() throws Exception {
        helper.createGroup(1214, "Root", null);
        helper.createGroup(1215, "Group A", bi(1214));
        helper.createGroup(1216, "Group B", bi(1215));

        transaction.begin();

        UserGroup root = provider.getById(bi(1214));
        UserGroup groupA = root.getChildren().iterator().next();
        UserGroup groupB = groupA.getChildren().iterator().next();
        provider.remove(groupB.getParent());

        transaction.rollback();

        Map<String, Object> rootData = helper.readGroup(bi(1214));
        Map<String, Object> groupAData = helper.readGroup(bi(1215));
        Map<String, Object> groupBData = helper.readGroup(bi(1216));
        Collection<BigInteger> rootChildren = helper.readGroupsByParent(bi(1214));
        Collection<BigInteger> groupAChildren = helper.readGroupsByParent(bi(1215));
        Collection<BigInteger> groupBChildren = helper.readGroupsByParent(bi(1216));

        assertEquals(bi(1214), rootData.get("id"));
        assertEquals("Root", rootData.get("name"));
        assertNull(rootData.get("parent"));
        assertEquals(bi(1215), groupAData.get("id"));
        assertEquals("Group A", groupAData.get("name"));
        assertEquals(bi(1214), groupAData.get("parent"));
        assertEquals(bi(1216), groupBData.get("id"));
        assertEquals("Group B", groupBData.get("name"));
        assertEquals(bi(1215), groupBData.get("parent"));
        assertEquals(1, rootChildren.size());
        assertEquals(bi(1215), rootChildren.iterator().next());
        assertEquals(1, groupAChildren.size());
        assertEquals(bi(1216), groupAChildren.iterator().next());
        assertTrue(groupBChildren.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(11)
    public void testRemove_shouldForbidNonTransactionalCall() throws Exception {
        helper.createGroup(1217, "Forbidden", null);

        transaction.begin();

        UserGroup group = provider.getById(bi(1217));

        transaction.commit();

        provider.remove(group);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(12)
    public void testRemove_shouldCheckNull() throws Exception {
        transaction.begin();

        provider.remove(null);

        transaction.commit();
    }
}
