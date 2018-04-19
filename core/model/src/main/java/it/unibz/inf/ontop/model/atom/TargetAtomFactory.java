package it.unibz.inf.ontop.model.atom;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

public interface TargetAtomFactory {

    TargetAtom getTripleTargetAtom(ImmutableTerm subject, ImmutableTerm pred, ImmutableTerm object);

    /**
     * Used for Datalog conversion.
     * Please consider the other methods
     */
    TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableSubstitution<ImmutableTerm> substitution);
}
