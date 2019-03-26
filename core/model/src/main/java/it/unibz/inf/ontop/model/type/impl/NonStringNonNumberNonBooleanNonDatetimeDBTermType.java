package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import javax.annotation.Nullable;
import java.util.Optional;

public class NonStringNonNumberNonBooleanNonDatetimeDBTermType extends DBTermTypeImpl {

    @Nullable
    private final RDFDatatype rdfDatatype;

    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
        rdfDatatype = null;
    }

    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype) {
        super(name, parentAncestry, false);
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.ofNullable(rdfDatatype);
    }

    /**
     * By default, we don't know if it is safe or not
     */
    @Override
    public boolean isNeedingIRISafeEncoding() {
        return true;
    }
}
