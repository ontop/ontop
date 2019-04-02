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
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;


import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.TIMESTAMP_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.TINYINT_STR;
import static it.unibz.inf.ontop.model.type.impl.MySQLDBTypeFactory.BIT_STR;

public class MySQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    protected static final String UUID_STR = "UUID";
    protected static final String  CURRENT_TZ_STR =
            "REPLACE(TIME_FORMAT(TIMEDIFF(NOW(),CONVERT_TZ(NOW(),@@session.time_zone,'+00:00')),'+%H:%i'),'+-','-')";

    private static final String UNSUPPORTED_MSG = "Not supported by MySQL";

    @Inject
    protected MySQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createMySQLRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createMySQLRegularFunctionTable(
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
     * We know that the normalization function DATETIME -> xsd:datetimeStamp will always be invalid
     *   (it is not bound to any timezone).
     * TODO: how to inform the user? In a mapping it would be invalid, but what about a cast in a SPARQL query?
     */
    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        Table<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> table = HashBasedTable.create();
        table.putAll(super.createNormalizationTable());

        // TIMESTAMP is not the default
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();
        DBTermType timestamp = dbTypeFactory.getDBTermType(0, TIMESTAMP_STR);

        DBTypeConversionFunctionSymbol timestampNormFunctionSymbol = createDateTimeNormFunctionSymbol(timestamp);
        table.put(timestamp, xsdDatetime, timestampNormFunctionSymbol);
        table.put(timestamp, xsdDatetimeStamp, timestampNormFunctionSymbol);

        // BIT and TINYINT (numbers) to boolean
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();
        DBTermType bit = dbTypeFactory.getDBTermType(0, BIT_STR);
        DBTermType tinyInt = dbTypeFactory.getDBTermType(0, TINYINT_STR);
        table.put(bit, xsdBoolean, new DefaultNumberNormAsBooleanFunctionSymbol(bit, dbStringType));
        table.put(tinyInt, xsdBoolean, new DefaultNumberNormAsBooleanFunctionSymbol(tinyInt, dbStringType));

        return ImmutableTable.copyOf(table);
    }

    /**
     * TODO: provide a MySQL specific implementation
     */
    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return super.createDateTimeDenormFunctionSymbol(timestampType);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    /**
     * Tricky as this information may be lost while converting SPARQL constants into DB ones
     */
    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: support it");
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType) {
        return new DefaultSQLTimestampISONormFunctionSymbol(
                dbDateTimestampType,
                dbStringType,
                (terms, converter, factory) -> serializeDateTimeNorm(dbDateTimestampType, terms, converter));
    }

    /**
     * For DATETIME, never provides a time zone.
     * For TIMESTAMP, provides the session time zone used for generating the string
     *  (NB: TIMESTAMP is stored as a duration from a fixed date time)
     *
     */
    protected String serializeDateTimeNorm(DBTermType dbDateTimestampType,
                                           ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter) {

        String dateTimeStringWithoutTz = String.format("REPLACE(CAST(%s AS CHAR(30)),' ', 'T')",
                termConverter.apply(terms.get(0)));

        return dbDateTimestampType.getName().equals(TIMESTAMP_STR)
                ? String.format("CONCAT(%s,%s)", dateTimeStringWithoutTz, CURRENT_TZ_STR)
                : dateTimeStringWithoutTz;
    }

    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("This method should not be called for MySQL");
    }

    /**
     * Made Implicit
     */
    protected DBTypeConversionFunctionSymbol createDatetimeToDatetimeCastFunctionSymbol(DBTermType inputType,
                                                                                        DBTermType targetType) {
        return new DefaultImplicitDBCastFunctionSymbol(inputType, targetType);
    }

    @Override
    protected String getUUIDNameInDialect() {
        return UUID_STR;
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        throw new RuntimeException("TODO: support it");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        throw new RuntimeException("TODO: support it");
    }
}
