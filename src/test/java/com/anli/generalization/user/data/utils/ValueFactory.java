package com.anli.generalization.user.data.utils;

import com.anli.generalization.user.data.entities.User;
import com.anli.generalization.user.data.entities.UserGrant;
import com.anli.generalization.user.data.entities.UserGroup;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValueFactory {

    public static BigInteger bi(long value) {
        return BigInteger.valueOf(value);
    }

    public static List<UserGrant> sequenceGrants(Collection<UserGrant> grants, BigInteger... ids) {
        List<UserGrant> sequenced = new ArrayList<>(ids.length);
        for (BigInteger id : ids) {
            for (UserGrant grant : grants) {
                if (id.equals(grant.getId())) {
                    sequenced.add(grant);
                    break;
                }
            }
        }
        return sequenced;
    }

    public static List<User> sequenceUsers(Collection<User> users, BigInteger... ids) {
        List<User> sequenced = new ArrayList<>(ids.length);
        for (BigInteger id : ids) {
            for (User user : users) {
                if (id.equals(user.getId())) {
                    sequenced.add(user);
                    break;
                }
            }
        }
        return sequenced;
    }

    public static List<UserGroup> sequenceGroups(Collection<UserGroup> groups, BigInteger... ids) {
        List<UserGroup> sequenced = new ArrayList<>(ids.length);
        for (BigInteger id : ids) {
            for (UserGroup group : groups) {
                if (id.equals(group.getId())) {
                    sequenced.add(group);
                    break;
                }
            }
        }
        return sequenced;
    }
}
