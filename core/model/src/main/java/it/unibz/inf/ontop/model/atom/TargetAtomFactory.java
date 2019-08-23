package it.unibz.inf.ontop.model.atom;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.apache.commons.rdf.api.IRI;

public interface TargetAtomFactory {

    TargetAtom getTripleTargetAtom(ImmutableTerm subject, ImmutableTerm pred, ImmutableTerm object);

    TargetAtom getTripleTargetAtom(ImmutableTerm subjectTerm, IRI classIRI);

    TargetAtom getTripleTargetAtom(ImmutableTerm subjectTerm, IRI propertyIRI, ImmutableTerm objectTerm);

    /**
     * Used for Datalog conversion.
     * Please consider the other methods
     */
    TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableSubstitution<ImmutableTerm> substitution);
}
