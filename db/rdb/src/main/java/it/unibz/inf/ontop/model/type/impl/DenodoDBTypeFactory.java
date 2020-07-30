package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.FLOAT_DOUBLE;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.NOTHING;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.WITH_ALL;

public class DenodoDBTypeFactory extends DefaultSQLDBTypeFactory {

    public static final String TIMESTAMPTZ_STR = "TIMESTAMP_WITH_TIMEZONE";

    @AssistedInject
    protected DenodoDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createDenodoTypeMap(rootTermType, typeFactory), createDenodoCodeMap());
    }

    protected static Map<String, DBTermType> createDenodoTypeMap(TermType rootTermType, TypeFactory typeFactory) {

        // TODO: shall we map it to xsd.datetimeStamp ? (would not follow strictly R2RML but be more precise)
        DatetimeDBTermType timestampTz = new DatetimeDBTermType(TIMESTAMPTZ_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        // TODO: check if lexical values can be considered as unique

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(TIMESTAMPTZ_STR, timestampTz);
        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createDenodoCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_PREC_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMPTZ_STR);
        return ImmutableMap.copyOf(map);
    }
}
