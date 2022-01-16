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
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.UUID;
import java.util.function.Function;


import static it.unibz.inf.ontop.model.term.functionsymbol.db.impl.MySQLDBFunctionSymbolFactory.UUID_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.*;
import static it.unibz.inf.ontop.model.type.impl.PostgreSQLDBTypeFactory.*;

public class PostgreSQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String RANDOM_STR = "RANDOM";
    private static final String POSITION_STR = "POSITION";

    @Inject
    protected PostgreSQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createPostgreSQLRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createPostgreSQLRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBFunctionSymbol positionFunctionSymbol = new SimpleTypedDBFunctionSymbolImpl(POSITION_STR, 2,
                dbTypeFactory.getDBLargeIntegerType(), false, abstractRootDBType,
                (terms, termConverter, termFactory) -> String.format("position(%s in %s)",
                        termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1))));
        table.put(POSITION_STR, 2, positionFunctionSymbol);

        DBFunctionSymbol nowFunctionSymbol = new WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(
                CURRENT_TIMESTAMP_STR,
                dbTypeFactory.getDBDateTimestampType(), abstractRootDBType);
        table.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);
        table.remove(REGEXP_LIKE_STR, 2);
        table.remove(REGEXP_LIKE_STR, 3);

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        builder.putAll(super.createNormalizationTable());

        //TIMESTAMP
        DBTermType timeStamp = dbTypeFactory.getDBTermType(TIMESTAMP_STR);
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(timeStamp);
        builder.put(timeStamp, xsdDatetime, datetimeNormFunctionSymbol);
        builder.put(timeStamp, xsdDatetimeStamp, datetimeNormFunctionSymbol);

        //TIMETZ
        DBTermType timeTZType = dbTypeFactory.getDBTermType(TIMETZ_STR);
        // Takes care of putting
        DefaultTimeTzNormalizationFunctionSymbol timeTZNormFunctionSymbol = new DefaultTimeTzNormalizationFunctionSymbol(
                timeTZType, dbStringType,
                (terms, termConverter, termFactory) -> String.format(
                        "REGEXP_REPLACE(CAST(%s AS TEXT),'([-+]\\d\\d)$', '\\1:00')", termConverter.apply(terms.get(0))));
        builder.put(timeTZType, typeFactory.getDatatype(XSD.TIME), timeTZNormFunctionSymbol);

        //GEOMETRY
        DBTermType defaultDBGeometryType = dbTypeFactory.getDBGeometryType();
        DBTypeConversionFunctionSymbol geometryNormFunctionSymbol = createGeometryNormFunctionSymbol(defaultDBGeometryType);
        builder.put(defaultDBGeometryType,typeFactory.getWktLiteralDatatype(), geometryNormFunctionSymbol);

        //GEOGRAPHY - Data type exclusive to PostGIS
        DBTermType defaultDBGeographyType = dbTypeFactory.getDBGeographyType();
        DBTypeConversionFunctionSymbol geographyNormFunctionSymbol = createGeometryNormFunctionSymbol(defaultDBGeographyType);
        builder.put(defaultDBGeographyType,typeFactory.getWktLiteralDatatype(), geographyNormFunctionSymbol);

        return builder.build();
    }

    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "array_to_string(array_agg(%s%s),%s)",
                        isDistinct ? "DISTINCT " : "",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1))
                ));
    }

    /**
     * Requires sometimes to type NULLs
     */
    @Override
    protected DBFunctionSymbol createTypeNullFunctionSymbol(DBTermType termType) {
        // Cannot CAST to SERIAL --> CAST to INTEGER instead
        if (termType.getCastName().equals(SERIAL_STR))
            return new NonSimplifiableTypedNullFunctionSymbol(termType, dbTypeFactory.getDBTermType(INTEGER_STR));
        else
            return new NonSimplifiableTypedNullFunctionSymbol(termType);


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
        return new NullToleratingDBConcatFunctionSymbol("CONCAT", arity, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new OneLetterDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }

    /**
     * TODO: find a way to use the stored TZ instead of the local one
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // Enforces ISO 8601: https://stackoverflow.com/questions/38834022/turn-postgres-date-representation-into-iso-8601-string
        // However, use the local TZ instead of the stored TZ
        return String.format("TO_JSON(%s)#>>'{}'", termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
        return new OneLetterBooleanNormFunctionSymbolImpl(booleanType, dbStringType);
    }

    @Override
    public DBFunctionSymbol getDBSubString2() {
        return getRegularDBFunctionSymbol(SUBSTR_STR, 2);
    }

    @Override
    public DBFunctionSymbol getDBSubString3() {
        return getRegularDBFunctionSymbol(SUBSTR_STR, 3);
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(UUID_STR, uuid, dbStringType,
                (terms, termConverter, termFactory) ->
                        "MD5(RANDOM()::text || CLOCK_TIMESTAMP()::text)::uuid");
    }

    @Override
    protected String getRandNameInDialect() {
        return RANDOM_STR;
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("Should not be used for PostgreSQL");
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("(POSITION(%s IN %s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));
        return String.format("LEFT(%s,CAST (SIGN(POSITION(%s IN %s))*(POSITION(%s IN %s)-1) AS INTEGER))", str, before, str, before, str);
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

    /**
     * Requires pgcrypto to be enabled (CREATE EXTENSION pgcrypto)
     */
    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("encode(digest(%s, 'sha1'), 'hex')", termConverter.apply(terms.get(0)));
    }

    /**
     * Requires pgcrypto to be enabled (CREATE EXTENSION pgcrypto)
     */
    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("encode(digest(%s, 'sha256'), 'hex')", termConverter.apply(terms.get(0)));
    }

    /**
     * Requires pgcrypto to be enabled (CREATE EXTENSION pgcrypto)
     */
    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("encode(digest(%s, 'sha512'), 'hex')", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        return String.format("(LPAD(EXTRACT(TIMEZONE_HOUR FROM %s)::text,2,'0') || ':' || LPAD(EXTRACT(TIMEZONE_MINUTE FROM %s)::text,2,'0'))", str, str);
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return new DBBooleanFunctionSymbolWithSerializerImpl(REGEXP_LIKE_STR + "2",
                ImmutableList.of(abstractRootDBType, abstractRootDBType), dbBooleanType, false,
                Serializers.getOperatorSerializer("~"));
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return new DBBooleanFunctionSymbolWithSerializerImpl(REGEXP_LIKE_STR + "3",
                ImmutableList.of(abstractRootDBType, abstractRootDBType, abstractRootType), dbBooleanType, false,
                /*
                 * TODO: is it safe to assume the flags are not empty?
                 */
                ((terms, termConverter, termFactory) -> {
                    /*
                     * Normalizes the flag
                     *   - DOT_ALL: s -> n
                     */
                    ImmutableTerm flagTerm = termFactory.getDBReplace(terms.get(2),
                            termFactory.getDBStringConstant("s"),
                            termFactory.getDBStringConstant("n"));

                    ImmutableTerm extendedPatternTerm = termFactory.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
                            termFactory.getDBStringConstant("(?"),
                            flagTerm,
                            termFactory.getDBStringConstant(")"),
                            terms.get(1)))
                            .simplify();


                    return String.format("%s ~ %s",
                            termConverter.apply(terms.get(0)),
                            termConverter.apply(extendedPatternTerm));
                }));
    }

    /**
     * Cast made explicit when the input type is char
     */
    @Override
    protected DBTypeConversionFunctionSymbol createStringToStringCastFunctionSymbol(DBTermType inputType,
                                                                                    DBTermType targetType) {
        switch (inputType.getName()) {
            case CHAR_STR:
                return new DefaultSimpleDBCastFunctionSymbol(inputType, targetType,
                        Serializers.getCastSerializer(targetType));
            default:
                // Implicit cast
                return super.createStringToStringCastFunctionSymbol(inputType, targetType);
        }
    }

    /**
     * Time extension - duration arithmetic
     */

    @Override
    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))/604800)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeDaysBetween(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))/86400)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))/3600)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))/60)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TRUNC((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s)))",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CEIL((EXTRACT (EPOCH FROM %s) - EXTRACT (EPOCH FROM %s))*1000)",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }
}
