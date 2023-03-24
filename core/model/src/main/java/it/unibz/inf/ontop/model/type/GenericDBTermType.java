package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;

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
    public ImmutableList<DBTermType> getGenericArguments();

    /**
     * Creates a new concrete type that uses the passed generic types.
     */
    public GenericDBTermType createOfTypes(ImmutableList<DBTermType> types);

    public static class GenericArgumentsExceptions extends OntopInternalBugException {
        public GenericArgumentsExceptions (String message) {
            super(message);
        }
    }
}
