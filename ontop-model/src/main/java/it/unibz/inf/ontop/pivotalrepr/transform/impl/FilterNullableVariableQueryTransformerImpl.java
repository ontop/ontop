package it.unibz.inf.ontop.pivotalrepr.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.NotFilterableNullVariableException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator.EvaluationResult;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.transform.FilterNullableVariableQueryTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.ExpressionOperation.IS_NOT_NULL;
import static it.unibz.inf.ontop.model.impl.ImmutabilityTools.foldBooleanExpressions;
import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

@Singleton
public class FilterNullableVariableQueryTransformerImpl implements FilterNullableVariableQueryTransformer {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private FilterNullableVariableQueryTransformerImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IntermediateQuery transform(IntermediateQuery query) throws NotFilterableNullVariableException {
        ConstructionNode rootNode = query.getRootConstructionNode();
        ImmutableList<Variable> nullableProjectedVariables = rootNode.getVariables().stream()
                .filter(v -> rootNode.isVariableNullable(query, v))
                .collect(ImmutableCollectors.toList());

        if (nullableProjectedVariables.isEmpty())
            return query;

        ImmutableExpression filterCondition = extractFilteringCondition(query, rootNode, nullableProjectedVariables);
        return constructQuery(query, rootNode, filterCondition);
    }

    private ImmutableExpression extractFilteringCondition(IntermediateQuery query, ConstructionNode rootNode,
                                                          ImmutableList<Variable> nullableProjectedVariables)
            throws NotFilterableNullVariableException {

        ImmutableSubstitution<ImmutableTerm> topSubstitution = rootNode.getSubstitution();

        Stream<ImmutableExpression> filteringExpressionStream = nullableProjectedVariables.stream()
                .map(v -> Optional.ofNullable(topSubstitution.get(v))
                        .orElse(v))
                .map(t -> DATA_FACTORY.getImmutableExpression(IS_NOT_NULL, t))
                .distinct();

        ImmutableExpression nonOptimizedExpression = foldBooleanExpressions(filteringExpressionStream)
                .orElseThrow(() -> new IllegalArgumentException("Is nullableProjectedVariables empty? After folding" +
                        "there should be one expression"));
        EvaluationResult evaluationResult = new ExpressionEvaluator()
                .evaluateExpression(nonOptimizedExpression);

        Optional<ImmutableExpression> optionalExpression = evaluationResult.getOptionalExpression();
        if (optionalExpression.isPresent())
            return optionalExpression.get();
        else if (evaluationResult.isEffectiveFalse())
            throw new NotFilterableNullVariableException(query, nonOptimizedExpression);
        else
            throw new UnexpectedTrueExpressionException(nonOptimizedExpression);
    }

    private IntermediateQuery constructQuery(IntermediateQuery query, ConstructionNode rootNode,
                                             ImmutableExpression filterCondition) {
        IntermediateQueryBuilder queryBuilder = query.newBuilder();
        queryBuilder.init(query.getProjectionAtom(), rootNode);
        FilterNode filterNode = iqFactory.createFilterNode(filterCondition);
        queryBuilder.addChild(rootNode, filterNode);

        ImmutableList<QueryNode> formerRootChildren = query.getChildren(rootNode);

        formerRootChildren
                .forEach(c -> queryBuilder.addChild(filterNode, c));

        Queue<QueryNode> parentQueue = new LinkedList<>(formerRootChildren);
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
