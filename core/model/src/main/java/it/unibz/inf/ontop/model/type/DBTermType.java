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
     * Returns true if the non-strict equality between two terms of this type
     * is equivalent to a strict equality
     */
    boolean areEqualitiesStrict();

    /**
     * Returns true if the non-strict equality between terms of these two types
     * are equivalent to a strict equality
     */
    Optional<Boolean> areEqualitiesStrict(DBTermType otherType);

    /**
     * Returns true if the non-strict equality between two attributes of the same database
     * of this type is equivalent to a strict equality.
     *
     * Useful for floating numbers.
     *
     * Note that we are NOT considering here equalities with constants coming from source part
     * of the mapping assertion.
     *
     */
    boolean areEqualitiesBetweenTwoDBAttributesStrict();

    Optional<Boolean> isValidLexicalValue(String lexicalValue);


    enum Category {
        STRING,
        INTEGER,
        DECIMAL,
        FLOAT_DOUBLE,
        BOOLEAN,
        DATE,
        DATETIME,
        UUID,
        OTHER
    }
}
