package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class IntervalDBTermType extends DBTermTypeImpl{
    private final RDFDatatype rdfDatatype;

    protected IntervalDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype) {
        super(name, parentAncestry, false, DBTermType.Category.INTERVAL);
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.of(rdfDatatype);
    }

    @Override
    public boolean isNeedingIRISafeEncoding() {
        return true;
    }

    @Override
    public boolean areEqualitiesStrict() {
        return false;
    }

    @Override
    public Optional<Boolean> areEqualitiesStrict(DBTermType otherType) {
        return Optional.of(false);
    }

    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        return false;
    }
}
