package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
        this(predicate, ImmutableList.copyOf(variableOrGroundTerms));
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
    public VariableOrGroundTerm getTerm(int index) {
        return arguments.get(index);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return arguments.stream()
                .flatMap(ImmutableTerm::getVariableStream)
                .collect(ImmutableCollectors.toSet());
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

            Stream<String> argumentStrings = arguments.stream()
                    .map(VariableOrGroundTerm::toString);

            sb.append(argumentStrings.collect(Collectors.joining(", ")));
            sb.append(")");
            string = sb.toString();
        }
        return string;
    }
}
