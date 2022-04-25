package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.SnowflakeDBTypeFactory.*;

public class SnowflakeDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UUID_STRING_STR = "UUID_STRING";
    private static final String RANDOM_STR = "RANDOM";

    @Inject
    protected SnowflakeDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createSnowflakeRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createSnowflakeRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));


        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        builder.putAll(super.createNormalizationTable());


        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();

        // NB: TIMESTAMP_TZ_STR is the default, already done.
        for (String timestampTypeString : ImmutableList.of(TIMESTAMP_LOCAL_TZ_STR, TIMESTAMP_NO_TZ_STR)) {
            DBTermType timestampType = dbTypeFactory.getDBTermType(timestampTypeString);

            DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(timestampType);
            builder.put(timestampType, xsdDatetime, datetimeNormFunctionSymbol);
            builder.put(timestampType, xsdDatetimeStamp, datetimeNormFunctionSymbol);
        }

        return builder.build();
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CONTAINS(%s,%s)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("NVL(SUBSTR(%s,0,POSITION(%s IN %s)-1),'')", str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        return String.format("SUBSTRING(%s,POSITION(%s IN %s) + LENGTH(%s), CAST( SIGN(POSITION(%s IN %s)) * LENGTH(%s) AS INTEGER))",
                str, after, str , after , after, str, str);
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("MD5(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA1(%s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA2(%s, 256)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA2(%s, 512)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        return String.format("(LPAD(EXTRACT(TIMEZONE_HOUR FROM %s)::text,2,'0') || ':' || LPAD(EXTRACT(TIMEZONE_MINUTE FROM %s)::text,2,'0'))", str, str);
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return createDBConcatOperator(arity);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType,
                Serializers.getOperatorSerializer(CONCAT_OP_STR));
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return createNullRejectingDBConcat(arity);
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TO_CHAR(%s, 'YYYY-MM-DDTHH24:MI:SS.FF3TZHTZM')", termConverter.apply(terms.get(0)));
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(LENGTH_STR, 1);
    }

    @Override
    protected String getRandNameInDialect() {
        return RANDOM_STR;
    }

    @Override
    protected String getUUIDNameInDialect() {
        return UUID_STRING_STR;
    }

    @Override
    protected String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        return "ROW_NUMBER() OVER (ORDER BY 1)";
    }
}
