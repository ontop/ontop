package it.unibz.inf.ontop.substitution.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

/**
 * Wrapper above an {@code ImmutableMap<Variable, ImmutableTerm>} map.
 */

public class ImmutableSubstitutionImpl<T extends ImmutableTerm> extends AbstractImmutableSubstitutionImpl<T> {

    private final ImmutableMap<Variable, T> map;

    protected ImmutableSubstitutionImpl(ImmutableMap<Variable, ? extends T> substitutionMap,
                                        TermFactory termFactory) {
        super(termFactory);
        this.map = (ImmutableMap<Variable, T>) substitutionMap;

        if (substitutionMap.entrySet().stream().anyMatch(e -> e.getKey().equals(e.getValue())))
            throw new IllegalArgumentException("Please do not insert entries like t/t in your substitution " +
                    "(for efficiency reasons)\n. Substitution: " + substitutionMap);
    }

    @Override
    public ImmutableMap<Variable, T> getImmutableMap() {
        return map;
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }

    @Override
    protected ImmutableSubstitution<T> constructNewSubstitution(ImmutableMap<Variable, T> map) {
        return new ImmutableSubstitutionImpl<T>(map, termFactory);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ImmutableSubstitution)
                && map.equals(((ImmutableSubstitution<?>) o).getImmutableMap());
    }
}
