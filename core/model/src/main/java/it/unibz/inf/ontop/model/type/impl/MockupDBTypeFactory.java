package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;

public class MockupDBTypeFactory implements DBTypeFactory {

    private final NonStringNonNumberNonBooleanNonDatetimeDBTermType rootDBType;
    private final TermTypeAncestry rootAncestry;

    @AssistedInject
    private MockupDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        rootDBType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType("AbstractDBType", rootTermType.getAncestry(), true);
        rootAncestry = rootDBType.getAncestry();
    }

    @Override
    public DBTermType getDBStringType() {
        return getDBTermType("STRING");
    }

    @Override
    public DBTermType getDBLargeIntegerType() {
        return getDBTermType("LARGE_INT");
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
    public DBTermType getDBHexBinaryType() {
        return getDBTermType("HEXBINARY");
    }

    @Override
    public DBTermType getAbstractRootDBType() {
        return rootDBType;
    }

    @Override
    public DBTermType getDBTermType(String typeName) {
        return new NonStringNonNumberNonBooleanNonDatetimeDBTermType(typeName, rootAncestry, false);
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
