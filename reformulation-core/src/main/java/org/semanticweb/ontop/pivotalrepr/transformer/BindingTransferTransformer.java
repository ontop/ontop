package org.semanticweb.ontop.pivotalrepr.transformer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.NonGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.FilterNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.GroupNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.LeftJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.proposal.BindingTransfer;

/**
 * Basic implementation: applies the bindings directly
 *
 * TODO: propose an optimized version that "extracts" the relevant variables from the bindings.
 *
 */
public class BindingTransferTransformer implements QueryNodeTransformer {

    private final ImmutableSubstitution<ImmutableTerm> transferredBindings;

    public BindingTransferTransformer(BindingTransfer transfer) {
        transferredBindings = transfer.getTransferredBindings();
    }

    @Override
    public FilterNode transform(FilterNode filterNode) {
        ImmutableBooleanExpression newBooleanExpression =
                transformOptionalFilterCondition(filterNode.getOptionalFilterCondition()).get();
        return new FilterNodeImpl(newBooleanExpression);
    }

    @Override
    public TableNode transform(TableNode tableNode) {
        return tableNode;
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return new LeftJoinNodeImpl(transformOptionalFilterCondition(leftJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public UnionNode transform(UnionNode unionNode) {
        return unionNode;
    }

    @Override
    public OrdinaryDataNode transform(OrdinaryDataNode ordinaryDataNode) {
        return ordinaryDataNode;
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode)  {
        return new InnerJoinNodeImpl(transformOptionalFilterCondition(innerJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) {
        return constructionNode;
    }

    @Override
    public GroupNode transform(GroupNode groupNode) throws QueryNodeTransformationException, NotNeededNodeException {
        ImmutableList.Builder<NonGroundTerm> groupingTermBuilder = ImmutableList.builder();
        for (NonGroundTerm groupingTerm : groupNode.getGroupingTerms()) {
            ImmutableTerm newTerm = transferredBindings.apply(groupingTerm);

            /**
             * We ignore the ground terms.
             */
            if (newTerm instanceof NonGroundTerm) {
                groupingTermBuilder.add((NonGroundTerm) newTerm);
            }
        }
        ImmutableList<NonGroundTerm> newGroupingTerms = groupingTermBuilder.build();
        /**
         * Declares the node as not needed anymore if it has no more grouping term.
         */
        if (newGroupingTerms.isEmpty()) {
            throw new NotNeededNodeException("This GROUP node is not needed anymore");
        }
        return new GroupNodeImpl(newGroupingTerms);
    }

    private Optional<ImmutableBooleanExpression> transformOptionalFilterCondition(
            Optional<ImmutableBooleanExpression> optionalFilterCondition) {
        if (optionalFilterCondition.isPresent()) {
            return Optional.of(transferredBindings.applyToBooleanExpression(optionalFilterCondition.get()));
        }
        else {
            return Optional.absent();
        }
    }
}
