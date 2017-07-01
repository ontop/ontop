package it.unibz.inf.ontop.iq.mapping;


import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;

/**
 * Immutable
 */
public interface TargetAtom {

    DistinctVariableOnlyDataAtom getProjectionAtom();
    ImmutableSubstitution<ImmutableTerm> getSubstitution();

    TargetAtom rename(InjectiveVar2VarSubstitution renamingSubstitution);
}
