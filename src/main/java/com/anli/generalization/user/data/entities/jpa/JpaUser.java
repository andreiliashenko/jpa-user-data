package com.anli.generalization.user.data.entities.jpa;

import com.anli.generalization.user.data.entities.User;
import com.anli.generalization.user.data.entities.UserGroup;
import java.util.Collection;
import java.util.HashSet;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import static com.anli.generalization.user.data.encryption.PasswordHasher.hash;
import static javax.persistence.FetchType.LAZY;

@Entity(name = "Users")
@Table(name = "users")
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class JpaUser extends IdGeneratingObject implements User {

    @Column(name = "name")
    protected String name;

    @Column(name = "login")
    protected String login;

    @Column(name = "password")
    protected String passwordHash;

    @ManyToMany(fetch = LAZY, mappedBy = "users")
    protected Collection<JpaUserGroup> userGroups;

    public JpaUser() {
        this.userGroups = new HashSet<>();
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
    public String getLogin() {
        return login;
    }

    @Override
    public void setLogin(String login) {
        this.login = login;
    }

    protected String getPasswordHash() {
        return passwordHash;
    }

    protected void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public void setPassword(String password) {
        setPasswordHash(hash(password));
    }

    @Override
    public boolean checkPassword(String password) {
        if (password == null) {
            return false;
        }
        String inputHash = hash(password);
        return inputHash.equals(getPasswordHash());
    }

    @Override
    public Collection<UserGroup> getUserGroups() {
        return (Collection) userGroups;
    }
}
