package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import javax.annotation.Nullable;
import java.util.Optional;

public class NonStringNonNumberDBTermType extends DBTermTypeImpl {

    @Nullable
    private final RDFDatatype rdfDatatype;

    protected NonStringNonNumberDBTermType(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
        rdfDatatype = null;
    }

    protected NonStringNonNumberDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype) {
        super(name, parentAncestry, false);
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.ofNullable(rdfDatatype);
    }
}
