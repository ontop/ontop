package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;

import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Renames query nodes according to one renaming substitution.
 */
public class QueryNodeRenamer implements HomogeneousQueryNodeTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final InjectiveVar2VarSubstitution renamingSubstitution;
    private final AtomFactory atomFactory;

    public QueryNodeRenamer(IntermediateQueryFactory iqFactory, InjectiveVar2VarSubstitution renamingSubstitution,
                            AtomFactory atomFactory) {
        this.iqFactory = iqFactory;
        this.renamingSubstitution = renamingSubstitution;
        this.atomFactory = atomFactory;
    }

    @Override
    public FilterNode transform(FilterNode filterNode) {
        return iqFactory.createFilterNode(renameBooleanExpression(filterNode.getFilterCondition()));
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return iqFactory.createExtensionalDataNode(
                extensionalDataNode.getRelationDefinition(),
                renamingSubstitution.applyToArgumentMap(extensionalDataNode.getArgumentMap()));
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return iqFactory.createLeftJoinNode(renameOptionalBooleanExpression(
                leftJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public UnionNode transform(UnionNode unionNode){
        return iqFactory.createUnionNode(renameProjectedVars(unionNode.getVariables()));
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return iqFactory.createIntensionalDataNode(renameDataAtom(intensionalDataNode.getProjectionAtom()));
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) {
        return iqFactory.createInnerJoinNode(renameOptionalBooleanExpression(innerJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) {
        return iqFactory.createConstructionNode(renameProjectedVars(constructionNode.getVariables()),
                renameSubstitution(constructionNode.getSubstitution()));
    }

    @Override
    public AggregationNode transform(AggregationNode aggregationNode) throws QueryNodeTransformationException {
        return iqFactory.createAggregationNode(renameProjectedVars(aggregationNode.getGroupingVariables()),
                renameSubstitution(aggregationNode.getSubstitution()));
    }

    private ImmutableSet<Variable> renameProjectedVars(ImmutableSet<Variable> projectedVariables) {
        return projectedVariables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public EmptyNode transform(EmptyNode emptyNode) {
        return iqFactory.createEmptyNode(emptyNode.getVariables());
    }

    public TrueNode transform(TrueNode trueNode) {
        return iqFactory.createTrueNode();
    }

    @Override
    public ValuesNode transform(ValuesNode valuesNode) throws QueryNodeTransformationException {
        ImmutableList<Variable> newOrderedVariables = valuesNode.getOrderedVariables().stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toList());

        return iqFactory.createValuesNode(newOrderedVariables, valuesNode.getValues());
    }

    @Override
    public DistinctNode transform(DistinctNode distinctNode) {
        return iqFactory.createDistinctNode();
    }

    @Override
    public SliceNode transform(SliceNode sliceNode) {
        return sliceNode.getLimit()
                .map(l -> iqFactory.createSliceNode(sliceNode.getOffset(), l))
                .orElseGet(() -> iqFactory.createSliceNode(sliceNode.getOffset()));
    }

    @Override
    public OrderByNode transform(OrderByNode orderByNode) {
        ImmutableList<OrderByNode.OrderComparator> newComparators = orderByNode.getComparators().stream()
                .map(c -> iqFactory.createOrderComparator(
                        renamingSubstitution.applyToTerm(c.getTerm()),
                        c.isAscending()))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createOrderByNode(newComparators);
    }

    private ImmutableExpression renameBooleanExpression(ImmutableExpression booleanExpression) {
        return renamingSubstitution.applyToBooleanExpression(booleanExpression);
    }


    private DataAtom<AtomPredicate> renameDataAtom(DataAtom<AtomPredicate> atom) {
        ImmutableList.Builder<VariableOrGroundTerm> argListBuilder = ImmutableList.builder();
        for (VariableOrGroundTerm term : atom.getArguments()) {
            argListBuilder.add(renamingSubstitution.applyToTerm(term));
        }
        return atomFactory.getDataAtom(atom.getPredicate(), argListBuilder.build());
    }

    private Optional<ImmutableExpression> renameOptionalBooleanExpression(
            Optional<ImmutableExpression> optionalExpression) {
        if (!optionalExpression.isPresent())
            return Optional.empty();

        ImmutableExpression expression = optionalExpression.get();
        return Optional.of(renameBooleanExpression(expression));
    }

    private <T extends ImmutableTerm> ImmutableSubstitution<T> renameSubstitution(ImmutableSubstitution<T> substitution) {
        return renamingSubstitution.applyRenaming(substitution);
    }
}
