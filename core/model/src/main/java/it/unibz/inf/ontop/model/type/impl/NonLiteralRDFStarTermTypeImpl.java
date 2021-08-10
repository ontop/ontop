package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.*;

public class NonLiteralRDFStarTermTypeImpl extends RDFStarTermTypeImpl implements NonLiteralRDFStarTermType {

    /**
     * For the root of all the non-literal RDF-star term types ONLY
     */
    private NonLiteralRDFStarTermTypeImpl(TermTypeAncestry parentAncestry) {
        super("NonLiteralRDFStarTerm", parentAncestry);
    }

    static NonLiteralRDFStarTermType createNonLiteralRDFStarTermRoot(TermTypeAncestry parentAncestry) {
        return new NonLiteralRDFStarTermTypeImpl(parentAncestry);
    }

}
