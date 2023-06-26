package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.*;

public class SparkSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    /**
     * SPARK-SQL 3.0.1 datatypes description : https://spark.apache.org/docs/latest/sql-ref-datatypes.html
     *
     * SQL to XML mappings : https://www.w3.org/2001/sw/rdb2rdf/wiki/Mapping_SQL_datatypes_to_XML_Schema_datatypes
     */

    protected static final String BYTE_STR = "BYTE";
    protected static final String SHORT_STR = "SHORT";
    protected static final String LONG_STR = "LONG";
    protected static final String STRING_STR = "STRING";
    public static final String DECIMAL_38_10_STR = "DECIMAL(38, 10)";
    private static final String ARRAY_STR = "ARRAY<T>";

    @AssistedInject
    protected SparkSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createSparkSQLTypeMap(rootTermType, typeFactory), createSparkSQLCodeMap(),
                createGenericAbstractTypeMap(rootTermType, typeFactory));
    }

    private static Map<String, DBTermType> createSparkSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {

        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        // Redefine the datatypes for numerical values
        RDFDatatype xsdByte = typeFactory.getDatatype(XSD.BYTE);
        RDFDatatype xsdShort = typeFactory.getDatatype(XSD.SHORT);
        RDFDatatype xsdInt = typeFactory.getDatatype(XSD.INT);
        RDFDatatype xsdLong = typeFactory.getDatatype(XSD.LONG);
        RDFDatatype xsdFloat = typeFactory.getDatatype(XSD.FLOAT);
        RDFDatatype xsdDecimal = typeFactory.getDatatype(XSD.DECIMAL);

        DBTermType byteType = new NumberDBTermType(BYTE_STR, rootAncestry, xsdByte, INTEGER);
        DBTermType shortType = new NumberDBTermType(SHORT_STR, rootAncestry, xsdShort, INTEGER);
        DBTermType intType = new NumberDBTermType(INT_STR, rootAncestry, xsdInt, INTEGER);
        DBTermType longType = new NumberDBTermType(LONG_STR, rootAncestry, xsdLong, INTEGER);
        DBTermType decimal3810Type = new NumberDBTermType(DECIMAL_38_10_STR, rootAncestry, xsdDecimal, DECIMAL);
        DBTermType floatType = new NumberDBTermType(FLOAT_STR, rootAncestry, xsdFloat, FLOAT_DOUBLE);
        DBTermType stringType = new StringDBTermType(STRING_STR, rootAncestry, typeFactory.getXsdStringDatatype());

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(STRING_STR, stringType);
        map.put(BYTE_STR,byteType);
        map.put(TINYINT_STR,byteType);
        map.put(SHORT_STR,shortType);
        map.put(SMALLINT_STR,shortType);
        map.put(INT_STR,intType);
        map.put(INTEGER_STR,intType);
        map.put(LONG_STR,longType);
        map.put(BIGINT_STR,longType);
        map.put(FLOAT_STR,floatType);
        map.put(REAL_STR,floatType);
        map.put(DECIMAL_38_10_STR, decimal3810Type);
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createSparkSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.STRING, STRING_STR);
        map.put(DefaultTypeCode.HEXBINARY,BINARY_STR);
        map.put(DefaultTypeCode.DECIMAL, DECIMAL_38_10_STR);
        map.put(DefaultTypeCode.ARRAY, ARRAY_STR);
        return ImmutableMap.copyOf(map);
    }

    private static ImmutableList<GenericDBTermType> createGenericAbstractTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        GenericDBTermType abstractArrayType = new ArrayDBTermType(ARRAY_STR, rootAncestry, s -> {
            if(s.equals("ARRAY"))
                return Optional.of(typeFactory.getDBTypeFactory().getDBStringType());
            if(!s.startsWith("ARRAY<") || !s.endsWith(">")) {
                return Optional.empty();
            }
            String contents = s.substring(6, s.length() - 1);

            int depth = 0;
            for(int i = 0; i < contents.length(); i++) {
                if(contents.charAt(i) == '<')
                    depth += 1;
                else if(contents.charAt(i) == '>')
                    depth -= 1;
                else if(contents.charAt(i) == ',' && depth == 0)
                    return Optional.empty();
            }
            if(depth != 0)
                return Optional.empty();

            return Optional.of(typeFactory.getDBTypeFactory().getDBTermType(contents));
        });

        List<GenericDBTermType> list = new ArrayList<>();
        list.add(abstractArrayType);
        return ImmutableList.copyOf(list);
    }

    @Override
    public String getDBTrueLexicalValue() {
        return "true";
    }

    @Override
    public String getDBFalseLexicalValue() { return "false"; }

}
