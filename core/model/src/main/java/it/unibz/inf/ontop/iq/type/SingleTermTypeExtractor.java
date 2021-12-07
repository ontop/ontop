package it.unibz.inf.ontop.iq.type;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;


/**
 * To be used for extracting single term types.
 *
 * Term types of variables are normally unique or "equivalent" in SQL queries (due to some form of strong typing enforced by RDBMS)
 * but NOT in SPARQL queries (dynamic typing).
 *
 * Accessible through Guice (recommended) or through CoreSingletons.
 *
 */
public interface SingleTermTypeExtractor {

    /**
     * If multiple types are detected in a UNION or a CONSTRUCTION node, selects one of them. They are expected to be "equivalent"
     *  (e.g. VARCHAR and TEXT).
     *
     * For data nodes and joins, multiple term types due to multiple occurrences of a VARIABLE is not a problem
     *  {@code ---> } the filter condition will fail (as STRICT equality is required). Any type can be therefore returned.
     *
     */
    Optional<TermType> extractSingleTermType(ImmutableTerm term, IQTree subTree);
}
