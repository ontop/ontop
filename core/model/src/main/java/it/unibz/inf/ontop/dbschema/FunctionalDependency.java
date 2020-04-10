package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.impl.FunctionalDependencyImpl;

/**
 * A functional dependency
 */

public interface FunctionalDependency {

    ImmutableSet<Attribute> getDeterminants();

    ImmutableSet<Attribute> getDependents();

    interface Builder {
        Builder addDeterminant(int determinantIndex);
        Builder addDeterminant(QuotedID determinantId) throws RelationDefinition.AttributeNotFoundException;

        Builder addDependent(int dependentIndex);
        Builder addDependent(QuotedID dependentId) throws RelationDefinition.AttributeNotFoundException;

        void build();
    }

    static Builder defaultBuilder(DatabaseRelationDefinition relation) {
        return new FunctionalDependencyImpl.BuilderImpl(relation);
    }
}
