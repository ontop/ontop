package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TermTypeInferenceImpl implements TermTypeInference {

    @Nullable
    private final TermType termType;

    /**
     * Determined
     */
    private TermTypeInferenceImpl(@Nonnull TermType termType) {
        this.termType = termType;
    }

    /**
     * Non fatal error
     */
    private TermTypeInferenceImpl() {
        this.termType = null;
    }

    public static TermTypeInference declareTermType(@Nonnull TermType termType) {
        return new TermTypeInferenceImpl(termType);
    }

    public static TermTypeInference declareNonFatalError() {
        return new TermTypeInferenceImpl();
    }

    @Override
    public Optional<TermType> getTermType() {
        return Optional.ofNullable(termType);
    }

    @Override
    public boolean isNonFatalError() {
        return termType == null;
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
