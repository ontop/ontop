package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.QueryTransformationException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.AND;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.IS_NOT_NULL;

public class NoNullValuesEnforcerImpl implements NoNullValueEnforcer {


    private final IntermediateQueryFactory iQFactory;
    private final TermFactory termFactory;

    @Inject
    private NoNullValuesEnforcerImpl(IntermediateQueryFactory iQFactory, TermFactory termFactory) {
        this.iQFactory = iQFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IntermediateQuery transform(IntermediateQuery originalQuery) throws QueryTransformationException {
        QueryNode rootNode = originalQuery.getRootNode();
        ImmutableList<Variable> nullableVariables = extractNullableVariables(originalQuery, rootNode);
        return nullableVariables.isEmpty() ?
                originalQuery :
                insertFilter(originalQuery, nullableVariables);
    }

    private ImmutableList<Variable> extractNullableVariables(IntermediateQuery query, QueryNode rootNode) {
        ImmutableSet<Variable> requiredVariables = rootNode instanceof ConstructionNode ?
                rootNode.getRequiredVariables(query):
                query.getVariables(rootNode);
        return requiredVariables.stream()
                .filter(v -> rootNode.isVariableNullable(query, v))
                .collect(ImmutableCollectors.toList());
    }


    private ImmutableExpression computeFilterExpression(ImmutableList<Variable> variables) {
        return variables.stream()
                .map(v -> termFactory.getImmutableExpression(IS_NOT_NULL, v))
                .reduce(null, (a, b) -> (a == null) ? b : termFactory.getImmutableExpression(AND, a, b));
    }

    /**
     * If the root is a construction node, inserts the filter below it.
     * Otherwise inserts it as the root.
     */
    private IntermediateQuery insertFilter(IntermediateQuery originalQuery, ImmutableList<Variable> nullableVariables) {

        FilterNode filterNode = iQFactory.createFilterNode(computeFilterExpression(nullableVariables));
        IntermediateQueryBuilder builder = originalQuery.newBuilder();
        QueryNode rootNode = originalQuery.getRootNode();
        QueryNode newRoot = rootNode instanceof ConstructionNode ?
                rootNode :
                filterNode;
        QueryNode child = rootNode instanceof ConstructionNode ?
                filterNode :
                rootNode;
        builder.init(originalQuery.getProjectionAtom(), newRoot);
        builder.addChild(newRoot, child, Optional.empty());
        copyChildren(originalQuery, rootNode, builder, child);
        return builder.build();
    }


    private void copyChildren(IntermediateQuery sourceQuery, QueryNode sourceParent, IntermediateQueryBuilder builder, QueryNode targetParent) {
        for (QueryNode child : sourceQuery.getChildren(sourceParent)) {
            builder.addChild(
                    targetParent,
                    child,
                    sourceQuery.getOptionalPosition(
                            sourceParent,
                            child
                    ));
            copyChildren(sourceQuery, child, builder, child);
        }
    }
}
