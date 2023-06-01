package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DatabaseInfoSupplier;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Optional;
import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.BINARY_VAR_STR;
import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.VARBINARY_STR;
import static it.unibz.inf.ontop.model.type.impl.H2SQLDBTypeFactory.DEFAULT_DECIMAL_STR;

public class H2SQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UUID_STR = "RANDOM_UUID";
    private static final String REGEXP_LIKE_STR = "REGEXP_LIKE";
    private static final String LISTAGG_STR = "LISTAGG";

    private static final String UNSUPPORTED_MSG = "Not supported by H2";
    private final DatabaseInfoSupplier databaseInfoSupplier;

    @Inject
    private H2SQLDBFunctionSymbolFactory(TypeFactory typeFactory, DatabaseInfoSupplier databaseInfoSupplier) {
        super(createH2RegularFunctionTable(typeFactory), typeFactory);
        this.databaseInfoSupplier = databaseInfoSupplier;
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createH2RegularFunctionTable(
            TypeFactory typeFactory) {

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> createNormalizationMap() {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        ImmutableMap.Builder<DBTermType, DBTypeConversionFunctionSymbol> builder = ImmutableMap.builder();
        builder.putAll(super.createNormalizationMap());

        DBTermType varBinary = dbTypeFactory.getDBTermType(VARBINARY_STR);
        builder.put(varBinary, createHexBinaryNormFunctionSymbol(varBinary));
        DBTermType varBinary2 = dbTypeFactory.getDBTermType(BINARY_VAR_STR);
        builder.put(varBinary2, createHexBinaryNormFunctionSymbol(varBinary2));

        return builder.build();
    }

    /**
     * Ensure geometries are recast back from text.
     *
     * At moment only WKT is supported, but in the future GML could also be.
     * It is therefore important to consider the target datatype for choosing the right normalization.
     *
     */
    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();

        //GEOMETRY
        DBTermType defaultDBGeometryType = dbTypeFactory.getDBGeometryType();
        DBTypeConversionFunctionSymbol geometryNormFunctionSymbol = createGeometryNormFunctionSymbol(defaultDBGeometryType);
        // For WKT
        builder.put(defaultDBGeometryType,typeFactory.getWktLiteralDatatype(), geometryNormFunctionSymbol);

        return builder.build();
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter,
                                       TermFactory termFactory) {
        return String.format("(POSITION(%s,%s) > 0)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("LEFT(%s,POSITION(%s,%s)-1)", str, before, str);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));

        // sign return 1 if positive number, 0 if 0, and -1 if negative number
        // it will return everything after the value if it is present or it will return an empty string if it is not present
        return String.format("SUBSTRING(%s,POSITION(%s,%s) + LENGTH(%s), SIGN(POSITION(%s,%s)) * LENGTH(%s))",
                str, after, str, after, after, str, str);
    }

    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("RAWTOHEX(HASH('SHA256', STRINGTOUTF8(%s), 1))", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms,
                                 Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    protected String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        return "ROWNUM()";
    }

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType, true);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return getNullRejectingDBConcat(arity);
    }

    /**
     * Treats NULLs as empty strings
     */
    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullToleratingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    @Override
    protected Optional<DBFunctionSymbol> createCeilFunctionSymbol(DBTermType dbTermType) {
        return Optional.of(new UnaryDBFunctionSymbolWithSerializerImpl(CEIL_STR, dbTermType, dbTermType, false,
                (terms, termConverter, termFactory) -> String.format(
                        "CAST(CEIL(%s) AS %s)", termConverter.apply(terms.get(0)), dbTermType.getCastName())));
    }

    @Override
    protected Optional<DBFunctionSymbol> createFloorFunctionSymbol(DBTermType dbTermType) {
        // TODO: check term type
        return Optional.of(new UnaryDBFunctionSymbolWithSerializerImpl(FLOOR_STR, dbTermType, dbTermType, false,
                (terms, termConverter, termFactory) -> String.format(
                        "CAST(FLOOR(%s) AS %s)", termConverter.apply(terms.get(0)), dbTermType.getCastName())));
    }

    @Override
    protected Optional<DBFunctionSymbol> createRoundFunctionSymbol(DBTermType dbTermType) {
        // TODO: check term type
        return Optional.of(new UnaryDBFunctionSymbolWithSerializerImpl(ROUND_STR, dbTermType, dbTermType, false,
                (terms, termConverter, termFactory) -> String.format(
                        "CAST(ROUND(%s) AS %s)", termConverter.apply(terms.get(0)), dbTermType.getCastName())));
    }

    /**
     * Asks the timezone to be included
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("REPLACE(FORMATDATETIME(%s,'yyyy-MM-dd HH:mm:ss.SSSXXX'), ' ', 'T')", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeHexBinaryNorm(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        if (databaseInfoSupplier.getDatabaseVersion()
                .filter(s -> s.startsWith("1"))
                .isPresent())
            return super.serializeHexBinaryNorm(terms, termConverter, termFactory);

        return String.format("UPPER(RAWTOHEX(%s))", termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBFunctionSymbol createDBAvg(DBTermType inputType, boolean isDistinct) {
        // To make sure the AVG does not return an integer but a decimal
        if (inputType.equals(dbIntegerType) && databaseInfoSupplier.getDatabaseVersion().get().startsWith("1"))
            return new ForcingFloatingDBAvgFunctionSymbolImpl(inputType, dbDecimalType, isDistinct);

        return super.createDBAvg(inputType, isDistinct);
    }

    @Override
    protected String getUUIDNameInDialect() {
        return UUID_STR;
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

    /**
     * XSD CAST functions
     */
    // H2 cannot detect NaN. A different error will be thrown by H2 and the boolean cast will fail.
    @Override
    protected String serializeCheckAndConvertBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("(CASE WHEN CAST(%1$s AS " + DEFAULT_DECIMAL_STR +") = 0 THEN 'false' " +
                        "WHEN %1$s = '' THEN 'false' " +
                        "ELSE 'true' " +
                        "END)",
                term);
    }

    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s ~ '^-?([0-9]+[.]?[0-9]*|[.][0-9]+)$' THEN " +
                        "CAST(FLOOR(ABS(CAST(%1$s AS " + DEFAULT_DECIMAL_STR + "))) * SIGN(CAST(%1$s AS " + DEFAULT_DECIMAL_STR + ")) AS INTEGER) " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    // H2 MOD function does not work with DECIMAL scale in practice. Less efficient vs. MOD solution.
    @Override
    protected String serializeCheckAndConvertStringFromDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("(CASE WHEN FLOOR(%1$s) = %s THEN CAST((%1$s) AS INTEGER) " +
                        "ELSE %1$s " +
                        "END)",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("CAST(TIMESTAMP %s AS DATE)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s !~ " + numericNonFPPattern + " THEN NULL " +
                        "ELSE CAST(%1$s AS "+ DEFAULT_DECIMAL_STR +") END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloatFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN UPPER(%1$s) LIKE 'TRUE' THEN 1.0 " +
                        "WHEN UPPER(%1$s) LIKE 'FALSE' THEN 0.0 " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeDateTrunc(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String template = String.format(" WHEN %s LIKE '%%s' THEN DATETRUNC(%%s, %s)", termConverter.apply(terms.get(1)), termConverter.apply(terms.get(0)));
        ImmutableList<String> possibleParts = ImmutableList.of("millennium", "century", "decade", "year", "quarter", "month", "day", "week", "hour", "minute", "second", "millisecond", "microsecond");
        StringBuilder serializationBuilder = new StringBuilder("CASE");
        possibleParts.stream()
                .forEach(part -> serializationBuilder.append(String.format(template, part, part)));
        serializationBuilder.append(" ELSE NULL END");
        return serializationBuilder.toString();
    }
}
