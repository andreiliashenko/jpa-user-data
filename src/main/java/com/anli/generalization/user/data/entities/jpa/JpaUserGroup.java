package com.anli.generalization.user.data.entities.jpa;

import com.anli.generalization.user.data.entities.User;
import com.anli.generalization.user.data.entities.UserGrant;
import com.anli.generalization.user.data.entities.UserGroup;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "UserGroup")
@Table(name = "user_groups")
@AttributeOverride(name = "id", column = @Column(name = "group_id"))
public class JpaUserGroup extends IdGeneratingObject implements UserGroup {

    @Column(name = "name")
    protected String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "group_id")
    protected JpaUserGroup parent;

    @OneToMany(fetch = LAZY, mappedBy = "parent")
    protected Collection<JpaUserGroup> children;

    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "groups_to_grants",
            joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "grant_id", referencedColumnName = "grant_id"))
    protected Collection<JpaUserGrant> grants;

    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "groups_to_users",
            joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"))
    protected Collection<JpaUser> users;

    public JpaUserGroup() {
        this.children = new HashSet<>();
        this.grants = new HashSet<>();
        this.users = new HashSet<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UserGroup getParent() {
        return parent;
    }

    @Override
    public void setParent(UserGroup parent) {
        this.parent = (JpaUserGroup) parent;
        if (parent != null) {
            parent.getChildren().add(this);
        }
    }

    @Override
    public Collection<UserGroup> getChildren() {
        return (Collection) children;
    }

    @Override
    public Collection<UserGrant> getGrants(boolean hierarchically) {
        if (!hierarchically) {
            return getGrants();
        }
        Set<UserGrant> allGrants = new HashSet<>();
        UserGroup group = this;
        while (group != null) {
            allGrants.addAll(group.getGrants());
            group = group.getParent();
        }
        return allGrants;
    }

    @Override
    public Collection<UserGrant> getGrants() {
        return (Collection) grants;
    }

    @Override
    public Collection<User> getUsers() {
        return (Collection) users;
    }

    @Override
    public void addUser(User user) {
        getUsers().add(user);
        user.getUserGroups().add(this);
    }

    @Override
    public void removeUser(User user) {
        getUsers().remove(user);
        user.getUserGroups().remove(this);
    }
}
