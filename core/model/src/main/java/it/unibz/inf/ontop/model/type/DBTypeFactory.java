package it.unibz.inf.ontop.model.type;

/**
 * For DB-dependent types
 */
public interface DBTypeFactory {

    DBTermType getDBStringType();

    DBTermType getDBIntegerType();


    /**
     * TODO: find a better name
     *
     * To be called ONLY by the TypeFactory
     */
    interface Factory {
        DBTypeFactory createDBFactory(TermType rootTermType);
    }
}
