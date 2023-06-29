package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;

public class MockupDBTypeFactory implements DBTypeFactory {

    private final NonStringNonNumberNonBooleanNonDatetimeDBTermType rootDBType;
    private final TypeFactory typeFactory;
    private final TermTypeAncestry rootAncestry;

    @AssistedInject
    private MockupDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        rootDBType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType("AbstractDBType",
                rootTermType.getAncestry(), true);
        this.typeFactory = typeFactory;
        rootAncestry = rootDBType.getAncestry();
    }

    @Override
    public DBTermType getDBStringType() {
        return getDBTermType("STRING");
    }

    @Override
    public DBTermType getDBLargeIntegerType() {
        return new NumberDBTermType("LARGE_INT", rootAncestry, typeFactory.getXsdIntegerDatatype(),
                DBTermType.Category.INTEGER);
    }

    @Override
    public DBTermType getDBDecimalType() {
        return getDBTermType("DEC");
    }

    @Override
    public DBTermType getDBBooleanType() {
        return getDBTermType("BOOL");
    }

    @Override
    public DBTermType getDBDateType() {
        return getDBTermType("DATE");
    }

    @Override
    public DBTermType getDBTimeType() {
        return getDBTermType("TIME");
    }

    @Override
    public DBTermType getDBDateTimestampType() {
        return getDBTermType("TIMESTAMP");
    }

    @Override
    public DBTermType getDBDoubleType() {
        return getDBTermType("DOUBLE");
    }

    @Override
    public DBTermType getDBGeometryType() {
        return getDBTermType("GEOMETRY");
    }

    @Override
    public DBTermType getDBGeographyType() {
        return getDBTermType("GEOGRAPHY");
    }

    @Override
    public boolean supportsDBGeometryType() {
        return false;
    }

    @Override
    public boolean supportsDBGeographyType() {
        return false;
    }

    @Override
    public boolean supportsDBDistanceSphere() {
        return false;
    }

    @Override
    public boolean supportsJson() {
        return false;
    }

    @Override
    public boolean supportsArrayType() {
        return false;
    }

    @Override
    public DBTermType getDBHexBinaryType() {
        return getDBTermType("HEXBINARY");
    }

    @Override
    public DBTermType getDBArrayType() {
        return getDBTermType("ARRAY");
    }

    @Override
    public DBTermType getDBArrayType(DBTermType baseType) {
        return getDBArrayType();
    }

    @Override
    public DBTermType getDBJsonType() {
        throw new UnsupportedOperationException("No JSON datatype supported");
    }

    @Override
    public DBTermType getAbstractRootDBType() {
        return rootDBType;
    }

    @Override
    public DBTermType getDBTermType(String typeName) {
        switch (typeName) {
            case "UUID":
                return new UUIDDBTermType("UUID", rootAncestry, typeFactory.getXsdStringDatatype());
            default:
                return new NonStringNonNumberNonBooleanNonDatetimeDBTermType(typeName, rootAncestry, false);
        }
    }

    @Override
    public DBTermType getDBTermType(String typeName, int columnSize) {
        return new NonStringNonNumberNonBooleanNonDatetimeDBTermType(typeName, rootAncestry, false);
    }

    @Override
    public String getDBTrueLexicalValue() {
        return "TRUE";
    }

    @Override
    public String getDBFalseLexicalValue() {
        return "FALSE";
    }

    @Override
    public String getNullLexicalValue() {
        return "NULL";
    }

    @Override
    public Optional<String> getDBNaNLexicalValue() {
        return Optional.of("NaN");
    }
}
