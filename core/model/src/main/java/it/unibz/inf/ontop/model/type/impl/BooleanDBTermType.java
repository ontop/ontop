package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class BooleanDBTermType extends DBTermTypeImpl implements DBTermType {

    private final RDFDatatype xsdBooleanDatatype;

    public BooleanDBTermType(String booleanStr, TermTypeAncestry ancestry, RDFDatatype xsdBooleanDatatype) {
        super(booleanStr, ancestry, false, Category.BOOLEAN);
        this.xsdBooleanDatatype = xsdBooleanDatatype;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.of(xsdBooleanDatatype);
    }

    @Override
    public boolean isNeedingIRISafeEncoding() {
        return false;
    }

    /**
     * Constants often can have different values (true, false, 0, 1).
     * TODO: double-check
     */
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
        return true;
    }
}
