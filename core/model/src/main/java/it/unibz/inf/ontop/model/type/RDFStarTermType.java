package it.unibz.inf.ontop.model.type;

import java.util.Optional;

/**
 * Overarching TermType representing RDF-star triples. This includes classical RDF-triples (see RDFTermType),
 * as well as triples containing other embedded triples in the subject and/or object position.
 */
public interface RDFStarTermType extends TermType {

    /**
     * If the RDF term type has a canonical natural DB type, returns it.
     * Otherwise, returns a DB string, if the type is not abstract.
     *
     * Throws an UnsupportedOperationException if is abstract
     */
    DBTermType getClosestDBType(DBTypeFactory dbTypeFactory) throws UnsupportedOperationException;
}