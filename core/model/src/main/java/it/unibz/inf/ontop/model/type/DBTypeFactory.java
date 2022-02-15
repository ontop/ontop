package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.DBConstant;

import java.util.Optional;

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

    DBTermType getDBGeometryType();

    DBTermType getDBGeographyType();

    boolean supportsDBGeometryType();

    boolean supportsDBGeographyType();

    boolean supportsDBDistanceSphere();

    DBTermType getDBHexBinaryType();

    DBTermType getDBArrayType();

    DBTermType getDBJsonType();

    /**
     * Returns an abstract type
     */
    DBTermType getAbstractRootDBType();

    DBTermType getDBTermType(String typeName);
    DBTermType getDBTermType(String typeName, int columnSize);

    String getDBTrueLexicalValue();
    String getDBFalseLexicalValue();
    String getNullLexicalValue();

    /**
     * Is empty if the DB does not support (and therefore does not store) not-a-number values
     */
    Optional<String> getDBNaNLexicalValue();

    /**
     * TODO: find a better name
     *
     * To be called ONLY by the TypeFactory
     */
    interface Factory {
        DBTypeFactory createDBFactory(TermType rootTermType, TypeFactory typeFactory);
    }
}
