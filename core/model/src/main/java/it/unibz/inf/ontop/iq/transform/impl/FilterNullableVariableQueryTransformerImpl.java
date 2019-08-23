package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.NotFilterableNullVariableException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator.EvaluationResult;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.FilterNullableVariableQueryTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.IS_NOT_NULL;

@Singleton
public class FilterNullableVariableQueryTransformerImpl implements FilterNullableVariableQueryTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final ExpressionEvaluator defaultExpressionEvaluator;
    private final TermFactory termFactory;
    private final ImmutabilityTools immutabilityTools;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private FilterNullableVariableQueryTransformerImpl(IntermediateQueryFactory iqFactory,
                                                       ExpressionEvaluator defaultExpressionEvaluator,
                                                       TermFactory termFactory, ImmutabilityTools immutabilityTools,
                                                       SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.defaultExpressionEvaluator = defaultExpressionEvaluator;
        this.termFactory = termFactory;
        this.immutabilityTools = immutabilityTools;
        this.substitutionFactory = substitutionFactory;
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
                .map(t -> termFactory.getImmutableExpression(IS_NOT_NULL, t))
                .distinct();

        ImmutableExpression nonOptimizedExpression = immutabilityTools.foldBooleanExpressions(filteringExpressionStream)
                .orElseThrow(() -> new IllegalArgumentException("Is nullableProjectedVariables empty? After folding" +
                        "there should be one expression"));
        EvaluationResult evaluationResult = defaultExpressionEvaluator.clone()
                .evaluateExpression(nonOptimizedExpression);

        Optional<ImmutableExpression> optionalExpression = evaluationResult.getOptionalExpression();
        if (optionalExpression.isPresent())
            return optionalExpression.get();
        else if (evaluationResult.isEffectiveFalse())
            throw new NotFilterableNullVariableException(query, nonOptimizedExpression);
        else
            throw new UnexpectedTrueExpressionException(nonOptimizedExpression);
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
                    immutabilityTools.foldBooleanExpressions(
                            ((FilterNode) formerRootChild).getFilterCondition(),
                            filterCondition).get());
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
                    .map(e -> immutabilityTools.foldBooleanExpressions(e, filterCondition).get())
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
