package it.unibz.inf.ontop.model.type;

/**
 * For DB-dependent types
 */
public interface DBTypeFactory {

    DBTermType getDBStringType();

    DBTermType getDBLargeIntegerType();

    DBTermType getDBDecimalType();

    DBTermType getDBBooleanType();

    DBTermType getDBDateType();

    DBTermType getDBTimeType();

    DBTermType getDBDateTimestampType();

    DBTermType getDBDoubleType();

    DBTermType getDBHexBinaryType();

    /**
     * Returns an abstract type
     */
    DBTermType getAbstractRootDBType();

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
        DBTypeFactory createDBFactory(TermType rootTermType, TypeFactory typeFactory);
    }
}
