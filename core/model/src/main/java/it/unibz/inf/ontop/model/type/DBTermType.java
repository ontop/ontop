package it.unibz.inf.ontop.model.type;

import java.util.Optional;

/**
 * For all sorts of DBs (SQL, MongoDB, etc.)
 */
public interface DBTermType extends TermType {

    String getName();

    String getCastName();

    Category getCategory();

    Optional<RDFDatatype> getNaturalRDFDatatype();

    /**
     * Returns true if some values in its value space may need an IRI safe encoding
     */
    boolean isNeedingIRISafeEncoding();

    enum Category {
        STRING,
        INTEGER,
        DECIMAL,
        FLOAT_DOUBLE,
        BOOLEAN,
        DATETIME,
        OTHER
    }
}
