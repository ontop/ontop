package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.Objects;
import java.util.Optional;

public class ImmutableCQ<P extends AtomPredicate> {
    private final ImmutableSet<Variable> answerVariables;
    private final Substitution<VariableOrGroundTerm> substitution;
    private final ImmutableList<DataAtom<P>> atoms;

    public ImmutableCQ(ImmutableSet<Variable> answerVariables, Substitution<VariableOrGroundTerm> substitution, ImmutableList<DataAtom<P>> atoms) {
        this.answerVariables = answerVariables;
        this.substitution = substitution;
        this.atoms = atoms;
    }

    public VariableOrGroundTerm getAnswerTerm(Variable variable) {
        return Optional.ofNullable(substitution.get(variable)).orElse(variable);
    }

    public ImmutableSet<Variable> getAnswerVariables() {
        return answerVariables;
    }

    public Substitution<VariableOrGroundTerm> getSubstitution() {
        return substitution;
    }

    public ImmutableList<DataAtom<P>> getAtoms() {
        return atoms;
    }

    @Override
    public int hashCode() {
        return Objects.hash(atoms, answerVariables, substitution);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableCQ) {
            ImmutableCQ<?> otherCQ = (ImmutableCQ<?>) other;
            return atoms.equals(otherCQ.atoms)
                    && answerVariables.equals(otherCQ.answerVariables)
                    && substitution.equals(otherCQ.substitution);
        }
        return false;
    }

    @Override
    public String toString() {
        return "q" + answerVariables + "/"  + substitution + " :- " + atoms;
    }
}
