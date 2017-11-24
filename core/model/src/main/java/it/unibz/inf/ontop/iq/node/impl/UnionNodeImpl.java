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

public class UnionNodeImpl extends QueryNodeImpl implements UnionNode {

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

    /**
     * Blocks an ascending substitution by inserting a construction node.
     *
     * Note that expects that the substitution does not rename a projected variable
     * into a non-projected one (this would produce an invalid construction node).
     * That is the responsibility of the SubstitutionPropagationExecutor
     * to prevent such bindings from appearing.
     */
    @Override
    public SubstitutionResults<UnionNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        /*
         * Reduce the domain of the substitution to the variables projected out by the union node
         */
        ImmutableSubstitution reducedSubstitution =
                substitution.reduceDomainToIntersectionWith(projectedVariables);

        if (reducedSubstitution.isEmpty()) {
            return DefaultSubstitutionResults.noChange();
        }
        /*
         * Asks for inserting a construction node between the child node and this node.
         * Such a construction node will contain the substitution.
         */
        else {
            ConstructionNode newParentOfChildNode = query.getFactory().createConstructionNode(projectedVariables,
                    (ImmutableSubstitution<ImmutableTerm>) reducedSubstitution);
            return DefaultSubstitutionResults.insertConstructionNode(newParentOfChildNode, childNode);
        }
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
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        for(QueryNode child : query.getChildren(this)) {
            if (child.isVariableNullable(query, variable))
                return true;
        }
        return false;
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
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {

        /*
         * All the children expected the given empty child
         */
        ImmutableList<QueryNode> children = query.getChildrenStream(this)
                .filter(c -> c != emptyChild)
                .collect(ImmutableCollectors.toList());

        switch (children.size()) {
            case 0:
                return new NodeTransformationProposalImpl(DECLARE_AS_EMPTY, emptyChild.getVariables());
            case 1:
                return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_NON_EMPTY_CHILD, children.get(0),
                        ImmutableSet.of());
            default:
                return new NodeTransformationProposalImpl(NO_LOCAL_CHANGE, ImmutableSet.of());
        }
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
    public IQTree liftBinding(ImmutableList<IQTree> children, VariableGenerator variableGenerator) {

        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.liftBinding(variableGenerator))
                .filter(c -> !(c instanceof EmptyNode))
                .collect(ImmutableCollectors.toList());

        switch (liftedChildren.size()) {
            case 0:
                return iqFactory.createEmptyNode(projectedVariables);
            case 1:
                return liftedChildren.get(0);
            default:
                return liftBindingFromLiftedChildren(liftedChildren, variableGenerator);
        }
    }


    /**
     * Has at least two children
     */
    private IQTree liftBindingFromLiftedChildren(ImmutableList<IQTree> liftedChildren, VariableGenerator variableGenerator) {

        /*
         * Cannot lift anything if some children do not have a construction node
         */
        if (liftedChildren.stream()
                .anyMatch(c -> !(c.getRootNode() instanceof ConstructionNode)))
            return iqFactory.createNaryIQTree(this, liftedChildren, true);

        ImmutableSubstitution<ImmutableTerm> mergedSubstitution = mergeChildSubstitutions(
                    projectedVariables,
                    liftedChildren.stream()
                            .map(c -> (ConstructionNode) c.getRootNode())
                            .map(ConstructionNode::getSubstitution)
                            .collect(ImmutableCollectors.toList()),
                    variableGenerator);

        if (mergedSubstitution.isEmpty()) {
            return iqFactory.createNaryIQTree(this, liftedChildren, true);
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
                                .flatMap(d2 -> combineDefinitions(d1, d2, variableGenerator))))
                .flatMap(t -> t);
    }

    /**
     * Compare and combine the bindings, returning only the compatible values.
     * In case of variable, we generate and return a new variable to avoid inconsistency during propagation
     *
     * TODO: revisit it?
     */
    private Optional<ImmutableTerm> combineDefinitions(ImmutableTerm d1, ImmutableTerm d2,
                                                       VariableGenerator variableGenerator) {
        if (d1.equals(d2)) {
            return Optional.of(d1);
        }
        else if (d1 instanceof Variable)  {
            return Optional.of(variableGenerator.generateNewVariableFromVar((Variable) d1));
        }
        else if (d2 instanceof Variable)  {
            return Optional.of(variableGenerator.generateNewVariableFromVar((Variable) d2));
        }
        else if ((d1 instanceof ImmutableFunctionalTerm) && (d2 instanceof ImmutableFunctionalTerm)) {
            ImmutableFunctionalTerm functionalTerm1 = (ImmutableFunctionalTerm) d1;
            ImmutableFunctionalTerm functionalTerm2 = (ImmutableFunctionalTerm) d2;

            /*
             * NB: function symbols are in charge of enforcing the declared arities
             */
            if (!functionalTerm1.getFunctionSymbol().equals(functionalTerm2.getFunctionSymbol())) {
                return Optional.empty();
            }

            ImmutableList<? extends ImmutableTerm> arguments1 = functionalTerm1.getArguments();
            ImmutableList<? extends ImmutableTerm> arguments2 = functionalTerm2.getArguments();
            if(arguments1.size()!=arguments2.size()){
                throw new IllegalStateException("Functions have different arities, they cannot be combined");
            }

            ImmutableList.Builder<ImmutableTerm> argumentBuilder = ImmutableList.builder();
            for(int i=0; i <  arguments1.size(); i++) {
                // Recursive
                Optional<ImmutableTerm> optionalNewArgument = combineDefinitions(arguments1.get(i), arguments2.get(i),
                        variableGenerator);
                if (optionalNewArgument.isPresent()) {
                    argumentBuilder.add(optionalNewArgument.get());
                }
                else {
                    return Optional.empty();
                }
            }
            return Optional.of(termFactory.getImmutableFunctionalTerm(functionalTerm1.getFunctionSymbol(),
                    argumentBuilder.build()));
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
                (ImmutableSubstitution<? extends VariableOrGroundTerm>) substitutionPair.propagatedSubstitution;

        IQTree newChild = liftedChildTree.getChild()
                .applyDescendingSubstitution(descendingSubstitution, Optional.empty());

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                    substitutionPair.bindings);

        return substitutionPair.bindings.isEmpty()
                ? newChild
                : iqFactory.createUnaryIQTree(newConstructionNode, newChild);
    }

}
