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
        Builder addDeterminant(QuotedID determinantId) throws AttributeNotFoundException;

        Builder addDependent(int dependentIndex);
        Builder addDependent(QuotedID dependentId) throws AttributeNotFoundException;

        void build();
    }

    static Builder defaultBuilder(NamedRelationDefinition relation) {
        return FunctionalDependencyImpl.builder(relation);
    }
}
