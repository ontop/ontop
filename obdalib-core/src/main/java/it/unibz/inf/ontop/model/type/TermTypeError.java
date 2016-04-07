package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.exception.OntopRuntimeException;
import it.unibz.inf.ontop.model.Term;

import java.util.Optional;

/**
 * TODO: better integrate into the Ontop exception hierarchy
 *
 * TODO: explain
 */
public class TermTypeError extends OntopRuntimeException {
    public TermTypeError(Term term, TermType expectedTermType, TermType actualTermType) {
        this(term, expectedTermType, actualTermType, Optional.empty());
    }

    public TermTypeError(Term term, TermType expectedTermType, TermType actualTermType, String additionalMessage) {
        this(term, expectedTermType, actualTermType, Optional.of(additionalMessage));
    }

    private TermTypeError(Term term, TermType expectedTermType, TermType actualTermType,
                          Optional<String> optionalMessage) {
        super("Incompatible type infered for " + term + ": expected: " + expectedTermType
                + ", actual: " + actualTermType + optionalMessage.map(m -> ". " + m).orElse(""));
    }
}
