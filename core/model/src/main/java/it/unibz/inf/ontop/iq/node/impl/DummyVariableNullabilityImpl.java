package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.node.DummyVariableNullability;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

public class DummyVariableNullabilityImpl implements DummyVariableNullability {

    private final Stream<Variable> variableStream;
    // Lazy
    private ImmutableSet<ImmutableSet<Variable>> nullableGroups;

    /**
     * "Dummy"
     */
    @AssistedInject
    private DummyVariableNullabilityImpl(@Assisted ImmutableFunctionalTerm functionalTerm,
                                    CoreUtilsFactory coreUtilsFactory, TermFactory termFactory,
                                    SubstitutionFactory substitutionFactory) {
        this(functionalTerm.getVariableStream(), coreUtilsFactory, termFactory, substitutionFactory);
    }

    /**
     * Dummy
     */
    @AssistedInject
    private DummyVariableNullabilityImpl(@Assisted Stream<Variable> variableStream,
                                    CoreUtilsFactory coreUtilsFactory, TermFactory termFactory,
                                    SubstitutionFactory substitutionFactory) {
        this.variableStream = variableStream;
    }

    @Override
    public boolean isPossiblyNullable(Variable variable) {
        return true;
    }

    @Override
    public boolean canPossiblyBeNullSeparately(ImmutableSet<Variable> variables) {
        return true;
    }

    @Override
    public boolean canPossiblyBeNullSeparately(ImmutableList<? extends ImmutableTerm> terms) {
        return true;
    }

    @Override
    public synchronized ImmutableSet<ImmutableSet<Variable>> getNullableGroups() {
        if (nullableGroups == null) {
            nullableGroups = variableStream
                    .map(ImmutableSet::of)
                    .collect(ImmutableCollectors.toSet());
        }
        return nullableGroups;
    }

    @Deprecated
    @Override
    public VariableNullability update(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                      ImmutableSet<Variable> projectedVariables) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VariableNullability applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        throw new UnsupportedOperationException();
    }
}
