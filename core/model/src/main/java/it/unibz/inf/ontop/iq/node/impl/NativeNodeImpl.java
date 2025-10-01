package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;


public class NativeNodeImpl extends LeafIQTreeImpl implements NativeNode {

    private static final String NATIVE_STRING = "NATIVE ";

    private final ImmutableMap<Variable, DBTermType> variableTypeMap;
    private final String nativeQueryString;
    private final VariableNullability variableNullability;
    // TODO: does sorted really here help?
    //  (ImmutableSet would preserve the insertion order anyway)
    private final ImmutableSortedSet<Variable> variables;
    private final ImmutableMap<Variable, QuotedID> columnNames;

    @AssistedInject
    private NativeNodeImpl(@Assisted ImmutableSortedSet<Variable> variables,
                           @Assisted("variableTypeMap") ImmutableMap<Variable, DBTermType> variableTypeMap,
                           @Assisted("columnNames") ImmutableMap<Variable, QuotedID> columnNames,
                           @Assisted String nativeQueryString,
                           @Assisted VariableNullability variableNullability,
                           IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                           CoreUtilsFactory coreUtilsFactory,
                           OntopModelSettings settings) {
        super(iqTreeTools, iqFactory, substitutionFactory, coreUtilsFactory);
        this.variables = variables;
        this.nativeQueryString = nativeQueryString;
        this.variableNullability = variableNullability;
        this.variableTypeMap = variableTypeMap;
        this.columnNames = columnNames;

        if (settings.isTestModeEnabled()) {
            if (!variables.equals(variableTypeMap.keySet()))
                throw new InvalidIntermediateQueryException("The variableTypeMap must contain " +
                        "all the projected variables and only them");
        }
    }

    @Override
    public ImmutableMap<Variable, DBTermType> getTypeMap() {
        return variableTypeMap;
    }

    @Override
    public String getNativeQueryString() {
        return nativeQueryString;
    }


    @Override
    public ImmutableSortedSet<Variable> getVariables() {
        return variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof NativeNodeImpl) {
            NativeNodeImpl that = (NativeNodeImpl) o;
            return nativeQueryString.equals(that.nativeQueryString) && variables.equals(that.variables);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nativeQueryString, variables);
    }

    @Override
    public ImmutableMap<Variable, QuotedID> getColumnNames() {
        return columnNames;
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            VariableGenerator variableGenerator) {
        throw new UnsupportedOperationException("NativeNode does not support descending substitutions (too late)");
    }

    @Override
    public NativeNode applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        throw new UnsupportedOperationException("NativeNode does not support renaming (too late)");
    }

    /**
     * TODO: implement seriously
     */
    @Override
    public boolean isDistinct() {
        return false;
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
     * Dummy implementation (considered too late for inferring it)
     */
    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        return ImmutableSet.of();
    }

    /**
     * Dummy implementation (considered too late for inferring it)
     */
    @Override
    public FunctionalDependencies inferFunctionalDependencies() {
        return FunctionalDependencies.empty();
    }

    @Override
    public VariableNonRequirement getVariableNonRequirement() {
        return VariableNonRequirement.of(getVariables());
    }

    @Override
    public String toString() {
        return NATIVE_STRING + variables + "\n" + nativeQueryString;
    }
}
