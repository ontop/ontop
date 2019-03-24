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
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsTrueFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.SQLServerDBTypeFactory;

import java.util.function.Function;

public class SQLServerDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    private static final String UUID_STR = "NEWID";
    private static final String REGEXP_LIKE_STR = "REGEXP_LIKE";

    private static final String UNSUPPORTED_MSG = "Not supported by SQL server";

    @Inject
    private SQLServerDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDefaultDenormalizationTable(typeFactory),
                createSQLServerRegularFunctionTable(typeFactory), typeFactory);
    }

    @Override
    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        builder.putAll(super.createNormalizationTable());

        // Other DB datetimes
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();

        // TODO: get rid of the typeCode (meaningless)
        DBTermType datetime2 = dbTypeFactory.getDBTermType(0, SQLServerDBTypeFactory.DATETIME2_STR);
        DBTypeConversionFunctionSymbol datetime2NormFunctionSymbol = createDateTimeNormFunctionSymbol(datetime2);
        builder.put(datetime2, xsdDatetime, datetime2NormFunctionSymbol);
        builder.put(datetime2, xsdDatetimeStamp, datetime2NormFunctionSymbol);

        DBTermType datetimeOffset = dbTypeFactory.getDBTermType(0, SQLServerDBTypeFactory.DATETIMEOFFSET_STR);
        DBTypeConversionFunctionSymbol datetimeOffsetNormFunctionSymbol = createDateTimeNormFunctionSymbol(datetimeOffset);
        builder.put(datetimeOffset, xsdDatetime, datetimeOffsetNormFunctionSymbol);
        builder.put(datetimeOffset, xsdDatetimeStamp, datetimeOffsetNormFunctionSymbol);

        return builder.build();
    }

    /**
     * TODO: update
     */
    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createSQLServerRegularFunctionTable(
            TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();

        Table<String, Integer, DBFunctionSymbol> table = HashBasedTable.create(
                createDefaultRegularFunctionTable(typeFactory));

        DBBooleanFunctionSymbol regexpLike2 = new DefaultSQLSimpleDBBooleanFunctionSymbol(REGEXP_LIKE_STR, 2, dbBooleanType,
                abstractRootDBType);
        table.put(REGEXP_LIKE_STR, 2, regexpLike2);

        DBBooleanFunctionSymbol regexpLike3 = new DefaultSQLSimpleDBBooleanFunctionSymbol(REGEXP_LIKE_STR, 3, dbBooleanType,
                abstractRootDBType);
        table.put(REGEXP_LIKE_STR, 3, regexpLike3);

        return ImmutableTable.copyOf(table);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter,
                                       TermFactory termFactory) {
        return String.format("CHARINDEX(%s,%s) > 0",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                        Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String before = termConverter.apply(terms.get(1));

        return String.format("LEFT(%s,CHARINDEX(%s,%s)-1)", str, before, str);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String str = termConverter.apply(terms.get(0));
        String after = termConverter.apply(terms.get(1));

        // sign return 1 if positive number, 0 if 0, and -1 if negative number
        // it will return everything after the value if it is present or it will return an empty string if it is not present
        return String.format("SUBSTRING(%s,CHARINDEX(%s,%s) + LENGTH(%s), SIGN(CHARINDEX(%s,%s)) * LENGTH(%s))",
                str, after, str, after, after, str, str);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("HASH('SHA256', STRINGTOUTF8(%s), 1)", termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * TODO: update
     */
    @Override
    protected String serializeTz(ImmutableList<? extends ImmutableTerm> terms,
                                 Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        // TODO: throw a better exception
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * TODO: update
     */
    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 2);
    }

    /**
     * TODO: update
     */
    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 3);
    }

    /**
     * Asks the timezone to be included
     */
    @Override
    protected String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter,
                                           TermFactory termFactory) {
        return String.format("CONVERT(varchar,%s,127)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String getUUIDNameInDialect() {
        return UUID_STR;
    }

    @Override
    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new SQLServerDBIsTrueFunctionSymbolImpl(dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDBBooleanIfElseNull() {
        return new SQLServerBooleanDBIfElseNullFunctionSymbolImpl(dbBooleanType);
    }

    /**
     * TODO: try to support the fragment that reduces to REPLACE
     */
    @Override
    public DBFunctionSymbol getDBRegexpReplace3() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace4() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
}
