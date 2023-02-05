package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * Tools for new-gen immutable unifying substitutions.
 */
@Singleton
public class ImmutableUnificationTools {

    private final SubstitutionFactory substitutionFactory;

    @Inject
    public ImmutableUnificationTools(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }

    public Optional<ImmutableSubstitution<ImmutableTerm>> computeMGU(ImmutableTerm args1, ImmutableTerm args2) {
        return substitutionFactory.onImmutableTerms().unifierBuilder().unify(args1, args2).build();
    }



    public final class ArgumentMapUnification {
        private final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap;
        private final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        private ArgumentMapUnification(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                      ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.argumentMap = argumentMap;
            this.substitution = substitution;
        }

        public  ImmutableMap<Integer, ? extends VariableOrGroundTerm> getArgumentMap() {
            return argumentMap;
        }

        public ImmutableSubstitution<VariableOrGroundTerm> getSubstitution() {
            return substitution;
        }

        private Optional<ImmutableUnificationTools.ArgumentMapUnification> unify(
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap) {

            ImmutableMap<Integer, VariableOrGroundTerm> updatedArgumentMap =
                    substitutionFactory.onVariableOrGroundTerms().applyToTerms(substitution, newArgumentMap);

            Optional<ImmutableSubstitution<VariableOrGroundTerm>> unifier = substitutionFactory.onVariableOrGroundTerms().unifierBuilder()
                            .unify(Sets.intersection(argumentMap.keySet(), updatedArgumentMap.keySet()).stream(), argumentMap::get, updatedArgumentMap::get)
                            .build();

            return unifier
                    .flatMap(u -> substitutionFactory.onVariableOrGroundTerms().unifierBuilder(substitution)
                            .unify(u.entrySet().stream(), Map.Entry::getKey, Map.Entry::getValue)
                            .build()
                            .map(s -> new ArgumentMapUnification(
                                    substitutionFactory.onVariableOrGroundTerms().applyToTerms(u, ExtensionalDataNode.union(argumentMap, updatedArgumentMap)),
                                    s)));
        }
    }


    public Optional<ImmutableUnificationTools.ArgumentMapUnification> getArgumentMapUnifier(
            Stream<ImmutableMap<Integer, ? extends VariableOrGroundTerm>> arguments) {
        return arguments
                .reduce(Optional.of(new ArgumentMapUnification(ImmutableMap.of(), substitutionFactory.getSubstitution())),
                        (o, n) -> o.flatMap(u -> u.unify(n)),
                        (m1, m2) -> {
                            throw new MinorOntopInternalBugException("Not expected to be run in parallel");
                        });
    }





    public InjectiveVar2VarSubstitution getPrioritizingRenaming(ImmutableSubstitution<?> substitution, ImmutableSet<Variable> priorityVariables) {
        ImmutableSubstitution<Variable> renaming = substitution.builder()
                .restrictDomainTo(priorityVariables)
                .restrictRangeTo(Variable.class)
                .restrictRange(t -> !priorityVariables.contains(t))
                .build();

        return substitutionFactory.extractAnInjectiveVar2VarSubstitutionFromInverseOf(renaming);
    }


}
