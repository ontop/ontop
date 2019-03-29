package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import javax.annotation.Nullable;
import java.util.Optional;

public class NumberDBTermType extends DBTermTypeImpl {

    private final String castName;
    @Nullable
    private final RDFDatatype rdfDatatype;

    protected NumberDBTermType(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
        rdfDatatype = null;
        castName = name;
    }

    protected NumberDBTermType(String name, String castName, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype) {
        super(name, parentAncestry, false);
        this.castName = castName;
        this.rdfDatatype = rdfDatatype;
    }

    protected NumberDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype) {
        super(name, parentAncestry, false);
        this.rdfDatatype = rdfDatatype;
        castName = name;
    }

    @Override
    public Category getCategory() {
        return Category.NUMBER;
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

    @Override
    public String getCastName() {
        return castName;
    }
}
