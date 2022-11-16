package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class JsonDBTermTypeImpl extends DBTermTypeImpl {

    protected JsonDBTermTypeImpl(String name, TermTypeAncestry parentAncestry) {
        super(name, parentAncestry, false, Category.JSON);
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.empty();
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
        return Optional.empty();
    }

    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        return false;
    }
}
