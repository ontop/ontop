package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;

public class ArrayDBTermType extends DBTermTypeImpl implements GenericDBTermType {

    private final Optional<DBTermType> itemType;
    private final ArrayTypeFromSignature parsingFunction;

    public ArrayDBTermType(String arrayStr, TermTypeAncestry ancestry, ArrayTypeFromSignature parsingFunction) {
        this(arrayStr, ancestry, Optional.empty(), parsingFunction);
    }

    public ArrayDBTermType(String arrayStr, TermTypeAncestry ancestry, Optional<DBTermType> itemType, ArrayTypeFromSignature parsingFunction) {
        super(arrayStr, ancestry, itemType.isEmpty(), Category.ARRAY);
        this.itemType = itemType;
        this.parsingFunction = parsingFunction;
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
    public Optional<GenericDBTermType> createFromSignature(String signature) {
        Optional<DBTermType> type = parsingFunction.getType(signature);

        return type.stream()
                .map(tp -> (GenericDBTermType)new ArrayDBTermType(signature, getAncestry(), Optional.of(tp), parsingFunction))
                .findFirst();
    }

    @Override
    public GenericDBTermType createFromGenericTypes(ImmutableList<DBTermType> types) {
        return new ArrayDBTermType(String.format("ARRAY[%s]", types.get(0).getCastName()), getAncestry(), Optional.of(types.get(0)), parsingFunction);
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

    public interface ArrayTypeFromSignature {
        public Optional<DBTermType> getType(String signature);
    }
}
