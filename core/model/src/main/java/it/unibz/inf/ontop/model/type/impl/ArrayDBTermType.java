package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.GenericDBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import org.checkerframework.checker.nullness.Opt;

import java.util.Optional;

public class ArrayDBTermType extends DBTermTypeImpl implements GenericDBTermType {

    private final Optional<DBTermType> itemType;

    public ArrayDBTermType(String arrayStr, TermTypeAncestry ancestry) {
        this(arrayStr, ancestry, Optional.empty());
    }

    public ArrayDBTermType(String arrayStr, TermTypeAncestry ancestry, Optional<DBTermType> itemType) {
        super(arrayStr, ancestry, itemType.isEmpty(), Category.ARRAY);
        this.itemType = itemType;
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

    @Override
    public GenericDBTermType createOfTypes(ImmutableList<DBTermType> types) {
        if(types.size() != 1) {
            throw new GenericDBTermType.GenericArgumentsExceptions(String.format("The %s<T> type takes exactly one argument", getName()));
        }

        return new ArrayDBTermType(String.format("%s<%s>", getName(), types.get(0).getName()), getAncestry(), Optional.of(types.get(0)));
    }

    @Override
    public ImmutableList<DBTermType> getGenericArguments() {
        return ImmutableList.of(this.itemType.get());
    }


    public static class IllegalArrayComparisonException extends OntopInternalBugException {
        public IllegalArrayComparisonException (String message) {
            super(message);
        }
    }
}
