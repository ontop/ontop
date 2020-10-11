package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class DatetimeDBTermType extends DBTermTypeImpl {
    private final RDFDatatype rdfDatatype;

    protected DatetimeDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype) {
        super(name, parentAncestry, false, Category.DATETIME);
        this.rdfDatatype = rdfDatatype;
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

    /**
     * TODO: check if TZ are allowed are not
     */
    @Override
    public boolean areEqualitiesStrict() {
        return false;
    }

    @Override
    public Optional<Boolean> areEqualitiesStrict(DBTermType otherType) {
        return Optional.of(false);
    }

    /**
     * TODO: check if TZ are allowed are not
     */
    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        return false;
    }
}
