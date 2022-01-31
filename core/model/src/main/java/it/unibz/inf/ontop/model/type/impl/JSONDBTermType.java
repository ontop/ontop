package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class JSONDBTermType extends DBTermTypeImpl implements DBTermType {


    private final RDFDatatype xsdStringDatatype;

    public JSONDBTermType(String jsonStr, TermTypeAncestry ancestry, RDFDatatype xsdStringDatatype) {
        super(jsonStr, ancestry, false, Category.STRING);
        this.xsdStringDatatype = xsdStringDatatype;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.of(xsdStringDatatype);
    }

    @Override
    public boolean isNeedingIRISafeEncoding() {
        return true;
    }

    @Override
    public boolean areEqualitiesStrict() {
        return true;
    }

    @Override
    public Optional<Boolean> areEqualitiesStrict(DBTermType otherType) {
        return Optional.of(otherType.equals(this));
    }

    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        return true;
    }
}
