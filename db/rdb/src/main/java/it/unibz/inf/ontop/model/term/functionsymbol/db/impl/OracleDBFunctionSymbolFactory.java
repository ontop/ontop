package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.NonDeterministicDBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.UUID;
import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.DATE_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.TIMESTAMP_STR;
import static it.unibz.inf.ontop.model.type.impl.OracleDBTypeFactory.TIMESTAMP_LOCAL_TZ_STR;
import static it.unibz.inf.ontop.model.type.impl.OracleDBTypeFactory.TIMESTAMP_TZ_STR;

public class OracleDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    @Inject
    protected OracleDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createOracleRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createOracleRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();
        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        return ImmutableTable.copyOf(table);
    }

    /**
     * TODO: shall we alert the user when TIMESTAMP is used with a XSD.DATETIMESTAMP?
     */
    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        Table<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> table = HashBasedTable.create();
        table.putAll(super.createNormalizationTable());

        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();
        DBTermType timestampLTzType = dbTypeFactory.getDBTermType(TIMESTAMP_LOCAL_TZ_STR);
        DBTypeConversionFunctionSymbol datetimeLTZNormFunctionSymbol = createDateTimeNormFunctionSymbol(timestampLTzType);
        table.put(timestampLTzType, xsdDatetime, datetimeLTZNormFunctionSymbol);
        table.put(timestampLTzType, xsdDatetimeStamp, datetimeLTZNormFunctionSymbol);

        DBTermType timestampType = dbTypeFactory.getDBTermType(TIMESTAMP_STR);
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(timestampType);
        table.put(timestampType, xsdDatetime, datetimeNormFunctionSymbol);
        // No TZ for TIMESTAMP --> incompatible with XSD.DATETIMESTAMP

        DBTermType dbDateType = dbTypeFactory.getDBTermType(DATE_STR);
        DBTypeConversionFunctionSymbol dateNormFunctionSymbol = new OracleDateNormFunctionSymbol(dbDateType, dbStringType);
        table.put(dbDateType, typeFactory.getDatatype(XSD.DATE), dateNormFunctionSymbol);

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createDenormalizationTable() {
        Table<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> table = HashBasedTable.create();
        table.putAll(super.createDenormalizationTable());

        DBTermType dbDateType = dbTypeFactory.getDBTermType(DATE_STR);
        DBTypeConversionFunctionSymbol dateDenormFunctionSymbol = new OracleDateDenormFunctionSymbol(dbStringType, dbDateType);
        table.put(dbDateType, typeFactory.getDatatype(XSD.DATE), dateDenormFunctionSymbol);

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support");
    }

    /**
     * Made explicit, so as to enforce the use of the same character set
     */
    @Override
    protected DBTypeConversionFunctionSymbol createStringToStringCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return new OracleCastToStringFunctionSymbol(inputType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createIntegerToStringCastFunctionSymbol(DBTermType inputType) {
        return new OracleCastIntegerToStringFunctionSymbol(inputType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDefaultCastToStringFunctionSymbol(DBTermType inputType) {
        return new OracleCastToStringFunctionSymbol(inputType, dbStringType);
    }

    /**
     * Overrides
     */
    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType) {
        return new DefaultSQLTimestampISONormFunctionSymbol(
                dbDateTimestampType,
                dbStringType,
                (terms, converter, factory) -> serializeDateTimeNorm(dbDateTimestampType, terms, converter));
    }

    /**
     * NB: In some JDBC connection settings, returns a comma instead of a period.
     */
    protected String serializeDateTimeNorm(DBTermType dbDateTimestampType,
                                           ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter) {
        /*
         * TIMESTAMP by default does not support time zones
         */
        if (dbDateTimestampType.equals(dbTypeFactory.getDBTermType(TIMESTAMP_STR))) {
            return String.format("REPLACE(REPLACE(TO_CHAR(%s,'YYYY-MM-DD HH24:MI:SSxFF'),' ','T'),',','.')", termConverter.apply(terms.get(0)));
        }
        // Timezone support
        else
            return String.format(
                    "REPLACE(REPLACE(REGEXP_REPLACE(TO_CHAR(%s,'YYYY-MM-DD HH24:MI:SSxFF TZH:TZM'),' ','T',1,1),' ',''),',','.')",
                    termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("This method should not be called for Oracle");
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return new OracleTimestampISODenormFunctionSymbol(timestampType, dbStringType);
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        throw new RuntimeException("TODO: support");
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("Do not call getUUIDNameInDialect for Oracle");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        throw new RuntimeException("TODO: support");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        throw new RuntimeException("TODO: support");
    }
}
