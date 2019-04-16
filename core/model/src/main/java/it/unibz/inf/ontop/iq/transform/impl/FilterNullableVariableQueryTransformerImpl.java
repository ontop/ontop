package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.NotFilterableNullVariableException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.FilterNullableVariableQueryTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

@Singleton
public class FilterNullableVariableQueryTransformerImpl implements FilterNullableVariableQueryTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private FilterNullableVariableQueryTransformerImpl(IntermediateQueryFactory iqFactory,
                                                       TermFactory termFactory,
                                                       SubstitutionFactory substitutionFactory,
                                                       CoreUtilsFactory coreUtilsFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public IntermediateQuery transform(IntermediateQuery query) throws NotFilterableNullVariableException {
        QueryNode rootNode = query.getRootNode();
        ImmutableList<Variable> nullableProjectedVariables = query.getProjectionAtom().getVariables().stream()
                .filter(v -> rootNode.isVariableNullable(query, v))
                .collect(ImmutableCollectors.toList());

        if (nullableProjectedVariables.isEmpty())
            return query;

        ImmutableExpression filterCondition = extractFilteringCondition(query, rootNode, nullableProjectedVariables);
        return constructQuery(query, rootNode, filterCondition);
    }

    private ImmutableExpression extractFilteringCondition(IntermediateQuery query, QueryNode rootNode,
                                                          ImmutableList<Variable> nullableProjectedVariables)
            throws NotFilterableNullVariableException {

        ImmutableSubstitution<ImmutableTerm> topSubstitution = Optional.of(rootNode)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode)n)
                .map(ConstructionNode::getSubstitution)
                .orElseGet(substitutionFactory::getSubstitution);

        Stream<ImmutableExpression> filteringExpressionStream = nullableProjectedVariables.stream()
                .map(v -> Optional.ofNullable(topSubstitution.get(v))
                        .orElse(v))
                .map(termFactory::getDBIsNotNull)
                .distinct();

        ImmutableExpression nonOptimizedExpression = termFactory.getConjunction(filteringExpressionStream)
                .orElseThrow(() -> new IllegalArgumentException("Is nullableProjectedVariables empty? After folding" +
                        "there should be one expression"));
        IncrementalEvaluation evaluationResult = nonOptimizedExpression.evaluate2VL(
                coreUtilsFactory.createDummyVariableNullability(nonOptimizedExpression), false);

        switch (evaluationResult.getStatus()) {
            case SAME_EXPRESSION:
                return nonOptimizedExpression;
            case SIMPLIFIED_EXPRESSION:
                return evaluationResult.getNewExpression().get();
            case IS_NULL:
            case IS_FALSE:
                throw new NotFilterableNullVariableException(query, nonOptimizedExpression);
            case IS_TRUE:
            default:
                throw new UnexpectedTrueExpressionException(nonOptimizedExpression);
        }
    }

    private IntermediateQuery constructQuery(IntermediateQuery query, QueryNode rootNode,
                                             ImmutableExpression filterCondition) {
        IntermediateQueryBuilder queryBuilder = query.newBuilder();
        queryBuilder.init(query.getProjectionAtom(), rootNode);

        QueryNode formerRootChild = query.getFirstChild(rootNode)
                .orElseThrow(() -> new InvalidIntermediateQueryException("The root node does not have a child " +
                        "while it has some child variables."));

        Queue<QueryNode> parentQueue = new LinkedList<>();

        if (formerRootChild instanceof FilterNode) {
            FilterNode newFilterNode = iqFactory.createFilterNode(
                    termFactory.getConjunction(
                            ((FilterNode) formerRootChild).getFilterCondition(),
                            filterCondition));
            queryBuilder.addChild(rootNode, newFilterNode);
            query.getChildren(formerRootChild)
                    .forEach(c -> {
                        queryBuilder.addChild(newFilterNode, c);
                        parentQueue.add(c);
                    });
        }
        else if (formerRootChild instanceof InnerJoinNode) {
            InnerJoinNode formerJoinNode = (InnerJoinNode) formerRootChild;

            ImmutableExpression newJoiningCondition = formerJoinNode.getOptionalFilterCondition()
                    .map(e -> termFactory.getConjunction(e, filterCondition))
                    .orElse(filterCondition);
            InnerJoinNode newJoinNode = formerJoinNode.changeOptionalFilterCondition(Optional.of(newJoiningCondition));

            queryBuilder.addChild(rootNode, newJoinNode);
            query.getChildren(formerJoinNode)
                    .forEach(c -> {
                        queryBuilder.addChild(newJoinNode, c);
                        parentQueue.add(c);
                    });
        }
        else {
            FilterNode filterNode = iqFactory.createFilterNode(filterCondition);
            queryBuilder.addChild(rootNode, filterNode);
            queryBuilder.addChild(filterNode, formerRootChild);
            parentQueue.add(formerRootChild);
        }

        while (!parentQueue.isEmpty()) {
            QueryNode parentNode = parentQueue.poll();
            query.getChildren(parentNode)
                    .forEach(c -> {
                        queryBuilder.addChild(parentNode, c, query.getOptionalPosition(parentNode, c));
                        parentQueue.add(c);
                    });
        }

        return queryBuilder.build();
    }


    private static class UnexpectedTrueExpressionException extends OntopInternalBugException {

        private UnexpectedTrueExpressionException(ImmutableExpression expression) {
            super("Bad expression produced: " + expression + ". It should not be evaluated as true");
        }
    }

}
