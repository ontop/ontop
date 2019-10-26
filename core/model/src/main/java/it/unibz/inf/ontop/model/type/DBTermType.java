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

    /**
     * Returns true if the mapping VALUE -> LEXICAL TERM is unique.
     *
     * Not the case for instance for floating numbers, timestamp with timezone, etc.
     *
     * Useful for decomposing constant IRIs
     *
     */
    boolean areLexicalTermsUnique();

    /**
     * Returns true if the non-strict equality between two terms of this type
     * is equivalent to a strict equality
     */
    boolean areEqualitiesStrict();

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
