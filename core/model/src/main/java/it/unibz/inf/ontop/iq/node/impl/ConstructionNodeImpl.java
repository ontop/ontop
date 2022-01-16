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
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;
import java.util.Optional;
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
                                 @Assisted ImmutableSubstitution<ImmutableTerm> substitution,
                                 ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 OntopModelSettings settings,
                                 ConstructionSubstitutionNormalizer substitutionNormalizer,
                                 NotRequiredVariableRemover notRequiredVariableRemover) {
        super(substitutionFactory, iqFactory, unificationTools, constructionNodeTools, substitutionTools, termFactory);
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.substitutionNormalizer = substitutionNormalizer;
        this.notRequiredVariableRemover = notRequiredVariableRemover;
        this.childVariables = extractChildVariables(projectedVariables, substitution);

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
                    "of the substitution must be projected.\n" + toString());
        }

        // The variables contained in the domain and in the range of the substitution must be disjoint
        if (substitutionDomain.stream()
                .anyMatch(childVariables::contains)) {
            throw new InvalidQueryNodeException("ConstructionNode: variables defined by the substitution cannot " +
                    "be used for defining other variables.\n" + toString());
        }

        // Substitution to non-projected variables is incorrect
        if (substitution.getImmutableMap().values().stream()
                .filter(v -> v instanceof Variable)
                .map(v -> (Variable) v)
                .anyMatch(v -> !projectedVariables.contains(v))) {
            throw new InvalidQueryNodeException(
                    "ConstructionNode: substituting a variable " +
                            "by a non-projected variable is incorrect.\n"
                + toString());
        }
    }

    private static ImmutableSet<Variable> extractChildVariables(ImmutableSet<Variable> projectedVariables,
                                                          ImmutableSubstitution<ImmutableTerm> substitution) {
        ImmutableSet<Variable> variableDefinedByBindings = substitution.getDomain();

        Stream<Variable> variablesRequiredByBindings = substitution.getImmutableMap().values().stream()
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

        ImmutableMap<Variable, ImmutableTerm> substitutionMap = substitution.getImmutableMap();

        collectedVariableBuilder.addAll(substitutionMap.keySet());
        for (ImmutableTerm term : substitutionMap.values()) {
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
        return !inferUniqueConstraints(child).isEmpty();
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
            ImmutableSubstitution<NonVariableTerm> def = substitution.getFragment(NonVariableTerm.class);
            return def.isEmpty()
                    ? ImmutableSet.of()
                    : ImmutableSet.of(def);
        }

        return childDefs.stream()
                .map(childDef -> childDef.composeWith(substitution))
                .map(s -> s.filter(projectedVariables::contains))
                .map(s -> s.getFragment(NonVariableTerm.class))
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
        return child.inferUniqueConstraints().stream()
                .filter(projectedVariables::containsAll)
                .collect(ImmutableCollectors.toSet());
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

            IQTree updatedChild = normalization.updateChild(shrunkChild);
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
                childConstructionNode.getSubstitution().composeWith(substitution).transform(v -> v.simplify(grandChild.getVariableNullability())),
                projectedVariables
        );

        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitutionNormalization.getNormalizedSubstitution();

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                newSubstitution);

        IQTree updatedGrandChild = substitutionNormalization.updateChild(grandChild);
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

        /**
         * After tauC propagation
         */
        PropagationResults(ImmutableSubstitution<NonFunctionalTerm> thetaCBar,
                           ImmutableSubstitution<ImmutableFunctionalTerm> thetaFBar,
                           ImmutableSubstitution<T> newDeltaC,
                           Optional<ImmutableExpression> f) {
            this.theta = thetaFBar.composeWith(thetaCBar);
            this.delta = newDeltaC;
            this.filter = f;
        }

        /**
         * After tauF propagation
         */
        PropagationResults(ImmutableSubstitution<ImmutableTerm> theta,
                           ImmutableSubstitution<T> delta,
                           Optional<ImmutableExpression> newF) {
            this.theta = theta;
            this.delta = delta;
            this.filter = newF;
        }
    }
}
