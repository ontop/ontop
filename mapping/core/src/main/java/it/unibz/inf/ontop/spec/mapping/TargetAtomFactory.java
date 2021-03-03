package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

/**
 * Accessible through Guice (recommended) or through MappingCoreSingletons.
 */
public interface TargetAtomFactory {

    TargetAtom getQuadTargetAtom(ImmutableTerm subjectTerm, ImmutableTerm predTerm, ImmutableTerm
            objectTerm, ImmutableTerm graphTerm);

    TargetAtom getTripleTargetAtom(ImmutableTerm subject, ImmutableTerm pred, ImmutableTerm object);

    /**
     * Used for Datalog conversion.
     * Please consider the other methods
     */
    TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableSubstitution<ImmutableTerm> substitution);
}
