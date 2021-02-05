package it.unibz.inf.ontop.exception;

/**
 * When multiple term types are found while the term type was expected to be unique.
 *
 * Should not happen - internal bug
 */
public class NonUniqueTermTypeException extends OntopInternalBugException {

    public NonUniqueTermTypeException(String message) {
        super(message);
    }
}
