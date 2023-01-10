package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.impl.AbstractProtoSubstitution;

public class SimpleProtoSubstitutionImpl<T extends ImmutableTerm> extends AbstractProtoSubstitution<T> {

    private final ImmutableMap<Variable, T> map;

    protected SimpleProtoSubstitutionImpl(ImmutableMap<Variable, ? extends T> substitutionMap,
                                        TermFactory termFactory) {
        super(termFactory);
        this.map = (ImmutableMap<Variable, T>) substitutionMap;

        if (substitutionMap.entrySet().stream().anyMatch(e -> e.getKey().equals(e.getValue())))
            throw new IllegalArgumentException("Please do not insert entries like t/t in your substitution " +
                    "(for efficiency reasons)\n. Proto-substitution: " + substitutionMap);
    }

    @Override
    public ImmutableMap<Variable, T> getImmutableMap() {
        return map;
    }
}
