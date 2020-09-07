package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ValuesNodeImpl extends LeafIQTreeImpl implements ValuesNode {


    private static final String VALUES_NODE_STR = "VALUES";
    // The variables as used in this node, we need to keep order.
    private final ImmutableList<Variable> orderedVariables;
    // The variables consistent with all interfaces, as unordered set.
    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableList<ImmutableList<Constant>> values;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;
    // LAZY
    private VariableNullability variableNullability;
    // LAZY
    private Boolean isDistinct;

    @AssistedInject
    protected ValuesNodeImpl(@Assisted("orderedVariables") ImmutableList<Variable> orderedVariables,
                             @Assisted("values") ImmutableList<ImmutableList<Constant>> values,
                             IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory,
                             OntopModelSettings settings, SubstitutionFactory substitutionFactory) {
        super(iqTreeTools, iqFactory);
        this.orderedVariables = orderedVariables;
        this.projectedVariables = ImmutableSet.copyOf(orderedVariables);
        this.values = values;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;

        if (settings.isTestModeEnabled())
            validate();
    }

    @Override
    public ImmutableList<ImmutableList<Constant>> getValues() {
        return values;
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        // Check if there is potential to create construction node
        ImmutableSet<Integer> singleValueVariableIndices = IntStream.range(0, orderedVariables.size())
                .filter(i -> 1 == getValueStream(orderedVariables.get(i))
                        .unordered()
                        .distinct()
                        .count())
                .boxed()
                .collect(ImmutableCollectors.toSet());

        if (singleValueVariableIndices.size() > 0) {
            // Can be normalized into a construction/child node pair. Start by creating ConstructionNode.
            ImmutableSubstitution<ImmutableTerm> substitutions = substitutionFactory.getSubstitution(
                    singleValueVariableIndices.stream()
                        .collect(ImmutableCollectors.toMap(orderedVariables::get, i -> values.get(0).get(i))));
            UnaryOperatorNode constructionNode = iqFactory.createConstructionNode(projectedVariables, substitutions);

            // Create the child node as ValueNode, possibly reduce to TrueNode
            ImmutableSet<Integer> multiValueVariableIndices = IntStream.range(0, orderedVariables.size())
                    .filter(i -> !singleValueVariableIndices.contains(i))
                    .boxed()
                    .collect(ImmutableCollectors.toSet());
            ImmutableList<Variable> newValuesNodeVariables =
                    multiValueVariableIndices.stream()
                    .map(orderedVariables::get)
                    .collect(ImmutableCollectors.toList());
            ImmutableList<ImmutableList<Constant>> newValuesNodeValues = values.stream()
                    .map(constants -> multiValueVariableIndices.stream()
                            .map(constants::get)
                            .collect(ImmutableCollectors.toList()))
                    .collect(ImmutableCollectors.toList());
            IQTree childNode = possiblyReduceToTrueNode(iqFactory.createValuesNode(newValuesNodeVariables, newValuesNodeValues));

            return iqFactory.createUnaryIQTree(constructionNode, childNode);
        }
        return possiblyReduceToTrueNode(this);
    }

    private IQTree possiblyReduceToTrueNode(ValuesNode valuesNode) {
        return ((valuesNode.getVariables().size() == 0) && (valuesNode.getValues().size() == 1))
                ? iqFactory.createTrueNode()
                : valuesNode;
    }


    public Stream<Constant> getValueStream(Variable variable) {
        int index = orderedVariables.indexOf(variable);
        if (index < 0)
            return Stream.empty();

        return values.stream()
                .map(t -> t.get(index));
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ValuesNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        return getVariableNullability().isPossiblyNullable(variable);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof ValuesNode)
                && ((ValuesNode) node).getVariables().equals(projectedVariables)
                && ((ValuesNode) node).getValues().equals(values);
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof ValuesNode) && queryNode.isSyntacticallyEquivalentTo(this);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformValues(this);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitValues(this);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        ImmutableList<Variable> newVariables = orderedVariables.stream()
                .map(freshRenamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toList());

        return newVariables.equals(orderedVariables)
                ? this
                : iqFactory.createValuesNode(newVariables, values);
    }

    @Override
    public IQTree applyFreshRenamingToAllVariables(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        return applyFreshRenaming(freshRenamingSubstitution);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        throw new RuntimeException("TODO: Support");
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isDistinct() {
        if (isDistinct == null) {
            isDistinct = (values.size() == values.stream()
                                                .unordered()
                                                .distinct()
                                                .count());
        }
        return isDistinct;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return values.isEmpty();
    }

    @Override
    public synchronized VariableNullability getVariableNullability() {
        // Implemented by looking through the values, if one of them is null
        // the corresponding variable is seen as nullable.
        if (variableNullability == null) {
            ImmutableSet<ImmutableSet<Variable>> nullableGroups = IntStream.range(0, orderedVariables.size())
                    .filter(i -> values.stream()
                            .map(t -> t.get(i))
                            .anyMatch(ImmutableTerm::isNull))
                    .boxed()
                    .map(orderedVariables::get)
                    .map(ImmutableSet::of)
                    .collect(ImmutableCollectors.toSet());
            variableNullability = coreUtilsFactory.createVariableNullability(nullableGroups, projectedVariables);
        }
        return variableNullability;
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
        // Add type checking of value/variable
        if (orderedVariables.size() != projectedVariables.size()) {
            throw new InvalidIntermediateQueryException("Variables must be unique");
        }
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        // TODO: Worth implementing?
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getNotInternallyRequiredVariables() {
        return projectedVariables;
    }

    @Override
    public ValuesNode clone() {
        return iqFactory.createValuesNode(orderedVariables, values);
    }

    @Override
    public ImmutableList<Variable> getOrderedVariables() {
        return orderedVariables;
    }

    @Override
    public String toString() {
        String valuesString = values.stream().map(tuple -> tuple.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","," (",")")))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        return VALUES_NODE_STR + " " + orderedVariables + valuesString;
    }
}
