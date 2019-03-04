package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class StringDBTermType extends DBTermTypeImpl {

    private final RDFDatatype xsdStringDatatype;

    protected StringDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype xsdStringDatatype) {
        super(name, parentAncestry, false);
        this.xsdStringDatatype = xsdStringDatatype;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
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
