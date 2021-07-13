package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Accessible through Guice (recommended) or through {@link CoreSingletons} .
 */
public interface CoreUtilsFactory {

    VariableGenerator createVariableGenerator(Collection<Variable> knownVariables);

    VariableNullability createVariableNullability(@Assisted("nullableGroups") ImmutableSet<ImmutableSet<Variable>> nullableGroups,
                                                  @Assisted("scope") ImmutableSet<Variable> scope);

    /**
     * Variables are considered as separately nullable
     */
    VariableNullability createSimplifiedVariableNullability(Stream<Variable> variables);

    /**
     * All the variables of the expression are treated as separately nullable
     */
    VariableNullability createSimplifiedVariableNullability(ImmutableFunctionalTerm functionalTerm);

    VariableNullability createEmptyVariableNullability(ImmutableSet<Variable> scope);

    ProjectionDecomposer createProjectionDecomposer(Predicate<ImmutableFunctionalTerm> decompositionOracle,
                                                    Predicate<NonFunctionalTerm> postprocessNonFunctionalDefinitionOracle);
}
