package com.anli.generalization.user.data.entities.jpa;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;

import static javax.persistence.GenerationType.TABLE;
import static javax.persistence.InheritanceType.TABLE_PER_CLASS;

@MappedSuperclass
@Inheritance(strategy = TABLE_PER_CLASS)
@TableGenerator(name = "id_generator", table = "id_generation_sequences", pkColumnName = "entity_set",
        pkColumnValue = "user-data", valueColumnName = "last_id", allocationSize = 1)
public abstract class IdGeneratingObject implements Serializable {

    @Id
    @GeneratedValue(generator = "id_generator", strategy = TABLE)
    protected BigInteger id;

    public BigInteger getId() {
        return id;
    }

    @Override
    public boolean equals(Object comparee) {
        if (getId() == null) {
            return false;
        }
        if (comparee == null) {
            return false;
        }
        if (!this.getClass().equals(comparee.getClass())) {
            return false;
        }
        IdGeneratingObject generatingComparee = (IdGeneratingObject) comparee;
        return this.getId().equals(generatingComparee.getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.getId());
        return hash;
    }
}
