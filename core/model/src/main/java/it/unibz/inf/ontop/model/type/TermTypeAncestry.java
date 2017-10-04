package it.unibz.inf.ontop.model.type;

import java.util.stream.Stream;


public interface TermTypeAncestry {

    /**
     * From the most specific to the most general ancestor
     */
    Stream<TermType> getTermTypes();

    /**
     * All the term types are expected to have the same origin
     */
    TermType getClosestCommonAncestor(TermTypeAncestry otherAncestry);

    /**
     * Builds a new ancestry
     */
    TermTypeAncestry newAncestry(TermType childType);

    boolean contains(TermType termType);
}
