package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.SQLDBTypeFactory;
import it.unibz.inf.ontop.model.type.TermType;

public class DefaultSQLDBTypeFactory implements SQLDBTypeFactory {

    private final TermType rootTermType;
    private final DBTermType rootDBType;
    private final DBTermType stringType, integerType, bigIntType, dateType, doubleType;
    private final DBTermType timeType, timestampType;

    @AssistedInject
    private DefaultSQLDBTypeFactory(@Assisted TermType rootTermType) {
        this.rootTermType = rootTermType;
        this.rootDBType = new DBTermTypeImpl("AbstractDBType", rootTermType.getAncestry(), true);
        this.stringType = new VarCharTermType(rootDBType.getAncestry());
        this.integerType = new IntegerDBTermType(rootDBType.getAncestry());
        this.bigIntType = new DBTermTypeImpl("BIGINT", rootDBType.getAncestry(), false);
        this.doubleType = new DBTermTypeImpl("DOUBLE", rootTermType.getAncestry(), false);
        this.dateType = new DBTermTypeImpl("DATE", rootDBType.getAncestry(), false);
        this.timeType = new DBTermTypeImpl("TIME", rootTermType.getAncestry(), false);
        this.timestampType = new DBTermTypeImpl("TIMESTAMP", rootTermType.getAncestry(), false);
    }

    @Override
    public DBTermType getDBStringType() {
        return stringType;
    }

    @Override
    public DBTermType getDBIntegerType() {
        return integerType;
    }

    @Override
    public DBTermType getDBLongType() {
        return bigIntType;
    }

    @Override
    public DBTermType getDBDateType() {
        return dateType;
    }

    @Override
    public DBTermType getDBTimeType() {
        return timeType;
    }

    @Override
    public DBTermType getDBDateTimestampType() {
        return timestampType;
    }

    @Override
    public DBTermType getDBDoubleType() {
        return doubleType;
    }
}
