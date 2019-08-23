package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;


public class TermTypeAncestryImpl extends TermTypeHierarchyImpl<TermType> implements TermTypeAncestry {

    /**
     * ONLY for the TermType origin (which must be unique)!
     */
    protected TermTypeAncestryImpl(TermType origin) {
        this(ImmutableList.of(origin));
    }

    private TermTypeAncestryImpl(ImmutableList<TermType> newAncestors) {
        super(newAncestors);
    }

    @Override
    public TermType getClosestCommonAncestor(TermTypeAncestry otherAncestry) {
        return getClosestCommonTermType(otherAncestry)
                .orElseThrow(DifferentTermTypeOriginException::new);
    }

    @Override
    public TermTypeAncestry newAncestry(TermType childType) {
        ImmutableList<TermType> newAncestors = Stream.concat(Stream.of(childType), getTermTypes())
                .collect(ImmutableCollectors.toList());
        return new TermTypeAncestryImpl(newAncestors);
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
