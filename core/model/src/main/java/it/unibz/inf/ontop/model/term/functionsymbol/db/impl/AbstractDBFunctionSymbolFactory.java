package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractDBFunctionSymbolFactory implements DBFunctionSymbolFactory {

    private static final String BNODE_PREFIX = "ontop-bnode-";
    private static final String PLACEHOLDER = "{}";

    /**
     * Name (in the DB dialect), arity -> predefined REGULAR DBFunctionSymbol
     *
     * A regular function symbol is identified by its name in the DB dialect and can be used in the INPUT MAPPING file.
     *
     */
    private final ImmutableTable<String, Integer, DBFunctionSymbol> predefinedRegularFunctionTable;

    // Created in init()
    private DBTypeConversionFunctionSymbol temporaryToStringCastFunctionSymbol;
    // Created in init()
    private DBBooleanFunctionSymbol dbStartsWithFunctionSymbol;
    // Created in init()
    private DBBooleanFunctionSymbol dbEndsWithFunctionSymbol;
    // Created in init()
    private DBBooleanFunctionSymbol dbLikeFunctionSymbol;
    // Created in init()
    private DBBooleanFunctionSymbol dbSimilarToFunctionSymbol;
    // Created in init()
    private DBIfElseNullFunctionSymbol ifElseNullFunctionSymbol;
    // Created in init()
    private DBNotFunctionSymbol dbNotFunctionSymbol;

    // Created in init()
    private DBBooleanFunctionSymbol containsFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol r2rmlIRISafeEncodeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol encodeForURIFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol strBeforeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol strAfterFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol md5FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol sha1FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol sha256FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol sha384FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol sha512FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol yearFromDatetimeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol yearFromDateFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol monthFromDatetimeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol monthFromDateFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol dayFromDatetimeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol dayFromDateFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol hoursFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol minutesFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol secondsFunctionSymbol;
    //Created in init
    private DBFunctionSymbol weekFunctionSymbol;
    //Created in init
    private DBFunctionSymbol quarterFunctionSymbol;
    //Created in init
    private DBFunctionSymbol decadeFunctionSymbol;
    //Created in init
    private DBFunctionSymbol centuryFunctionSymbol;
    //Created in init
    private DBFunctionSymbol millenniumFunctionSymbol;
    //Created in init
    private DBFunctionSymbol millisecondsFunctionSymbol;
    //Created in init
    private DBFunctionSymbol microsecondsFunctionSymbol;
    //Created in init
    private DBFunctionSymbol dateTruncFunctionSymbol;
    //Created in init
    // Created in init()
    private DBFunctionSymbol tzFunctionSymbol;

    // Time extension - duration arithmetic
    private DBFunctionSymbol weeksBetweenFromDateTimeFunctionSymbol;
    private DBFunctionSymbol weeksBetweenFromDateFunctionSymbol;
    private DBFunctionSymbol daysBetweenFromDateTimeFunctionSymbol;
    private DBFunctionSymbol daysBetweenFromDateFunctionSymbol;
    private DBFunctionSymbol hoursBetweenFromDateTimeFunctionSymbol;
    private DBFunctionSymbol minutesBetweenFromDateTimeFunctionSymbol;
    private DBFunctionSymbol secondsBetweenFromDateTimeFunctionSymbol;
    private DBFunctionSymbol millisBetweenFromDateTimeFunctionSymbol;

    private final Map<String, DBFunctionSymbol> extractFunctionSymbolsMap;
    private final Map<String, DBFunctionSymbol> currentDateTimeFunctionSymbolsMap;

    // Created in init()
    private DBBooleanFunctionSymbol nonStrictNumericEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol nonStrictStringEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol nonStrictDatetimeEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol nonStrictDateEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol nonStrictDefaultEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol booleanIfElseNullFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol nonDistinctGroupConcat;
    // Created in init()
    private DBFunctionSymbol distinctGroupConcat;

    // Created in init()
    private DBFunctionSymbol rowUniqueStrFct;
    // Created in init()
    private DBFunctionSymbol rowNumberFct;
    private DBFunctionSymbol rowNumberWithOrderByFct;

    private IRISafenessDeclarationFunctionSymbol iriSafenessDeclarationFunctionSymbol;

    private final Map<DBTermType, DBBooleanFunctionSymbol> jsonIsScalarMap;
    private final Map<DBTermType, DBBooleanFunctionSymbol> jsonIsBooleanMap;
    private final Map<DBTermType, DBBooleanFunctionSymbol> jsonIsNumberMap;
    private final Map<DBTermType, DBBooleanFunctionSymbol> isArrayMap;

    private DBFunctionSymbol checkAndConvertBooleanFunctionSymbol;
    private DBFunctionSymbol checkAndConvertBooleanFromStringFunctionSymbol;
    private DBFunctionSymbol checkAndConvertDoubleFunctionSymbol;
    private DBFunctionSymbol checkAndConvertFloatFunctionSymbol;
    private DBFunctionSymbol checkAndConvertFloatFromBooleanFunctionSymbol;
    private DBFunctionSymbol checkAndConvertFloatFromDoubleFunctionSymbol;
    private DBFunctionSymbol checkAndConvertFloatFromNonFPNumericFunctionSymbol;
    private DBFunctionSymbol checkAndConvertDecimalFunctionSymbol;
    private DBFunctionSymbol checkAndConvertDecimalFromBooleanFunctionSymbol;
    private DBFunctionSymbol checkAndConvertIntegerFunctionSymbol;
    private DBFunctionSymbol checkAndConvertIntegerFromBooleanFunctionSymbol;
    private DBFunctionSymbol checkAndConvertStringFromDecimalFunctionSymbol;
    private DBFunctionSymbol checkAndConvertDateTimeFromDateFunctionSymbol;
    private DBFunctionSymbol checkAndConvertDateTimeFromStringFunctionSymbol;
    private DBFunctionSymbol checkAndConvertDateFromDateTimeFunctionSymbol;
    private DBFunctionSymbol checkAndConvertDateFromStringFunctionSymbol;

    /**
     *  For conversion function symbols that are SIMPLE CASTs from an undetermined type (no normalization)
     */
    private final Map<DBTermType, DBTypeConversionFunctionSymbol> castMap;
    /**
     *  For conversion function symbols that implies a NORMALIZATION as specified in R2RML
     *
     *  Created in init()
     */
    private ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> normalizationMap;

    /**
     *  For NORMALIZATION as RDF lexical term where the RDF datatype matters.
     *  Most functions are going to the map, not the table.
     *
     *  Created in init()
     */
    private ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable;

    /**
     *  For conversion function symbols that implies a DENORMALIZATION from RDF lexical term.
     *
     *  Created in init()
     */
    private ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> deNormalizationMap;

    /**
     *  For conversion function symbols that implies a DENORMALIZATION from RDF lexical term
     *  when the target RDF datatype matters.
     *
     *  Most functions are going to the map, not the table.
     *
     *  Created in init()
     */
    private ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> deNormalizationTable;

    /**
     * Created in init()
     */
    private ImmutableTable<Integer, Boolean, DBFunctionSymbol> countTable;

    /**
     * Only for SIMPLE casts to DB string.
     * (Source DB type -> function symbol)
     *
     * NB: why using a map instead of table? Because tables are not thread-safe while some maps are.
     */
    private final Map<DBTermType, DBTypeConversionFunctionSymbol> simpleCastToDBStringMap;

    /**
     * Only for SIMPLE casts FROM DB string.
     * (Target DB type -> function symbol)
     *
     */
    private final Map<DBTermType, DBTypeConversionFunctionSymbol> simpleCastFromDBStringMap;

    /**
     *  For conversion function symbols that are SIMPLE CASTs from a determined type (no normalization)
     */
    private final Table<DBTermType, DBTermType, DBTypeConversionFunctionSymbol> otherSimpleCastTable;

    // Output type
    private final Table<String, DBTermType, DBMathBinaryOperator> binaryMathTable;
    private final Table<String, ImmutableList<DBTermType>, DBMathBinaryOperator> binaryMathTableWithInputType;

    private final Map<String, DBMathBinaryOperator> untypedBinaryMathMap;

    /**
     * For the CASE functions
     */
    private final Map<Integer, DBFunctionSymbol> caseMapWithOrder;
    private final Map<Integer, DBFunctionSymbol> caseMapWithoutOrder;

    /**
     * For the CASE functions
     */
    private final Map<Integer, DBBooleanFunctionSymbol> booleanCaseMapWithOrder;
    private final Map<Integer, DBBooleanFunctionSymbol> booleanCaseMapWithoutOrder;

    /**
     * For the strict equalities
     */
    private final Map<Integer, DBStrictEqFunctionSymbol> strictEqMap;

    /**
     * For the strict NOT equalities
     */
    private final Map<Integer, DBBooleanFunctionSymbol> strictNEqMap;

    /**
     * For the FalseORNulls
     */
    private final Map<Integer, FalseOrNullFunctionSymbol> falseOrNullMap;

    /**
     * For the TrueORNulls
     */
    private final Map<Integer, TrueOrNullFunctionSymbol> trueOrNullMap;

    /**
     * Coalesce functions according to their arities
     */
    private final Map<Integer, DBFunctionSymbol> coalesceMap;


    /**
     * Coalesce functions according to their arities
     */
    private final Map<Integer, DBBooleanFunctionSymbol> booleanCoalesceMap;

    private final Map<InequalityLabel, DBBooleanFunctionSymbol> numericInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> booleanInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> stringInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> datetimeInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> dateInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> defaultInequalityMap;

    private final Map<DBTermType, DBFunctionSymbol> absMap;
    private final Map<DBTermType, DBFunctionSymbol> ceilMap;
    private final Map<DBTermType, DBFunctionSymbol> floorMap;
    private final Map<DBTermType, DBFunctionSymbol> roundMap;

    private final Map<DBTermType, DBFunctionSymbol> typeNullMap;

    private final TypeFactory typeFactory;
    private final DBTermType rootDBType;
    private final DBTermType dbStringType;
    private final DBTermType dbBooleanType;
    private final DBTermType dbIntegerType;
    private final DBTermType dbDecimalType;
    private final DBTermType dbDoubleType;
    private final DBTermType dbDateTimestampType;
    private final DBTermType dbDateType;

    /**
     * Name (in the DB dialect), arity -> not predefined untyped DBFunctionSymbol
     */
    private final Table<String, Integer, DBFunctionSymbol> untypedFunctionTable;

    /**
     * Name (in the DB dialect), arity -> DBBooleanFunctionSymbol
     *
     * Only for boolean function symbols that are not predefined but created on-the-fly
     */
    private final Table<String, Integer, DBBooleanFunctionSymbol> notPredefinedBooleanFunctionTable;

    private final Map<ImmutableList<Template.Component>, IRIStringTemplateFunctionSymbol> iriTemplateMap;
    private final Map<ImmutableList<Template.Component>, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap;

    private final Map<IRI, DBFunctionSymbol> iriStringResolverMap;

    private final Map<DBTermType, DBFunctionSymbol> distinctSumMap;
    private final Map<DBTermType, DBFunctionSymbol> regularSumMap;

    private final Map<DBTermType, DBFunctionSymbol> distinctAvgMap;
    private final Map<DBTermType, DBFunctionSymbol> regularAvgMap;
    private final Map<Map.Entry<DBTermType, Boolean>, DBFunctionSymbol> distinctStdevMap;
    private final Map<Map.Entry<DBTermType, Boolean>, DBFunctionSymbol> regularStdevMap;
    private final Map<Map.Entry<DBTermType, Boolean>, DBFunctionSymbol> distinctVarianceMap;
    private final Map<Map.Entry<DBTermType, Boolean>, DBFunctionSymbol> regularVarianceMap;

    private final Map<DBTermType, DBFunctionSymbol> minMap;
    private final Map<DBTermType, DBFunctionSymbol> maxMap;
    private final Map<DBTermType, DBFunctionSymbol> sampleMap;

    // NB: Multi-threading safety is NOT a concern here
    // (we don't create fresh bnode templates for a SPARQL query)
    private final AtomicInteger counter;


    protected AbstractDBFunctionSymbolFactory(ImmutableTable<String, Integer, DBFunctionSymbol> predefinedRegularFunctionTable,
                                              TypeFactory typeFactory) {
        this.counter = new AtomicInteger();
        this.typeFactory = typeFactory;
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        this.rootDBType = dbTypeFactory.getAbstractRootDBType();
        this.predefinedRegularFunctionTable = predefinedRegularFunctionTable;
        this.dbStringType = dbTypeFactory.getDBStringType();
        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.dbIntegerType = dbTypeFactory.getDBLargeIntegerType();
        this.dbDecimalType = dbTypeFactory.getDBDecimalType();
        this.dbDoubleType = dbTypeFactory.getDBDoubleType();
        this.dbDateTimestampType = dbTypeFactory.getDBDateTimestampType();
        this.dbDateType = dbTypeFactory.getDBDateType();

        // NB: in terms of design, we prefer avoiding using tables as they are not thread-safe
        this.binaryMathTable = HashBasedTable.create();
        this.binaryMathTableWithInputType = HashBasedTable.create();
        this.untypedFunctionTable = HashBasedTable.create();
        this.notPredefinedBooleanFunctionTable = HashBasedTable.create();

        this.simpleCastToDBStringMap = new ConcurrentHashMap<>();
        this.simpleCastFromDBStringMap = new ConcurrentHashMap<>();
        this.otherSimpleCastTable = HashBasedTable.create();

        this.untypedBinaryMathMap = new ConcurrentHashMap<>();
        this.caseMapWithOrder = new ConcurrentHashMap<>();
        this.caseMapWithoutOrder = new ConcurrentHashMap<>();
        this.booleanCaseMapWithOrder = new ConcurrentHashMap<>();
        this.booleanCaseMapWithoutOrder = new ConcurrentHashMap<>();
        this.strictEqMap = new ConcurrentHashMap<>();
        this.strictNEqMap = new ConcurrentHashMap<>();
        this.falseOrNullMap = new ConcurrentHashMap<>();
        this.trueOrNullMap = new ConcurrentHashMap<>();
        this.castMap = new ConcurrentHashMap<>();
        this.iriTemplateMap = new ConcurrentHashMap<>();
        this.bnodeTemplateMap = new ConcurrentHashMap<>();
        this.iriStringResolverMap = new ConcurrentHashMap<>();
        this.numericInequalityMap = new ConcurrentHashMap<>();
        this.booleanInequalityMap = new ConcurrentHashMap<>();
        this.stringInequalityMap = new ConcurrentHashMap<>();
        this.datetimeInequalityMap = new ConcurrentHashMap<>();
        this.dateInequalityMap = new ConcurrentHashMap<>();
        this.defaultInequalityMap = new ConcurrentHashMap<>();
        this.coalesceMap = new ConcurrentHashMap<>();
        this.booleanCoalesceMap = new ConcurrentHashMap<>();

        this.absMap = new ConcurrentHashMap<>();
        this.ceilMap = new ConcurrentHashMap<>();
        this.floorMap = new ConcurrentHashMap<>();
        this.roundMap = new ConcurrentHashMap<>();

        this.distinctSumMap = new ConcurrentHashMap<>();
        this.regularSumMap = new ConcurrentHashMap<>();

        this.distinctAvgMap = new ConcurrentHashMap<>();
        this.regularAvgMap = new ConcurrentHashMap<>();

        this.distinctStdevMap = new ConcurrentHashMap<>();
        this.regularStdevMap = new ConcurrentHashMap<>();

        this.distinctVarianceMap = new ConcurrentHashMap<>();
        this.regularVarianceMap = new ConcurrentHashMap<>();

        this.minMap = new ConcurrentHashMap<>();
        this.maxMap = new ConcurrentHashMap<>();
        this.sampleMap = new ConcurrentHashMap<>();

        this.typeNullMap = new ConcurrentHashMap<>();

        this.extractFunctionSymbolsMap = new ConcurrentHashMap<>();
        this.currentDateTimeFunctionSymbolsMap = new ConcurrentHashMap<>();

        this.isArrayMap = new ConcurrentHashMap<>();
        this.jsonIsNumberMap = new ConcurrentHashMap<>();
        this.jsonIsBooleanMap = new ConcurrentHashMap<>();
        this.jsonIsScalarMap = new ConcurrentHashMap<>();
    }

    /**
     * Called automatically by Guice
     */
    @Inject
    protected void init() {
        normalizationMap = createNormalizationMap();
        normalizationTable = createNormalizationTable();
        deNormalizationMap = createDenormalizationMap();
        deNormalizationTable = createDenormalizationTable();
        countTable = createDBCountTable();

        temporaryToStringCastFunctionSymbol = new TemporaryDBTypeConversionToStringFunctionSymbolImpl(rootDBType, dbStringType);
        dbStartsWithFunctionSymbol = createStrStartsFunctionSymbol();
        dbEndsWithFunctionSymbol = createStrEndsFunctionSymbol();
        dbLikeFunctionSymbol = createLikeFunctionSymbol();
        dbSimilarToFunctionSymbol = createSimilarToFunctionSymbol();
        ifElseNullFunctionSymbol = createRegularIfElseNull();
        dbNotFunctionSymbol = createDBNotFunctionSymbol(dbBooleanType);

        booleanIfElseNullFunctionSymbol = createDBBooleanIfElseNull();
        nonStrictNumericEqOperator = createNonStrictNumericEquality();
        nonStrictStringEqOperator = createNonStrictStringEquality();
        nonStrictDatetimeEqOperator = createNonStrictDatetimeEquality();
        nonStrictDateEqOperator = createNonStrictDateEquality();
        nonStrictDefaultEqOperator = createNonStrictDefaultEquality();
        r2rmlIRISafeEncodeFunctionSymbol = createEncodeURLorIRI(true);
        encodeForURIFunctionSymbol = createEncodeURLorIRI(false);
        strAfterFunctionSymbol = createStrAfterFunctionSymbol();
        containsFunctionSymbol = createContainsFunctionSymbol();
        strBeforeFunctionSymbol = createStrBeforeFunctionSymbol();

        md5FunctionSymbol = createMD5FunctionSymbol();
        sha1FunctionSymbol = createSHA1FunctionSymbol();
        sha256FunctionSymbol = createSHA256FunctionSymbol();
        sha384FunctionSymbol = createSHA384FunctionSymbol();
        sha512FunctionSymbol = createSHA512FunctionSymbol();

        yearFromDatetimeFunctionSymbol = createYearFromDatetimeFunctionSymbol();
        yearFromDateFunctionSymbol = createYearFromDateFunctionSymbol();
        monthFromDatetimeFunctionSymbol = createMonthFromDatetimeFunctionSymbol();
        monthFromDateFunctionSymbol = createMonthFromDateFunctionSymbol();
        dayFromDatetimeFunctionSymbol = createDayFromDatetimeFunctionSymbol();
        dayFromDateFunctionSymbol = createDayFromDateFunctionSymbol();
        hoursFunctionSymbol = createHoursFunctionSymbol();
        minutesFunctionSymbol = createMinutesFunctionSymbol();
        secondsFunctionSymbol = createSecondsFunctionSymbol();
        weekFunctionSymbol = createWeekFunctionSymbol();
        quarterFunctionSymbol = createQuarterFunctionSymbol();
        decadeFunctionSymbol = createDecadeFunctionSymbol();
        centuryFunctionSymbol = createCenturyFunctionSymbol();
        millenniumFunctionSymbol = createMillenniumFunctionSymbol();
        millisecondsFunctionSymbol = createMillisecondsFunctionSymbol();
        microsecondsFunctionSymbol = createMicrosecondsFunctionSymbol();
        dateTruncFunctionSymbol = createDateTruncFunctionSymbol();
        tzFunctionSymbol = createTzFunctionSymbol();

        weeksBetweenFromDateTimeFunctionSymbol = createWeeksBetweenFromDateTimeFunctionSymbol();
        weeksBetweenFromDateFunctionSymbol = createWeeksBetweenFromDateFunctionSymbol();
        daysBetweenFromDateTimeFunctionSymbol = createDaysBetweenFromDateTimeFunctionSymbol();
        daysBetweenFromDateFunctionSymbol = createDaysBetweenFromDateFunctionSymbol();
        hoursBetweenFromDateTimeFunctionSymbol = createHoursBetweenFromDateTimeFunctionSymbol();
        minutesBetweenFromDateTimeFunctionSymbol = createMinutesBetweenFromDateTimeFunctionSymbol();
        secondsBetweenFromDateTimeFunctionSymbol = createSecondsBetweenFromDateTimeFunctionSymbol();
        millisBetweenFromDateTimeFunctionSymbol = createMillisBetweenFromDateTimeFunctionSymbol();

        nonDistinctGroupConcat = createDBGroupConcat(dbStringType, false);
        distinctGroupConcat = createDBGroupConcat(dbStringType, true);

        rowUniqueStrFct = createDBRowUniqueStr();
        rowNumberFct = createDBRowNumber();
        rowNumberWithOrderByFct = createDBRowNumberWithOrderBy();
        iriSafenessDeclarationFunctionSymbol = new IRISafenessDeclarationFunctionSymbolImpl(rootDBType);

        checkAndConvertDateFromDateTimeFunctionSymbol = createCheckAndConvertDateFromDateTimeFunctionSymbol();
        checkAndConvertDateFromStringFunctionSymbol = createCheckAndConvertDateFromStringFunctionSymbol();
        checkAndConvertBooleanFunctionSymbol = createCheckAndConvertBooleanFunctionSymbol();
        checkAndConvertBooleanFromStringFunctionSymbol = createCheckAndConvertBooleanFromStringFunctionSymbol();
        checkAndConvertIntegerFunctionSymbol = createCheckAndConvertIntegerFunctionSymbol();
        checkAndConvertIntegerFromBooleanFunctionSymbol = createCheckAndConvertIntegerFromBooleanFunctionSymbol();
        checkAndConvertDecimalFunctionSymbol = createCheckAndConvertDecimalFunctionSymbol();
        checkAndConvertDecimalFromBooleanFunctionSymbol = createCheckAndConvertDecimalFromBooleanFunctionSymbol();
        checkAndConvertDoubleFunctionSymbol = createCheckAndConvertDoubleFunctionSymbol();
        checkAndConvertFloatFunctionSymbol = createCheckAndConvertFloatFunctionSymbol();
        checkAndConvertFloatFromBooleanFunctionSymbol = createCheckAndConvertFloatFromBooleanFunctionSymbol();
        checkAndConvertFloatFromDoubleFunctionSymbol = createCheckAndConvertFloatFromDoubleFunctionSymbol();
        checkAndConvertFloatFromNonFPNumericFunctionSymbol = createCheckAndConvertFloatFromNonFPNumericFunctionSymbol();
        checkAndConvertStringFromDecimalFunctionSymbol = createCheckAndConvertStringFromDecimalFunctionSymbol();
        checkAndConvertDateTimeFromDateFunctionSymbol = createCheckAndConvertDateTimeFromDateFunctionSymbol();
        checkAndConvertDateTimeFromStringFunctionSymbol = createCheckAndConvertDateTimeFromStringFunctionSymbol();
    }

    protected ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> createNormalizationMap() {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        ImmutableMap.Builder<DBTermType, DBTypeConversionFunctionSymbol> builder = ImmutableMap.builder();

        // Date time
        DBTermType defaultDBDateTimestampType = dbTypeFactory.getDBDateTimestampType();
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(defaultDBDateTimestampType);
        builder.put(defaultDBDateTimestampType, datetimeNormFunctionSymbol);
        // Boolean
        builder.put(dbBooleanType, createBooleanNormFunctionSymbol(dbBooleanType));
        // Binary
        DBTermType defaultBinaryType = dbTypeFactory.getDBHexBinaryType();
        DBTypeConversionFunctionSymbol hexBinaryNormFunctionSymbol = createHexBinaryNormFunctionSymbol(defaultBinaryType);
        builder.put(defaultBinaryType, hexBinaryNormFunctionSymbol);

        return builder.build();
    }

    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        return ImmutableTable.of();
    }

    protected ImmutableMap<DBTermType, DBTypeConversionFunctionSymbol> createDenormalizationMap() {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        DBTermType timestampType = dbTypeFactory.getDBDateTimestampType();
        DBTermType booleanType = dbTypeFactory.getDBBooleanType();
        DBTermType binaryType = dbTypeFactory.getDBHexBinaryType();

        ImmutableMap.Builder<DBTermType, DBTypeConversionFunctionSymbol> builder = ImmutableMap.builder();

        // Date time
        DBTypeConversionFunctionSymbol timestampDenormalization = createDateTimeDenormFunctionSymbol(timestampType);
        builder.put(timestampType, timestampDenormalization);

        // Boolean
        builder.put(booleanType, createBooleanDenormFunctionSymbol());

        // Binary
        DBTypeConversionFunctionSymbol hexBinaryDenormFunctionSymbol = createHexBinaryDenormFunctionSymbol(binaryType);
        builder.put(binaryType, hexBinaryDenormFunctionSymbol);

        return builder.build();
    }

    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createDenormalizationTable() {
        return ImmutableTable.of();
    }

    protected ImmutableTable<Integer, Boolean, DBFunctionSymbol> createDBCountTable() {

        ImmutableTable.Builder<Integer, Boolean, DBFunctionSymbol> builder = ImmutableTable.builder();
        Stream.of(false, true)
                .forEach(isUnary -> Stream.of(false, true)
                        .forEach(isDistinct ->
                                builder.put(isUnary ? 1 : 0, isDistinct, createDBCount(isUnary, isDistinct))));
        return builder.build();
    }


    @Override
    public IRIStringTemplateFunctionSymbol getIRIStringTemplateFunctionSymbol(ImmutableList<Template.Component> iriTemplate) {
        return iriTemplateMap.computeIfAbsent(iriTemplate,
                        t -> IRIStringTemplateFunctionSymbolImpl.createFunctionSymbol(iriTemplate, typeFactory));
    }

    @Override
    public BnodeStringTemplateFunctionSymbol getBnodeStringTemplateFunctionSymbol(ImmutableList<Template.Component> bnodeTemplate) {
        return bnodeTemplateMap.computeIfAbsent(bnodeTemplate,
                        t -> BnodeStringTemplateFunctionSymbolImpl.createFunctionSymbol(bnodeTemplate, typeFactory));
    }

    @Override
    public BnodeStringTemplateFunctionSymbol getFreshBnodeStringTemplateFunctionSymbol(int arity) {
        if (arity <= 0)
            throw new IllegalArgumentException("A positive BNode arity is expected");

        Template.Builder builder = Template.builder();
        builder.addSeparator(BNODE_PREFIX + counter.incrementAndGet());
        for (int i = 0; i < arity - 1; i++) // except the last one
            builder.addColumn().addSeparator("/");
        builder.addColumn();
        return getBnodeStringTemplateFunctionSymbol(builder.build());
    }

    @Override
    public DBTypeConversionFunctionSymbol getTemporaryConversionToDBStringFunctionSymbol() {
        return temporaryToStringCastFunctionSymbol;
    }

    @Override
    public DBTypeConversionFunctionSymbol getDBCastFunctionSymbol(DBTermType targetType) {
        return castMap
                .computeIfAbsent(targetType, this::createSimpleCastFunctionSymbol);
    }

    @Override
    public DBTypeConversionFunctionSymbol getDBCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        if (inputType.equals(dbStringType)) {
            if (simpleCastFromDBStringMap.containsKey(targetType))
                return simpleCastFromDBStringMap.get(targetType);

            DBTypeConversionFunctionSymbol castFunctionSymbol = createSimpleCastFunctionSymbol(inputType, targetType);
            simpleCastFromDBStringMap.put(targetType, castFunctionSymbol);
            return castFunctionSymbol;
        }
        else if (targetType.equals(dbStringType)) {
            if (simpleCastToDBStringMap.containsKey(inputType))
                return simpleCastToDBStringMap.get(inputType);

            DBTypeConversionFunctionSymbol castFunctionSymbol = createSimpleCastFunctionSymbol(inputType, targetType);
            simpleCastToDBStringMap.put(inputType, castFunctionSymbol);
            return castFunctionSymbol;
        }
        /*
         * Mutable tables are not thread-safe
         */
        else {
            synchronized (otherSimpleCastTable) {
                if (otherSimpleCastTable.contains(inputType, targetType))
                    return otherSimpleCastTable.get(inputType, targetType);

                DBTypeConversionFunctionSymbol castFunctionSymbol = createSimpleCastFunctionSymbol(inputType, targetType);
                otherSimpleCastTable.put(inputType, targetType, castFunctionSymbol);
                return castFunctionSymbol;
            }
        }
    }

    @Override
    public DBFunctionSymbol getRegularDBFunctionSymbol(String nameInDialect, int arity) {
        String canonicalName = canonicalizeRegularFunctionSymbolName(nameInDialect);

        // Looks first in the immutable table
        Optional<DBFunctionSymbol> optionalPredefinedSymbol = Optional.ofNullable(predefinedRegularFunctionTable.get(canonicalName, arity));
        if (optionalPredefinedSymbol.isPresent()) {
            return optionalPredefinedSymbol.get();
        }

        /*
         * Mutable tables are not thread-safe
         */
        synchronized (untypedFunctionTable) {
            Optional<DBFunctionSymbol> optionalUntypedSymbol = Optional.ofNullable(
                    untypedFunctionTable.get(canonicalName, arity));
            // NB: we don't look inside notPredefinedBooleanFunctionTable to avoid enforcing the boolean type
            if (optionalUntypedSymbol.isPresent())
                return optionalUntypedSymbol.get();

            DBFunctionSymbol symbol = createRegularUntypedFunctionSymbol(canonicalName, arity);
            untypedFunctionTable.put(canonicalName, arity, symbol);
            return symbol;
        }
    }

    @Override
    public DBBooleanFunctionSymbol getRegularDBBooleanFunctionSymbol(String nameInDialect, int arity) {
        String canonicalName = canonicalizeRegularFunctionSymbolName(nameInDialect);

        // Looks first in the immutable table
        Optional<DBFunctionSymbol> optionalRegularSymbol = Optional.ofNullable(predefinedRegularFunctionTable.get(canonicalName, arity));
        if (optionalRegularSymbol.isPresent()) {
            DBFunctionSymbol functionSymbol = optionalRegularSymbol.get();
            if (functionSymbol instanceof DBBooleanFunctionSymbol)
                return (DBBooleanFunctionSymbol) functionSymbol;
            else
                throw new IllegalArgumentException(nameInDialect + " is known not to be a boolean function symbol");
        }

        /*
         * Mutable tables are not thread-safe
         */
        synchronized (notPredefinedBooleanFunctionTable) {
            Optional<DBFunctionSymbol> optionalSymbol = Optional.ofNullable(notPredefinedBooleanFunctionTable.get(canonicalName, arity));

            // NB: we don't look inside untypedFunctionTable as they are not declared as boolean

            if (optionalSymbol.isPresent()) {
                DBFunctionSymbol functionSymbol = optionalSymbol.get();
                if (functionSymbol instanceof DBBooleanFunctionSymbol)
                    return (DBBooleanFunctionSymbol) functionSymbol;
                else
                    throw new IllegalArgumentException(nameInDialect + " is known not to be a boolean function symbol");
            }

            DBBooleanFunctionSymbol symbol = createRegularBooleanFunctionSymbol(canonicalName, arity);
            notPredefinedBooleanFunctionTable.put(canonicalName, arity, symbol);
            return symbol;
        }
    }

    @Override
    public DBFunctionSymbol getDBCase(int arity, boolean doOrderingMatter) {
        if ((arity < 3) || (arity % 2 == 0))
            throw new IllegalArgumentException("Arity of a CASE function symbol must be odd and >= 3");

        return doOrderingMatter
                ? caseMapWithOrder.computeIfAbsent(arity, a -> createDBCase(arity, true))
                : caseMapWithoutOrder.computeIfAbsent(arity, a -> createDBCase(arity, false));

    }

    @Override
    public DBBooleanFunctionSymbol getDBBooleanCase(int arity, boolean doOrderingMatter) {
        if ((arity < 3) || (arity % 2 == 0))
            throw new IllegalArgumentException("Arity of a CASE function symbol must be odd and >= 3");

        return doOrderingMatter
                ? booleanCaseMapWithOrder.computeIfAbsent(arity, a -> createDBBooleanCase(arity, true))
                : booleanCaseMapWithoutOrder.computeIfAbsent(arity, a -> createDBBooleanCase(arity, false));
    }

    @Override
    public DBIfElseNullFunctionSymbol getDBIfElseNull() {
        return ifElseNullFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBBooleanIfElseNull() {
        return booleanIfElseNullFunctionSymbol;
    }

    @Override
    public DBStrictEqFunctionSymbol getDBStrictEquality(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of a strict equality must be >= 2");

        return strictEqMap
                .computeIfAbsent(arity, a -> createDBStrictEquality(arity));
    }

    @Override
    public DBBooleanFunctionSymbol getDBStrictNEquality(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of a strict equality must be >= 2");

        return strictNEqMap
                .computeIfAbsent(arity, a -> createDBStrictNEquality(arity));
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictNumericEquality() {
        return nonStrictNumericEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictStringEquality() {
        return nonStrictStringEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictDatetimeEquality() {
        return nonStrictDatetimeEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictDateEquality() {
        return nonStrictDateEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictDefaultEquality() {
        return nonStrictDefaultEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNumericInequality(InequalityLabel inequalityLabel) {
        return numericInequalityMap
                .computeIfAbsent(inequalityLabel, this::createNumericInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBBooleanInequality(InequalityLabel inequalityLabel) {
        return booleanInequalityMap
                .computeIfAbsent(inequalityLabel, this::createBooleanInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBStringInequality(InequalityLabel inequalityLabel) {
        return stringInequalityMap
                .computeIfAbsent(inequalityLabel, this::createStringInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBDatetimeInequality(InequalityLabel inequalityLabel) {
        return datetimeInequalityMap
                .computeIfAbsent(inequalityLabel, this::createDatetimeInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBDateInequality(InequalityLabel inequalityLabel) {
        return dateInequalityMap
                .computeIfAbsent(inequalityLabel, this::createDateInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBDefaultInequality(InequalityLabel inequalityLabel) {
        return defaultInequalityMap
                .computeIfAbsent(inequalityLabel, this::createDefaultInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBStartsWith() {
        return dbStartsWithFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBEndsWith() {
        return dbEndsWithFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getR2RMLIRISafeEncode() {
        return r2rmlIRISafeEncodeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBEncodeForURI() {
        return encodeForURIFunctionSymbol;
    }

    @Override
    public DBNotFunctionSymbol getDBNot() {
        return dbNotFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBCoalesce(int arity) {
        if (arity < 1)
            throw new IllegalArgumentException("Minimal arity is 1");

        return coalesceMap
                .computeIfAbsent(arity, (this::createCoalesceFunctionSymbol));
    }

    @Override
    public DBBooleanFunctionSymbol getDBBooleanCoalesce(int arity) {
        if (arity < 1)
            throw new IllegalArgumentException("Minimal arity is 1");

        return booleanCoalesceMap
                .computeIfAbsent(arity, (this::createBooleanCoalesceFunctionSymbol));
    }

    @Override
    public FalseOrNullFunctionSymbol getFalseOrNullFunctionSymbol(int arity) {
        return falseOrNullMap
                .computeIfAbsent(arity, (this::createFalseOrNullFunctionSymbol));
    }

    @Override
    public TrueOrNullFunctionSymbol getTrueOrNullFunctionSymbol(int arity) {
        return trueOrNullMap
                .computeIfAbsent(arity, (this::createTrueOrNullFunctionSymbol));
    }

    @Override
    public DBBooleanFunctionSymbol getDBContains() {
        return containsFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBLike() {
        return dbLikeFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBSimilarTo() {
        return dbSimilarToFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBStrBefore() {
        return strBeforeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBStrAfter() {
        return strAfterFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMd5() {
        return md5FunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSha1() {
        return sha1FunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSha256() {
        return sha256FunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSha384() {
        return sha384FunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSha512() {
        return sha512FunctionSymbol;
    }

    @Override
    public DBMathBinaryOperator getDBMathBinaryOperator(String dbMathOperatorName, DBTermType dbNumericType) {
        // Mutable tables are not thread-safe
        synchronized (binaryMathTable) {
            DBMathBinaryOperator existingOperator = binaryMathTable.get(dbMathOperatorName, dbNumericType);
            if (existingOperator != null) {
                return existingOperator;
            }

            DBMathBinaryOperator newOperator = createDBBinaryMathOperator(dbMathOperatorName, dbNumericType);
            binaryMathTable.put(dbMathOperatorName, dbNumericType, newOperator);
            return newOperator;
        }
    }

    @Override
    public DBMathBinaryOperator getDBMathBinaryOperator(String dbMathOperatorName, DBTermType arg1Type, DBTermType arg2Type) {
        ImmutableList<DBTermType> types = ImmutableList.of(arg1Type, arg2Type);

        // Mutable tables are not thread-safe
        synchronized (binaryMathTableWithInputType) {
            DBMathBinaryOperator existingOperator = binaryMathTableWithInputType.get(dbMathOperatorName, types);
            if (existingOperator != null) {
                return existingOperator;
            }

            DBMathBinaryOperator newOperator = createDBBinaryMathOperator(dbMathOperatorName, arg1Type, arg2Type);
            binaryMathTableWithInputType.put(dbMathOperatorName, types, newOperator);
            return newOperator;
        }
    }

    @Override
    public DBMathBinaryOperator getUntypedDBMathBinaryOperator(String dbMathOperatorName) {
        DBMathBinaryOperator existingOperator = untypedBinaryMathMap.get(dbMathOperatorName);
        if (existingOperator != null) {
            return existingOperator;
        }

        DBMathBinaryOperator newOperator = createUntypedDBBinaryMathOperator(dbMathOperatorName);
        untypedBinaryMathMap.put(dbMathOperatorName, newOperator);
        return newOperator;
    }

    @Override
    public Optional<DBFunctionSymbol> getAbs(DBTermType dbTermType) {
        DBFunctionSymbol existingFunctionSymbol = absMap.get(dbTermType);
        if (existingFunctionSymbol != null)
            return Optional.of(existingFunctionSymbol);
        Optional<DBFunctionSymbol> dbFunctionSymbol = createAbsFunctionSymbol(dbTermType);
        dbFunctionSymbol.ifPresent(fs -> absMap.put(dbTermType, fs));
        return dbFunctionSymbol;
    }

    @Override
    public Optional<DBFunctionSymbol> getCeil(DBTermType dbTermType) {
        DBFunctionSymbol existingFunctionSymbol = ceilMap.get(dbTermType);
        if (existingFunctionSymbol != null)
            return Optional.of(existingFunctionSymbol);
        Optional<DBFunctionSymbol> dbFunctionSymbol = createCeilFunctionSymbol(dbTermType);
        dbFunctionSymbol.ifPresent(fs -> ceilMap.put(dbTermType, fs));
        return dbFunctionSymbol;
    }

    @Override
    public Optional<DBFunctionSymbol> getFloor(DBTermType dbTermType) {
        DBFunctionSymbol existingFunctionSymbol = floorMap.get(dbTermType);
        if (existingFunctionSymbol != null)
            return Optional.of(existingFunctionSymbol);
        Optional<DBFunctionSymbol> dbFunctionSymbol = createFloorFunctionSymbol(dbTermType);
        dbFunctionSymbol.ifPresent(fs -> floorMap.put(dbTermType, fs));
        return dbFunctionSymbol;
    }

    @Override
    public Optional<DBFunctionSymbol> getRound(DBTermType dbTermType) {
        DBFunctionSymbol existingFunctionSymbol = roundMap.get(dbTermType);
        if (existingFunctionSymbol != null)
            return Optional.of(existingFunctionSymbol);
        Optional<DBFunctionSymbol> dbFunctionSymbol = createRoundFunctionSymbol(dbTermType);
        dbFunctionSymbol.ifPresent(fs -> roundMap.put(dbTermType, fs));
        return dbFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBYearFromDatetime() {
        return yearFromDatetimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBYearFromDate() {
        return yearFromDateFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMonthFromDatetime() {
        return monthFromDatetimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMonthFromDate() {
        return monthFromDateFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBDayFromDatetime() {
        return dayFromDatetimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBDayFromDate() {
        return dayFromDateFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBHours() {
        return hoursFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBWeek() {
        return weekFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBQuarter() {
        return quarterFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBCentury() {
        return centuryFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBDecade() {
        return decadeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMillennium() {
        return millenniumFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMilliseconds() {
        return millisecondsFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMicroseconds() {
        return microsecondsFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBDateTrunc(String datePart) {
        return dateTruncFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMinutes() {
        return minutesFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSeconds() {
        return secondsFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBTz() {
        return tzFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getExtractFunctionSymbol(String component) {
        return extractFunctionSymbolsMap.computeIfAbsent(component, this::createExtractFunctionSymbol);
    }

    @Override
    public DBFunctionSymbol getCurrentDateTimeSymbol(String type) {
        return currentDateTimeFunctionSymbolsMap.computeIfAbsent(type, this::createCurrentDateTimeFunctionSymbol);
    }

    /**
     * Time extension - duration arithmetic
     */
    @Override
    public DBFunctionSymbol getDBWeeksBetweenFromDateTime() { return weeksBetweenFromDateTimeFunctionSymbol; }

    @Override
    public DBFunctionSymbol getDBWeeksBetweenFromDate() { return weeksBetweenFromDateFunctionSymbol; }

    @Override
    public DBFunctionSymbol getDBDaysBetweenFromDateTime() {
        return daysBetweenFromDateTimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBDaysBetweenFromDate() {
        return daysBetweenFromDateFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBHoursBetweenFromDateTime() {
        return hoursBetweenFromDateTimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMinutesBetweenFromDateTime() {
        return minutesBetweenFromDateTimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSecondsBetweenFromDateTime() {
        return secondsBetweenFromDateTimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMillisBetweenFromDateTime() {
        return millisBetweenFromDateTimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getTypedNullFunctionSymbol(DBTermType termType) {
        return typeNullMap
                .computeIfAbsent(termType, this::createTypeNullFunctionSymbol);
    }

    @Override
    public DBFunctionSymbol getDBRowUniqueStr() {
        return rowUniqueStrFct;
    }

    @Override
    public DBFunctionSymbol getDBRowNumber() {
        return rowNumberFct;
    }

    @Override
    public DBFunctionSymbol getDBRowNumberWithOrderBy() {
        return rowNumberWithOrderByFct;
    }

    @Override
    public DBFunctionSymbol getDBIriStringResolver(IRI baseIRI) {
        return iriStringResolverMap.computeIfAbsent(baseIRI, this::createDBIriStringResolver);
    }

    @Override
    public DBFunctionSymbol getDBCount(int arity, boolean isDistinct) {
        if (arity > 1) {
            throw new IllegalArgumentException("COUNT is 0-ary or unary");
        }
        return countTable.get(arity, isDistinct);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBSum(DBTermType dbType, boolean isDistinct) {
        Function<DBTermType, DBFunctionSymbol> creationFct = t -> createDBSum(dbType, isDistinct);

        return isDistinct
                ? distinctSumMap.computeIfAbsent(dbType, creationFct)
                : regularSumMap.computeIfAbsent(dbType, creationFct);
    }

    /**
     * By default, we assume that the DB sum complies to the semantics of a null-ignoring sum.
     */
    @Override
    public DBFunctionSymbol getDBSum(DBTermType dbType, boolean isDistinct) {
        return getNullIgnoringDBSum(dbType, isDistinct);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBAvg(DBTermType dbType, boolean isDistinct) {
        Function<DBTermType, DBFunctionSymbol> creationFct = t -> createDBAvg(dbType, isDistinct);

        return isDistinct
                ? distinctAvgMap.computeIfAbsent(dbType, creationFct)
                : regularAvgMap.computeIfAbsent(dbType, creationFct);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBStdev(DBTermType dbType, boolean isPop, boolean isDistinct) {
        Function<Map.Entry<DBTermType, Boolean>, DBFunctionSymbol> creationFct = entry -> createDBStdev(dbType, isPop, isDistinct);

        return isDistinct
                ? distinctStdevMap.computeIfAbsent(Maps.immutableEntry(dbType, isPop), creationFct)
                : regularStdevMap.computeIfAbsent(Maps.immutableEntry(dbType, isPop), creationFct);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBVariance(DBTermType dbType, boolean isPop, boolean isDistinct) {
        Function<Map.Entry<DBTermType, Boolean>, DBFunctionSymbol> creationFct = entry -> createDBVariance(dbType, isPop, isDistinct);

        return isDistinct
                ? distinctVarianceMap.computeIfAbsent(Maps.immutableEntry(dbType, isPop), creationFct)
                : regularVarianceMap.computeIfAbsent(Maps.immutableEntry(dbType, isPop), creationFct);
    }

    @Override
    public DBFunctionSymbol getDBMin(DBTermType dbType) {
        return minMap.computeIfAbsent(dbType, this::createDBMin);
    }

    @Override
    public DBFunctionSymbol getDBMax(DBTermType dbType) {
        return maxMap.computeIfAbsent(dbType, this::createDBMax);
    }

    @Override
    public DBFunctionSymbol getDBSample(DBTermType dbType) {
        return sampleMap.computeIfAbsent(dbType, this::createDBSample);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBGroupConcat(boolean isDistinct) {
        return isDistinct ? distinctGroupConcat : nonDistinctGroupConcat;
    }

    @Override
    public DBFunctionSymbol getDBIntIndex(int nbValues) {
        // TODO: cache it
        return new DBIntIndexFunctionSymbolImpl(dbIntegerType, rootDBType, nbValues);
    }

    @Override
    public DBFunctionSymbol getDBJsonElt(ImmutableList<String> path) {
            throw new UnsupportedOperationException("Json support unavailable for this DBMS");
    }

    @Override
    public DBFunctionSymbol getDBJsonEltAsText(ImmutableList<String> path) {
            throw new UnsupportedOperationException("Json support unavailable for this DBMS");
    }

    @Override
    public DBBooleanFunctionSymbol getDBJsonIsScalar(DBTermType dbType) {
        return jsonIsScalarMap.computeIfAbsent(dbType, this::createJsonIsScalar);
    }

    @Override
    public DBBooleanFunctionSymbol getDBJsonIsNumber(DBTermType dbType) {
        return jsonIsNumberMap.computeIfAbsent(dbType, this::createJsonIsNumber);
    }

    @Override
    public DBBooleanFunctionSymbol getDBJsonIsBoolean(DBTermType dbType) {
        return jsonIsBooleanMap.computeIfAbsent(dbType, this::createJsonIsBoolean);
    }

    @Override
    public DBBooleanFunctionSymbol getDBIsArray(DBTermType dbType) {
        return isArrayMap.computeIfAbsent(dbType, this::createIsArray);
    }

    @Override
    public IRISafenessDeclarationFunctionSymbol getIRISafenessDeclaration() {
        return iriSafenessDeclarationFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol checkAndConvertBoolean() { return checkAndConvertBooleanFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertBooleanFromString() { return checkAndConvertBooleanFromStringFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertDouble() { return checkAndConvertDoubleFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertFloat() { return checkAndConvertFloatFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertFloatFromBoolean() { return checkAndConvertFloatFromBooleanFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertFloatFromDouble() { return checkAndConvertFloatFromDoubleFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertFloatFromNonFPNumeric() { return checkAndConvertFloatFromNonFPNumericFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertDecimal() { return checkAndConvertDecimalFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertDecimalFromBoolean() { return checkAndConvertDecimalFromBooleanFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertInteger() { return checkAndConvertIntegerFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertIntegerFromBoolean() { return checkAndConvertIntegerFromBooleanFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertStringFromDecimal() { return checkAndConvertStringFromDecimalFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertDateTimeFromDate() { return checkAndConvertDateTimeFromDateFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertDateTimeFromString() { return checkAndConvertDateTimeFromStringFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertDateFromDatetime() { return checkAndConvertDateFromDateTimeFunctionSymbol; }

    @Override
    public DBFunctionSymbol checkAndConvertDateFromString() { return checkAndConvertDateFromStringFunctionSymbol; }

    @Override
    public DBFunctionSymbol getDBArrayAccess() {
        throw new UnsupportedOperationException("Array support unavailable for this DBMS");
    }

    protected abstract DBFunctionSymbol createDBCount(boolean isUnary, boolean isDistinct);
    protected abstract DBFunctionSymbol createDBSum(DBTermType termType, boolean isDistinct);
    protected abstract DBFunctionSymbol createDBAvg(DBTermType termType, boolean isDistinct);
    protected abstract DBFunctionSymbol createDBStdev(DBTermType termType, boolean isPop, boolean isDistinct);
    protected abstract DBFunctionSymbol createDBVariance(DBTermType termType, boolean isPop, boolean isDistinct);
    protected abstract DBFunctionSymbol createDBMin(DBTermType termType);
    protected abstract DBFunctionSymbol createDBMax(DBTermType termType);

    protected abstract DBFunctionSymbol createDBSample(DBTermType termType);

    protected DBFunctionSymbol createDBGroupConcat(DBTermType dbStringType, boolean isDistinct) {
        return new NullIgnoringDBGroupConcatFunctionSymbol(dbStringType, isDistinct,
                (terms, termConverter, termFactory) -> String.format(
                        "LISTAGG(%s%s,%s) WITHIN GROUP (ORDER BY %s)",
                        isDistinct ? "DISTINCT " : "",
                        termConverter.apply(terms.get(0)),
                        termConverter.apply(terms.get(1)),
                        termConverter.apply(terms.get(0))
                ));
    }

    protected abstract DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType);
    protected abstract DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType);
    protected abstract DBTypeConversionFunctionSymbol createHexBinaryNormFunctionSymbol(DBTermType binaryType);
    protected abstract DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType);
    protected abstract DBTypeConversionFunctionSymbol createBooleanDenormFunctionSymbol();
    protected abstract DBTypeConversionFunctionSymbol createGeometryNormFunctionSymbol(DBTermType geoType);
    protected abstract DBTypeConversionFunctionSymbol createHexBinaryDenormFunctionSymbol(DBTermType binaryType);

    protected DBBooleanFunctionSymbol createLikeFunctionSymbol() {
        return new DBLikeFunctionSymbolImpl(dbBooleanType, rootDBType);
    }

    protected DBBooleanFunctionSymbol createSimilarToFunctionSymbol() {
        return new DBSimilarToFunctionSymbolImpl(dbBooleanType, rootDBType);
    }

    protected DBIfElseNullFunctionSymbol createRegularIfElseNull() {
        return new DefaultDBIfElseNullFunctionSymbol(dbBooleanType, rootDBType);
    }

    protected DBBooleanFunctionSymbol createStrStartsFunctionSymbol() {
        return new DefaultDBStrStartsWithFunctionSymbol(rootDBType, dbBooleanType);
    }

    protected DBBooleanFunctionSymbol createStrEndsFunctionSymbol() {
        return new DefaultDBStrEndsWithFunctionSymbol(
                rootDBType, dbBooleanType);
    }

    /**
     * Can be overridden
     */
    protected DBMathBinaryOperator createDBBinaryMathOperator(String dbMathOperatorName, DBTermType dbNumericType)
        throws UnsupportedOperationException {
        switch (dbMathOperatorName) {
            case SPARQL.NUMERIC_MULTIPLY:
                return createMultiplyOperator(dbNumericType);
            case SPARQL.NUMERIC_DIVIDE:
                return createDivideOperator(dbNumericType);
            case SPARQL.NUMERIC_ADD:
                return createAddOperator(dbNumericType);
            case SPARQL.NUMERIC_SUBTRACT:
                return createSubtractOperator(dbNumericType);
            default:
                throw new UnsupportedOperationException("The math operator " + dbMathOperatorName + " is not supported");
        }
    }

    protected DBMathBinaryOperator createDBBinaryMathOperator(String dbMathOperatorName, DBTermType arg1Type, DBTermType arg2Type)
            throws UnsupportedOperationException {
        DBTermType inferOutputType = inferOutputTypeMathOperator(dbMathOperatorName, arg1Type, arg2Type);
        return createDBBinaryMathOperator(dbMathOperatorName, inferOutputType);
    }

    protected DBTermType inferOutputTypeMathOperator(String dbMathOperatorName, DBTermType arg1Type, DBTermType arg2Type) {
        if (arg1Type.getCategory().equals(arg2Type.getCategory()))
            return arg1Type;
        switch (arg1Type.getCategory()) {
            case INTEGER:
                return arg2Type;
            case FLOAT_DOUBLE:
                return arg2Type.getCategory() == DBTermType.Category.INTEGER ? arg1Type : arg2Type;
            default:
                return arg1Type;
        }
    }

    protected DBMathBinaryOperator createUntypedDBBinaryMathOperator(String dbMathOperatorName) throws UnsupportedOperationException {
        switch (dbMathOperatorName) {
            case SPARQL.NUMERIC_MULTIPLY:
                return createUntypedMultiplyOperator();
            case SPARQL.NUMERIC_DIVIDE:
                return createUntypedDivideOperator();
            case SPARQL.NUMERIC_ADD:
                return createUntypedAddOperator();
            case SPARQL.NUMERIC_SUBTRACT:
                return createUntypedSubtractOperator();
            default:
                throw new UnsupportedOperationException("The untyped math operator " + dbMathOperatorName + " is not supported");
        }
    }

    protected DBBooleanFunctionSymbol createContainsFunctionSymbol() {
        return new DBContainsFunctionSymbolImpl(rootDBType, dbBooleanType, this::serializeContains);
    }

    protected DBFunctionSymbol createStrBeforeFunctionSymbol() {
        return new DBStrBeforeFunctionSymbolImpl(dbStringType, rootDBType, this::serializeStrBefore);
    }

    protected DBFunctionSymbol createStrAfterFunctionSymbol() {
        return new DBStrAfterFunctionSymbolImpl(dbStringType, rootDBType, this::serializeStrAfter);
    }

    protected FalseOrNullFunctionSymbol createFalseOrNullFunctionSymbol(int arity) {
        return new FalseOrNullFunctionSymbolImpl(arity, dbBooleanType);
    }

    protected TrueOrNullFunctionSymbol createTrueOrNullFunctionSymbol(int arity) {
        return new TrueOrNullFunctionSymbolImpl(arity, dbBooleanType);
    }

    protected DBFunctionSymbol createMD5FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_MD5", rootDBType, dbStringType, this::serializeMD5);
    }

    protected DBFunctionSymbol createSHA1FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_SHA1", rootDBType, dbStringType, this::serializeSHA1);
    }

    protected DBFunctionSymbol createSHA256FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_SHA256", rootDBType, dbStringType, this::serializeSHA256);
    }

    protected DBFunctionSymbol createSHA384FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_SHA384", rootDBType, dbStringType, this::serializeSHA384);
    }

    protected DBFunctionSymbol createSHA512FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_SHA512", rootDBType, dbStringType, this::serializeSHA512);
    }

    protected DBFunctionSymbol createYearFromDatetimeFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_YEAR_FROM_DATETIME", rootDBType, dbIntegerType, false,
                this::serializeYearFromDatetime);
    }

    protected DBFunctionSymbol createYearFromDateFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_YEAR_FROM_DATE", rootDBType, dbIntegerType, false,
                this::serializeYearFromDate);
    }

    protected DBFunctionSymbol createMonthFromDatetimeFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MONTH_FROM_DATETIME", rootDBType, dbIntegerType, false,
                this::serializeMonthFromDatetime);
    }

    protected DBFunctionSymbol createMonthFromDateFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MONTH_FROM_DATE", rootDBType, dbIntegerType, false,
                this::serializeMonthFromDate);
    }

    protected DBFunctionSymbol createDayFromDatetimeFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_DAY_FROM_DATETIME", rootDBType, dbIntegerType, false,
                this::serializeDayFromDatetime);
    }

    protected DBFunctionSymbol createDayFromDateFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_DAY_FROM_DATE", rootDBType, dbIntegerType, false,
                this::serializeDayFromDate);
    }

    protected DBFunctionSymbol createHoursFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_HOURS", rootDBType, dbIntegerType, false,
                this::serializeHours);
    }

    protected DBFunctionSymbol createMinutesFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MINUTES", rootDBType, dbIntegerType, false,
                this::serializeMinutes);
    }

    protected DBFunctionSymbol createWeekFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_WEEK", rootDBType, dbIntegerType, false,
                this::serializeWeek);
    }

    protected DBFunctionSymbol createQuarterFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_QUARTER", rootDBType, dbIntegerType, false,
                this::serializeQuarter);
    }

    protected DBFunctionSymbol createDecadeFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_DECADE", rootDBType, dbIntegerType, false,
                this::serializeDecade);
    }

    protected DBFunctionSymbol createCenturyFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_CENTURY", rootDBType, dbIntegerType, false,
                this::serializeCentury);
    }

    protected DBFunctionSymbol createMillenniumFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MILLENNIUM", rootDBType, dbIntegerType, false,
                this::serializeMillennium);
    }

    protected DBFunctionSymbol createMillisecondsFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MILLISECONDS", rootDBType, dbDecimalType, false,
                this::serializeMilliseconds);
    }

    protected DBFunctionSymbol createMicrosecondsFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MICROSECONDS", rootDBType, dbIntegerType, false,
                this::serializeMicroseconds);
    }

    protected DBFunctionSymbol createDateTruncFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_DATE_TRUNC", ImmutableList.of(dbStringType, dbDateType), dbDateTimestampType, false,
                this::serializeDateTrunc) {
            @Override
            protected boolean mayReturnNullWithoutNullArguments() {
                return false;
            }
        };
    }

    protected DBFunctionSymbol createSecondsFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_SECONDS", rootDBType, dbDecimalType, false,
                this::serializeSeconds);
    }

    protected DBFunctionSymbol createTzFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_TZ", rootDBType, dbStringType, false,
                this::serializeTz);
    }

    protected DBFunctionSymbol createExtractFunctionSymbol(String component) {
        return new UnaryDBFunctionSymbolWithSerializerImpl("EXTRACT_" + component, rootDBType, rootDBType, false,
                (t, c, f) -> serializeExtract(component, t, c, f));
    }

    protected DBFunctionSymbol createCurrentDateTimeFunctionSymbol(String type) {
        return new DBFunctionSymbolWithSerializerImpl("CURRENT_" + type, ImmutableList.of(), rootDBType, false,
                (t, c, f) -> serializeCurrentDateTime(type, t, c, f));
    }

    protected DBFunctionSymbol createDBRowUniqueStr() {
        return new DBFunctionSymbolWithSerializerImpl("ROW_UNIQUE_STR", ImmutableList.of(), dbStringType, true,
                (t, c, f) -> serializeDBRowUniqueStr(c, f)) {
            @Override
            public boolean isDeterministic() {
                return false;
            }
        };
    }

    protected DBFunctionSymbol createDBRowNumber() {
        return new DBFunctionSymbolWithSerializerImpl("ROW_NUMBER", ImmutableList.of(), dbIntegerType, true,
                (t, c, f) -> serializeDBRowNumber(c, f));
    }

    protected DBFunctionSymbol createDBRowNumberWithOrderBy() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("ROW_NUMBER_WITH_ORDERBY", rootDBType, dbIntegerType, true,
                this::serializeDBRowNumberWithOrderBy);
    }


    protected abstract DBMathBinaryOperator createMultiplyOperator(DBTermType dbNumericType);
    protected abstract DBMathBinaryOperator createDivideOperator(DBTermType dbNumericType);
    protected abstract DBMathBinaryOperator createAddOperator(DBTermType dbNumericType) ;
    protected abstract DBMathBinaryOperator createSubtractOperator(DBTermType dbNumericType);

    protected abstract DBMathBinaryOperator createUntypedMultiplyOperator();
    protected abstract DBMathBinaryOperator createUntypedDivideOperator();
    protected abstract DBMathBinaryOperator createUntypedAddOperator();
    protected abstract DBMathBinaryOperator createUntypedSubtractOperator();

    protected abstract DBBooleanFunctionSymbol createNonStrictNumericEquality();
    protected abstract DBBooleanFunctionSymbol createNonStrictStringEquality();
    protected abstract DBBooleanFunctionSymbol createNonStrictDatetimeEquality();
    protected abstract DBBooleanFunctionSymbol createNonStrictDateEquality();
    protected abstract DBBooleanFunctionSymbol createNonStrictDefaultEquality();

    protected abstract DBBooleanFunctionSymbol createNumericInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createBooleanInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createStringInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createDatetimeInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createDateInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createDefaultInequality(InequalityLabel inequalityLabel);

    /**
     * Can be overridden
     */
    protected String canonicalizeRegularFunctionSymbolName(String nameInDialect) {
        return nameInDialect.toUpperCase();
    }

    protected abstract DBFunctionSymbol createRegularUntypedFunctionSymbol(String nameInDialect, int arity);

    protected abstract DBBooleanFunctionSymbol createRegularBooleanFunctionSymbol(String nameInDialect, int arity);

    protected abstract DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType);

    protected abstract DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType,
                                                                                     DBTermType targetType);

    protected abstract DBFunctionSymbol createDBCase(int arity, boolean doOrderingMatter);
    protected abstract DBBooleanFunctionSymbol createDBBooleanCase(int arity, boolean doOrderingMatter);

    protected abstract DBFunctionSymbol createCoalesceFunctionSymbol(int arity);
    protected abstract DBBooleanFunctionSymbol createBooleanCoalesceFunctionSymbol(int arity);

    protected DBBooleanFunctionSymbol createDBBooleanIfElseNull() {
        return new BooleanDBIfElseNullFunctionSymbolImpl(dbBooleanType);
    }

    protected abstract DBStrictEqFunctionSymbol createDBStrictEquality(int arity);

    protected abstract DBBooleanFunctionSymbol createDBStrictNEquality(int arity);

    protected abstract DBNotFunctionSymbol createDBNotFunctionSymbol(DBTermType dbBooleanType);

    protected abstract DBFunctionSymbol createEncodeURLorIRI(boolean preserveInternationalChars);

    protected abstract Optional<DBFunctionSymbol> createAbsFunctionSymbol(DBTermType dbTermType);
    protected abstract Optional<DBFunctionSymbol> createCeilFunctionSymbol(DBTermType dbTermType);
    protected abstract Optional<DBFunctionSymbol> createFloorFunctionSymbol(DBTermType dbTermType);
    protected abstract Optional<DBFunctionSymbol> createRoundFunctionSymbol(DBTermType dbTermType);

    protected DBFunctionSymbol createTypeNullFunctionSymbol(DBTermType termType) {
        return new SimplifiableTypedNullFunctionSymbol(termType);
    }

    protected DBFunctionSymbol createDBIriStringResolver(IRI baseIRI) {
        return new DBIriStringResolverFunctionSymbolImpl(baseIRI, "^[a-zA-Z]+:", rootDBType, dbStringType);
    }

    protected abstract String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter,
                                     TermFactory termFactory);

    protected abstract String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                                 Function<ImmutableTerm, String> termConverter,
                                                 TermFactory termFactory);

    protected abstract String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                                 Function<ImmutableTerm, String> termConverter,
                                                 TermFactory termFactory);

    protected abstract String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter,
                                           TermFactory termFactory);

    protected abstract String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter,
                                           TermFactory termFactory);

    protected abstract String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeSHA384(ImmutableList<? extends ImmutableTerm> terms,
                                              Function<ImmutableTerm, String> termConverter,
                                              TermFactory termFactory);

    protected abstract String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeYearFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeYearFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter,
                                                    TermFactory termFactory);

    protected abstract String serializeMonthFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeMonthFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                         Function<ImmutableTerm, String> termConverter,
                                                         TermFactory termFactory);

    protected abstract String serializeDayFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeDayFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                       Function<ImmutableTerm, String> termConverter,
                                                       TermFactory termFactory);

    protected abstract String serializeHours(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeMinutes(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeSeconds(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeWeek(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);

    protected abstract String serializeQuarter(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);

    protected abstract String serializeDecade(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);

    protected abstract String serializeCentury(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);

    protected abstract String serializeMillennium(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);

    protected abstract String serializeMilliseconds(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);

    protected abstract String serializeMicroseconds(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);

    protected abstract String serializeDateTrunc(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter,
                                                    TermFactory termFactory);

    protected abstract String serializeTz(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);

    protected String serializeExtract(String component,
                                               ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory) {
        return "EXTRACT(" + component + " FROM " + termConverter.apply(terms.get(0)) + ")";
    }

    protected String serializeCurrentDateTime(String type,
                                      ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter,
                                      TermFactory termFactory) {
        return "CURRENT_" + type;
    }

    /**
     * Time extension - duration arithmetic
     */

    protected abstract String serializeWeeksBetweenFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                                Function<ImmutableTerm, String> termConverter,
                                                                TermFactory termFactory);

    protected abstract String serializeWeeksBetweenFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter,
                                                            TermFactory termFactory);

    protected abstract String serializeDaysBetweenFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                               Function<ImmutableTerm, String> termConverter,
                                                               TermFactory termFactory);

    protected abstract String serializeDaysBetweenFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                           Function<ImmutableTerm, String> termConverter,
                                                           TermFactory termFactory);

    protected abstract String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter,
                                                    TermFactory termFactory);

    protected abstract String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                                      Function<ImmutableTerm, String> termConverter,
                                                      TermFactory termFactory);

    protected abstract String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                                      Function<ImmutableTerm, String> termConverter,
                                                      TermFactory termFactory);

    protected abstract String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter,
                                                     TermFactory termFactory);

    /**
     * Time extension - duration arithmetic
     */
    protected DBFunctionSymbol createWeeksBetweenFromDateTimeFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_WEEK_DIFF_FROM_DATETIME", ImmutableList.of(rootDBType, rootDBType), dbIntegerType, false,
                this::serializeWeeksBetweenFromDateTime);
    }

    protected DBFunctionSymbol createWeeksBetweenFromDateFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_WEEK_DIFF_FROM_DATETIME", ImmutableList.of(rootDBType, rootDBType), dbIntegerType, false,
                this::serializeWeeksBetweenFromDate);
    }

    protected DBFunctionSymbol createDaysBetweenFromDateTimeFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_DAY_DIFF_FROM_DATETIME", ImmutableList.of(rootDBType, rootDBType), dbIntegerType, false,
                this::serializeDaysBetweenFromDateTime);
    }

    protected DBFunctionSymbol createDaysBetweenFromDateFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_DAY_DIFF_FROM_DATETIME", ImmutableList.of(rootDBType, rootDBType), dbIntegerType, false,
                this::serializeDaysBetweenFromDate);
    }

    protected DBFunctionSymbol createHoursBetweenFromDateTimeFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_HOUR_DIFF_FROM_DATETIME", ImmutableList.of(rootDBType, rootDBType), dbIntegerType, false,
                this::serializeHoursBetween);
    }

    protected DBFunctionSymbol createMinutesBetweenFromDateTimeFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_MINUTE_DIFF_FROM_DATETIME", ImmutableList.of(rootDBType, rootDBType), dbIntegerType, false,
                this::serializeMinutesBetween);
    }

    protected DBFunctionSymbol createSecondsBetweenFromDateTimeFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_SECOND_DIFF_FROM_DATETIME", ImmutableList.of(rootDBType, rootDBType), dbIntegerType, false,
                this::serializeSecondsBetween);
    }

    protected DBFunctionSymbol createMillisBetweenFromDateTimeFunctionSymbol() {
        return new DBFunctionSymbolWithSerializerImpl("DB_MILLIS_DIFF_FROM_DATETIME", ImmutableList.of(rootDBType, rootDBType), dbIntegerType, false,
                this::serializeMillisBetween);
    }

    protected DBBooleanFunctionSymbol createIsArray(DBTermType dbType) {
        throw new UnsupportedOperationException("Unsupported nested datatype: " + dbType.getName());
    }

    protected DBBooleanFunctionSymbol createJsonIsNumber(DBTermType dbType) {
        throw new UnsupportedOperationException("Unsupported JSON-like datatype: " + dbType.getName());
    }

    protected DBBooleanFunctionSymbol createJsonIsBoolean(DBTermType dbType) {
        throw new UnsupportedOperationException("Unsupported JSON-like datatype: " + dbType.getName());
    }

    protected DBBooleanFunctionSymbol createJsonIsScalar(DBTermType dbType) {
        throw new UnsupportedOperationException("Unsupported JSON-like datatype: " + dbType.getName());
    }

    /**
     * SPARQL XSD cast functions
     */
    protected abstract String serializeCheckAndConvertBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter,
                                                              TermFactory termFactory);

    protected abstract String serializeCheckAndConvertBooleanFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                                        Function<ImmutableTerm, String> termConverter,
                                                                        TermFactory termFactory);

    protected abstract String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                             Function<ImmutableTerm, String> termConverter,
                                                             TermFactory termFactory);

    protected abstract String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter,
                                                            TermFactory termFactory);

    protected abstract String serializeCheckAndConvertFloatFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                                       Function<ImmutableTerm, String> termConverter,
                                                                       TermFactory termFactory);

    protected abstract String serializeCheckAndConvertFloatFromDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                                      Function<ImmutableTerm, String> termConverter,
                                                                      TermFactory termFactory);

    protected abstract String serializeCheckAndConvertFloatFromNonFPNumeric(ImmutableList<? extends ImmutableTerm> terms,
                                                                            Function<ImmutableTerm, String> termConverter,
                                                                            TermFactory termFactory);

    protected abstract String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter,
                                                              TermFactory termFactory);

    protected abstract String serializeCheckAndConvertDecimalFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                                         Function<ImmutableTerm, String> termConverter,
                                                                         TermFactory termFactory);

    protected abstract String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter,
                                                              TermFactory termFactory);

    protected abstract String serializeCheckAndConvertIntegerFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                                         Function<ImmutableTerm, String> termConverter,
                                                                         TermFactory termFactory);

    protected abstract String serializeCheckAndConvertStringFromDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                                        Function<ImmutableTerm, String> termConverter,
                                                                        TermFactory termFactory);

    protected abstract String serializeCheckAndConvertDateTimeFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                                       Function<ImmutableTerm, String> termConverter,
                                                                       TermFactory termFactory);

    protected abstract String serializeCheckAndConvertDateTimeFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                                         Function<ImmutableTerm, String> termConverter,
                                                                         TermFactory termFactory);

    protected abstract String serializeCheckAndConvertDateFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                                       Function<ImmutableTerm, String> termConverter,
                                                                       TermFactory termFactory);

    protected abstract String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                                     Function<ImmutableTerm, String> termConverter,
                                                                     TermFactory termFactory);

    protected DBFunctionSymbol createCheckAndConvertBooleanFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_BOOLEAN", rootDBType, dbBooleanType, false,
                this::serializeCheckAndConvertBoolean);
    }

    protected DBFunctionSymbol createCheckAndConvertBooleanFromStringFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_BOOLEAN_FROM_STRING", dbStringType, dbBooleanType, false,
                this::serializeCheckAndConvertBooleanFromString);
    }

    protected DBFunctionSymbol createCheckAndConvertDoubleFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_DOUBLE", rootDBType, dbDoubleType, false,
                this::serializeCheckAndConvertDouble);
    }

    protected DBFunctionSymbol createCheckAndConvertFloatFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_FLOAT", rootDBType, dbDoubleType, false,
                this::serializeCheckAndConvertFloat);
    }

    protected DBFunctionSymbol createCheckAndConvertFloatFromBooleanFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_FLOAT_FROM_BOOLEAN", dbBooleanType, dbDoubleType, false,
                this::serializeCheckAndConvertFloatFromBoolean);
    }

    protected DBFunctionSymbol createCheckAndConvertFloatFromDoubleFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_FLOAT_FROM_DOUBLE", dbDoubleType, dbDoubleType, false,
                this::serializeCheckAndConvertFloatFromDouble);
    }

    protected DBFunctionSymbol createCheckAndConvertFloatFromNonFPNumericFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_FLOAT_FROM_NONFPNUMERIC", rootDBType, dbDoubleType, false,
                this::serializeCheckAndConvertFloatFromNonFPNumeric);
    }

    protected DBFunctionSymbol createCheckAndConvertDecimalFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_DECIMAL", rootDBType, dbDecimalType, false,
                this::serializeCheckAndConvertDecimal);
    }

    protected DBFunctionSymbol createCheckAndConvertDecimalFromBooleanFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_DECIMAL_FROM_BOOLEAN", dbBooleanType, dbDecimalType, false,
                this::serializeCheckAndConvertDecimalFromBoolean);
    }

    protected DBFunctionSymbol createCheckAndConvertIntegerFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_INTEGER", rootDBType, dbIntegerType, false,
                this::serializeCheckAndConvertInteger);
    }

    protected DBFunctionSymbol createCheckAndConvertIntegerFromBooleanFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_INTEGER_FROM_BOOLEAN", dbBooleanType, dbIntegerType, false,
                this::serializeCheckAndConvertIntegerFromBoolean);
    }

    protected DBFunctionSymbol createCheckAndConvertStringFromDecimalFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_STRING_FROM_DECIMAL", dbDecimalType, dbStringType, false,
                this::serializeCheckAndConvertStringFromDecimal);
    }

    protected DBFunctionSymbol createCheckAndConvertDateTimeFromDateFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_DATETIME_FROM_DATE", dbDateType,
                dbDateTimestampType, false,
                this::serializeCheckAndConvertDateTimeFromDate);
    }

    protected DBFunctionSymbol createCheckAndConvertDateTimeFromStringFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_CHECK_AND_CONVERT_DATETIME_FROM_STRING", dbStringType,
                dbDateTimestampType, false,
                this::serializeCheckAndConvertDateTimeFromString);
    }

    protected DBFunctionSymbol createCheckAndConvertDateFromDateTimeFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_DATE_FROM_DATETIME", dbDateTimestampType, dbDateType, false,
                this::serializeCheckAndConvertDateFromDateTime);
    }

    protected DBFunctionSymbol createCheckAndConvertDateFromStringFunctionSymbol() {
        return new UnaryCastDBFunctionSymbolWithSerializerImpl("DB_DATE_FROM_STRING", dbStringType, dbDateType, false,
                this::serializeCheckAndConvertDateFromString);
    }

    /**
     * By default, uses the row number
     */
    protected String serializeDBRowUniqueStr(Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableFunctionalTerm newTerm = termFactory.getDBCastFunctionalTerm(dbStringType,
                termFactory.getImmutableFunctionalTerm(getDBRowNumber()));

        return termConverter.apply(newTerm);
    }

    protected abstract String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory);

    protected abstract String serializeDBRowNumberWithOrderBy(ImmutableList<? extends ImmutableTerm> terms,
                                                                Function<ImmutableTerm, String> converter,
                                                              TermFactory termFactory);



    @Override
    public DBTypeConversionFunctionSymbol getConversion2RDFLexicalFunctionSymbol(DBTermType inputType, RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .flatMap(t -> Optional.ofNullable(normalizationTable.get(inputType, t)))
                .orElseGet(() -> Optional.ofNullable(normalizationMap.get(inputType))
                        // Fallback to simple cast
                        .orElseGet(() -> getDBCastFunctionSymbol(inputType, dbStringType)));
    }

    @Override
    public DBTypeConversionFunctionSymbol getConversionFromRDFLexical2DBFunctionSymbol(DBTermType targetDBType,
                                                                                       RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .flatMap(t -> Optional.ofNullable(deNormalizationTable.get(targetDBType, t)))
                .orElseGet(() -> getConversionFromRDFLexical2DBFunctionSymbol(targetDBType));
    }

    @Override
    public DBTypeConversionFunctionSymbol getConversionFromRDFLexical2DBFunctionSymbol(DBTermType targetDBType) {
        return Optional.ofNullable(deNormalizationMap.get(targetDBType))
                // Fallback to simple cast
                .orElseGet(() -> getDBCastFunctionSymbol(dbStringType, targetDBType));
    }

}
