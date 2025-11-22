package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

/**
 * Concrete RDF term type representing RDF-star embedded triple terms.
 */
public class TripleRDFTermType extends RDFTermTypeImpl {

    protected TripleRDFTermType(TermTypeAncestry parentAncestry) {
        super("RDF_STAR_TRIPLE", parentAncestry, DBTypeFactory::getDBStringType);
    }
}
