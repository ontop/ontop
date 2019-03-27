package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class StringDBTermType extends DBTermTypeImpl {

    private final String completeName;
    private final RDFDatatype xsdStringDatatype;

    protected StringDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype xsdStringDatatype) {
        super(name, parentAncestry, false);
        this.xsdStringDatatype = xsdStringDatatype;
        this.completeName = name;
    }

    protected StringDBTermType(String name, String completeName,
                               TermTypeAncestry parentAncestry, RDFDatatype xsdStringDatatype) {
        super(name, parentAncestry, false);
        this.completeName = completeName;
        this.xsdStringDatatype = xsdStringDatatype;
    }

    @Override
    public String getCompleteName() {
        return completeName;
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
