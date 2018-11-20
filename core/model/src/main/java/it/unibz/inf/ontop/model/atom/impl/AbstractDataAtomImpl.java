package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class AbstractDataAtomImpl<P extends AtomPredicate>
        implements DataAtom<P> {

    private final P predicate;
    private final ImmutableList<? extends VariableOrGroundTerm> arguments;

    // Lazy (cache)
    @Nullable
    private String string;

    protected AbstractDataAtomImpl(P predicate, ImmutableList<? extends VariableOrGroundTerm> variableOrGroundTerms) {
        this.predicate = predicate;
        this.arguments = variableOrGroundTerms;
        this.string = null;

        if (predicate.getArity() != arguments.size()) {
            throw new IllegalArgumentException("Arity violation: " + predicate + " was expecting " + predicate.getArity()
                    + ", not " + arguments.size());
        }
    }

    protected AbstractDataAtomImpl(P predicate, VariableOrGroundTerm... variableOrGroundTerms) {
        this.predicate = predicate;
        this.arguments = ImmutableList.copyOf(variableOrGroundTerms);
    }

    @Override
    public P getPredicate() {
        return predicate;
    }

    @Override
    public int getArity() {
        return predicate.getArity();
    }

    @Override
    public int getEffectiveArity() {
        return arguments.size();
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
    public ImmutableSet<Variable> getVariables() {
        return arguments.stream()
                .flatMap(ImmutableTerm::getVariableStream)
                .collect(ImmutableCollectors.toSet());
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

    /**
     * Cached toString()
     */
    @Override
    public String toString() {
        if (string == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(predicate.toString());
            sb.append("(");

            List<String> argumentStrings = arguments.stream()
                    .map(VariableOrGroundTerm::toString)
                    .collect(Collectors.toList());

            sb.append(String.join(",", argumentStrings));
            sb.append(")");
            string = sb.toString();
        }
        return string;
    }
}
