package it.unibz.inf.ontop.iq.mapping.impl;

import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.mapping.TargetAtom;

public class TargetAtomImpl implements TargetAtom {
    private final DistinctVariableOnlyDataAtom atom;
    private final ImmutableSubstitution<ImmutableTerm> substitution;

    public TargetAtomImpl(DistinctVariableOnlyDataAtom atom, ImmutableSubstitution<ImmutableTerm> substitution) {
        this.atom = atom;
        this.substitution = substitution;
    }

    @Override
    public DistinctVariableOnlyDataAtom getProjectionAtom() {
        return atom;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public TargetAtom rename(InjectiveVar2VarSubstitution renamingSubstitution) {
        return new TargetAtomImpl(renamingSubstitution.applyToDistinctVariableOnlyDataAtom(atom),
                renamingSubstitution.applyRenaming(substitution));
    }
}
