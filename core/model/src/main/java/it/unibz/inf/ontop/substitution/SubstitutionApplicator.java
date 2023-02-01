package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

public interface SubstitutionApplicator<T extends ImmutableTerm>  {


    T apply(ImmutableSubstitution<? extends T> substitution, Variable variable);

    T applyToTerm(ImmutableSubstitution<? extends T> substitution, T t);


    default ImmutableFunctionalTerm apply(ImmutableSubstitution<? extends T> substitution, ImmutableFunctionalTerm term) {
        if (term.getFunctionSymbol() instanceof BooleanFunctionSymbol)
            return apply(substitution, (ImmutableExpression)term);

        if (substitution.isEmpty())
            return term;

        return substitution.getTermFactory().getImmutableFunctionalTerm(term.getFunctionSymbol(),
                getImmutableTermInstance().applyToTerms(substitution, term.getTerms()));
    }

    default ImmutableExpression apply(ImmutableSubstitution<? extends T> substitution, ImmutableExpression expression) {
        if (substitution.isEmpty())
            return expression;

        return substitution.getTermFactory().getImmutableExpression(expression.getFunctionSymbol(),
                getImmutableTermInstance().applyToTerms(substitution, expression.getTerms()));
    }

    default ImmutableList<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends Variable> variables) {
        return variables.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toList());
    }

    default ImmutableSet<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableSet<? extends Variable> terms) {
        return terms.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toSet());
    }

    default ImmutableList<T> applyToTerms(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends T> terms) {
        return terms.stream()
                .map(t -> applyToTerm(substitution, t))
                .collect(ImmutableCollectors.toList());
    }

    default ImmutableMap<Integer, T> applyToTerms(ImmutableSubstitution<? extends T> substitution, ImmutableMap<Integer, ? extends T> argumentMap) {
        return argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> applyToTerm(substitution, e.getValue())));
    }


    static SubstitutionApplicator<ImmutableTerm> getImmutableTermInstance() {
        return new SubstitutionApplicator<>() {
            @Override
            public ImmutableTerm apply(ImmutableSubstitution<? extends ImmutableTerm> substitution, Variable variable) {
                return Optional.<ImmutableTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public ImmutableTerm applyToTerm(ImmutableSubstitution<? extends ImmutableTerm> substitution, ImmutableTerm t) {
                if (t instanceof Variable) {
                    return apply(substitution, (Variable) t);
                }
                if (t instanceof Constant) {
                    return t;
                }
                if (t instanceof ImmutableFunctionalTerm) {
                    return apply(substitution, (ImmutableFunctionalTerm) t);
                }
                throw new IllegalArgumentException("Unexpected kind of term: " + t.getClass());
            }
        };
    }

    static SubstitutionApplicator<Variable> getVariableInstance() {
        return new SubstitutionApplicator<>() {
            @Override
            public Variable apply(ImmutableSubstitution<? extends Variable> substitution, Variable variable) {
                return Optional.<Variable>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public Variable applyToTerm(ImmutableSubstitution<? extends Variable> substitution, Variable t) {
                return apply(substitution, t);
            }
        };
    }

    static SubstitutionApplicator<VariableOrGroundTerm> getVariableOrGroundTermInstance() {
        return new SubstitutionApplicator<>() {
            @Override
            public VariableOrGroundTerm apply(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, Variable variable) {
                return Optional.<VariableOrGroundTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }
            @Override
            public VariableOrGroundTerm applyToTerm(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, VariableOrGroundTerm t) {
                return (t instanceof Variable) ? apply(substitution, (Variable) t) : t;
            }
        };
    }

    static SubstitutionApplicator<NonFunctionalTerm> getNonFunctionalTermInstance() {
        return new SubstitutionApplicator<>() {
            @Override
            public NonFunctionalTerm apply(ImmutableSubstitution<? extends NonFunctionalTerm> substitution, Variable variable) {
                return Optional.<NonFunctionalTerm>ofNullable(substitution.get(variable)).orElse(variable);
            }

            @Override
            public NonFunctionalTerm applyToTerm(ImmutableSubstitution<? extends NonFunctionalTerm> substitution, NonFunctionalTerm t) {
                return (t instanceof Variable)  ? apply(substitution, (Variable) t) : t;
            }
        };
    }

}
