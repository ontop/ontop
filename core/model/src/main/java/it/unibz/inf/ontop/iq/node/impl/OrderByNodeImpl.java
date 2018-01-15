package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.NodeTransformationProposal;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.QueryNodeVisitor;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class OrderByNodeImpl extends QueryModifierNodeImpl implements OrderByNode {

    private static final String ORDER_BY_NODE_STR = "ORDER BY";

    private final ImmutableList<OrderComparator> comparators;


    @AssistedInject
    private OrderByNodeImpl(@Assisted ImmutableList<OrderComparator> comparators, IntermediateQueryFactory iqFactory) {
        super(iqFactory);
        this.comparators = comparators;
    }

    @Override
    public ImmutableList<OrderComparator> getComparators() {
        return comparators;
    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child) {
        return transformer.transformOrderBy(tree, this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        if (!child.getVariables().containsAll(getLocalVariables())) {
            throw new InvalidIntermediateQueryException("Some variables used in the node " + this
                    + " are not provided by its child " + child);
        }
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public OrderByNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return comparators.stream()
                .flatMap(c -> c.getTerm().getVariableStream())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return isEquivalentTo(node);
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return queryNode instanceof OrderByNode
                && ((OrderByNode) queryNode).getComparators().equals(comparators);
    }

    @Override
    public OrderByNode clone() {
        return iqFactory.createOrderByNode(comparators);
    }

    @Override
    public String toString() {
        return ORDER_BY_NODE_STR + " " + comparators;
    }
}
