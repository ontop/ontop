package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.*;

public class UnionNodeImpl extends CompositeQueryNodeImpl implements UnionNode {

    private static final String UNION_NODE_STR = "UNION";
    private final ImmutableSet<Variable> projectedVariables;
    private final ConstructionNodeTools constructionTools;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @AssistedInject
    private UnionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                          ConstructionNodeTools constructionTools, IntermediateQueryFactory iqFactory,
                          SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        super(substitutionFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.constructionTools = constructionTools;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public UnionNode clone() {
        return new UnionNodeImpl(projectedVariables, constructionTools, iqFactory,
                substitutionFactory, termFactory);
    }

    @Override
    public UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public SubstitutionResults<UnionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        ImmutableSet<Variable> newProjectedVariables = constructionTools.computeNewProjectedVariables(substitution,
                projectedVariables);

        /*
         * Stops the substitution if does not affect the projected variables
         */
        if (newProjectedVariables.equals(projectedVariables)) {
            return DefaultSubstitutionResults.noChange();
        }
        /*
         * Otherwise, updates the projected variables and propagates the substitution down.
         */
        else {
            UnionNode newNode = new UnionNodeImpl(newProjectedVariables, constructionTools, iqFactory, substitutionFactory, termFactory);
            return DefaultSubstitutionResults.newNode(newNode, substitution);
        }
    }

    @Override
    public boolean hasAChildWithLiftableDefinition(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> (c.getRootNode() instanceof ConstructionNode)
                        && ((ConstructionNode) c.getRootNode()).getSubstitution().isDefining(variable));
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        for(QueryNode child : query.getChildren(this)) {
            if (child.isVariableNullable(query, variable))
                return true;
        }
        return false;
    }


    @Override
    public ImmutableSet<Variable> getNullableVariables(ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.getNullableVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public boolean isConstructed(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> c.isConstructed(variable));
    }

    /**
     * TODO: make it compatible definitions together (requires a VariableGenerator so as to lift bindings)
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.liftIncompatibleDefinitions(variable))
                .collect(ImmutableCollectors.toList());
        
        return iqFactory.createNaryIQTree(this, liftedChildren);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(this,
                children.stream()
                        .map(c -> c.propagateDownConstraint(constraint))
                        .collect(ImmutableCollectors.toList()));
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        if (node instanceof UnionNode) {
            return projectedVariables.equals(((UnionNode)node).getVariables());
        }
        return false;
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
        throw new UnsupportedOperationException("The TrueNode child of a UnionNode is not expected to be removed");
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return projectedVariables;
    }

    @Override
    public String toString() {
        return UNION_NODE_STR + " " + projectedVariables;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        if (!(queryNode instanceof UnionNode))
            return false;
        return projectedVariables.equals(((UnionNode) queryNode).getVariables());
    }

    @Override
    public IQTree liftBinding(ImmutableList<IQTree> children, VariableGenerator variableGenerator, IQProperties currentIQProperties) {

        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.liftBinding(variableGenerator))
                .filter(c -> !c.isDeclaredAsEmpty())
                .map(c -> projectAwayUnnecessaryVariables(c, currentIQProperties))
                .collect(ImmutableCollectors.toList());

        switch (liftedChildren.size()) {
            case 0:
                return iqFactory.createEmptyNode(projectedVariables);
            case 1:
                return liftedChildren.get(0);
            default:
                return liftBindingFromLiftedChildren(liftedChildren, variableGenerator, currentIQProperties);
        }
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children) {
        ImmutableSet<Variable> updatedProjectedVariables = constructionTools.computeNewProjectedVariables(
                    descendingSubstitution, projectedVariables);

        ImmutableList<IQTree> updatedChildren = children.stream()
                .map(c -> c.applyDescendingSubstitution(descendingSubstitution, constraint))
                .filter(c -> !c.isDeclaredAsEmpty())
                .collect(ImmutableCollectors.toList());

        switch (updatedChildren.size()) {
            case 0:
                return iqFactory.createEmptyNode(updatedProjectedVariables);
            case 1:
                return updatedChildren.get(0);
            default:
                UnionNode newRootNode = iqFactory.createUnionNode(updatedProjectedVariables);
                return iqFactory.createNaryIQTree(newRootNode, updatedChildren);
        }
    }


    /**
     * Has at least two children
     */
    private IQTree liftBindingFromLiftedChildren(ImmutableList<IQTree> liftedChildren, VariableGenerator variableGenerator,
                                                 IQProperties currentIQProperties) {

        /*
         * Cannot lift anything if some children do not have a construction node
         */
        if (liftedChildren.stream()
                .anyMatch(c -> !(c.getRootNode() instanceof ConstructionNode)))
            return iqFactory.createNaryIQTree(this, liftedChildren, currentIQProperties.declareLifted());

        ImmutableSubstitution<ImmutableTerm> mergedSubstitution = mergeChildSubstitutions(
                    projectedVariables,
                    liftedChildren.stream()
                            .map(c -> (ConstructionNode) c.getRootNode())
                            .map(ConstructionNode::getSubstitution)
                            .collect(ImmutableCollectors.toList()),
                    variableGenerator);

        if (mergedSubstitution.isEmpty()) {
            return iqFactory.createNaryIQTree(this, liftedChildren, currentIQProperties.declareLifted());
        }

        ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables, mergedSubstitution);

        ImmutableSet<Variable> unionVariables = newRootNode.getChildVariables();
        UnionNode newUnionNode = iqFactory.createUnionNode(unionVariables);

        NaryIQTree unionIQ = iqFactory.createNaryIQTree(newUnionNode,
                liftedChildren.stream()
                        .map(c -> (UnaryIQTree) c)
                        .map(c -> updateChild(c, mergedSubstitution, unionVariables))
                        .collect(ImmutableCollectors.toList()));

        return iqFactory.createUnaryIQTree(newRootNode, unionIQ);
    }

    private ImmutableSubstitution<ImmutableTerm> mergeChildSubstitutions(
            ImmutableSet<Variable> projectedVariables,
            ImmutableCollection<ImmutableSubstitution<ImmutableTerm>> childSubstitutions,
            VariableGenerator variableGenerator) {

        ImmutableMap<Variable, ImmutableTerm> substitutionMap = projectedVariables.stream()
                .flatMap(v -> mergeDefinitions(v, childSubstitutions, variableGenerator)
                        .map(d -> Stream.of(new SimpleEntry<>(v, d)))
                        .orElseGet(Stream::empty))
                .collect(ImmutableCollectors.toMap());

        return substitutionFactory.getSubstitution(substitutionMap);
    }

    private Optional<ImmutableTerm> mergeDefinitions(
            Variable variable,
            ImmutableCollection<ImmutableSubstitution<ImmutableTerm>> childSubstitutions,
            VariableGenerator variableGenerator) {

        if (childSubstitutions.stream()
                .anyMatch(s -> !s.isDefining(variable)))
            return Optional.empty();

        return childSubstitutions.stream()
                .map(s -> Optional.of(s.get(variable)))
                .reduce((od1, od2) -> od1
                        .flatMap(d1 -> od2
                                .flatMap(d2 -> combineDefinitions(d1, d2, variableGenerator, true))))
                .flatMap(t -> t);
    }

    /**
     * Compare and combine the bindings, returning only the compatible (partial) values.
     * In case of variable, we generate and return a new variable to avoid inconsistency during propagation
     *
     */
    private Optional<ImmutableTerm> combineDefinitions(ImmutableTerm d1, ImmutableTerm d2,
                                                       VariableGenerator variableGenerator,
                                                       boolean topLevel) {
        if (d1.equals(d2)) {
            return Optional.of(d1);
        }
        else if (d1 instanceof Variable)  {
            return topLevel
                    ? Optional.empty()
                    : Optional.of(variableGenerator.generateNewVariableFromVar((Variable) d1));
        }
        else if (d2 instanceof Variable)  {
            return topLevel
                    ? Optional.empty()
                    : Optional.of(variableGenerator.generateNewVariableFromVar((Variable) d2));
        }
        else if ((d1 instanceof ImmutableFunctionalTerm) && (d2 instanceof ImmutableFunctionalTerm)) {
            ImmutableFunctionalTerm functionalTerm1 = (ImmutableFunctionalTerm) d1;
            ImmutableFunctionalTerm functionalTerm2 = (ImmutableFunctionalTerm) d2;

            /*
             * Different function symbols: stops the common part here
             */
            if (!functionalTerm1.getFunctionSymbol().equals(functionalTerm2.getFunctionSymbol())) {
                return topLevel
                        ? Optional.empty()
                        : Optional.of(variableGenerator.generateNewVariable());
            }
            else {
                ImmutableList<? extends ImmutableTerm> arguments1 = functionalTerm1.getArguments();
                ImmutableList<? extends ImmutableTerm> arguments2 = functionalTerm2.getArguments();
                if (arguments1.size() != arguments2.size()) {
                    throw new IllegalStateException("Functions have different arities, they cannot be combined");
                }

                ImmutableList.Builder<ImmutableTerm> argumentBuilder = ImmutableList.builder();
                for (int i = 0; i < arguments1.size(); i++) {
                    // Recursive
                    ImmutableTerm newArgument = combineDefinitions(arguments1.get(i), arguments2.get(i),
                            variableGenerator, false)
                            .orElseGet(variableGenerator::generateNewVariable);
                    argumentBuilder.add(newArgument);
                }
                return Optional.of(termFactory.getImmutableFunctionalTerm(functionalTerm1.getFunctionSymbol(),
                        argumentBuilder.build()));
            }
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * TODO: find a better name
     */
    private IQTree updateChild(UnaryIQTree liftedChildTree, ImmutableSubstitution<ImmutableTerm> mergedSubstitution,
                               ImmutableSet<Variable> projectedVariables) {
        ConstructionNode constructionNode = (ConstructionNode) liftedChildTree.getRootNode();

        /*
         * TODO: remove this test after removing the query modifiers from the construction nodes
         */
        if (constructionNode.getOptionalModifiers().isPresent())
            throw new UnsupportedOperationException("ConstructionNodes with query modifiers are not supported " +
                    "under a union node");

        ConstructionNodeTools.NewSubstitutionPair substitutionPair = constructionTools.traverseConstructionNode(
                mergedSubstitution, constructionNode.getSubstitution(),
                constructionNode.getVariables(), projectedVariables);

        // NB: this is expected to be ok given that the expected compatibility of the merged substitution with
        // this construction node
        ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution =
                substitutionFactory.getSubstitution(
                        (ImmutableMap<Variable, ? extends VariableOrGroundTerm>)(ImmutableMap<Variable, ?>)
                                substitutionPair.propagatedSubstitution.getImmutableMap());

        IQTree newChild = liftedChildTree.getChild()
                .applyDescendingSubstitution(descendingSubstitution, Optional.empty());

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                    substitutionPair.bindings);

        return substitutionPair.bindings.isEmpty()
                ? newChild
                : iqFactory.createUnaryIQTree(newConstructionNode, newChild);
    }

    /**
     * Projects away variables only for child construction nodes
     */
    private IQTree projectAwayUnnecessaryVariables(IQTree child, IQProperties currentIQProperties) {
        if (child.getRootNode() instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) child.getRootNode();

            if (constructionNode.getOptionalModifiers().isPresent())
                return child;

            AscendingSubstitutionNormalization normalization = normalizeAscendingSubstitution(
                    constructionNode.getSubstitution(), projectedVariables);
            Optional<ConstructionNode> proposedConstructionNode = normalization.generateTopConstructionNode();

            if (proposedConstructionNode
                    .filter(c -> c.isSyntacticallyEquivalentTo(constructionNode))
                    .isPresent())
                return child;

            IQTree grandChild = normalization.normalizeChild(((UnaryIQTree) child).getChild());

            return proposedConstructionNode
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, grandChild, currentIQProperties.declareLifted()))
                    .orElse(grandChild);
        }
        else
            return child;
    }

}
