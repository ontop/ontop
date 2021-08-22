package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import javax.annotation.Nullable;
import java.util.Optional;

public class NonStringNonNumberNonBooleanNonDatetimeDBTermType extends DBTermTypeImpl {

    @Nullable
    private final RDFDatatype rdfDatatype;
    private final StrictEqSupport support;
    private final boolean isNeedingIRISafeEncoding;


    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(String name, TermTypeAncestry parentAncestry,
                                                                boolean isAbstract) {
        super(name, parentAncestry, isAbstract, Category.OTHER);
        rdfDatatype = null;
        this.support = StrictEqSupport.SAME_TYPE_NO_CONSTANT;
        this.isNeedingIRISafeEncoding = true;
    }

    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(String name, TermTypeAncestry parentAncestry,
                                                                RDFDatatype rdfDatatype) {
        this(name, parentAncestry, rdfDatatype, true);
    }

    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(String name, TermTypeAncestry parentAncestry,
                                                                RDFDatatype rdfDatatype, boolean isNeedingIRISafeEncoding) {
        super(name, parentAncestry, false, Category.OTHER);
        this.rdfDatatype = rdfDatatype;
        this.support = StrictEqSupport.SAME_TYPE_NO_CONSTANT;
        this.isNeedingIRISafeEncoding = isNeedingIRISafeEncoding;
    }

    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(
            String name, TermTypeAncestry parentAncestry,
            StrictEqSupport support) {
        this(name, parentAncestry, support, true);
    }

    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(
            String name, TermTypeAncestry parentAncestry,
            StrictEqSupport support, boolean isNeedingIRISafeEncoding) {
        super(name, parentAncestry, false, Category.OTHER);
        this.support = support;
        this.rdfDatatype = null;
        this.isNeedingIRISafeEncoding = isNeedingIRISafeEncoding;
    }

    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(
            String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype,
            StrictEqSupport support) {
        this(name, parentAncestry, rdfDatatype, support, true);
    }

    protected NonStringNonNumberNonBooleanNonDatetimeDBTermType(
            String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype,
            StrictEqSupport support, boolean isNeedingIRISafeEncoding) {
        super(name, parentAncestry, false, Category.OTHER);
        this.support = support;
        this.rdfDatatype = rdfDatatype;
        this.isNeedingIRISafeEncoding = isNeedingIRISafeEncoding;
    }


        @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.ofNullable(rdfDatatype);
    }

    @Override
    public boolean isNeedingIRISafeEncoding() {
        return isNeedingIRISafeEncoding;
    }

    @Override
    public boolean areEqualitiesStrict() {
        switch (support) {
            case WITH_ALL:
            case SAME_TYPE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Optional<Boolean> areEqualitiesStrict(DBTermType otherType) {
        switch (support) {
            case WITH_ALL:
                return Optional.of(true);
            case SAME_TYPE:
                return Optional.of(equals(otherType));
            case SAME_TYPE_NO_CONSTANT:
            case NOTHING:
            default:
                return Optional.of(false);
        }
    }

    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        switch (support) {
            case WITH_ALL:
            case SAME_TYPE:
            case SAME_TYPE_NO_CONSTANT:
                return true;
            case NOTHING:
            default:
                return false;
        }
    }

    protected enum StrictEqSupport {
        WITH_ALL,
        SAME_TYPE,
        SAME_TYPE_NO_CONSTANT,
        NOTHING
    }
}
