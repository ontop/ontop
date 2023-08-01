package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public class ImmutableCQ<P extends AtomPredicate> {
    private final ImmutableList<VariableOrGroundTerm> answerTerms;
    private final ImmutableList<DataAtom<P>> atoms;

    public ImmutableCQ(ImmutableList<VariableOrGroundTerm> answerTerms, ImmutableList<DataAtom<P>> atoms) {
        this.answerTerms = answerTerms;
        this.atoms = atoms;
    }

    public ImmutableList<VariableOrGroundTerm> getAnswerTerms() {
        return answerTerms;
    }

    public ImmutableList<DataAtom<P>> getAtoms() {
        return atoms;
    }

    @Override
    public int hashCode() {
        return atoms.hashCode() ^ answerTerms.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ImmutableCQ)
                && atoms.equals(((ImmutableCQ<?>)other).atoms)
                && answerTerms.equals(((ImmutableCQ<?>)other).answerTerms);
    }

    @Override
    public String toString() {
        return "q" + answerTerms + " :- " + atoms;
    }
}
