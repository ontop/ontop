package it.unibz.inf.ontop.pivotalrepr.mapping;


import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;

/**
 * Immutable
 */
public interface TargetAtom {

    DistinctVariableOnlyDataAtom getProjectionAtom();
    ImmutableSubstitution<ImmutableTerm> getSubstitution();

    TargetAtom rename(InjectiveVar2VarSubstitution renamingSubstitution);
}
