package it.unibz.inf.ontop.pivotalrepr;

/**
 * Transforms an IntermediateQuery
 */
public interface QueryTransformer {

    IntermediateQuery transform(IntermediateQuery originalQuery)
            throws IntermediateQueryBuilderException, NotNeededNodeException;
}
