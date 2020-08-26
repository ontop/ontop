package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.DefaultSQLDBTypeFactory.TIMESTAMP_STR;

public class DremioDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String NOT_YET_SUPPORTED_MSG = "Not yet supported for Dremio";
    protected static final String  CURRENT_TZ_STR = "";

    @Inject
    protected DremioDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDremioRegularFunctionTable(typeFactory), typeFactory);
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDremioRegularFunctionTable(
            TypeFactory typeFactory) {

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        return createDefaultRegularFunctionTable(typeFactory);
    }

    private static final String REGEXP_LIKE_STR = "REGEXP_LIKE";

    @Override
    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "GROUP_CONCAT(%s%s SEPARATOR %s)",
                        isDistinct ? "DISTINCT " : "",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1))
                ));
    }

    /**
     * TODO: provide a MySQL specific implementation
     */
    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return super.createDateTimeDenormFunctionSymbol(timestampType);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("INSTR(%s,%s) > 0",
                termConverter.apply(terms.get(0)),
                termConverter.apply(terms.get(1)));
    }

    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));
        return String.format("LEFT(%s,INSTR(%s,%s)-1)", str,  str, before);
    }

    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));
        // sign return 1 if positive number, 0 if 0 and -1 if negative number
        // it will return everything after the value if it is present or it will return an empty string if it is not present
        return String.format("SUBSTRING(%s,LOCATE(%s,%s) + LENGTH(%s), SIGN(LOCATE(%s,%s)) * LENGTH(%s))",
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
        return String.format("SHA2(%s,256)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("SHA2(%s,512)", termConverter.apply(terms.get(0)));
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

    @Override
    protected DBConcatFunctionSymbol createNullRejectingDBConcat(int arity) {
        return (DBConcatFunctionSymbol) getRegularDBFunctionSymbol(CONCAT_STR, arity);
    }

    @Override
    protected DBConcatFunctionSymbol createDBConcatOperator(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_OP_STR, arity, dbStringType, abstractRootDBType,
                Serializers.getOperatorSerializer(CONCAT_OP_STR));
    }

    @Override
    protected DBConcatFunctionSymbol createRegularDBConcat(int arity) {
        return new NullRejectingDBConcatFunctionSymbol(CONCAT_STR, arity, dbStringType, abstractRootDBType, false);
    }

    /**
     * Made Implicit
     */
    protected DBTypeConversionFunctionSymbol createDatetimeToDatetimeCastFunctionSymbol(DBTermType inputType,
                                                                                        DBTermType targetType) {
        return new DefaultImplicitDBCastFunctionSymbol(inputType, targetType);
    }

    @Override
    protected DBFunctionSymbol createR2RMLIRISafeEncode() {
        return new MySQLR2RMLSafeIRIEncodeFunctionSymbolImpl(dbStringType);
    }

    @Override
    protected String getUUIDNameInDialect() {
        throw new UnsupportedOperationException("Do not call getUUIDNameInDialect for Dremio");
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return new DBBooleanFunctionSymbolWithSerializerImpl("REGEXP_MATCHES_2",
                ImmutableList.of(abstractRootDBType, abstractRootDBType), dbBooleanType, false,
                (terms, termConverter, termFactory) -> String.format(
                        // NB: BINARY is for making it case sensitive
                        "(%s REGEXP BINARY %s)",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1))));
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return new DBBooleanFunctionSymbolWithSerializerImpl("REGEXP_MATCHES_3",
                ImmutableList.of(abstractRootDBType, abstractRootDBType, abstractRootDBType), dbBooleanType, false,
                this::serializeDBRegexpMatches3);
    }

    protected String serializeDBRegexpMatches3(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String string = termConverter.apply(terms.get(0));
        String pattern = termConverter.apply(terms.get(1));
        ImmutableTerm flagTerm = terms.get(2);
        if (flagTerm instanceof DBConstant) {
            String flags = ((DBConstant) flagTerm).getValue();
            switch (flags) {
                // Case sensitive
                case "":
                    return String.format("(%s REGEXP BINARY %s)", string, pattern);
                // Case insensitive
                case "i":
                    // TODO: is it robust to collation?
                    return String.format("(%s REGEXP %s)", string, pattern);
                default:
                    break;
            }
        }

        return getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 3)
                .getNativeDBString(terms, termConverter, termFactory);
    }

}
