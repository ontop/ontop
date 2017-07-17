package it.unibz.inf.ontop.model.predicate;

/**
 * TODO: explain
 *
 *  Most of the time, does not provide any type for its arguments
 */
public interface AtomPredicate extends Predicate {

    @Deprecated
    COL_TYPE getType(int column);

    @Deprecated
    COL_TYPE[] getTypes();

    @Deprecated
    boolean isClass();

    @Deprecated
    boolean isObjectProperty();

    @Deprecated
    boolean isAnnotationProperty();

    @Deprecated
    boolean isDataProperty();

    @Deprecated
    boolean isSameAsProperty();

    @Deprecated
    boolean isCanonicalIRIProperty();

    @Deprecated
    boolean isTriplePredicate();

}
