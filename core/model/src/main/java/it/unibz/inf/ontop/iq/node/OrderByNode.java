package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.Optional;

/**
 * Follows a NULLS FIRST semantics (if ascending, NULLS LAST otherwise), similarly to SPARQL
 *
 * See https://www.w3.org/TR/sparql11-query/#modOrderBy
 *
 * See {@link IntermediateQueryFactory#createOrderByNode} for creating a new instance.
 */
public interface OrderByNode extends QueryModifierNode {

    ImmutableList<OrderComparator> getComparators();

    Optional<OrderByNode> applySubstitution(Substitution<? extends ImmutableTerm> substitution);

    interface OrderComparator {

        boolean isAscending();

        NonGroundTerm getTerm();
    }

    @Override
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformOrderBy(tree, this, child);
    }

    @Override
    default <T> T acceptVisitor(IQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.visitOrderBy(tree, this, child);
    }

}
