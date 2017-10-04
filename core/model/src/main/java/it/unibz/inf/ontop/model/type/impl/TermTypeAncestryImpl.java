package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;


public class TermTypeAncestryImpl implements TermTypeAncestry {

    private final ImmutableList<TermType> types;

    private TermTypeAncestryImpl(ImmutableList<TermType> types) {
        this.types = types;
    }

    /**
     * ONLY for the TermType origin (which must be unique)!
     */
    protected TermTypeAncestryImpl(TermType origin) {
        this.types = ImmutableList.of(origin);
    }

    @Override
    public Stream<TermType> getTermTypes() {
        return types.stream();
    }

    @Override
    public TermType getClosestCommonAncestor(TermTypeAncestry otherAncestry) {
        return otherAncestry.getTermTypes()
                .filter(types::contains)
                .findFirst()
                .orElseThrow(DifferentTermTypeOriginException::new);
    }

    @Override
    public TermTypeAncestry newAncestry(TermType childType) {
        ImmutableList<TermType> newAncestors = Stream.concat(Stream.of(childType), types.stream())
                .collect(ImmutableCollectors.toList());
        return new TermTypeAncestryImpl(newAncestors);
    }

    @Override
    public boolean contains(TermType termType) {
        return types.contains(termType);
    }

    /**
     * Internal bug: all the term types must have the same origin
     */
    private static class DifferentTermTypeOriginException extends OntopInternalBugException {

        private DifferentTermTypeOriginException() {
            super("Internal bug: all all the term types must have the same origin");
        }
    }
}
