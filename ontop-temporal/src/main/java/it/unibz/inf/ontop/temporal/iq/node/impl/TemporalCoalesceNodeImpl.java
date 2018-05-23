package it.unibz.inf.ontop.temporal.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.TemporalIQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.temporal.iq.node.TemporalCoalesceNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalQueryNodeVisitor;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemporalCoalesceNodeImpl implements TemporalCoalesceNode {

    private static final String COALESCE_NODE_STR = "TEMPORAL COALESCE";

    private final ImmutableSet<NonGroundTerm> terms;

    private final TemporalIntermediateQueryFactory iqFactory;

    @AssistedInject
    protected TemporalCoalesceNodeImpl(@Assisted ImmutableSet<NonGroundTerm> terms, TemporalIntermediateQueryFactory iqFactory) {
        this.terms = terms;
        this.iqFactory = iqFactory;
    }

    @Override
    public ImmutableSet<NonGroundTerm> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return COALESCE_NODE_STR  + terms.toString();
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        ((TemporalQueryNodeVisitor)visitor).visit(this);
    }

    @Override
    public QueryNode clone() {
        try {
            return iqFactory.createTemporalCoalesceNode(terms);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TemporalCoalesceNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return this;
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
         return ImmutableSet.of();
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        return false;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return isEquivalentTo(node);
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        try {
            throw new Exception("getLocallyRequiredVariables is not implemented in coalesce");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        try {
            throw new Exception("getRequiredVariables is not implemented in coalesce");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        try {
            throw new Exception("getLocallyDefinedVariables is not implemented in coalesce");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return queryNode instanceof TemporalCoalesceNode
                && this.terms.equals(((TemporalCoalesceNode) queryNode).getTerms());
    }


//    @Override
//    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
//        IQTree newChild = childIQTree.liftBinding(variableGenerator);
//        QueryNode newChildRoot = newChild.getRootNode();
//        if(newChildRoot instanceof ConstructionNode ){
//            IQTree coalesceLevelTree =  iqFactory
//                    .createUnaryIQTree(this, ((UnaryIQTree)newChild).getChild(), currentIQProperties.declareLifted());
//            return iqFactory.createUnaryIQTree((ConstructionNode)newChildRoot, coalesceLevelTree);
//        }else if(newChildRoot instanceof EmptyNode){
//            return newChild;
//        }
//        return iqFactory.createUnaryIQTree(this, newChild, currentIQProperties.declareLifted());
//    }

    @Override
    public IQTree liftBinding(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = child.liftBinding(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        IQProperties liftedProperties = currentIQProperties.declareLifted();

        if (newChildRoot instanceof ConstructionNode)
            return liftChildConstructionNode((ConstructionNode) newChildRoot, (UnaryIQTree) newChild, liftedProperties);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else if (newChildRoot instanceof DistinctNode) {
            return iqFactory.createUnaryIQTree(
                    (DistinctNode) newChildRoot,
                    iqFactory.createUnaryIQTree(this, ((UnaryIQTree)newChild).getChild(), liftedProperties),
                    liftedProperties);
        }
        else
            return iqFactory.createUnaryIQTree(this, newChild, liftedProperties);

    }

    /**
     * Lifts the construction node above and updates the order comparators
     */
    private IQTree liftChildConstructionNode(ConstructionNode newChildRoot, UnaryIQTree newChild, IQProperties liftedProperties) {

        UnaryIQTree newCoalesceTree = iqFactory.createUnaryIQTree(
                applySubstitution(newChildRoot.getSubstitution()),
                newChild.getChild(),
                liftedProperties);

        return iqFactory.createUnaryIQTree(newChildRoot, newCoalesceTree, liftedProperties);
    }

    private TemporalCoalesceNode applySubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        ImmutableSet<NonGroundTerm> newNonGroundTerms = terms.stream()
                .flatMap(c -> Stream.of(substitution.apply(c)))
                        .filter(t -> t instanceof NonGroundTerm)
                        .map(t -> (NonGroundTerm) t)
                .collect(ImmutableCollectors.toSet());

        Set<NonGroundTerm> newVariables = new HashSet<>();

        for(NonGroundTerm ngTerm : newNonGroundTerms){
            if (ngTerm instanceof NonGroundFunctionalTerm){
                newVariables.addAll(((NonGroundFunctionalTerm) ngTerm)
                        .getArguments().stream()
                        .filter(t -> t instanceof Variable)
                        .map(t -> (Variable)t)
                        .collect(Collectors.toSet()));
            } else if(ngTerm instanceof Variable){
                newVariables.add(ngTerm);
            }
        }

        return iqFactory.createTemporalCoalesceNode(ImmutableSet.copyOf(newVariables));
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {

        TemporalCoalesceNode newTemporalCoalesceTree = applySubstitution(descendingSubstitution);
        IQTree newChild = child.applyDescendingSubstitution(descendingSubstitution, constraint);

        return iqFactory.createUnaryIQTree(newTemporalCoalesceTree, newChild);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {

        TemporalCoalesceNode newTemporalCoalesceTree = applySubstitution(descendingSubstitution);
        IQTree newChild = child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution);

        return iqFactory.createUnaryIQTree(newTemporalCoalesceTree, newChild);
    }


    @Override
    public ImmutableSet<Variable> getNullableVariables(IQTree child) {
        return child.getNullableVariables();
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return false;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        try {
            throw new Exception("liftIncompatibleDefinitions is not implemented in coalesce");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {

        return iqFactory.createUnaryIQTree(this, child);
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child) {
        if (transformer instanceof TemporalIQTransformer){
            return ((TemporalIQTransformer) transformer).transformTemporalCoalesce(tree, this, child);
        } else {
            return transformer.transformNonStandardUnaryNode(tree, this, child);
        }
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {

    }
}
