package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;

import it.unibz.inf.ontop.iq.transform.node.DefaultQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.iq.transform.node.QueryNodeTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

/**
 * Renames query nodes according to one renaming substitution.
 */
public class QueryNodeRenamer extends DefaultQueryNodeTransformer {

    private final InjectiveSubstitution<Variable> renamingSubstitution;
    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;

    public QueryNodeRenamer(IntermediateQueryFactory iqFactory, InjectiveSubstitution<Variable> renamingSubstitution,
                            AtomFactory atomFactory, SubstitutionFactory substitutionFactory) {
        super(iqFactory);
        this.renamingSubstitution = renamingSubstitution;
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public FilterNode transform(FilterNode filterNode, UnaryIQTree tree) {
        ImmutableExpression booleanExpression = filterNode.getFilterCondition();
        return iqFactory.createFilterNode(renamingSubstitution.apply(booleanExpression));
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return iqFactory.createExtensionalDataNode(
                extensionalDataNode.getRelationDefinition(),
                substitutionFactory.onVariableOrGroundTerms().applyToTerms(renamingSubstitution, extensionalDataNode.getArgumentMap()));
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode, BinaryNonCommutativeIQTree tree) {
        Optional<ImmutableExpression> optionalExpression = leftJoinNode.getOptionalFilterCondition();
        return iqFactory.createLeftJoinNode(optionalExpression.map(renamingSubstitution::apply));
    }

    @Override
    public UnionNode transform(UnionNode unionNode, NaryIQTree tree) {
        return iqFactory.createUnionNode(substitutionFactory.apply(renamingSubstitution, unionNode.getVariables()));
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        DataAtom<AtomPredicate> atom = intensionalDataNode.getProjectionAtom();
        return iqFactory.createIntensionalDataNode(atomFactory.getDataAtom(
                atom.getPredicate(),
                substitutionFactory.onVariableOrGroundTerms().applyToTerms(renamingSubstitution, atom.getArguments())));
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode, NaryIQTree tree) {
        Optional<ImmutableExpression> optionalExpression = innerJoinNode.getOptionalFilterCondition();
        return iqFactory.createInnerJoinNode(optionalExpression.map(renamingSubstitution::apply));
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode, UnaryIQTree tree) {
        Substitution<ImmutableTerm> substitution = constructionNode.getSubstitution();
        return iqFactory.createConstructionNode(
                substitutionFactory.apply(renamingSubstitution, constructionNode.getVariables()),
                substitutionFactory.rename(renamingSubstitution, substitution));
    }

    @Override
    public AggregationNode transform(AggregationNode aggregationNode, UnaryIQTree tree) throws QueryNodeTransformationException {
        Substitution<ImmutableFunctionalTerm> substitution = aggregationNode.getSubstitution();
        return iqFactory.createAggregationNode(
                substitutionFactory.apply(renamingSubstitution, aggregationNode.getGroupingVariables()),
                substitutionFactory.onImmutableFunctionalTerms().rename(renamingSubstitution, substitution));
    }

    @Override
    public FlattenNode transform(FlattenNode flattenNode, UnaryIQTree tree) {
        return iqFactory.createFlattenNode(
                substitutionFactory.apply(renamingSubstitution, flattenNode.getOutputVariable()),
                substitutionFactory.apply(renamingSubstitution, flattenNode.getFlattenedVariable()),
                flattenNode.getIndexVariable()
                        .map(v -> substitutionFactory.apply(renamingSubstitution, v)),
                flattenNode.getFlattenedType());
    }

    @Override
    public EmptyNode transform(EmptyNode emptyNode) {
        ImmutableSet<Variable> newVariables = substitutionFactory.apply(renamingSubstitution, emptyNode.getVariables());
        return iqFactory.createEmptyNode(newVariables);
    }

    @Override
    public TrueNode transform(TrueNode trueNode) {
        return iqFactory.createTrueNode();
    }

    @Override
    public ValuesNode transform(ValuesNode valuesNode) throws QueryNodeTransformationException {
        return iqFactory.createValuesNode(
                substitutionFactory.apply(renamingSubstitution, valuesNode.getOrderedVariables()),
                valuesNode.getValues());
    }

    @Override
    public DistinctNode transform(DistinctNode distinctNode, UnaryIQTree tree) {
        return iqFactory.createDistinctNode();
    }

    @Override
    public SliceNode transform(SliceNode sliceNode, UnaryIQTree tree) {
        return sliceNode.getLimit().isPresent()
                ? iqFactory.createSliceNode(sliceNode.getOffset(), sliceNode.getLimit().getAsLong())
                : iqFactory.createSliceNode(sliceNode.getOffset());
    }

    @Override
    public OrderByNode transform(OrderByNode orderByNode, UnaryIQTree tree) {
        ImmutableList<OrderByNode.OrderComparator> newComparators = orderByNode.getComparators().stream()
                .map(c -> iqFactory.createOrderComparator(
                        substitutionFactory.onNonGroundTerms().rename(renamingSubstitution, c.getTerm()),
                        c.isAscending()))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createOrderByNode(newComparators);
    }
}
