package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.AbstractFunctionalTermImpl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class AbstractDataAtomImpl<P extends AtomPredicate>
        extends AbstractFunctionalTermImpl
        implements DataAtom<P> {

    private final P predicate;
    private final ImmutableList<? extends VariableOrGroundTerm> arguments;

    protected AbstractDataAtomImpl(P predicate, ImmutableList<? extends VariableOrGroundTerm> variableOrGroundTerms) {
        super(predicate);
        this.predicate = predicate;
        this.arguments = variableOrGroundTerms;

        if (predicate.getArity() != arguments.size()) {
            throw new IllegalArgumentException("Arity violation: " + predicate + " was expecting " + predicate.getArity()
                    + ", not " + arguments.size());
        }
    }

    protected AbstractDataAtomImpl(P predicate, VariableOrGroundTerm... variableOrGroundTerms) {
        super(predicate);
        this.predicate = predicate;
        this.arguments = ImmutableList.copyOf(variableOrGroundTerms);
    }

    @Override
    public P getPredicate() {
        return predicate;
    }

    @Override
    public int getEffectiveArity() {
        return getTerms().size();
    }

    @Override
    public ImmutableList<? extends VariableOrGroundTerm> getArguments() {
        return arguments;
    }

    @Override
    public boolean containsGroundTerms() {
        for (ImmutableTerm term : getArguments()) {
            if (term.isGround()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public VariableOrGroundTerm getTerm(int index) {
        return arguments.get(index);
    }

    @Override
    public ImmutableList<Term> getTerms() {
        return ImmutableList.copyOf(arguments);
    }

    @Override
    public void setTerm(int index, Term term) {
        throw new UnsupportedOperationException("A DataAtom is immutable.");
    }

    @Override
    public void updateTerms(List<Term> literals) {
        throw new UnsupportedOperationException("A DataAtom is immutable.");
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return ImmutableSet.copyOf(super.getVariables());
    }

    protected static boolean hasDuplicates(DataAtom atom) {
        ImmutableList<? extends VariableOrGroundTerm> termList = atom.getArguments();
        Set<VariableOrGroundTerm> termSet = new HashSet<>(termList);

        return termSet.size() < termList.size();
    }

    /**
     * TODO: improve
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof DataAtom) {
            return toString().equals(other.toString());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
