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
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.QueryNodeVisitor;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValuesNodeImpl extends LeafIQTreeImpl implements ValuesNode {


    private static final String VALUES_NODE_STR = "VALUES";
    // The variables as used in this node, we need to keep order.
    private final ImmutableList<Variable> orderedVariables;
    // The variables consistent with all interfaces, as unordered set.
    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableList<ImmutableList<Constant>> values;
    private final CoreUtilsFactory coreUtilsFactory;
    // LAZY
    private VariableNullability variableNullability;

    @AssistedInject
    protected ValuesNodeImpl(@Assisted("orderedVariables") ImmutableList<Variable> orderedVariables,
                             @Assisted("values") ImmutableList<ImmutableList<Constant>> values,
                             IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory,
                             OntopModelSettings settings) {
        super(iqTreeTools, iqFactory);
        this.orderedVariables = orderedVariables;
        this.projectedVariables = ImmutableSet.copyOf(orderedVariables);
        this.values = values;
        this.coreUtilsFactory = coreUtilsFactory;

        if (settings.isTestModeEnabled())
            validate();
    }

    @Override
    public ImmutableList<ImmutableList<Constant>> getValues() {
        return values;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        throw new RuntimeException("TODO: Support");
    }

    @Override
    public ValuesNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        throw new RuntimeException("TODO: Support");
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
        throw new RuntimeException("TODO: Support");
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        throw new RuntimeException("TODO: Support");
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        throw new RuntimeException("TODO: Support");
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        throw new RuntimeException("TODO: Support");
    }

    @Override
    public IQTree applyFreshRenamingToAllVariables(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        throw new RuntimeException("TODO: Support");
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
        throw new RuntimeException("TODO: Support");
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
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(VALUES_NODE_STR + " " + orderedVariables);
        for (ImmutableList<Constant> tuple : values) {
            stringBuilder.append('\n');
            stringBuilder.append("  ");
            stringBuilder.append(tuple.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",","(",")"))
            );
        }
        return stringBuilder.toString();
    }
}
