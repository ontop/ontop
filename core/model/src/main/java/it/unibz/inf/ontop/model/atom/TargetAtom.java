package it.unibz.inf.ontop.model.atom;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * Immutable
 *
 * TODO: move it to ontop-mapping-core after getting rid of Datalog
 */
public interface TargetAtom {

    DistinctVariableOnlyDataAtom getProjectionAtom();
    ImmutableSubstitution<ImmutableTerm> getSubstitution();

    ImmutableTerm getSubstitutedTerm(int index);

    ImmutableList<ImmutableTerm> getSubstitutedTerms();

    TargetAtom rename(InjectiveVar2VarSubstitution renamingSubstitution);

    /**
     * Returns a new (immutable) TargetAtom
     */
    TargetAtom changeSubstitution(ImmutableSubstitution<ImmutableTerm> newSubstitution);

    Optional<IRI> getPredicateIRI();
}
