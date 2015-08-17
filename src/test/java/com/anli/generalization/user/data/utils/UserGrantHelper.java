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

public class UserGrantHelper extends SqlHelper {

    private static final String INSERT_GRANT = "insert into user_grants "
            + "(grant_id, description) values (?, ?)";
    private static final String LINK_GRANT_TO_GROUP = "insert into groups_to_grants "
            + "(group_id, grant_id) values (?, ?)";
    private static final String SELECT_GRANT = "select grant_id, description "
            + "from user_grants where grant_id = ?";
    private static final String SELECT_GRANTS_BY_GROUP = "select grant_id from groups_to_grants "
            + "where group_id = ?";

    public UserGrantHelper(DataSource dataSource) {
        super(dataSource);
    }

    public void createGrant(long id, String description) {
        executor.executeUpdate(INSERT_GRANT, asList(id, description));
    }

    public void linkGrantsToGroup(long groupId, long... grantIds) {
        for (long grantId : grantIds) {
            executor.executeUpdate(LINK_GRANT_TO_GROUP, asList(groupId, grantId));
        }
    }

    public Map<String, Object> readGrant(BigInteger id) {
        return executor.executeSelect(SELECT_GRANT, asList(new BigDecimal(id)), new GrantReader());
    }

    public Collection<BigInteger> readGrantsByGroup(BigInteger groupId) {
        return executor.executeSelect(SELECT_GRANTS_BY_GROUP, asList(new BigDecimal(groupId)),
                new IdSelector());
    }

    protected class GrantReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("grant_id", BigDecimal.class)));
                result.put("description", resultSet.getValue("description", String.class));
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
