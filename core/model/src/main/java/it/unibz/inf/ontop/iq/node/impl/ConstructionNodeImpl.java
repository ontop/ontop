package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryNodeException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm.FunctionalTermDecomposition;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.VariableGeneratorImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "BindingAnnotationWithoutInject"})
public class ConstructionNodeImpl extends ExtendedProjectionNodeImpl implements ConstructionNode {

    private static final String CONSTRUCTION_NODE_STR = "CONSTRUCT";

    private final ImmutableSet<Variable> projectedVariables;
    private final Substitution<ImmutableTerm> substitution;
    private final ImmutableSet<Variable> childVariables;

    private final ConstructionSubstitutionNormalizer substitutionNormalizer;
    private final NotRequiredVariableRemover notRequiredVariableRemover;

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 @Assisted Substitution<? extends ImmutableTerm> substitution,
                                 SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 OntopModelSettings settings, IQTreeTools iqTreeTools,
                                 ConstructionSubstitutionNormalizer substitutionNormalizer,
                                 NotRequiredVariableRemover notRequiredVariableRemover) {
        super(substitutionFactory, iqFactory, iqTreeTools, termFactory);
        this.projectedVariables = projectedVariables;
        this.substitution = substitutionFactory.covariantCast(substitution);
        this.substitutionNormalizer = substitutionNormalizer;
        this.notRequiredVariableRemover = notRequiredVariableRemover;

        // only the variables that are also used in the bindings for the child of the construction node
        this.childVariables = Sets.difference(
                        Sets.union(this.projectedVariables, this.substitution.getRangeVariables()),
                        this.substitution.getDomain())
                .immutableCopy();

        if (settings.isTestModeEnabled())
            validateNode();
    }

    /**
     * Without substitution.
     */
    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 IQTreeTools iqTreeTools,
                                 SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 OntopModelSettings settings,
                                 ConstructionSubstitutionNormalizer substitutionNormalizer,
                                 NotRequiredVariableRemover notRequiredVariableRemover) {
        this(projectedVariables, substitutionFactory.getSubstitution(),
                substitutionFactory, termFactory, iqFactory, settings, iqTreeTools,substitutionNormalizer, notRequiredVariableRemover);
    }


    /**
     * Validates the node independently of its child
     */
    private void validateNode() throws InvalidQueryNodeException {
        ImmutableSet<Variable> substitutionDomain = substitution.getDomain();

        // The substitution domain must be a subset of the projectedVariables
        if (!projectedVariables.containsAll(substitutionDomain)) {
            throw new InvalidQueryNodeException("ConstructionNode: all the domain variables " +
                    "of the substitution must be projected.\n" + this);
        }

        // The variables contained in the domain and in the range of the substitution must be disjoint
        if (!Sets.intersection(substitutionDomain, childVariables).isEmpty()) {
            throw new InvalidQueryNodeException("ConstructionNode: variables defined by the substitution cannot " +
                    "be used for defining other variables.\n" + this);
        }

        // Substitution to non-projected variables is incorrect
        if (!Sets.difference(substitution.restrictRangeTo(Variable.class).getRangeSet(), projectedVariables).isEmpty()) {
            throw new InvalidQueryNodeException(
                    "ConstructionNode: substituting a variable " +
                            "by a non-projected variable is incorrect.\n" + this);
        }
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public Substitution<ImmutableTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public ConstructionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getChildVariables() {
        return childVariables;
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return Sets.union(
                projectedVariables,
                Sets.union(
                        substitution.getDomain(),
                        substitution.getRangeVariables())).immutableCopy();
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        if (!inferUniqueConstraints(child).isEmpty())
            return true;
        if (child instanceof TrueNode)
            return true;

        QueryNode childRoot = child.getRootNode();
        return (childRoot instanceof SliceNode)
                && ((SliceNode) childRoot).getLimit().filter(l -> l == 1).isPresent();
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        if (!childVariables.contains(variable)) {
            return iqFactory.createUnaryIQTree(this, child);
        }

        IQTree newChild = child.liftIncompatibleDefinitions(variable, variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the construction node
         */
        if ((newChildRoot instanceof UnionNode)
                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {

            ImmutableList<IQTree> newChildren = newChild.getChildren().stream()
                    .<IQTree>map(c -> iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            UnionNode newUnionNode = iqFactory.createUnionNode(getVariables());
            return iqFactory.createNaryIQTree(newUnionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }


    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformConstruction(tree,this, child);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformConstruction(tree,this, child, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitConstruction(this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidQueryNodeException, InvalidIntermediateQueryException {
        validateNode();

        ImmutableSet<Variable> requiredChildVariables = getChildVariables();

        ImmutableSet<Variable> childVariables = child.getVariables();

        if (!childVariables.containsAll(requiredChildVariables)) {
            throw new InvalidIntermediateQueryException("This child " + child
                    + " does not project all the variables " +
                    "required by the CONSTRUCTION node (" + requiredChildVariables + ")\n" + this);
        }
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        ImmutableSet<Substitution<NonVariableTerm>> childDefs = child.getPossibleVariableDefinitions();

        if (childDefs.isEmpty()) {
            Substitution<NonVariableTerm> def = substitution.restrictRangeTo(NonVariableTerm.class);
            return def.isEmpty()
                    ? ImmutableSet.of()
                    : ImmutableSet.of(def);
        }

        return childDefs.stream()
                .map(childDef -> childDef.compose(substitution))
                .map(s -> s.builder()
                        .restrictDomainTo(projectedVariables)
                        .restrictRangeTo(NonVariableTerm.class)
                        .build())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.removeDistincts();
        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChild.equals(child));
        return iqFactory.createUnaryIQTree(this, newChild, newTreeCache);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        if (child instanceof TrueNode) {
            return projectedVariables.stream()
                    .map(ImmutableSet::of)
                    .collect(ImmutableCollectors.toSet());
        }

        ImmutableSet<ImmutableSet<Variable>> childConstraints = child.inferUniqueConstraints();

        if (childConstraints.isEmpty())
            return ImmutableSet.of();

        ImmutableSet<ImmutableSet<Variable>> preservedConstraints = childConstraints.stream()
                .filter(projectedVariables::containsAll)
                .collect(ImmutableCollectors.toSet());

        VariableNullability variableNullability = getVariableNullability(child);
        ImmutableMap<Variable, ImmutableSet<Variable>> determinedByMap = getDeterminedByMap(variableNullability);

        ImmutableSet<ImmutableSet<Variable>> transformedConstraints = childConstraints.stream()
                .flatMap(childConstraint -> extractTransformedUniqueConstraint(childConstraint, determinedByMap))
                .collect(ImmutableCollectors.toSet());

        return transformedConstraints.isEmpty()
                ? preservedConstraints
                : preservedConstraints.isEmpty()
                    ? transformedConstraints
                    : Sets.union(preservedConstraints, transformedConstraints).immutableCopy();
    }

    /**
     * TODO: consider variable equality?
     */
    private Stream<ImmutableSet<Variable>> extractTransformedUniqueConstraint(ImmutableSet<Variable> childConstraint,
                                                                              ImmutableMap<Variable, ImmutableSet<Variable>> determinedByMap) {
        return getNewRepresentations(childConstraint, determinedByMap).stream();
    }

    /**
     * For each projected variable, computes the set of variables that uniquely determine it. This can happen by
     * (i) Variable is just kept in projection                                              Set(x) -> x
     * (ii) Variable is constructed using a function injective on a set of other variables  Y -> x where x = f(X), Y subset of X such that f is injective on Y
     */
    private ImmutableMap<Variable, ImmutableSet<Variable>> getDeterminedByMap(VariableNullability variableNullability) {
        return projectedVariables.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        v -> getDeterminedBy(Optional.ofNullable(substitution.get(v)).orElse(v), variableNullability)
                ));
    }

    private ImmutableSet<Variable> getDeterminedBy(ImmutableTerm term, VariableNullability variableNullability) {
        if(term instanceof Variable)
            return ImmutableSet.of((Variable)term);
        if(!(term instanceof ImmutableFunctionalTerm))
            return ImmutableSet.of();
        ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;

        VariableGenerator uselessVariableGenerator = new VariableGeneratorImpl(ImmutableSet.of(), termFactory);
        Optional<FunctionalTermDecomposition> analysis = functionalTerm.analyzeInjectivity(ImmutableSet.of(), variableNullability, uselessVariableGenerator);
        return analysis
                .map(t -> t.getLiftableTerm().getVariableStream())
                .orElse(Stream.of())
                .filter(v -> term.getVariableStream().anyMatch(v2 -> v2.equals(v)))
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * Finds all possible new representations of a previously holding UC after substitution.
     * This requires us to find, for each variable in the original UC, a projected variable that is determined by it.
     */
    private ImmutableSet<ImmutableSet<Variable>> getNewRepresentations(ImmutableSet<Variable> previousUC, ImmutableMap<Variable, ImmutableSet<Variable>> determinedByMap) {
        ImmutableSet.Builder<ImmutableSet<Variable>> builder = ImmutableSet.builder();
        ImmutableSet<Variable> relatedVariables = projectedVariables.stream()
                .filter(v -> previousUC.contains(v) || !Sets.intersection(previousUC, determinedByMap.get(v)).isEmpty())
                .collect(ImmutableCollectors.toSet());

        List<ImmutableList<Variable>> setsToCheck = relatedVariables.stream()
                .map(ImmutableList::of)
                .collect(Collectors.toList());

        while(!setsToCheck.isEmpty()) {
            var next = setsToCheck.remove(0);
            if(includesAll(next, previousUC, determinedByMap)) {
                builder.add(next.stream().collect(ImmutableCollectors.toSet()));
                continue;
            }
            setsToCheck.addAll(
                    relatedVariables.stream()
                            .filter(v -> v.getName().compareTo(next.get(next.size() - 1).getName()) > 0) //Only test variables in alphabetical order
                            .filter(v -> !includesAll(next, determinedByMap.get(v), determinedByMap)) //Skip variables that do not add new determinants
                            .map(v -> Stream.concat(next.stream(), Stream.of(v)).collect(ImmutableCollectors.toList()))
                            .collect(Collectors.toSet())
            );
        }
        var result = builder.build();

        return result.stream()
                .filter(uc -> result.stream()
                        .noneMatch(uc2 -> uc.containsAll(uc2) && !uc.equals(uc2)))
                .collect(ImmutableCollectors.toSet());
    }

    private boolean includesAll(ImmutableList<Variable> variables, ImmutableSet<Variable> target, ImmutableMap<Variable, ImmutableSet<Variable>> determinedByMap) {
        return variables.stream()
                .map(determinedByMap::get)
                .reduce(Set.of(), (result, item) -> Sets.union(result, item), (set1, set2) -> Sets.union(set1, set2))
                .containsAll(target);
    }

    private boolean isAtomicConstraint(ImmutableFunctionalTerm functionalTerm, ImmutableSet<Variable> childConstraint,
                                       VariableNullability variableNullability) {

        if (!functionalTerm.getVariables().containsAll(childConstraint))
            return false;

        VariableGenerator uselessVariableGenerator = new VariableGeneratorImpl(ImmutableSet.of(), termFactory);
        Optional<FunctionalTermDecomposition> analysis = functionalTerm.analyzeInjectivity(ImmutableSet.of(), variableNullability, uselessVariableGenerator);
        return analysis
                .map(FunctionalTermDecomposition::getLiftableTerm)
                .filter(t -> t.getVariableStream()
                        .collect(Collectors.toSet())
                        .containsAll(childConstraint))
                .isPresent();
    }

    private Stream<ImmutableSet<Variable>> extractDuplicatedConstraints(ImmutableSet<Variable> childConstraint) {
        Substitution<Variable> fullRenaming = getSubstitution().builder()
                .restrictRangeTo(Variable.class)
                .restrictRange(childConstraint::contains)
                .build();

        if (fullRenaming.isEmpty())
            return Stream.empty();

        ImmutableSet<Variable> fullRenamingDomain = fullRenaming.getDomain();

        //noinspection UnstableApiUsages
        return IntStream.range(1, fullRenamingDomain.size() + 1)
                .mapToObj(i -> Sets.combinations(fullRenamingDomain, i))
                .flatMap(Collection::stream)
                .map(fullRenaming::restrictDomainTo)
                .filter(Substitution::isInjective)
                .map(substitutionFactory::extractAnInjectiveVar2VarSubstitutionFromInverseOf)
                .map(s -> substitutionFactory.apply(s, childConstraint))
                .filter(projectedVariables::containsAll);
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        var childFDs = child.inferFunctionalDependencies();
        var nullability = getVariableNullability(child);
        ImmutableMap<Variable, ImmutableSet<Variable>> determinedByMap = getDeterminedByMap(nullability);
        return Stream.concat(childFDs.stream(), newDependenciesFromSubstitution(nullability))
                .flatMap(e -> translateFunctionalDependency(e.getKey(), e.getValue(), determinedByMap))
                .collect(FunctionalDependencies.toFunctionalDependencies())
                .concat(FunctionalDependencies.fromUniqueConstraints(uniqueConstraints, variables));
    }


    /**
     * Computes the functional dependencies that can be taken from substitutions. E.g. x = F(a) ==> a -> x
     */
    private Stream<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> newDependenciesFromSubstitution(VariableNullability nullability) {
        var variableToSubstitution = substitution.stream()
                .filter(e -> isDeterministic(e.getValue()))
                .map(e -> Maps.immutableEntry(
                        e.getValue().getVariableStream().collect(ImmutableCollectors.toSet()),
                        ImmutableSet.of(e.getKey())
                ))
                .filter(e -> !e.getKey().isEmpty());
        var substitutionToVariable = substitution.stream()
                .filter(e -> e.getValue() instanceof ImmutableFunctionalTerm)
                .filter(e -> isAtomicConstraint((ImmutableFunctionalTerm) e.getValue(), ((ImmutableFunctionalTerm) e.getValue()).getVariables(), nullability))
                .map(e -> Maps.immutableEntry(
                        ImmutableSet.of(e.getKey()),
                        e.getValue().getVariableStream().collect(ImmutableCollectors.toSet())
                ))
                .filter(e -> !e.getValue().isEmpty());
        var renamingDependencies = getSubstitution().builder()
                .restrictRangeTo(Variable.class)
                .build()
                .stream()
                .map(entry -> Maps.immutableEntry(ImmutableSet.of(entry.getKey()), ImmutableSet.of(entry.getValue())));
        return Streams.concat(variableToSubstitution, substitutionToVariable, renamingDependencies);
    }

    private Stream<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> translateFunctionalDependency(ImmutableSet<Variable> determinants,
                                                                                                            ImmutableSet<Variable> dependents,
                                                                                                            ImmutableMap<Variable, ImmutableSet<Variable>> determinedByMap) {
        //Dependents of new FD are all projected previous dependents + new variables that only use dependent variables in their substitution (with deterministic functions).
        var keptDependents = Sets.intersection(dependents, projectedVariables);
        var newDependents = substitution.stream()
                .filter(s -> dependents.containsAll(s.getValue().getVariableStream().collect(ImmutableCollectors.toSet()))
                        && this.isDeterministic(s.getValue()))
                .map(s -> s.getKey());
        var allDependents = Stream.concat(keptDependents.stream(), newDependents)
                .collect(ImmutableCollectors.toSet());
        if(allDependents.isEmpty())
            return Stream.of();


        Stream<ImmutableSet<Variable>> preservedDeterminants = projectedVariables.containsAll(determinants) ? Stream.of(determinants) : Stream.of();
        var newDeterminants = extractTransformedUniqueConstraint(determinants, determinedByMap);
        var allDeterminants = Streams.concat(preservedDeterminants, newDeterminants);

        return allDeterminants
                .map(determinant -> Maps.immutableEntry(determinant, Sets.difference(allDependents, determinant).immutableCopy()))
                .filter(e -> !e.getValue().isEmpty());
    }

    private boolean isDeterministic(ImmutableTerm term) {
        if(!(term instanceof ImmutableFunctionalTerm))
            return true;
        ImmutableFunctionalTerm f = (ImmutableFunctionalTerm) term;
        if(!f.getFunctionSymbol().isDeterministic())
            return false;
        return f.getTerms().stream()
                .allMatch(this::isDeterministic);
    }

    /**
     * For a construction node, none of the projected variables is required.
     */
    @Override
    public VariableNonRequirement computeVariableNonRequirement(IQTree child) {
        return VariableNonRequirement.of(getVariables());
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getChildVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return substitution.getDomain();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstructionNodeImpl that = (ConstructionNodeImpl) o;
        return projectedVariables.equals(that.projectedVariables) && substitution.equals(that.substitution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectedVariables, substitution);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return CONSTRUCTION_NODE_STR + " " + projectedVariables + " " + "[" + substitution + "]" ;
    }

    /**
     *  - Merges with a child construction
     *  - Removes itself if useless
     */
    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {

        IQTree liftedChild = child.normalizeForOptimization(variableGenerator);
        IQTree shrunkChild = notRequiredVariableRemover.optimize(liftedChild, childVariables, variableGenerator);
        QueryNode shrunkChildRoot = shrunkChild.getRootNode();
        if (shrunkChildRoot instanceof ConstructionNode)
            return mergeWithChild((ConstructionNode) shrunkChildRoot, (UnaryIQTree) shrunkChild, treeCache, variableGenerator);
        else if (shrunkChild.isDeclaredAsEmpty()) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
        /*
         * If useless, returns the child
         */
        else if (shrunkChild.getVariables().equals(projectedVariables))
            return shrunkChild;
        else {
            ConstructionSubstitutionNormalization normalization = substitutionNormalizer.normalizeSubstitution(
                    substitution.transform(t -> t.simplify(shrunkChild.getVariableNullability())),
                    projectedVariables);

            Optional<ConstructionNode> newTopConstructionNode = normalization.generateTopConstructionNode();

            IQTree updatedChild = normalization.updateChild(shrunkChild, variableGenerator);
            IQTree newChild = newTopConstructionNode
                    .map(c -> notRequiredVariableRemover.optimize(updatedChild, c.getChildVariables(), variableGenerator))
                    .orElse(updatedChild)
                    .normalizeForOptimization(variableGenerator);

            return newTopConstructionNode
                    .<IQTree>map(c -> iqFactory.createUnaryIQTree(c, newChild,
                            treeCache.declareAsNormalizedForOptimizationWithEffect()))
                    .orElseGet(() ->
                            iqTreeTools.createConstructionNodeTreeIfNontrivial(newChild, projectedVariables));
        }
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(
                substitutionFactory.apply(renamingSubstitution, projectedVariables),
                substitutionFactory.rename(renamingSubstitution, substitution));

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(newConstructionNode, newChild, newTreeCache);
    }

    @Override
    protected Optional<ExtendedProjectionNode> computeNewProjectionNode(ImmutableSet<Variable> newProjectedVariables,
                                                                        Substitution<ImmutableTerm> theta, IQTree newChild) {
        return Optional.of(theta)
                .filter(t -> !(t.isEmpty() && newProjectedVariables.equals(newChild.getVariables())))
                .map(t -> iqFactory.createConstructionNode(newProjectedVariables, t));
    }

    private IQTree mergeWithChild(ConstructionNode childConstructionNode, UnaryIQTree childIQ, IQTreeCache treeCache, VariableGenerator variableGenerator) {

        IQTree grandChild = childIQ.getChild();

        ConstructionSubstitutionNormalization substitutionNormalization = substitutionNormalizer.normalizeSubstitution(
                childConstructionNode.getSubstitution().compose(substitution)
                        .transform(t -> t.simplify(grandChild.getVariableNullability())),
                projectedVariables);

        Substitution<ImmutableTerm> newSubstitution = substitutionNormalization.getNormalizedSubstitution();

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                newSubstitution);

        IQTree updatedGrandChild = substitutionNormalization.updateChild(grandChild, variableGenerator);
        IQTree newGrandChild = notRequiredVariableRemover.optimize(updatedGrandChild,
                newConstructionNode.getChildVariables(), variableGenerator)
                .normalizeForOptimization(variableGenerator);

        return newGrandChild.getVariables().equals(newConstructionNode.getVariables())
                ? newGrandChild
                : iqFactory.createUnaryIQTree(newConstructionNode, newGrandChild, treeCache.declareAsNormalizedForOptimizationWithEffect());
    }

    public static class PropagationResults<T extends VariableOrGroundTerm> {

        public final Substitution<T> delta;
        public final Optional<ImmutableExpression> filter;
        public final Substitution<ImmutableTerm> theta;

       PropagationResults(Substitution<ImmutableTerm> theta,
                          Substitution<T> delta,
                          Optional<ImmutableExpression> newF) {
            this.theta = theta;
            this.delta = delta;
            this.filter = newF;
        }
    }
}
