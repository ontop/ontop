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
        Builder addDeterminant(Attribute determinant);
        Builder addDeterminant(QuotedID determinantId);

        Builder addDependent(Attribute dependent);
        Builder addDependent(QuotedID dependentId);

        void build();
    }

    static Builder defaultBuilder(DatabaseRelationDefinition relation) {
        return new FunctionalDependencyImpl.BuilderImpl(relation);
    }
}
