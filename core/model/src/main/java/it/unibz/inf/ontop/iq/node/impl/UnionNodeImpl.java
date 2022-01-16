package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class UnionNodeImpl extends CompositeQueryNodeImpl implements UnionNode {

    private static final String UNION_NODE_STR = "UNION";

    private final ImmutableSet<Variable> projectedVariables;

    private final ConstructionNodeTools constructionTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final NotRequiredVariableRemover notRequiredVariableRemover;

    @AssistedInject
    private UnionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                          ConstructionNodeTools constructionTools, IntermediateQueryFactory iqFactory,
                          SubstitutionFactory substitutionFactory, TermFactory termFactory,
                          CoreUtilsFactory coreUtilsFactory,
                          NotRequiredVariableRemover notRequiredVariableRemover) {
        super(substitutionFactory, termFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.constructionTools = constructionTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.notRequiredVariableRemover = notRequiredVariableRemover;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
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
                .map(s -> s.filter(projectedVariables::contains))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public boolean hasAChildWithLiftableDefinition(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> (c.getRootNode() instanceof ConstructionNode)
                        && ((ConstructionNode) c.getRootNode()).getSubstitution().isDefining(variable));
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

    @Override
    public boolean isDistinct(IQTree tree, ImmutableList<IQTree> children) {
        if (children.stream().anyMatch(c -> !c.isDistinct()))
            return false;

        return IntStream.range(0, children.size())
                .allMatch(i -> children.subList(i+1, children.size()).stream()
                        .allMatch(o -> areDisjoint(children.get(i), o)));
    }

    /**
     * Returns true if we are sure the two children can only return different tuples
     */
    private boolean areDisjoint(IQTree child1, IQTree child2) {
        VariableNullability variableNullability1 = child1.getVariableNullability();
        VariableNullability variableNullability2 = child2.getVariableNullability();

        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleDefs1 = child1.getPossibleVariableDefinitions();
        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleDefs2 = child2.getPossibleVariableDefinitions();

        return projectedVariables.stream()
                // We don't consider variables nullable on both side
                .filter(v -> !(variableNullability1.isPossiblyNullable(v) && variableNullability2.isPossiblyNullable(v)))
                .anyMatch(v -> areDisjointWhenNonNull(extractDefs(possibleDefs1, v), extractDefs(possibleDefs2, v), variableNullability1));
    }

    @Override
    public IQTree makeDistinct(ImmutableList<IQTree> children) {
        ImmutableMap<IQTree, ImmutableSet<IQTree>> compatibilityMap = extractCompatibilityMap(children);

        if (areGroupDisjoint(compatibilityMap)) {
            // NB: multiple occurrences of the same child are automatically eliminated
            return makeDistinctDisjointGroups(ImmutableSet.copyOf(compatibilityMap.values()));
        }
        /*
         * Fail-back: in the presence of non-disjoint groups of children,
         * puts the DISTINCT above.
         *
         * TODO: could be improved
         */
        else {
            return makeDistinctGroup(ImmutableSet.copyOf(children));
        }
    }

    private ImmutableMap<IQTree, ImmutableSet<IQTree>> extractCompatibilityMap(ImmutableList<IQTree> children) {
        return IntStream.range(0, children.size())
                .boxed()
                // Compare to itself
                .flatMap(i -> IntStream.range(i, children.size())
                        .boxed()
                        .flatMap(j -> (i.equals(j) || (!areDisjoint(children.get(i), children.get(j))))
                                ? Stream.of(
                                Maps.immutableEntry(children.get(i), children.get(j)),
                                Maps.immutableEntry(children.get(j), children.get(i)))
                                : Stream.empty()))
                .collect(ImmutableCollectors.toMultimap())
                .asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ImmutableSet.copyOf(e.getValue())));
    }

    private IQTree makeDistinctDisjointGroups(ImmutableSet<ImmutableSet<IQTree>> disjointGroups) {
        ImmutableList<IQTree> newChildren = disjointGroups.stream()
                .map(this::makeDistinctGroup)
                .collect(ImmutableCollectors.toList());

        switch (newChildren.size()) {
            case 0:
                throw new MinorOntopInternalBugException("Was expecting to have at least one group of Union children");
            case 1:
                return newChildren.get(0);
            default:
                return iqFactory.createNaryIQTree(this, newChildren);
        }
    }

    private IQTree makeDistinctGroup(ImmutableSet<IQTree> childGroup) {
        switch (childGroup.size()) {
            case 0:
                throw new MinorOntopInternalBugException("Unexpected empty child group");
            case 1:
                return iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(), childGroup.iterator().next());
            default:
                return iqFactory.createUnaryIQTree(
                        iqFactory.createDistinctNode(),
                        iqFactory.createNaryIQTree(this, ImmutableList.copyOf(childGroup)));
        }
    }

    private boolean areGroupDisjoint(ImmutableMap<IQTree, ImmutableSet<IQTree>> compatibilityMap) {
        return compatibilityMap.values().stream()
                .allMatch(g -> g.stream()
                        .allMatch(t -> compatibilityMap.get(t).equals(g)));
    }

    private static ImmutableSet<ImmutableTerm> extractDefs(ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleDefs,
                                                           Variable v) {
        if (possibleDefs.isEmpty())
            return ImmutableSet.of(v);

        return possibleDefs.stream()
                .map(s -> s.applyToVariable(v))
                .collect(ImmutableCollectors.toSet());
    }

    private boolean areDisjointWhenNonNull(ImmutableSet<ImmutableTerm> defs1, ImmutableSet<ImmutableTerm> defs2,
                                           VariableNullability variableNullability) {
        return defs1.stream()
                .allMatch(d1 -> defs2.stream()
                        .allMatch(d2 -> areDisjointWhenNonNull(d1, d2, variableNullability)));
    }

    private boolean areDisjointWhenNonNull(ImmutableTerm t1, ImmutableTerm t2, VariableNullability variableNullability) {
        IncrementalEvaluation evaluation = t1.evaluateStrictEq(t2, variableNullability);
        switch(evaluation.getStatus()) {
            case SIMPLIFIED_EXPRESSION:
                return evaluation.getNewExpression()
                        .orElseThrow(() -> new MinorOntopInternalBugException("An expression was expected"))
                        .evaluate2VL(variableNullability)
                        .isEffectiveFalse();
            case IS_NULL:
            case IS_FALSE:
                return true;
            case SAME_EXPRESSION:
            case IS_TRUE:
            default:
                return false;
        }
    }


    /**
     * TODO: make it compatible definitions together (requires a VariableGenerator so as to lift bindings)
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.liftIncompatibleDefinitions(variable, variableGenerator))
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
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer,
                                    ImmutableList<IQTree> children, T context) {
        return transformer.transformUnion(tree,this, children, context);
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
            if (!child.getVariables().equals(unionVariables)) {
                throw new InvalidIntermediateQueryException("This child " + child
                        + " does not project exactly all the variables " +
                        "of the UNION node (" + unionVariables + ")\n" + this);
            }
        }
    }

    @Override
    public IQTree removeDistincts(ImmutableList<IQTree> children, IQTreeCache treeCache) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(IQTree::removeDistincts)
                .collect(ImmutableCollectors.toList());

        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChildren.equals(children));

        return iqFactory.createNaryIQTree(this, children, newTreeCache);
    }

    /**
     * TODO: implement it seriously
     */
    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(ImmutableList<IQTree> children) {
        return ImmutableSet.of();
    }

    /**
     * All the variables of an union could be projected out
     */
    @Override
    public ImmutableSet<Variable> computeNotInternallyRequiredVariables(ImmutableList<IQTree> children) {
        return getVariables();
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
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
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnionNodeImpl unionNode = (UnionNodeImpl) o;
        return projectedVariables.equals(unionNode.projectedVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectedVariables);
    }

    /**
     * TODO: refactor
     */
    @Override
    public IQTree normalizeForOptimization(ImmutableList<IQTree> children, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.normalizeForOptimization(variableGenerator))
                .filter(c -> !c.isDeclaredAsEmpty())
                .map(c -> notRequiredVariableRemover.optimize(c, projectedVariables, variableGenerator))
                .collect(ImmutableCollectors.toList());

        switch (liftedChildren.size()) {
            case 0:
                return iqFactory.createEmptyNode(projectedVariables);
            case 1:
                return liftedChildren.get(0);
            default:
                return liftBindingFromLiftedChildrenAndFlatten(liftedChildren, variableGenerator, treeCache);
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

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution,
                                     ImmutableList<IQTree> children, IQTreeCache treeCache) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(c -> c.applyFreshRenaming(renamingSubstitution))
                .collect(ImmutableCollectors.toList());

        UnionNode newUnionNode = iqFactory.createUnionNode(
                getVariables().stream()
                        .map(renamingSubstitution::applyToVariable)
                        .collect(ImmutableCollectors.toSet()));

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);

        return iqFactory.createNaryIQTree(newUnionNode, newChildren, newTreeCache);
    }


    /**
     * Has at least two children
     */
    private IQTree liftBindingFromLiftedChildrenAndFlatten(ImmutableList<IQTree> liftedChildren, VariableGenerator variableGenerator,
                                                           IQTreeCache treeCache) {

        /*
         * Cannot lift anything if some children do not have a construction node
         */
        if (liftedChildren.stream()
                .anyMatch(c -> !(c.getRootNode() instanceof ConstructionNode)))
            return iqFactory.createNaryIQTree(this, flattenChildren(liftedChildren), treeCache.declareAsNormalizedForOptimizationWithEffect());

        ImmutableList<ImmutableSubstitution<ImmutableTerm>> tmpNormalizedChildSubstitutions = liftedChildren.stream()
                .map(c -> (ConstructionNode) c.getRootNode())
                .map(ConstructionNode::getSubstitution)
                .map(substitution -> substitution.transform(this::normalizeNullAndRDFConstants))
                .collect(ImmutableCollectors.toList());

        ImmutableSubstitution<ImmutableTerm> mergedSubstitution = mergeChildSubstitutions(
                    projectedVariables, tmpNormalizedChildSubstitutions, variableGenerator);

        if (mergedSubstitution.isEmpty()) {
            return iqFactory.createNaryIQTree(this, flattenChildren(liftedChildren), treeCache.declareAsNormalizedForOptimizationWithEffect());
        }
        ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables,
                // Cleans up the temporary "normalization"
                mergedSubstitution.transform(v -> v.simplify()));

        ImmutableSet<Variable> unionVariables = newRootNode.getChildVariables();
        UnionNode newUnionNode = iqFactory.createUnionNode(unionVariables);

        NaryIQTree unionIQ = iqFactory.createNaryIQTree(newUnionNode,
                IntStream.range(0, liftedChildren.size())
                        .mapToObj(i -> updateChild((UnaryIQTree) liftedChildren.get(i), mergedSubstitution,
                                tmpNormalizedChildSubstitutions.get(i), unionVariables))
                        .flatMap(this::flattenChild)
                        .map(c -> c.getVariables().equals(unionVariables)
                                ? c
                                : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(unionVariables), c))
                        .collect(ImmutableCollectors.toList()));

        return iqFactory.createUnaryIQTree(newRootNode, unionIQ);
    }

    private ImmutableList<IQTree> flattenChildren(ImmutableList<IQTree> liftedChildren) {
        ImmutableList<IQTree> flattenedChildren = liftedChildren.stream()
                .flatMap(this::flattenChild)
                .collect(ImmutableCollectors.toList());
        return (liftedChildren.size() == flattenedChildren.size())
                ? liftedChildren
                : flattenedChildren;
    }

    private Stream<IQTree> flattenChild(IQTree child) {
        return (child.getRootNode() instanceof UnionNode)
                ? child.getChildren().stream()
                : Stream.of(child);
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
                        .map(d -> Stream.of(Maps.immutableEntry(v, d)))
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

            FunctionSymbol firstFunctionSymbol = functionalTerm1.getFunctionSymbol();

            /*
             * Different function symbols: stops the common part here.
             *
             * Same for functions that do not strongly type their arguments (unsafe to decompose). Example: STRICT_EQ
             */
            if ((!firstFunctionSymbol.equals(functionalTerm2.getFunctionSymbol()))
                    || (!firstFunctionSymbol.shouldBeDecomposedInUnion())) {
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
                return Optional.of(termFactory.getImmutableFunctionalTerm(firstFunctionSymbol,
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
        ImmutableSubstitution<VariableOrGroundTerm> descendingSubstitution =
                substitutionPair.propagatedSubstitution.transform(v -> (VariableOrGroundTerm)v);

        IQTree newChild = liftedChildTree.getChild()
                .applyDescendingSubstitution(descendingSubstitution, Optional.empty());

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                    // Cleans up the temporary "normalization", in particular non-lifted RDF(NULL,NULL)
                    substitutionPair.bindings.transform(v -> v.simplify()));

        return substitutionPair.bindings.isEmpty()
                ? newChild
                : iqFactory.createUnaryIQTree(newConstructionNode, newChild);
    }
}
