package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDatabaseRelationDefinition extends AbstractRelationDefinition implements DatabaseRelationDefinition {

    private final RelationID id;

    private UniqueConstraint primaryKey; // nullable
    private final List<UniqueConstraint> uniqueConstraints = new LinkedList<>();
    private final List<FunctionalDependency> otherFunctionalDependencies = new ArrayList<>();
    private final List<ForeignKeyConstraint> foreignKeys = new ArrayList<>();

    AbstractDatabaseRelationDefinition(RelationID id, AttributeListBuilder builder) {
        super(id.getSQLRendering(), builder);
        this.id = id;
    }

    @JsonProperty("name")
    @JsonSerialize(using = RelationIDImpl.RelationIDSerializer.class)
    public RelationID getID() {
        return id;
    }



    /**
     * returns the list of unique constraints (including the primary key if present)
     *
     * @return
     */
    @Override
    public ImmutableList<UniqueConstraint> getUniqueConstraints() {
        return ImmutableList.copyOf(uniqueConstraints);
    }

    public void addFunctionalDependency(FunctionalDependency constraint) {
        if (constraint instanceof UniqueConstraint) {
            UniqueConstraint uc = (UniqueConstraint) constraint;
            if (uc.isPrimaryKey()) {
                if (primaryKey != null)
                    throw new IllegalArgumentException("Duplicate PK " + primaryKey + " " + uc);
                primaryKey = uc;
            }
            uniqueConstraints.add(uc);
        }
        else
            otherFunctionalDependencies.add(constraint);
    }

    @Override
    public ImmutableList<FunctionalDependency> getOtherFunctionalDependencies() {
        return ImmutableList.copyOf(otherFunctionalDependencies);
    }

    /**
     * @return primary key
     */
    @JsonIgnore
    public Optional<UniqueConstraint> getPrimaryKey() {
        return Optional.ofNullable(primaryKey);
    }


    /**
     * adds a foreign key constraints
     *
     * @param fk a foreign key
     */

    @Override
    public void addForeignKeyConstraint(ForeignKeyConstraint fk) {
        foreignKeys.add(fk);
    }

    /**
     * returns the list of foreign key constraints
     *
     * @return list of foreign keys
     */
    @JsonSerialize(contentUsing = ForeignKeyConstraint.ForeignKeyConstraintSerializer.class)
    @Override
    public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
        return ImmutableList.copyOf(foreignKeys);
    }

}
