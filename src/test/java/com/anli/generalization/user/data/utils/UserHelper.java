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

public class UserHelper extends SqlHelper {

    private static final String INSERT_USER = "insert into users "
            + "(user_id, name, login, password) values (?, ?, ?, ?)";
    private static final String LINK_USER_TO_GROUP = "insert into groups_to_users "
            + "(group_id, user_id) values (?, ?)";
    private static final String SELECT_USER = "select user_id, name, login, password "
            + "from users where user_id = ?";
    private static final String SELECT_USERS_BY_GROUP = "select user_id from groups_to_users "
            + "where group_id = ?";

    public UserHelper(DataSource dataSource) {
        super(dataSource);
    }

    public void createUser(long id, String name, String login, String passwordHash) {
        executor.executeUpdate(INSERT_USER, asList(id, name, login, passwordHash));
    }

    public void linkUsersToGroup(long groupId, long... userIds) {
        for (long userId : userIds) {
            executor.executeUpdate(LINK_USER_TO_GROUP, asList(groupId, userId));
        }
    }

    public void linkGroupToUsers(long userId, long... groupIds) {
        for (long groupId : groupIds) {
            executor.executeUpdate(LINK_USER_TO_GROUP, asList(groupId, userId));
        }
    }

    public Map<String, Object> readUser(BigInteger id) {
        return executor.executeSelect(SELECT_USER, asList(new BigDecimal(id)), new UserReader());
    }

    public Collection<BigInteger> readUsersByGroup(BigInteger groupId) {
        return executor.executeSelect(SELECT_USERS_BY_GROUP, asList(new BigDecimal(groupId)),
                new SqlHelper.IdSelector());
    }

    protected class UserReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("user_id", BigDecimal.class)));
                result.put("name", resultSet.getValue("name", String.class));
                result.put("login", resultSet.getValue("login", String.class));
                result.put("passwordHash", resultSet.getValue("password", String.class));
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
