package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class DatetimeDBTermType extends DBTermTypeImpl {
    private final RDFDatatype rdfDatatype;

    protected DatetimeDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype,
                                 boolean areLexicalTermsUnique) {
        super(name, parentAncestry, false, areLexicalTermsUnique);
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public Category getCategory() {
        return Category.DATETIME;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.of(rdfDatatype);
    }

    /**
     * TODO: check if it is safe or not
     */
    @Override
    public boolean isNeedingIRISafeEncoding() {
        return true;
    }

    @Override
    public boolean areEqualitiesStrict() {
        return false;
    }
}
