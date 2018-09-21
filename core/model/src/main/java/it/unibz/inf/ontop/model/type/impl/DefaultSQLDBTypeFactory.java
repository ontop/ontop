package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.SQLDBTypeFactory;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultSQLDBTypeFactory implements SQLDBTypeFactory {

    protected static final String ABSTRACT_DB_TYPE_STR = "AbstractDBType";
    protected static final String VARCHAR_STR = "VARCHAR";
    protected static final String INTEGER_STR = "INTEGER";
    protected static final String BIGINT_STR = "BIGINT";
    protected static final String DOUBLE_STR = "DOUBLE PRECISION";
    protected static final String DATE_STR = "DATE";
    protected static final String TIME_STR = "TIME";
    protected static final String TIMESTAMP_STR = "TIMESTAMP";

    protected enum DefaultTypeCode {
        STRING,
        INTEGER,
        LONG,
        DATE,
        TIME,
        DATETIMESTAMP,
        DOUBLE
    }

    // MUTABLE
    private final Map<String, DBTermType> sqlTypeMap;
    private final ImmutableMap<DefaultTypeCode, String> defaultTypeCodeMap;

    @AssistedInject
    private DefaultSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        this(createDefaultSQLTypeMap(rootTermType, typeFactory), ImmutableMap.copyOf(createDefaultSQLCodeMap()));
    }

    protected DefaultSQLDBTypeFactory(Map<String, DBTermType> typeMap,
                                      ImmutableMap<DefaultTypeCode, String> defaultTypeCodeMap) {
        sqlTypeMap = typeMap;
        this.defaultTypeCodeMap = defaultTypeCodeMap;
    }

    /**
     * Returns a mutable map so that it can be modified by sub-classes
     */
    protected static Map<String, DBTermType> createDefaultSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        DBTermType rootDBType = new NonStringNonNumberDBTermType(ABSTRACT_DB_TYPE_STR, rootTermType.getAncestry(), true);

        // TODO: complete
        return Stream.of(rootDBType,
                    new StringDBTermType(VARCHAR_STR, rootDBType.getAncestry(), typeFactory.getXsdStringDatatype()),
                    new NumberDBTermType(INTEGER_STR, rootDBType.getAncestry(), typeFactory.getXsdIntegerDatatype()),
                    new NumberDBTermType(BIGINT_STR, rootDBType.getAncestry(), typeFactory.getXsdIntegerDatatype()),
                    new NumberDBTermType(DOUBLE_STR, rootTermType.getAncestry(), typeFactory.getXsdDoubleDatatype()),
                    new NonStringNonNumberDBTermType(DATE_STR, rootDBType.getAncestry(), typeFactory.getDatatype(XSD.DATE)),
                    new NonStringNonNumberDBTermType(TIME_STR, rootTermType.getAncestry(), typeFactory.getDatatype(XSD.TIME)),
                    new NonStringNonNumberDBTermType(TIMESTAMP_STR, rootTermType.getAncestry(), typeFactory.getXsdDatetimeDatatype()))
                .collect(Collectors.toMap(
                        DBTermType::getName,
                        t -> t));
    }

    /**
     * Returns a mutable map so that it can be modified by sub-classes
     */
    protected static Map<DefaultTypeCode, String> createDefaultSQLCodeMap() {
        Map<DefaultTypeCode, String> map = new HashMap<>();
        map.put(DefaultTypeCode.STRING, VARCHAR_STR);
        map.put(DefaultTypeCode.INTEGER, INTEGER_STR);
        map.put(DefaultTypeCode.LONG, BIGINT_STR);
        map.put(DefaultTypeCode.DATE, DATE_STR);
        map.put(DefaultTypeCode.TIME, TIME_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMP_STR);
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_STR);
        return map;
    }

    @Override
    public DBTermType getDBTermType(int typeCode, String typeName) {
        String typeString = preprocessTypeName(typeName);

        /*
         * Creates a new term type if not known
         */
        return sqlTypeMap.computeIfAbsent(typeString,
                s -> new NonStringNonNumberDBTermType(s, sqlTypeMap.get(ABSTRACT_DB_TYPE_STR).getAncestry(), false));
    }

    /**
     * Can be overridden
     */
    protected String preprocessTypeName(String typeName) {
        return typeName.replaceAll("\\([\\d, ]+\\)", "");
    }

    @Override
    public DBTermType getDBStringType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.STRING));
    }

    @Override
    public DBTermType getDBIntegerType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.INTEGER));
    }

    @Override
    public DBTermType getDBLongType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.LONG));
    }

    @Override
    public DBTermType getDBDateType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.DATE));
    }

    @Override
    public DBTermType getDBTimeType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.TIME));
    }

    @Override
    public DBTermType getDBDateTimestampType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.DATETIMESTAMP));
    }

    @Override
    public DBTermType getDBDoubleType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.DOUBLE));
    }
}
