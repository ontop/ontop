package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;

public class MockupDBTypeFactory implements DBTypeFactory {

    private final NonStringNonNumberNonBooleanDBTermType rootDBType;
    private final TermTypeAncestry rootAncestry;

    @AssistedInject
    private MockupDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        rootDBType = new NonStringNonNumberNonBooleanDBTermType("AbstractDBType", rootTermType.getAncestry(), true);
        rootAncestry = rootDBType.getAncestry();
    }

    @Override
    public DBTermType getDBStringType() {
        return getDBTermType(-1, "STRING");
    }

    @Override
    public DBTermType getDBLargeIntegerType() {
        return getDBTermType(-1, "LARGE_INT");
    }

    @Override
    public DBTermType getDBDecimalType() {
        return getDBTermType(-1, "DEC");
    }

    @Override
    public DBTermType getDBBooleanType() {
        return getDBTermType(-1, "BOOL");
    }

    @Override
    public DBTermType getDBDateType() {
        return getDBTermType(-1, "DATE");
    }

    @Override
    public DBTermType getDBTimeType() {
        return getDBTermType(-1, "TIME");
    }

    @Override
    public DBTermType getDBDateTimestampType() {
        return getDBTermType(-1, "TIMESTAMP");
    }

    @Override
    public DBTermType getDBDoubleType() {
        return getDBTermType(-1, "DOUBLE");
    }

    @Override
    public DBTermType getDBHexBinaryType() {
        return getDBTermType(-1, "HEXBINARY");
    }

    @Override
    public DBTermType getAbstractRootDBType() {
        return rootDBType;
    }

    @Override
    public DBTermType getDBTermType(int typeCode, String typeName) {
        return new NonStringNonNumberNonBooleanDBTermType(typeName, rootAncestry, false);
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
