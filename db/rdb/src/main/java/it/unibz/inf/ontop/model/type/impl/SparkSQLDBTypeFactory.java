package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Map;

public class SparkSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    /**
     * SPARK-SQL 3.0.1 datatypes description available at :
     *
     * https://spark.apache.org/docs/latest/sql-ref-datatypes.html
     *
     * http://spark.apache.org/docs/1.6.2/api/java/org/apache/spark/sql/types/package-summary.html
     */

    protected static final String BYTE_STR = "BYTE";
    protected static final String SHORT_STR = "SHORT";
    protected static final String LONG_STR = "LONG";
    protected static final String STRING_STR = "STRING";
    protected static final String DEC_STR = "DEC";
    protected static final String INTERVAL_STR = "INTERVAL"; //TODO
    protected static final String ARRAY_STR = "ARRAY"; //TODO
    protected static final String STRUCT_STR = "STRUCT"; //TODO
    protected static final String MAP_STR = "MAP"; //TODO

    @AssistedInject
    protected SparkSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createSparkSQLTypeMap(rootTermType, typeFactory), createSparkSQLCodeMap());
    }

    private static Map<String, DBTermType> createSparkSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {

        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        DBTermType stringType = new StringDBTermType(STRING_STR, rootAncestry, typeFactory.getXsdStringDatatype());

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(STRING_STR, stringType);
        map.put(BYTE_STR, map.get(TINYINT_STR));
        map.put(SHORT_STR, map.get(SMALLINT_STR));
        map.put(LONG_STR, map.get(BIGINT_STR));
        map.put(DEC_STR, map.get(DECIMAL_STR));

        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createSparkSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.STRING, STRING_STR);

        return ImmutableMap.copyOf(map);
    }

    /**
     * NB: TRUE is an alias of 1
     */
    @Override
    public String getDBTrueLexicalValue() {
        return "1";
    }

    /**
     * NB: FALSE is an alias of 0
     */
    @Override
    public String getDBFalseLexicalValue() {
        return "0";
    }

}
