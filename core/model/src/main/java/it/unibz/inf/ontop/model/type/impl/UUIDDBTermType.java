package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.UUID;

public class UUIDDBTermType extends DBTermTypeImpl {
    private final RDFDatatype xsdString;

    protected UUIDDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype xsdString) {
        super(name, parentAncestry, false, UUID);
        this.xsdString = xsdString;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.of(xsdString);
    }

    @Override
    public boolean isNeedingIRISafeEncoding() {
        return false;
    }

    /**
     * TODO: revise that
     */
    @Override
    public boolean areEqualitiesStrict() {
        return true;
    }

    /**
     * TODO: revise that
     */
    @Override
    public Optional<Boolean> areEqualitiesStrict(DBTermType otherType) {
        return Optional.of(true);
    }

    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        return true;
    }
}
