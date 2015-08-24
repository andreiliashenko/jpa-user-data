package com.anli.generalization.user.data.utils;

import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import static java.util.Arrays.asList;

public class UserGroupHelper extends SqlHelper {

    private static final String INSERT_GROUP = "insert into user_groups "
            + "(group_id, name, parent_id) values (?, ?, ?)";
    private static final String SELECT_GROUP = "select group_id, name, parent_id "
            + "from user_groups where group_id = ?";
    private static final String SELECT_GROUPS_BY_USER = "select group_id from groups_to_users "
            + "where user_id = ?";
    private static final String SELECT_GROUPS_BY_PARENT = "select group_id from user_groups "
            + "where parent_id = ?";

    public UserGroupHelper(DataSource dataSource) {
        super(dataSource);
    }

    public void createGroup(long id, String name, BigInteger parentId) {
        executor.executeUpdate(INSERT_GROUP, asList(id, name,
                parentId != null ? new BigDecimal(parentId) : null));
    }

    public Map<String, Object> readGroup(BigInteger id) {
        return executor.executeSelect(SELECT_GROUP, asList(new BigDecimal(id)), new GroupReader());
    }

    public Collection<BigInteger> readGroupsByUser(BigInteger userId) {
        return executor.executeSelect(SELECT_GROUPS_BY_USER, asList(new BigDecimal(userId)),
                new IdSelector());
    }

    public Collection<BigInteger> readGroupsByParent(BigInteger parentId) {
        return executor.executeSelect(SELECT_GROUPS_BY_PARENT, asList(new BigDecimal(parentId)),
                new IdSelector());
    }

    protected class GroupReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("group_id", BigDecimal.class)));
                result.put("name", resultSet.getValue("name", String.class));
                result.put("parent", getBigInteger(resultSet.getValue("parentId", BigDecimal.class)));
            } else {
                return null;
            }
            if (resultSet.next()) {
                throw new RuntimeException("More than 1 record with this primaryKey");
            }
            return result;
        }
    }
}
