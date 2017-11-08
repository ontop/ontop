package it.unibz.inf.ontop.datalog.impl;

import it.unibz.inf.ontop.datalog.TargetAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

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
}
