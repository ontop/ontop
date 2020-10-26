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
        super(createHiveSQLTypeMap(rootTermType, typeFactory), createHiveSQLCodeMap());
    }

    private static Map<String, DBTermType> createHiveSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(BYTE_STR, map.get(TINYINT_STR));
        map.put(SHORT_STR, map.get(SMALLINT_STR));
        map.put(LONG_STR, map.get(BIGINT_STR));
        map.put(STRING_STR, map.get(TEXT_STR));
        map.put(DEC_STR, map.get(DECIMAL_STR));

        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createHiveSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        return ImmutableMap.copyOf(map);
    }

}
