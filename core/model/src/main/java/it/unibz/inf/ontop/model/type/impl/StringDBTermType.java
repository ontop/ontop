package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class StringDBTermType extends DBTermTypeImpl {

    private final String castName;
    private final RDFDatatype xsdStringDatatype;

    protected StringDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype xsdStringDatatype) {
        super(name, parentAncestry, false, Category.STRING);
        this.xsdStringDatatype = xsdStringDatatype;
        this.castName = name;
    }

    protected StringDBTermType(String name, String castName,
                               TermTypeAncestry parentAncestry, RDFDatatype xsdStringDatatype) {
        super(name, parentAncestry, false, Category.STRING);
        this.castName = castName;
        this.xsdStringDatatype = xsdStringDatatype;
    }

    @Override
    public String getCastName() {
        return castName;
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
        switch (otherType.getCategory()) {
            case STRING:
                return Optional.of(true);
            case INTEGER:
            case DECIMAL:
            case FLOAT_DOUBLE:
            case DATETIME:
                return Optional.of(false);
            case OTHER:
            default:
                return Optional.empty();
        }
    }

    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        return true;
    }
}
