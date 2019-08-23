package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
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
        return iqFactory.createExtensionalDataNode(renameDataAtom(extensionalDataNode.getProjectionAtom()));
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return iqFactory.createLeftJoinNode(renameOptionalBooleanExpression(
                leftJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public UnionNode transform(UnionNode unionNode){
        return iqFactory.createUnionNode(renameProjectedVars(unionNode.getVariables()));
//        return unionNode.clone();
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

    private ImmutableSet<Variable> renameProjectedVars(ImmutableSet<Variable> projectedVariables) {
        return projectedVariables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
    }

    @Override
    public EmptyNode transform(EmptyNode emptyNode) {
        return emptyNode.clone();
    }

    public TrueNode transform(TrueNode trueNode) {
        return trueNode.clone();
    }

    @Override
    public DistinctNode transform(DistinctNode distinctNode) {
        return iqFactory.createDistinctNode();
    }

    @Override
    public SliceNode transform(SliceNode sliceNode) {
        return sliceNode.clone();
    }

    @Override
    public OrderByNode transform(OrderByNode orderByNode) {
        ImmutableList<OrderByNode.OrderComparator> newComparators = orderByNode.getComparators().stream()
                .map(c -> iqFactory.createOrderComparator(
                        renamingSubstitution.applyToNonGroundTerm(c.getTerm()),
                        c.isAscending()))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createOrderByNode(newComparators);
    }

    private ImmutableExpression renameBooleanExpression(ImmutableExpression booleanExpression) {
        return renamingSubstitution.applyToBooleanExpression(booleanExpression);
    }


    private DataAtom renameDataAtom(DataAtom<? extends AtomPredicate> atom) {
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

    private ImmutableSubstitution<ImmutableTerm> renameSubstitution(ImmutableSubstitution<ImmutableTerm> substitution) {
        return renamingSubstitution.applyRenaming(substitution);
    }
}
