package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class StringDBTermType extends DBTermTypeImpl {

    private final String castName;
    private final RDFDatatype xsdStringDatatype;

    protected StringDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype xsdStringDatatype) {
        super(name, parentAncestry, false, true);
        this.xsdStringDatatype = xsdStringDatatype;
        this.castName = name;
    }

    protected StringDBTermType(String name, String castName,
                               TermTypeAncestry parentAncestry, RDFDatatype xsdStringDatatype) {
        super(name, parentAncestry, false, true);
        this.castName = castName;
        this.xsdStringDatatype = xsdStringDatatype;
    }

    @Override
    public String getCastName() {
        return castName;
    }

    @Override
    public Category getCategory() {
        return Category.STRING;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.of(xsdStringDatatype);
    }

    @Override
    public boolean isNeedingIRISafeEncoding() {
        return true;
    }
}
