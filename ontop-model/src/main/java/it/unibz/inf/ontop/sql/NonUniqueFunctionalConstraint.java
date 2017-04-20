package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.sql.impl.NonUniqueFunctionalConstraintImpl;

/**
 * A functional dependency (which is not a unique constraint)
 */
public interface NonUniqueFunctionalConstraint {

    ImmutableSet<Attribute> getDeterminants();

    ImmutableSet<Attribute> getDependents();

    interface Builder {
        Builder addDeterminant(Attribute determinant);
        Builder addDependent(Attribute dependent);

        NonUniqueFunctionalConstraint build();
    }

    static Builder defaultBuilder() {
        return new NonUniqueFunctionalConstraintImpl.BuilderImpl();
    }
}
