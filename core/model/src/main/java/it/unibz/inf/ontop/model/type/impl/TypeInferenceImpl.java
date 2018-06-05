package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeInference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static it.unibz.inf.ontop.model.type.TypeInference.Status.DETERMINED;
import static it.unibz.inf.ontop.model.type.TypeInference.Status.NON_FATAL_ERROR;
import static it.unibz.inf.ontop.model.type.TypeInference.Status.NOT_DETERMINED;

public class TypeInferenceImpl implements TypeInference {

    @Nullable
    private final TermType termType;
    private final Status status;

    private TypeInferenceImpl(@Nonnull TermType termType) {
        this.termType = termType;
        this.status = DETERMINED;
    }

    private TypeInferenceImpl(Status status) {
        if (status == DETERMINED)
            throw new IllegalArgumentException("Do not use this constructor when the term type is determined");
        this.termType = null;
        this.status = status;
    }

    public static TypeInference declareTermType(TermType termType) {
        return new TypeInferenceImpl(termType);
    }

    public static TypeInference declareNotYetDetermined() {
        return new TypeInferenceImpl(NOT_DETERMINED);
    }

    public static TypeInference declareNonFatalError() {
        return new TypeInferenceImpl(NON_FATAL_ERROR);
    }

    @Override
    public Optional<TermType> getTermType() {
        return Optional.ofNullable(termType);
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
