package com.anli.generalization.user.data.entities.jpa;

import com.anli.generalization.user.data.entities.UserGrant;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "UserGrant")
@Table(name = "user_grants")
public class JpaUserGrant implements Serializable, UserGrant {

    @Id
    @Column(name = "grant_id")
    protected BigInteger id;

    @Column(name = "description")
    protected String description;

    @Override
    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JpaUserGrant)) {
            return false;
        }
        JpaUserGrant other = (JpaUserGrant) obj;
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(getId());
        return hash;
    }
}
