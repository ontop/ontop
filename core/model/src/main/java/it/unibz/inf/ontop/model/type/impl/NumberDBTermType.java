package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import javax.annotation.Nullable;
import java.util.Optional;

public class NumberDBTermType extends DBTermTypeImpl {

    @Nullable
    private final RDFDatatype rdfDatatype;

    protected NumberDBTermType(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
        rdfDatatype = null;
    }

    protected NumberDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype) {
        super(name, parentAncestry, false);
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.ofNullable(rdfDatatype);
    }

    /**
     * False by default as the vast majority of number formats are not sensible to IRI safe encoding.
     * In theory rationals would pose problems if they use the "/" character, but I have so far never seen them in a DB.
     */
    @Override
    public boolean isNeedingIRISafeEncoding() {
        return false;
    }
}
