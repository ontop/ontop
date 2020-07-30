package it.unibz.inf.ontop.iq.type;

import it.unibz.inf.ontop.exception.NonUniqueTermTypeException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;


/**
 * To be used ONLY for extracting unique term types.
 *
 * Term types of variables are normally unique in SQL queries (due to strong typing enforced by RDBMS)
 * but NOT in SPARQL queries (dynamic typing).
 *
 * Accessible through Guice (recommended) or through CoreSingletons.
 *
 */
public interface UniqueTermTypeExtractor {

    /**
     * Is expecting the term type to be unique.
     * If multiple types are detected in an UNION or a CONSTRUCTION node, throws a NonUniqueTermTypeException.
     *
     * For data nodes and joins, multiple term types due to multiple occurrences of a VARIABLE is not a problem
     *  --> the filter condition will fail (as STRICT equality is required). Any type can be therefore returned.
     *
     * DO NOT use it when you don't have such a uniqueness guarantee
     * (-> NonUniqueTermTypeException is interpreted as an internal bug)
     *
     */
    Optional<TermType> extractUniqueTermType(ImmutableTerm term, IQTree subTree) throws NonUniqueTermTypeException;
}
