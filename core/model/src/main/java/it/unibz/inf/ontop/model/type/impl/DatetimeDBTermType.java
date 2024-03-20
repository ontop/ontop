package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import javax.annotation.Nullable;
import java.util.Optional;

public class DatetimeDBTermType extends DBTermTypeImpl {
    private final RDFDatatype rdfDatatype;

    @Nullable
    private final Boolean hasTimeZone;

    protected DatetimeDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype) {
        super(name, parentAncestry, false, Category.DATETIME);
        this.rdfDatatype = rdfDatatype;
        this.hasTimeZone = null;
    }

    protected DatetimeDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype, boolean hasTimeZone) {
        super(name, parentAncestry, false, Category.DATETIME);
        this.rdfDatatype = rdfDatatype;
        this.hasTimeZone = hasTimeZone;
    }

    public Optional<Boolean> hasTimeZone() {
        return Optional.ofNullable(hasTimeZone);
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
