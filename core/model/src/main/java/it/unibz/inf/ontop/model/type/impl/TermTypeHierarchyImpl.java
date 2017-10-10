package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeHierarchy;

import java.util.Optional;
import java.util.stream.Stream;

public class TermTypeHierarchyImpl<T extends TermType> implements TermTypeHierarchy<T> {

    private final ImmutableList<T> types;

    protected TermTypeHierarchyImpl(ImmutableList<T> types) {
        this.types = types;
    }

    @Override
    public Stream<T> getTermTypes() {
        return types.stream();
    }

    @Override
    public boolean contains(T termType) {
        return types.contains(termType);
    }

    protected Optional<T> getClosestCommonTermType(TermTypeHierarchy<T> otherHierarchy) {
        return otherHierarchy.getTermTypes()
                .filter(types::contains)
                .findFirst();
    }
}
