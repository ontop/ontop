package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.model.type.RDFTermType;

/**
 * When the RDF term types are "reified" as constants
 *
 * Should disappear from the query before its translation
 * into a native query.
 *
 */
public interface RDFTermTypeConstant extends Constant {

    @Override
    RDFTermType getType();
}
