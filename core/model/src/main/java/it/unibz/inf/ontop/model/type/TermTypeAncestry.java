package it.unibz.inf.ontop.model.type;


/**
 * Ancestry: "natural" hierarchy of Term types
 *
 */
public interface TermTypeAncestry extends TermTypeHierarchy<TermType> {

    /**
     * All the term types are expected to have the same origin
     */
    TermType getClosestCommonAncestor(TermTypeAncestry otherAncestry);

    /**
     * Builds a new ancestry
     */
    TermTypeAncestry newAncestry(TermType childType);
}
