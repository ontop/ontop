package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TermTypeInferenceImpl implements TermTypeInference {

    @Nullable
    private final TermType termType;

    @Nullable
    private final Variable variable;

    /**
     * Determined
     */
    private TermTypeInferenceImpl(@Nonnull TermType termType) {
        this.termType = termType;
        this.variable = null;
    }

    /**
     * Corresponds to the type of a variable
     */
    private TermTypeInferenceImpl(@Nonnull Variable variable) {
        this.termType = null;
        this.variable = variable;
    }

    public static TermTypeInference declareTermType(@Nonnull TermType termType) {
        return new TermTypeInferenceImpl(termType);
    }

    public static TermTypeInference declareRedirectionVariable(@Nonnull Variable variable) {
        return new TermTypeInferenceImpl(variable);
    }

    @Override
    public Optional<TermType> getTermType() {
        return Optional.ofNullable(termType);
    }

    @Override
    public Optional<Variable> getRedirectionVariable() {
        return Optional.ofNullable(variable);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof TermTypeInference)
                && getTermType().equals(((TermTypeInference) o).getTermType());
    }

    @Override
    public int hashCode() {
        return getTermType().hashCode();
    }
}
