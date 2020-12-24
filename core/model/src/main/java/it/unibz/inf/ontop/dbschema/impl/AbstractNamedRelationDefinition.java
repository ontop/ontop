package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractNamedRelationDefinition extends AbstractRelationDefinition implements NamedRelationDefinition {

    private final RelationID id;
    private final ImmutableSet<RelationID> allIds;

    private UniqueConstraint primaryKey; // nullable
    private final List<UniqueConstraint> uniqueConstraints = new LinkedList<>();
    private final List<FunctionalDependency> otherFunctionalDependencies = new ArrayList<>();
    private final List<ForeignKeyConstraint> foreignKeys = new ArrayList<>();

    AbstractNamedRelationDefinition(ImmutableList<RelationID> allIds, AttributeListBuilder builder) {
        super(allIds.get(0).getSQLRendering(), builder);
        this.id = allIds.get(0);
        this.allIds =  ImmutableSet.copyOf(allIds);
    }


    @Override
    public RelationID getID() {
        return id;
    }

    @Override
    public ImmutableSet<RelationID> getAllIDs() {
        return allIds;
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

    @Override
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
    @Override
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
    @Override
    public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
        return ImmutableList.copyOf(foreignKeys);
    }

}
