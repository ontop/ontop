package it.unibz.inf.ontop.model.type;

import java.util.Optional;

/**
 * For all sorts of DBs (SQL, MongoDB, etc.)
 */
public interface DBTermType extends TermType {

    String getName();

    /**
     * Returns true if it is a character or string DB type.
     */
    boolean isString();

    boolean isNumber();

    boolean isBoolean();

    Optional<RDFDatatype> getNaturalRDFDatatype();

    /**
     * Returns true if some values in its value space may need an IRI safe encoding
     */
    boolean isNeedingIRISafeEncoding();
}
