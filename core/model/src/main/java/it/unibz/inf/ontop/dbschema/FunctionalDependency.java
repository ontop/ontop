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
        Builder addDependent(Attribute dependent);

        FunctionalDependency build();
    }

    static Builder defaultBuilder() {
        return new FunctionalDependencyImpl.BuilderImpl();
    }
}
