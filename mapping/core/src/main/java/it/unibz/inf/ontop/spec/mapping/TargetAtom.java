package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * Immutable
 */
public interface TargetAtom {

    DistinctVariableOnlyDataAtom getProjectionAtom();
    ImmutableSubstitution<ImmutableTerm> getSubstitution();

    ImmutableTerm getSubstitutedTerm(int index);

    ImmutableList<ImmutableTerm> getSubstitutedTerms();

    Optional<IRI> getPredicateIRI();
}
