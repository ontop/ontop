package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class ArrayDBTermType extends DBTermTypeImpl implements DBTermType {

    public ArrayDBTermType(String arrayStr, TermTypeAncestry ancestry, RDFDatatype xsdBooleanDatatype) {
        super(arrayStr, ancestry, false, DBTermType.Category.OTHER);
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.empty();
    }

    @Override
    public boolean isNeedingIRISafeEncoding() {
        return false;
    }

    @Override
    public boolean areEqualitiesStrict() {
        throw new IllegalArrayComparisonException("A query should not check equality between two arrays");
    }

    @Override
    public Optional<Boolean> areEqualitiesStrict(DBTermType otherType) {
        throw new IllegalArrayComparisonException("A query should not check equality between two arrays");
    }

    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        throw new IllegalArrayComparisonException("A query should not check equality between two arrays");
    }


    public class IllegalArrayComparisonException extends OntopInternalBugException {
        public IllegalArrayComparisonException (String message) {
            super(message);
        }
    }
}
