package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;

public class ImmutableCQ<P extends AtomPredicate> {
    private final ImmutableList<Variable> answerVariables;
    private final ImmutableList<DataAtom<P>> atoms;

    public ImmutableCQ(ImmutableList<Variable> answerVariables, ImmutableList<DataAtom<P>> atoms) {
        this.answerVariables = answerVariables;
        this.atoms = atoms;
    }

    public ImmutableList<Variable> getAnswerVariables() {
        return answerVariables;
    }

    public ImmutableList<DataAtom<P>> getAtoms() {
        return atoms;
    }

    @Override
    public int hashCode() {
        return atoms.hashCode() ^ answerVariables.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ImmutableCQ)
                && atoms.equals(((ImmutableCQ)other).atoms)
                && answerVariables.equals(((ImmutableCQ)other).answerVariables);
    }

    @Override
    public String toString() {
        return "q" + answerVariables + " :- " + atoms;
    }
}
