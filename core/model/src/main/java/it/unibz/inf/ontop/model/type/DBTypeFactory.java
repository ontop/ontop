package it.unibz.inf.ontop.model.type;

/**
 * For DB-dependent types
 */
public interface DBTypeFactory {

    DBTermType getDBStringType();

    DBTermType getDBIntegerType();

    DBTermType getDBLongType();

    DBTermType getDBDateType();

    DBTermType getDBTimeType();

    DBTermType getDBDateTimestampType();

    DBTermType getDBDoubleType();

    /**
     * BC: TODO: should we keep the typeCode? Still needed?
     */
    DBTermType getDBTermType(int typeCode, String typeName);


    /**
     * TODO: find a better name
     *
     * To be called ONLY by the TypeFactory
     */
    interface Factory {
        DBTypeFactory createDBFactory(TermType rootTermType);
    }
}
