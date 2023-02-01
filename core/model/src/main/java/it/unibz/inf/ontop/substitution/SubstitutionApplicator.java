package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

public interface SubstitutionApplicator<T extends ImmutableTerm>  {


    T applyToVariable(ImmutableSubstitution<? extends T> substitution, Variable variable);

    T apply(ImmutableSubstitution<? extends T> substitution, T t);


    default ImmutableList<T> applyToVariables(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends Variable> variables) {
        return variables.stream()
                .map(v -> applyToVariable(substitution, v))
                .collect(ImmutableCollectors.toList());
    }

    default ImmutableSet<T> applyToVariables(ImmutableSubstitution<? extends T> substitution, ImmutableSet<? extends Variable> terms) {
        return terms.stream()
                .map(v -> applyToVariable(substitution, v))
                .collect(ImmutableCollectors.toSet());
    }

    default ImmutableList<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends T> terms) {
        return terms.stream()
                .map(t -> apply(substitution, t))
                .collect(ImmutableCollectors.toList());
    }

    default ImmutableMap<Integer, T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableMap<Integer, ? extends T> argumentMap) {
        return argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> apply(substitution, e.getValue())));
    }


    static SubstitutionApplicator<ImmutableTerm> getImmutableTermInstance() {
        return new SubstitutionApplicator<>() {
            @Override
            public ImmutableTerm applyToVariable(ImmutableSubstitution<? extends ImmutableTerm> substitution, Variable variable) {
                return Optional.<ImmutableTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public ImmutableTerm apply(ImmutableSubstitution<? extends ImmutableTerm> substitution, ImmutableTerm t) {
                if (t instanceof Variable) {
                    return applyToVariable(substitution, (Variable) t);
                }
                if (t instanceof Constant) {
                    return t;
                }
                if (t instanceof ImmutableFunctionalTerm) {
                    return substitution.applyToFunctionalTerm((ImmutableFunctionalTerm) t);
                }
                throw new IllegalArgumentException("Unexpected kind of term: " + t.getClass());
            }
        };
    }

    static SubstitutionApplicator<Variable> getVariableInstance() {
        return new SubstitutionApplicator<>() {
            @Override
            public Variable applyToVariable(ImmutableSubstitution<? extends Variable> substitution, Variable variable) {
                return Optional.<Variable>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public Variable apply(ImmutableSubstitution<? extends Variable> substitution, Variable t) {
                return applyToVariable(substitution, t);
            }
        };
    }

    static SubstitutionApplicator<VariableOrGroundTerm> getVariableOrGroundTermInstance() {
        return new SubstitutionApplicator<>() {
            @Override
            public VariableOrGroundTerm applyToVariable(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, Variable variable) {
                return Optional.<VariableOrGroundTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }
            @Override
            public VariableOrGroundTerm apply(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, VariableOrGroundTerm t) {
                if (t instanceof Variable)
                    return applyToVariable(substitution, (Variable) t);

                return t;
            }
        };
    }

    static SubstitutionApplicator<NonFunctionalTerm> getNonFunctionalTermInstance() {
        return new SubstitutionApplicator<>() {
            @Override
            public NonFunctionalTerm applyToVariable(ImmutableSubstitution<? extends NonFunctionalTerm> substitution, Variable variable) {
                return Optional.<NonFunctionalTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public NonFunctionalTerm apply(ImmutableSubstitution<? extends NonFunctionalTerm> substitution, NonFunctionalTerm t) {
                if (t instanceof Variable)
                    return applyToVariable(substitution, (Variable) t);

                return t;
            }
        };
    }

}
