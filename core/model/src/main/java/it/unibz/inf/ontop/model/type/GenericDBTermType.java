package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableList;

import java.util.Optional;

/**
 * For Generic types such as ARRAY<T>
 */
public interface GenericDBTermType extends DBTermType {

    /**
     * Gets the types used by this generic type.
     * For instance, for ARRAY<STRING> this returns a DBStringType,
     * for MAP<STRING, INT> it returns a DBStringType and a DBIntType.
     */
    ImmutableList<DBTermType> getGenericArguments();

    /**
     * Creates a new concrete type that uses the correct generic types deduced from the type string.
     */
    Optional<GenericDBTermType> createFromSignature(String signature);//int[] ARRAY<int>

    GenericDBTermType createFromGenericTypes(ImmutableList<DBTermType> types);
}
