package it.unibz.inf.ontop.exception;


/**
 * A mapping assertion cannot project any null value.
 *
 */
public class NullVariableInMappingException extends MappingException {

    public NullVariableInMappingException(NotFilterableNullVariableException e) {
        super(e);
    }
}
