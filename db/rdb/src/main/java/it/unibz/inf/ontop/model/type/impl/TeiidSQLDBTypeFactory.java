package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;
import static it.unibz.inf.ontop.model.type.DBTermType.Category.INTEGER;

public class TeiidSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String STRING_STR = "STRING";

    protected static final String BIGINTEGER_STR = "BIGINTEGER";
    protected static final String BIGDECIMAL_STR = "BIGDECIMAL";
    protected static final String BYTE_STR = "BYTE";
    protected static final String SHORT_STR = "SHORT";
    protected static final String SERIAL_STR = "SERIAL";
    protected static final String LONG_STR = "LONG";
    protected static final String GEOMETRY_STR = "GEOMETRY";
    protected static final String GEOGRAPHY_STR = "GEOGRAPHY";


    @AssistedInject
    protected TeiidSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createTeiidSQLTypeMap(rootTermType, typeFactory), createTeiidSQLCodeMap());
    }

    protected static Map<String, DBTermType> createTeiidSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);

        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFDatatype hexBinary = typeFactory.getDatatype(XSD.HEXBINARY);
        RDFDatatype xsdDecimal = typeFactory.getXsdDecimalDatatype();

        StringDBTermType stringType = new StringDBTermType(STRING_STR, rootAncestry, xsdString);
        map.put(STRING_STR, stringType);

        NumberDBTermType bigintegerType = new NumberDBTermType(BIGINTEGER_STR, rootAncestry, xsdInteger, INTEGER);
        map.put(BIGINTEGER_STR, bigintegerType);

        NonStringNonNumberNonBooleanNonDatetimeDBTermType geometryType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOMETRY_STR, rootAncestry, hexBinary);
        map.put(GEOMETRY_STR, geometryType);

        NonStringNonNumberNonBooleanNonDatetimeDBTermType geographyType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOGRAPHY_STR, rootAncestry, hexBinary);
        map.put(GEOGRAPHY_STR, geographyType);

        NumberDBTermType bigdecimalType = new NumberDBTermType(BIGDECIMAL_STR, rootAncestry, xsdDecimal, DECIMAL);
        map.put(BIGDECIMAL_STR, bigdecimalType);

        NumberDBTermType byteType = new NumberDBTermType(BYTE_STR, rootAncestry, xsdInteger,INTEGER);
        map.put(BYTE_STR, byteType);

        NumberDBTermType shortType = new NumberDBTermType(SHORT_STR, rootAncestry, xsdInteger,INTEGER);
        map.put(SHORT_STR, shortType);

        NumberDBTermType serialType = new NumberDBTermType(SERIAL_STR, rootAncestry, xsdInteger,INTEGER);
        map.put(SERIAL_STR, serialType);

        NumberDBTermType longType = new NumberDBTermType(LONG_STR, rootAncestry, xsdInteger,INTEGER);
        map.put(LONG_STR, longType);

        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createTeiidSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();

        map.put(DefaultTypeCode.STRING, STRING_STR);
        map.put(DefaultTypeCode.DECIMAL, BIGDECIMAL_STR);

        return ImmutableMap.copyOf(map);
    }
}
