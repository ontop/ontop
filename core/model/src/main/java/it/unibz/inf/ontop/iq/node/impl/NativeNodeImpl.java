package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;


public class NativeNodeImpl extends LeafIQTreeImpl implements NativeNode {

    private static final String NATIVE_STRING = "NATIVE ";

    private final ImmutableSet<Variable> variables;
    private final String nativeQueryString;
    private final VariableNullability variableNullability;

    @AssistedInject
    private NativeNodeImpl(@Assisted ImmutableSet<Variable> variables,
                           @Assisted String nativeQueryString,
                           @Assisted VariableNullability variableNullability,
                           IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        super(iqTreeTools, iqFactory);
        this.variables = variables;
        this.nativeQueryString = nativeQueryString;
        this.variableNullability = variableNullability;
    }

    @Override
    public String getNativeQueryString() {
        return nativeQueryString;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        throw new UnsupportedOperationException("Should NativeNode support visitors?");
    }

    @Override
    public LeafIQTree acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        throw new UnsupportedOperationException("NativeNode does not support transformer (too late)");
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        throw new UnsupportedOperationException("NativeNode does not support transformer (too late)");
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return variables;
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        return variableNullability.isPossiblyNullable(variable);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return isEquivalentTo(node);
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
        return variables;
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof NativeNode)
                && ((NativeNode) queryNode).getVariables().equals(variables)
                && ((NativeNode) queryNode).getNativeQueryString().equals(nativeQueryString);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return variables;
    }

    @Override
    public IQTree acceptTransformer(IQTransformer transformer) {
        throw new UnsupportedOperationException("NativeNode does not support transformer (too late)");
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        throw new UnsupportedOperationException("NativeNode does not support descending substitutions (too late)");
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return variables;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public VariableNullability getVariableNullability() {
        return variableNullability;
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
    }

    /**
     * TODO: should we support it?
     */
    @Override
    public ImmutableSet<TermType> getPossibleTermTypes(Variable variable) {
        throw new UnsupportedOperationException("NativeNode does not support term type inference (too late)");
    }

    @Override
    public String toString() {
        return NATIVE_STRING + variables + "\n" + nativeQueryString;
    }
}
