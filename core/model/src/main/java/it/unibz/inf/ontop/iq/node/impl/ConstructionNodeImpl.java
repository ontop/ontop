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
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm.FunctionalTermDecomposition;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.VariableGeneratorImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "BindingAnnotationWithoutInject"})
public class ConstructionNodeImpl extends ExtendedProjectionNodeImpl implements ConstructionNode {

    private static final String CONSTRUCTION_NODE_STR = "CONSTRUCT";

    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableSubstitution<ImmutableTerm> substitution;
    private final ImmutableSet<Variable> childVariables;

    private final ConstructionSubstitutionNormalizer substitutionNormalizer;
    private final NotRequiredVariableRemover notRequiredVariableRemover;

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 @Assisted ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                 ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 OntopModelSettings settings,
                                 ConstructionSubstitutionNormalizer substitutionNormalizer,
                                 NotRequiredVariableRemover notRequiredVariableRemover) {
        super(substitutionFactory, iqFactory, unificationTools, constructionNodeTools, substitutionTools, termFactory);
        this.projectedVariables = projectedVariables;
        this.substitution = (ImmutableSubstitution<ImmutableTerm>) substitution;
        this.substitutionNormalizer = substitutionNormalizer;
        this.notRequiredVariableRemover = notRequiredVariableRemover;
        this.childVariables = extractChildVariables(projectedVariables, this.substitution);

        if (settings.isTestModeEnabled())
            validateNode();
    }

    /**
     * Without substitution.
     */
    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 OntopModelSettings settings,
                                 ConstructionSubstitutionNormalizer substitutionNormalizer,
                                 NotRequiredVariableRemover notRequiredVariableRemover) {
        this(projectedVariables, substitutionFactory.getSubstitution(), unificationTools, constructionNodeTools,
                substitutionTools, substitutionFactory, termFactory, iqFactory, settings, substitutionNormalizer, notRequiredVariableRemover);
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
        if (substitutionDomain.stream()
                .anyMatch(childVariables::contains)) {
            throw new InvalidQueryNodeException("ConstructionNode: variables defined by the substitution cannot " +
                    "be used for defining other variables.\n" + this);
        }

        // Substitution to non-projected variables is incorrect
        if (substitution.getRange().stream()
                .filter(v -> v instanceof Variable)
                .map(v -> (Variable) v)
                .anyMatch(v -> !projectedVariables.contains(v))) {
            throw new InvalidQueryNodeException(
                    "ConstructionNode: substituting a variable " +
                            "by a non-projected variable is incorrect.\n" + this);
        }
    }

    private static ImmutableSet<Variable> extractChildVariables(ImmutableSet<Variable> projectedVariables,
                                                          ImmutableSubstitution<ImmutableTerm> substitution) {
        ImmutableSet<Variable> variableDefinedByBindings = substitution.getDomain();

        Stream<Variable> variablesRequiredByBindings = substitution.getRange().stream()
                .flatMap(ImmutableTerm::getVariableStream);

        //return only the variables that are also used in the bindings for the child of the construction node
        return Stream.concat(projectedVariables.stream(), variablesRequiredByBindings)
                .filter(v -> !variableDefinedByBindings.contains(v))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
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
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        collectedVariableBuilder.addAll(projectedVariables);

        collectedVariableBuilder.addAll(substitution.getDomain());
        for (ImmutableTerm term : substitution.getRange()) {
            if (term instanceof Variable) {
                collectedVariableBuilder.add((Variable)term);
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                collectedVariableBuilder.addAll(((ImmutableFunctionalTerm)term).getVariables());
            }
        }

        return collectedVariableBuilder.build();
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        if (!inferUniqueConstraints(child).isEmpty())
            return true;
        if (child instanceof TrueNode)
            return true;

        QueryNode childRoot = child.getRootNode();
        if ((childRoot instanceof SliceNode)
                && ((SliceNode) childRoot).getLimit().filter(l -> l == 1).isPresent())
            return true;

        return false;
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
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
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
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> childDefs = child.getPossibleVariableDefinitions();

        if (childDefs.isEmpty()) {
            ImmutableSubstitution<NonVariableTerm> def = substitution.builder().restrictRangeTo(NonVariableTerm.class).build();
            return def.isEmpty()
                    ? ImmutableSet.of()
                    : ImmutableSet.of(def);
        }

        return childDefs.stream()
                .map(childDef -> substitutionFactory.compose(childDef, substitution))
                .map(ImmutableSubstitution::builder)
                .map(b -> b.restrictRangeTo(NonVariableTerm.class))
                .map(b -> b.restrictDomain(projectedVariables::contains))
                .map(ImmutableSubstitution.Builder::build)
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

        ImmutableSet<ImmutableSet<Variable>> transformedConstraints = childConstraints.stream()
                .flatMap(childConstraint -> extractTransformedUniqueConstraint(childConstraint, variableNullability))
                .collect(ImmutableCollectors.toSet());

        return transformedConstraints.isEmpty()
                ? preservedConstraints
                : preservedConstraints.isEmpty()
                    ? transformedConstraints
                    : Sets.union(preservedConstraints, transformedConstraints).immutableCopy();
    }

    /**
     * TODO: consider variable equality?
     *
     * TODO: consider producing composite constraints?
     *
     */
    private Stream<ImmutableSet<Variable>> extractTransformedUniqueConstraint(ImmutableSet<Variable> childConstraint,
                                                                              VariableNullability variableNullability) {
        Stream<ImmutableSet<Variable>> atomicConstraints = substitution.entrySet().stream()
                .filter(e -> e.getValue() instanceof ImmutableFunctionalTerm)
                .filter(e -> isAtomicConstraint((ImmutableFunctionalTerm)e.getValue(), childConstraint, variableNullability))
                .map(Map.Entry::getKey)
                .map(ImmutableSet::of);

        Stream<ImmutableSet<Variable>> duplicatedConstraints = extractDuplicatedConstraints(childConstraint);

        return Stream.concat(atomicConstraints, duplicatedConstraints);
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
        ImmutableSubstitution<Variable> fullRenaming = getSubstitution()
                .filter((k, v) -> childConstraint.contains(v))
                .castTo(Variable.class);

        if (fullRenaming.isEmpty())
            return Stream.empty();

        ImmutableSet<Variable> fullRenamingDomain = fullRenaming.getDomain();

        //noinspection UnstableApiUsage
        return IntStream.range(1, fullRenamingDomain.size() + 1)
                .mapToObj(i -> Sets.combinations(fullRenamingDomain, i))
                .flatMap(Collection::stream)
                .map(comb -> fullRenaming.filter(comb::contains))
                // Remove non-injective substitutions
                .filter(s -> {
                    ImmutableCollection<Variable> values = s.getRange();
                    return values.size() == ImmutableSet.copyOf(values).size();
                })
                // Inverse
                .map(s -> substitutionFactory.getInjectiveVar2VarSubstitution(
                        s.entrySet().stream()
                                .collect(ImmutableCollectors.toMap(
                                        Map.Entry::getValue,
                                        Map.Entry::getKey))))
                .map(s -> childConstraint.stream()
                            .map(s::applyToVariable)
                            .collect(ImmutableCollectors.toSet()))
                .filter(projectedVariables::containsAll);
    }

    /**
     * For a construction node, none of the projected variables is required.
     */
    @Override
    public ImmutableSet<Variable> computeNotInternallyRequiredVariables(IQTree child) {
        return getVariables();
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
                    substitution.transform(v -> v.simplify(shrunkChild.getVariableNullability())), projectedVariables);

            Optional<ConstructionNode> newTopConstructionNode = normalization.generateTopConstructionNode();

            IQTree updatedChild = normalization.updateChild(shrunkChild, variableGenerator);
            IQTree newChild = newTopConstructionNode
                    .map(c -> notRequiredVariableRemover.optimize(updatedChild, c.getChildVariables(), variableGenerator))
                    .orElse(updatedChild)
                    .normalizeForOptimization(variableGenerator);

            return newTopConstructionNode
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, newChild,
                            treeCache.declareAsNormalizedForOptimizationWithEffect()))
                    .orElseGet(() -> projectedVariables.equals(newChild.getVariables())
                            ? newChild
                            : iqFactory.createUnaryIQTree(
                                    iqFactory.createConstructionNode(projectedVariables),
                                    newChild));
        }
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);

        ImmutableSet<Variable> newVariables = projectedVariables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toSet());

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(
                newVariables,
                renamingSubstitution.applyRenaming(substitution));

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(newConstructionNode, newChild, newTreeCache);
    }

    @Override
    protected Optional<ExtendedProjectionNode> computeNewProjectionNode(ImmutableSet<Variable> newProjectedVariables,
                                                                        ImmutableSubstitution<ImmutableTerm> theta, IQTree newChild) {
        return Optional.of(theta)
                .filter(t -> !(t.isEmpty() && newProjectedVariables.equals(newChild.getVariables())))
                .map(t -> iqFactory.createConstructionNode(newProjectedVariables, t));
    }

    private IQTree mergeWithChild(ConstructionNode childConstructionNode, UnaryIQTree childIQ, IQTreeCache treeCache, VariableGenerator variableGenerator) {

        IQTree grandChild = childIQ.getChild();

        ConstructionSubstitutionNormalization substitutionNormalization = substitutionNormalizer.normalizeSubstitution(
                substitutionFactory.compose(childConstructionNode.getSubstitution(), substitution)
                        .transform(v -> v.simplify(grandChild.getVariableNullability())),
                projectedVariables
        );

        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitutionNormalization.getNormalizedSubstitution();

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

        public final ImmutableSubstitution<T> delta;
        public final Optional<ImmutableExpression> filter;
        public final ImmutableSubstitution<ImmutableTerm> theta;

       PropagationResults(ImmutableSubstitution<ImmutableTerm> theta,
                           ImmutableSubstitution<T> delta,
                           Optional<ImmutableExpression> newF) {
            this.theta = theta;
            this.delta = delta;
            this.filter = newF;
        }
    }
}
