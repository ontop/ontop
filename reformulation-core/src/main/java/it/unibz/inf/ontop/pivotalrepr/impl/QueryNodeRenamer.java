package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.pivotalrepr.*;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Renames query nodes according to one renaming substitution.
 */
public class QueryNodeRenamer implements HomogeneousQueryNodeTransformer {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final InjectiveVar2VarSubstitution renamingSubstitution;

    public QueryNodeRenamer(InjectiveVar2VarSubstitution renamingSubstitution) {
        this.renamingSubstitution = renamingSubstitution;
    }

    @Override
    public FilterNode transform(FilterNode filterNode) {
        return new FilterNodeImpl(renameBooleanExpression(
                filterNode.getOptionalFilterCondition().get()));
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return new ExtensionalDataNodeImpl(renameDataAtom(extensionalDataNode.getProjectionAtom()));
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return new LeftJoinNodeImpl(renameOptionalBooleanExpression(
                leftJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public UnionNode transform(UnionNode unionNode){
        return new UnionNodeImpl(renameProjectedVars(
                unionNode.getVariables()));
//        return unionNode.clone();
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return new IntensionalDataNodeImpl(renameDataAtom(intensionalDataNode.getProjectionAtom()));
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) {
        return new InnerJoinNodeImpl(renameOptionalBooleanExpression(
                innerJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) {
        return new ConstructionNodeImpl(renameProjectedVars(constructionNode.getVariables()),
                renameSubstitution(constructionNode.getSubstitution()),
                renameOptionalModifiers(constructionNode.getOptionalModifiers())
                );
    }

    private ImmutableSet<Variable> renameProjectedVars(ImmutableSet<Variable> projectedVariables) {
        return projectedVariables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
    }

    @Override
    public GroupNode transform(GroupNode groupNode) {
        ImmutableList.Builder<NonGroundTerm> renamedTermBuilder = ImmutableList.builder();
        for (NonGroundTerm term : groupNode.getGroupingTerms()) {
            renamedTermBuilder.add(renamingSubstitution.applyToNonGroundTerm(term));
        }
        return new GroupNodeImpl(renamedTermBuilder.build());
    }

    @Override
    public EmptyNode transform(EmptyNode emptyNode) {
        return emptyNode.clone();
    }

    public TrueNode transform(TrueNode trueNode) {
        return trueNode.clone();
    }

    private Optional<ImmutableQueryModifiers> renameOptionalModifiers(Optional<ImmutableQueryModifiers> optionalModifiers) {
        if (optionalModifiers.isPresent()) {
            return renamingSubstitution.applyToQueryModifiers(optionalModifiers.get());
        }
        else {
            return Optional.empty();
        }
    }

    private ImmutableExpression renameBooleanExpression(ImmutableExpression booleanExpression) {
        return renamingSubstitution.applyToBooleanExpression(booleanExpression);
    }


    private DataAtom renameDataAtom(DataAtom atom) {
        ImmutableList.Builder<VariableOrGroundTerm> argListBuilder = ImmutableList.builder();
        for (VariableOrGroundTerm term : atom.getArguments()) {
            argListBuilder.add(renamingSubstitution.applyToVariableOrGroundTerm(term));
        }
        return DATA_FACTORY.getDataAtom(atom.getPredicate(), argListBuilder.build());
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
