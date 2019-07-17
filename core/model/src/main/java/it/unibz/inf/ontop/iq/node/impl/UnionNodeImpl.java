package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class UnionNodeImpl extends CompositeQueryNodeImpl implements UnionNode {

    private static final String UNION_NODE_STR = "UNION";
    private final ImmutableSet<Variable> projectedVariables;
    private final ConstructionNodeTools constructionTools;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final ConstructionSubstitutionNormalizer substitutionNormalizer;

    @AssistedInject
    private UnionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                          ConstructionNodeTools constructionTools, IntermediateQueryFactory iqFactory,
                          SubstitutionFactory substitutionFactory, TermFactory termFactory,
                          CoreUtilsFactory coreUtilsFactory,
                          ConstructionSubstitutionNormalizer substitutionNormalizer) {
        super(substitutionFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.constructionTools = constructionTools;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionNormalizer = substitutionNormalizer;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public UnionNode clone() {
        return new UnionNodeImpl(projectedVariables, constructionTools, iqFactory,
                substitutionFactory, termFactory, coreUtilsFactory, substitutionNormalizer);
    }

    @Override
    public UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(
            ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.getPossibleVariableDefinitions().stream())
                .map(s -> s.reduceDomainToIntersectionWith(projectedVariables))
                .collect(ImmutableCollectors.toSet());
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
    public VariableNullability getVariableNullability(ImmutableList<IQTree> children) {
        ImmutableSet<VariableNullability> variableNullabilities = children.stream()
                .map(IQTree::getVariableNullability)
                .collect(ImmutableCollectors.toSet());

        ImmutableMultimap<Variable, ImmutableSet<Variable>> multimap = variableNullabilities.stream()
                .flatMap(vn -> vn.getNullableGroups().stream())
                .flatMap(g -> g.stream()
                        .map(v -> Maps.immutableEntry(v, g)))
                .collect(ImmutableCollectors.toMultimap());

        ImmutableMap<Variable, ImmutableSet<Variable>> preselectedGroupMap = multimap.asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> intersect(e.getValue())));

        ImmutableSet<ImmutableSet<Variable>> nullableGroups = preselectedGroupMap.keySet().stream()
                .map(v -> computeNullableGroup(v, preselectedGroupMap, variableNullabilities))
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> scope = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        return coreUtilsFactory.createVariableNullability(nullableGroups, scope);
    }

    private ImmutableSet<Variable> computeNullableGroup(Variable mainVariable,
                                                        ImmutableMap<Variable,ImmutableSet<Variable>> preselectedGroupMap,
                                                        ImmutableSet<VariableNullability> variableNullabilities) {
        return preselectedGroupMap.get(mainVariable).stream()
                .filter(v -> mainVariable.equals(v)
                        || areInterdependent(mainVariable, v, preselectedGroupMap, variableNullabilities))
                .collect(ImmutableCollectors.toSet());
    }

    private boolean areInterdependent(Variable v1, Variable v2,
                                      ImmutableMap<Variable, ImmutableSet<Variable>> preselectedGroupMap,
                                      ImmutableSet<VariableNullability> variableNullabilities) {
        return preselectedGroupMap.get(v2).contains(v1)
                && variableNullabilities.stream()
                .allMatch(vn -> {
                    boolean v1Nullable = vn.isPossiblyNullable(v1);
                    boolean v2Nullable = vn.isPossiblyNullable(v2);

                    return (v1Nullable && v2Nullable) || ((!v1Nullable) && (!v2Nullable));
                });
    }

    private static ImmutableSet<Variable> intersect(Collection<ImmutableSet<Variable>> groups) {
        return groups.stream()
                .reduce((g1, g2) -> Sets.intersection(g1, g2).immutableCopy())
                .orElseThrow(() -> new IllegalArgumentException("groups must not be empty"));
    }

    @Override
    public boolean isConstructed(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> c.isConstructed(variable));
    }

    /**
     * TODO: detect when children are disjoint and distinct
     */
    @Override
    public boolean isDistinct(ImmutableList<IQTree> children) {
        return false;
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
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, ImmutableList<IQTree> children) {
        return transformer.transformUnion(tree,this, children);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, ImmutableList<IQTree> children) {
        return visitor.visitUnion(this, children);
    }

    @Override
    public void validateNode(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException {
        if (children.size() < 2) {
            throw new InvalidIntermediateQueryException("UNION node " + this
                    +" does not have at least 2 children node.");
        }

        ImmutableSet<Variable> unionVariables = getVariables();

        for (IQTree child : children) {
            if (!child.getVariables().containsAll(unionVariables)) {
                throw new InvalidIntermediateQueryException("This child " + child
                        + " does not project all the variables " +
                        "required by the UNION node (" + unionVariables + ")\n" + this);
            }
        }
    }

    @Override
    public IQTree removeDistincts(ImmutableList<IQTree> children, IQProperties properties) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(IQTree::removeDistincts)
                .collect(ImmutableCollectors.toList());

        IQProperties newProperties = newChildren.equals(children)
                ? properties.declareDistinctRemovalWithoutEffect()
                : properties.declareDistinctRemovalWithEffect();

        return iqFactory.createNaryIQTree(this, children, newProperties);
    }

    /**
     * TODO: implement it seriously
     */
    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(ImmutableList<IQTree> children) {
        return ImmutableSet.of();
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


    /**
     * TODO: refactor
     */
    private IQTree liftBinding(ImmutableList<IQTree> children, VariableGenerator variableGenerator, IQProperties currentIQProperties) {

        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.normalizeForOptimization(variableGenerator))
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

    /**
     * TODO: refactor
     */
    @Override
    public IQTree normalizeForOptimization(ImmutableList<IQTree> children, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        return liftBinding(children, variableGenerator, currentIQProperties);
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

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableList<IQTree> children) {
        ImmutableSet<Variable> updatedProjectedVariables = constructionTools.computeNewProjectedVariables(
                descendingSubstitution, projectedVariables);

        ImmutableList<IQTree> updatedChildren = children.stream()
                .map(c -> c.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution))
                .collect(ImmutableCollectors.toList());

        UnionNode newRootNode = iqFactory.createUnionNode(updatedProjectedVariables);
        return iqFactory.createNaryIQTree(newRootNode, updatedChildren);
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
            return iqFactory.createNaryIQTree(this, liftedChildren, currentIQProperties.declareNormalizedForOptimization());

        ImmutableList<ImmutableSubstitution<ImmutableTerm>> tmpNormalizedChildSubstitutions = liftedChildren.stream()
                .map(c -> (ConstructionNode) c.getRootNode())
                .map(ConstructionNode::getSubstitution)
                .map(this::tmpNormalizeNullAndRDFConstantsInSubstitution)
                .collect(ImmutableCollectors.toList());

        ImmutableSubstitution<ImmutableTerm> mergedSubstitution = mergeChildSubstitutions(
                    projectedVariables, tmpNormalizedChildSubstitutions, variableGenerator);

        if (mergedSubstitution.isEmpty()) {
            return iqFactory.createNaryIQTree(this, liftedChildren, currentIQProperties.declareNormalizedForOptimization());
        }
        ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables,
                // Cleans up the temporary "normalization"
                mergedSubstitution.simplifyValues());

        ImmutableSet<Variable> unionVariables = newRootNode.getChildVariables();
        UnionNode newUnionNode = iqFactory.createUnionNode(unionVariables);

        NaryIQTree unionIQ = iqFactory.createNaryIQTree(newUnionNode,
                IntStream.range(0, liftedChildren.size())
                        .boxed()
                        .map(i -> updateChild((UnaryIQTree) liftedChildren.get(i), mergedSubstitution,
                                tmpNormalizedChildSubstitutions.get(i), unionVariables))
                        .collect(ImmutableCollectors.toList()));

        return iqFactory.createUnaryIQTree(newRootNode, unionIQ);
    }

    private ImmutableSubstitution<ImmutableTerm> tmpNormalizeNullAndRDFConstantsInSubstitution(
            ImmutableSubstitution<ImmutableTerm> substitution) {
        return substitutionFactory.getSubstitution(
                substitution.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> normalizeNullAndRDFConstants(e.getValue())
                )));
    }

    /**
     * RDF constants are transformed into RDF ground terms
     * Trick: NULL --> RDF(NULL,NULL)
     *
     * This "normalization" is temporary --> it will be "cleaned" but simplify the terms afterwards
     *
     */
    private ImmutableTerm normalizeNullAndRDFConstants(ImmutableTerm definition) {
        if (definition instanceof RDFConstant) {
            RDFConstant constant = (RDFConstant) definition;
            return termFactory.getRDFFunctionalTerm(
                    termFactory.getDBStringConstant(constant.getValue()),
                    termFactory.getRDFTermTypeConstant(constant.getType()));
        }
        else if ((definition instanceof Constant) && definition.isNull())
            return termFactory.getRDFFunctionalTerm(
                    termFactory.getNullConstant(), termFactory.getNullConstant());
        else
            return definition;
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
                .map(s -> s.get(variable))
                .map(this::normalizeNullAndRDFConstants)
                .map(Optional::of)
                .reduce((od1, od2) -> od1
                        .flatMap(d1 -> od2
                                .flatMap(d2 -> combineDefinitions(d1, d2, variableGenerator, true))))
                .flatMap(t -> t);
    }

    /**
     * Compare and combine the bindings, returning only the compatible (partial) values.
     *
     */
    private Optional<ImmutableTerm> combineDefinitions(ImmutableTerm d1, ImmutableTerm d2,
                                                       VariableGenerator variableGenerator,
                                                       boolean topLevel) {
        if (d1.equals(d2)) {
            return Optional.of(
                    // Top-level var-to-var must not be renamed since they are about projected variables
                    (d1.isGround() || (topLevel && (d1 instanceof Variable)))
                            ? d1
                            : replaceVariablesByFreshOnes((NonGroundTerm)d1, variableGenerator));
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
                ImmutableList<? extends ImmutableTerm> arguments1 = functionalTerm1.getTerms();
                ImmutableList<? extends ImmutableTerm> arguments2 = functionalTerm2.getTerms();
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

    private NonGroundTerm replaceVariablesByFreshOnes(NonGroundTerm term, VariableGenerator variableGenerator) {
        if (term instanceof Variable)
            return variableGenerator.generateNewVariableFromVar((Variable) term);
        NonGroundFunctionalTerm functionalTerm = (NonGroundFunctionalTerm) term;

        return termFactory.getNonGroundFunctionalTerm(functionalTerm.getFunctionSymbol(),
                    functionalTerm.getTerms().stream()
                        .map(a -> a.isGround()
                                ? a
                                // RECURSIVE
                                : replaceVariablesByFreshOnes((NonGroundTerm) a, variableGenerator))
                        .collect(ImmutableCollectors.toList()));
    }

    /**
     * TODO: find a better name
     */
    private IQTree updateChild(UnaryIQTree liftedChildTree, ImmutableSubstitution<ImmutableTerm> mergedSubstitution,
                               ImmutableSubstitution<ImmutableTerm> tmpNormalizedSubstitution,
                               ImmutableSet<Variable> projectedVariables) {
        ConstructionNode constructionNode = (ConstructionNode) liftedChildTree.getRootNode();

        ConstructionNodeTools.NewSubstitutionPair substitutionPair = constructionTools.traverseConstructionNode(
                mergedSubstitution, tmpNormalizedSubstitution,
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
                    // Cleans up the temporary "normalization", in particular non-lifted RDF(NULL,NULL)
                    substitutionPair.bindings.simplifyValues());

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

            IQTree grandChild = ((UnaryIQTree) child).getChild();

            ConstructionSubstitutionNormalization normalization = substitutionNormalizer.normalizeSubstitution(
                    constructionNode.getSubstitution(), projectedVariables);
            Optional<ConstructionNode> proposedConstructionNode = normalization.generateTopConstructionNode();

            if (proposedConstructionNode
                    .filter(c -> c.isSyntacticallyEquivalentTo(constructionNode))
                    .isPresent())
                return child;

            IQTree newGrandChild = normalization.updateChild(grandChild);

            return proposedConstructionNode
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, newGrandChild, currentIQProperties.declareNormalizedForOptimization()))
                    .orElse(newGrandChild);
        }
        else
            return child;
    }

}
