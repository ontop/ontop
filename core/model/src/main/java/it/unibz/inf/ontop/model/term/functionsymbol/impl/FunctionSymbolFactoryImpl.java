package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.*;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.ofn.OfnMultitypedInputBinarySPARQLFunctionSymbolImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.ofn.OfnSimpleBinarySPARQLFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.*;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class FunctionSymbolFactoryImpl implements FunctionSymbolFactory {

    private final TypeFactory typeFactory;
    private final RDFTermFunctionSymbol rdfTermFunctionSymbol;
    private final BooleanFunctionSymbol areCompatibleRDFStringFunctionSymbol;
    private final BooleanFunctionSymbol lexicalNonStrictEqualityFunctionSymbol;
    private final NotYetTypedEqualityFunctionSymbol notYetTypedEqualityFunctionSymbol;
    private final BooleanFunctionSymbol lexicalEBVFunctionSymbol;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;

    private final Map<Integer, FunctionSymbol> commonDenominatorMap;
    private final Map<Integer, SPARQLFunctionSymbol> concatMap;
    private final Map<Integer, SPARQLFunctionSymbol> coalesceMap;
    private final Map<String, SPARQLAggregationFunctionSymbol> distinctSparqlGroupConcatMap;
    private final Map<String, SPARQLAggregationFunctionSymbol> nonDistinctSparqlGroupConcatMap;
    // TODO: use a cache with a limited budget
    private final Map<IRI, SPARQLFunctionSymbol> sparqlIRIMap;
    private final Map<RDFTermType, BooleanFunctionSymbol> isAMap;
    private final Map<InequalityLabel, BooleanFunctionSymbol> lexicalInequalityFunctionSymbolMap;
    private final BooleanFunctionSymbol rdf2DBBooleanFunctionSymbol;
    private final FunctionSymbol langTypeFunctionSymbol;
    private final FunctionSymbol rdfDatatypeFunctionSymbol;
    private final BooleanFunctionSymbol lexicalLangMatchesFunctionSymbol;
    private final FunctionSymbol commonNumericTypeFunctionSymbol;
    private final FunctionSymbol EBVSPARQLLikeFunctionSymbol;
    private final FunctionSymbol extractLexicalTermFunctionSymbol;

    private final MetaRDFTermType metaRDFType;
    private final DBTermType dbBooleanType;
    private final DBTermType dbStringType;
    private final SPARQLFunctionSymbol iriNoBaseFunctionSymbol;

    /**
     * Created in init()
     */
    private ImmutableTable<String, Integer, SPARQLFunctionSymbol> regularSparqlFunctionTable;
    /**
     * Created in init()
     */
    private ImmutableTable<String, Integer, SPARQLFunctionSymbol> distinctSparqlAggregateFunctionTable;



    @Inject
    private FunctionSymbolFactoryImpl(TypeFactory typeFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory) {
        this.typeFactory = typeFactory;
        this.rdfTermFunctionSymbol = new RDFTermFunctionSymbolImpl(
                typeFactory.getDBTypeFactory().getDBStringType(),
                typeFactory.getMetaRDFTermType());
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        this.dbStringType = dbTypeFactory.getDBStringType();

        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.metaRDFType = typeFactory.getMetaRDFTermType();

        this.commonDenominatorMap = new ConcurrentHashMap<>();
        this.concatMap = new ConcurrentHashMap<>();
        this.coalesceMap = new ConcurrentHashMap<>();
        this.distinctSparqlGroupConcatMap = new ConcurrentHashMap<>();
        this.nonDistinctSparqlGroupConcatMap = new ConcurrentHashMap<>();
        this.sparqlIRIMap = new ConcurrentHashMap<>();
        this.isAMap = new ConcurrentHashMap<>();
        this.lexicalInequalityFunctionSymbolMap = new ConcurrentHashMap<>();
        this.areCompatibleRDFStringFunctionSymbol = new AreCompatibleRDFStringFunctionSymbolImpl(metaRDFType, dbBooleanType);
        rdf2DBBooleanFunctionSymbol = new RDF2DBBooleanFunctionSymbolImpl(typeFactory.getXsdBooleanDatatype(),
                dbBooleanType, dbStringType);
        this.lexicalNonStrictEqualityFunctionSymbol = new LexicalNonStrictEqualityFunctionSymbolImpl(metaRDFType,
                typeFactory.getXsdBooleanDatatype(), typeFactory.getXsdDatetimeDatatype(), typeFactory.getXsdStringDatatype(),
                dbStringType, dbBooleanType, typeFactory.getDatatype(XSD.DATETIMESTAMP), typeFactory.getDatatype(XSD.DATE));
        this.langTypeFunctionSymbol = new LangTagFunctionSymbolImpl(metaRDFType, dbStringType);
        this.rdfDatatypeFunctionSymbol = new RDFDatatypeStringFunctionSymbolImpl(metaRDFType, dbStringType);
        this.lexicalLangMatchesFunctionSymbol = new LexicalLangMatchesFunctionSymbolImpl(dbStringType, dbBooleanType);
        this.commonNumericTypeFunctionSymbol = new CommonPropagatedOrSubstitutedNumericTypeFunctionSymbolImpl(metaRDFType);
        this.EBVSPARQLLikeFunctionSymbol = new EBVSPARQLLikeFunctionSymbolImpl(typeFactory.getAbstractRDFSLiteral(), typeFactory.getXsdBooleanDatatype());
        this.lexicalEBVFunctionSymbol = new LexicalEBVFunctionSymbolImpl(dbStringType, metaRDFType, dbBooleanType);
        this.notYetTypedEqualityFunctionSymbol = new NotYetTypedEqualityFunctionSymbolImpl(
                dbTypeFactory.getAbstractRootDBType(), dbBooleanType);

        this.iriNoBaseFunctionSymbol = new IriSPARQLFunctionSymbolImpl(typeFactory.getAbstractRDFTermType(),
                typeFactory.getXsdStringDatatype(), typeFactory.getIRITermType());
        this.extractLexicalTermFunctionSymbol = new ExtractLexicalTermFunctionSymbolImpl(typeFactory.getAbstractRDFTermType(), dbStringType);
    }

    @Inject
    protected void init() {
        this.regularSparqlFunctionTable = createSPARQLFunctionSymbolTable();
        this.distinctSparqlAggregateFunctionTable = createDistinctSPARQLAggregationFunctionSymbolTable();
    }


    protected ImmutableTable<String, Integer, SPARQLFunctionSymbol> createSPARQLFunctionSymbolTable() {
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();
        RDFDatatype xsdDecimal = typeFactory.getXsdDecimalDatatype();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFDatatype xsdLong = typeFactory.getXsdLongDatatype();
        RDFDatatype xsdDouble = typeFactory.getXsdDoubleDatatype();
        RDFDatatype xsdFloat = typeFactory.getXsdFloatDatatype();
        RDFDatatype wktLiteral = typeFactory.getWktLiteralDatatype();
        RDFDatatype xsdAnyUri = typeFactory.getXsdAnyUri();
        RDFDatatype xsdAnySimpleType = typeFactory.getXsdAnyUri();
        RDFDatatype rdfsLiteral = typeFactory.getAbstractRDFSLiteral();
        RDFTermType abstractRDFType = typeFactory.getAbstractRDFTermType();
        ObjectRDFType bnodeType = typeFactory.getBlankNodeType();
        ObjectRDFType iriType = typeFactory.getIRITermType();
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDate = typeFactory.getXsdDate();
        RDFDatatype abstractNumericType = typeFactory.getAbstractOntopNumericDatatype();
        RDFDatatype dateOrDatetime = typeFactory.getAbstractOntopDateOrDatetimeDatatype();

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbBoolean = dbTypeFactory.getDBBooleanType();
        DBTermType dbInteger = dbTypeFactory.getDBLargeIntegerType();
        DBTermType dbTimestamp = dbTypeFactory.getDBDateTimestampType();
        DBTermType dbDate = dbTypeFactory.getDBDateType();

        ImmutableSet<SPARQLFunctionSymbol> functionSymbols = ImmutableSet.of(
                new UcaseSPARQLFunctionSymbolImpl(xsdString),
                new LcaseSPARQLFunctionSymbolImpl(xsdString),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_ENCODE_FOR_URI", XPathFunction.ENCODE_FOR_URI,
                        xsdString, xsdString, true,
                        TermFactory::getDBEncodeForURI),
                new StartsWithSPARQLFunctionSymbolImpl(xsdString, xsdBoolean),
                new EndsWithSPARQLFunctionSymbolImpl(xsdString, xsdBoolean),
                new ContainsSPARQLFunctionSymbolImpl(xsdString, xsdBoolean),
                new SubStr2SPARQLFunctionSymbolImpl(xsdString, xsdInteger),
                new SubStr3SPARQLFunctionSymbolImpl(xsdString, xsdInteger),
                new StrlenSPARQLFunctionSymbolImpl(xsdString, xsdInteger),
                new LangSPARQLFunctionSymbolImpl(rdfsLiteral, xsdString),
                new LangMatchesSPARQLFunctionSymbolImpl(xsdString, xsdBoolean),
                new StrSPARQLFunctionSymbolImpl(abstractRDFType, xsdString, bnodeType),
                new DatatypeSPARQLFunctionSymbolImpl(rdfsLiteral, iriType),
                new IsIRISPARQLFunctionSymbolImpl(iriType, abstractRDFType, xsdBoolean),
                new IsBlankSPARQLFunctionSymbolImpl(bnodeType, abstractRDFType, xsdBoolean),
                new IsLiteralSPARQLFunctionSymbolImpl(rdfsLiteral, abstractRDFType, xsdBoolean),
                new IsNumericSPARQLFunctionSymbolImpl(abstractNumericType, abstractRDFType, xsdBoolean),
                new ReplaceSPARQLFunctionSymbolImpl(3, xsdString),
                new ReplaceSPARQLFunctionSymbolImpl(4, xsdString),
                new RegexSPARQLFunctionSymbolImpl(2, xsdString, xsdBoolean),
                new RegexSPARQLFunctionSymbolImpl(3, xsdString, xsdBoolean),
                new StrBeforeSPARQLFunctionSymbolImpl(xsdString),
                new StrAfterSPARQLFunctionSymbolImpl(xsdString),
                new NotSPARQLFunctionSymbolImpl(xsdBoolean),
                new LogicalOrSPARQLFunctionSymbolImpl(xsdBoolean),
                new LogicalAndSPARQLFunctionSymbolImpl(xsdBoolean),
                new BoundSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean),
                new Md5SPARQLFunctionSymbolImpl(xsdString),
                new Sha1SPARQLFunctionSymbolImpl(xsdString),
                new Sha256SPARQLFunctionSymbolImpl(xsdString),
                new Sha384SPARQLFunctionSymbolImpl(xsdString),
                new Sha512SPARQLFunctionSymbolImpl(xsdString),
                new NumericBinarySPARQLFunctionSymbolImpl("SP_MULTIPLY", SPARQL.NUMERIC_MULTIPLY, abstractNumericType),
                new NumericBinarySPARQLFunctionSymbolImpl("SP_ADD", SPARQL.NUMERIC_ADD, abstractNumericType),
                new NumericBinarySPARQLFunctionSymbolImpl("SP_SUBSTRACT", SPARQL.NUMERIC_SUBTRACT, abstractNumericType),
                new DivideSPARQLFunctionSymbolImpl(abstractNumericType, xsdDecimal),
                new NonStrictEqSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean, dbBoolean),
                new LessThanSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean, dbBoolean),
                new GreaterThanSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean, dbBoolean),
                new SameTermSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean),
                new UnaryNumericSPARQLFunctionSymbolImpl("SP_ABS", XPathFunction.NUMERIC_ABS, abstractNumericType,
                        this.dbFunctionSymbolFactory::getAbs),
                new UnaryNumericSPARQLFunctionSymbolImpl("SP_CEIL", XPathFunction.NUMERIC_CEIL, abstractNumericType,
                        this.dbFunctionSymbolFactory::getCeil),
                new UnaryNumericSPARQLFunctionSymbolImpl("SP_FLOOR", XPathFunction.NUMERIC_FLOOR, abstractNumericType,
                        this.dbFunctionSymbolFactory::getFloor),
                new UnaryNumericSPARQLFunctionSymbolImpl("SP_ROUND", XPathFunction.NUMERIC_ROUND, abstractNumericType,
                        this.dbFunctionSymbolFactory::getRound),
                new MultitypedInputUnarySPARQLFunctionSymbolImpl("SP_YEAR", SPARQL.YEAR, dateOrDatetime,
                        xsdInteger, false, dbTypeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTimestamp))
                                return Optional.of(dbFunctionSymbolFactory.getDBYearFromDatetime());
                            else if (t.isA(dbDate))
                                return Optional.of(dbFunctionSymbolFactory.getDBYearFromDate());
                            else
                                return Optional.empty();
                        }),
                new MultitypedInputUnarySPARQLFunctionSymbolImpl("SP_MONTH", SPARQL.MONTH,
                        dateOrDatetime, xsdInteger, false, dbTypeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTimestamp))
                                return Optional.of(dbFunctionSymbolFactory.getDBMonthFromDatetime());
                            else if (t.isA(dbDate))
                                return Optional.of(dbFunctionSymbolFactory.getDBMonthFromDate());
                            else
                                return Optional.empty();
                        }),
                new MultitypedInputUnarySPARQLFunctionSymbolImpl("SP_DAY", SPARQL.DAY,
                        dateOrDatetime, xsdInteger, false, dbTypeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTimestamp))
                                return Optional.of(dbFunctionSymbolFactory.getDBDayFromDatetime());
                            else if (t.isA(dbDate))
                                return Optional.of(dbFunctionSymbolFactory.getDBDayFromDate());
                            else
                                return Optional.empty();
                        }),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_HOURS", XPathFunction.HOURS_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBHours),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_MINUTES", XPathFunction.MINUTES_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBMinutes),

                new SimpleUnarySPARQLFunctionSymbolImpl("SP_WEEK", Ontop.WEEK_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBWeek),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_QUARTER", Ontop.QUARTER_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBQuarter),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_DECADE", Ontop.DECADE_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBDecade),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_CENTURY", Ontop.CENTURY_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBCentury),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_MILLENNIUM", Ontop.MILLENNIUM_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBMillennium),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_MILLISECONDS", Ontop.MILLISECONDS_FROM_DATETIME,
                        xsdDatetime, xsdDecimal, false, TermFactory::getDBMilliseconds),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_MICROSECONDS", Ontop.MICROSECONDS_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBMicroseconds),

                new DateTruncSPARQLFunctionSymbolImpl(xsdDatetime,
                        xsdString, (t) -> dbFunctionSymbolFactory.getDBDateTrunc(t)),

                new SimpleUnarySPARQLFunctionSymbolImpl("SP_SECONDS", XPathFunction.SECONDS_FROM_DATETIME,
                        xsdDatetime, xsdDecimal, false, TermFactory::getDBSeconds),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_TZ", SPARQL.TZ,
                xsdDatetime, xsdString, false, TermFactory::getDBTz),
                new UnaryBnodeSPARQLFunctionSymbolImpl(xsdString, bnodeType),
                new NowSPARQLFunctionSymbolImpl(xsdDatetime),
                new IfSPARQLFunctionSymbolImpl(xsdBoolean, abstractRDFType),
                new CountSPARQLFunctionSymbolImpl(abstractRDFType, xsdInteger, false),
                new CountSPARQLFunctionSymbolImpl(xsdInteger, false),
                new SumSPARQLFunctionSymbolImpl(false, abstractRDFType),
                new MinOrMaxOrSampleSPARQLFunctionSymbolImpl(typeFactory, MinOrMaxOrSampleSPARQLFunctionSymbolImpl.MinMaxSampleType.MIN),
                new MinOrMaxOrSampleSPARQLFunctionSymbolImpl(typeFactory, MinOrMaxOrSampleSPARQLFunctionSymbolImpl.MinMaxSampleType.MAX),
                new AvgSPARQLFunctionSymbolImpl(abstractRDFType, false),
                new StdevSPARQLFunctionSymbolImpl(abstractRDFType, false, false),
                new StdevSPARQLFunctionSymbolImpl(abstractRDFType, true, false),
                new StdevSPARQLFunctionSymbolImpl(abstractRDFType, false),
                new VarianceSPARQLFunctionSymbolImpl(abstractRDFType, false, false),
                new VarianceSPARQLFunctionSymbolImpl(abstractRDFType, true, false),
                new VarianceSPARQLFunctionSymbolImpl(abstractRDFType, false),
                new MinOrMaxOrSampleSPARQLFunctionSymbolImpl(typeFactory, MinOrMaxOrSampleSPARQLFunctionSymbolImpl.MinMaxSampleType.SAMPLE),
                /*
                 * Geo SF relation Functions
                 */
                new GeofSfEqualsFunctionSymbolImpl(GEOF.SF_EQUALS, wktLiteral, xsdBoolean),
                new GeofSfDisjointFunctionSymbolImpl(GEOF.SF_DISJOINT, wktLiteral, xsdBoolean),
                new GeofSfIntersectsFunctionSymbolImpl(GEOF.SF_INTERSECTS, wktLiteral, xsdBoolean),
                new GeofSfTouchesFunctionSymbolImpl(GEOF.SF_TOUCHES, wktLiteral, xsdBoolean),
                new GeofSfCrossesFunctionSymbolImpl(GEOF.SF_CROSSES, wktLiteral, xsdBoolean),
                new GeofSfWithinFunctionSymbolImpl(GEOF.SF_WITHIN, wktLiteral, xsdBoolean),
                new GeofSfContainsFunctionSymbolImpl(GEOF.SF_CONTAINS, wktLiteral, xsdBoolean),
                new GeofSfOverlapsFunctionSymbolImpl(GEOF.SF_OVERLAPS, wktLiteral, xsdBoolean),

                /*
                 * Geo Egenhofer relation Functions
                 */
                new GeofEhEqualsFunctionSymbolImpl(GEOF.EH_EQUALS, wktLiteral, xsdBoolean),
                new GeofEhDisjointFunctionSymbolImpl(GEOF.EH_DISJOINT, wktLiteral, xsdBoolean),
                new GeofEhMeetFunctionSymbolImpl(GEOF.EH_MEET, wktLiteral, xsdBoolean),
                new GeofEhOverlapFunctionSymbolImpl(GEOF.EH_OVERLAP, wktLiteral, xsdBoolean),
                new GeofEhCoversFunctionSymbolImpl(GEOF.EH_COVERS, wktLiteral, xsdBoolean),
                new GeofEhCoveredByFunctionSymbolImpl(GEOF.EH_COVEREDBY, wktLiteral, xsdBoolean),
                new GeofEhInsideFunctionSymbolImpl(GEOF.EH_INSIDE, wktLiteral, xsdBoolean),
                new GeofEhContainsFunctionSymbolImpl(GEOF.EH_CONTAINS, wktLiteral, xsdBoolean),
                /*
                 * Geo Egenhofer Functions
                 */

                /*
                 * Geo RCC8 relation Functions
                 */
                new GeofRcc8EqFunctionSymbolImpl(GEOF.RCC8_EQ, wktLiteral, xsdBoolean),
                new GeofRcc8DcFunctionSymbolImpl(GEOF.RCC8_DC, wktLiteral, xsdBoolean),
                new GeofRcc8EcFunctionSymbolImpl(GEOF.RCC8_EC, wktLiteral, xsdBoolean),
                new GeofRcc8PoFunctionSymbolImpl(GEOF.RCC8_PO, wktLiteral, xsdBoolean),
                new GeofRcc8TppFunctionSymbolImpl(GEOF.RCC8_TPP, wktLiteral, xsdBoolean),
                new GeofRcc8TppiFunctionSymbolImpl(GEOF.RCC8_TPPI, wktLiteral, xsdBoolean),
                new GeofRcc8NtppFunctionSymbolImpl(GEOF.RCC8_NTPP, wktLiteral, xsdBoolean),
                new GeofRcc8NtppiFunctionSymbolImpl(GEOF.RCC8_NTPPI, wktLiteral, xsdBoolean),
                /*
                 * Geo RCC8 Functions
                 */

                /*
                 * Geo Non-Topological Functions
                 */
                new GeofDistanceFunctionSymbolImpl(GEOF.DISTANCE, wktLiteral, iriType, xsdDouble, this),
                new GeofBufferFunctionSymbolImpl(GEOF.BUFFER, wktLiteral, xsdDecimal, iriType),
                new GeofIntersectionFunctionSymbolImpl(GEOF.INTERSECTION, wktLiteral),
                new GeofBoundaryFunctionSymbolImpl(GEOF.BOUNDARY, wktLiteral),
                new GeofConvexHullFunctionSymbolImpl(GEOF.CONVEXHULL, wktLiteral),
                new GeofDifferenceFunctionSymbolImpl(GEOF.DIFFERENCE, wktLiteral),
                new GeofEnvelopeFunctionSymbolImpl(GEOF.ENVELOPE, wktLiteral),
                new GeofGetSRIDFunctionSymbolImpl(GEOF.GETSRID, wktLiteral, xsdAnyUri),
                new GeofSymDifferenceFunctionSymbolImpl(GEOF.SYMDIFFERENCE, wktLiteral, iriType),
                new GeofUnionFunctionSymbolImpl(GEOF.UNION, wktLiteral, iriType),
                new GeofRelateFunctionSymbolImpl(GEOF.RELATE, wktLiteral, xsdString, xsdBoolean),
                new GeofRelateMFunctionSymbolImpl(GEOF.RELATEM, wktLiteral, xsdString),

                /*
                 * Time extension - duration arithmetic
                 */
                new OfnMultitypedInputBinarySPARQLFunctionSymbolImpl("OFN_WEEKS_BETWEEN", OFN.WEEKS_BETWEEN,
                        dateOrDatetime, xsdLong, false, dbTypeFactory,
                        (DBTermType t) -> {
                            if (t.isA(dbTimestamp))
                                return Optional.of(dbFunctionSymbolFactory.getDBWeeksBetweenFromDateTime());
                            else if (t.isA(dbDate))
                                return Optional.of(dbFunctionSymbolFactory.getDBWeeksBetweenFromDate());
                            else
                                return Optional.empty();
                        }),
                new OfnMultitypedInputBinarySPARQLFunctionSymbolImpl("OFN_DAYS_BETWEEN", OFN.DAYS_BETWEEN,
                        dateOrDatetime, xsdLong, false, dbTypeFactory,
                        (DBTermType t) -> {
                            if (t.isA(dbTimestamp))
                                return Optional.of(dbFunctionSymbolFactory.getDBDaysBetweenFromDateTime());
                            else if (t.isA(dbDate))
                                return Optional.of(dbFunctionSymbolFactory.getDBDaysBetweenFromDate());
                            else
                                return Optional.empty();
                        }),
                new OfnSimpleBinarySPARQLFunctionSymbolImpl("OFN_HOURS_BETWEEN", OFN.HOURS_BETWEEN, xsdDatetime, xsdLong,
                        false, TermFactory::getDBHoursBetweenFromDateTime),
                new OfnSimpleBinarySPARQLFunctionSymbolImpl("OFN_MINUTES_BETWEEN", OFN.MINUTES_BETWEEN, xsdDatetime, xsdLong,
                        false, TermFactory::getDBMinutesBetweenFromDateTime),
                new OfnSimpleBinarySPARQLFunctionSymbolImpl("OFN_SECONDS_BETWEEN", OFN.SECONDS_BETWEEN, xsdDatetime, xsdLong,
                        false, TermFactory::getDBSecondsBetweenFromDateTime),
                new OfnSimpleBinarySPARQLFunctionSymbolImpl("OFN_MILLIS_BETWEEN", OFN.MILLIS_BETWEEN, xsdDatetime, xsdLong,
                        false, TermFactory::getDBMillisBetweenFromDateTime),
                /*
                 * XSD Cast functions
                 */
                new SPARQLCastFunctionSymbolImpl("XSD_CAST_STRING", XSD.STRING, xsdString, typeFactory,
                        (DBTermType t) -> Optional.of(dbFunctionSymbolFactory.getDBCastFunctionSymbol(dbStringType))),
                new SPARQLCastFunctionSymbolImpl("XSD_CAST_BOOLEAN", XSD.BOOLEAN, xsdBoolean, typeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTypeFactory.getDBDecimalType())
                                    || t.isA(typeFactory.getDBTypeFactory().getDBDoubleType())
                                    || t.isA(typeFactory.getDBTypeFactory().getDBLargeIntegerType()))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertBoolean());
                            else if (t.isA(dbStringType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertBooleanFromString());
                            else if (t.isA(dbBooleanType))
                                return Optional.of(dbFunctionSymbolFactory.getDBCastFunctionSymbol(dbBooleanType));
                            else
                                return Optional.empty();
                        }),
                new SPARQLCastFunctionSymbolImpl("XSD_CAST_INTEGER", XSD.INTEGER, xsdInteger, typeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTypeFactory.getDBDecimalType())
                                    || t.isA(typeFactory.getDBTypeFactory().getDBDoubleType())
                                    || t.isA(dbStringType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertInteger());
                            else if (t.isA(dbBooleanType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertIntegerFromBoolean());
                            else if (t.isA(dbInteger))
                                return Optional.of(dbFunctionSymbolFactory.getDBCastFunctionSymbol(dbInteger));
                            else
                                return Optional.empty();
                        }),
                new SPARQLCastFunctionSymbolImpl("XSD_CAST_DECIMAL", XSD.DECIMAL, xsdDecimal, typeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTypeFactory.getDBDecimalType()))
                                return Optional.of(dbFunctionSymbolFactory.getDBCastFunctionSymbol(dbTypeFactory.getDBDecimalType()));
                            else if (t.isA(typeFactory.getDBTypeFactory().getDBLargeIntegerType())
                                    || t.isA(typeFactory.getDBTypeFactory().getDBDoubleType())
                                    || t.isA(dbStringType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertDecimal());
                            else if (t.isA(dbBooleanType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertDecimalFromBoolean());
                            else
                                return Optional.empty();
                        }),
                new SPARQLCastFunctionSymbolImpl("XSD_CAST_DOUBLE", XSD.DOUBLE, xsdDouble, typeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTypeFactory.getDBDecimalType())
                                    || t.isA(typeFactory.getDBTypeFactory().getDBLargeIntegerType())
                                    || t.isA(dbStringType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertDouble());
                            else if (t.isA(dbBooleanType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertFloatFromBoolean());
                            else if (t.isA(dbTypeFactory.getDBDoubleType()))
                                return Optional.of(dbFunctionSymbolFactory.getDBCastFunctionSymbol(dbTypeFactory.getDBDoubleType()));
                            else
                                return Optional.empty();
                        }),
                new SPARQLCastFunctionSymbolImpl("XSD_CAST_FLOAT", XSD.FLOAT, xsdFloat, typeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTypeFactory.getDBDecimalType())
                                    || t.isA(typeFactory.getDBTypeFactory().getDBLargeIntegerType()))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertFloatFromNonFPNumeric());
                            else if (t.isA(dbStringType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertFloat());
                            else if (t.isA(dbBooleanType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertFloatFromBoolean());
                            else if (t.isA(dbTypeFactory.getDBDoubleType()))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertFloatFromDouble());
                            else
                                return Optional.empty();
                        }),
                new SPARQLCastFunctionSymbolImpl("XSD_CAST_DATE", XSD.DATE, xsdDate, typeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbTimestamp))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertDateFromDatetime());
                            else if (t.isA(dbStringType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertDateFromString());
                            else if (t.isA(dbDate))
                                return Optional.of(dbFunctionSymbolFactory.getDBCastFunctionSymbol(dbDate));
                            else
                                return Optional.empty();
                        }),
                new SPARQLCastFunctionSymbolImpl("XSD_CAST_DATETIME", XSD.DATETIME, xsdDatetime, typeFactory,
                        (DBTermType t) ->  {
                            if (t.isA(dbDate))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertDateTimeFromDate());
                            else if (t.isA(dbStringType))
                                return Optional.of(dbFunctionSymbolFactory.checkAndConvertDateTimeFromString());
                            else if (t.isA(dbTimestamp))
                                return Optional.of(dbFunctionSymbolFactory.getDBCastFunctionSymbol(
                                        typeFactory.getDBTypeFactory().getDBDateTimestampType()));
                            else
                                return Optional.empty();
                        })
        );

        ImmutableTable.Builder<String, Integer, SPARQLFunctionSymbol> tableBuilder = ImmutableTable.builder();

        for(SPARQLFunctionSymbol functionSymbol : functionSymbols) {
            tableBuilder.put(functionSymbol.getOfficialName(), functionSymbol.getArity(), functionSymbol);
        }
        return tableBuilder.build();
    }

    private ImmutableTable<String, Integer, SPARQLFunctionSymbol> createDistinctSPARQLAggregationFunctionSymbolTable() {
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFTermType abstractRDFType = typeFactory.getAbstractRDFTermType();

        ImmutableSet<SPARQLFunctionSymbol> functionSymbols = ImmutableSet.of(
                new CountSPARQLFunctionSymbolImpl(abstractRDFType, xsdInteger, true),
                new CountSPARQLFunctionSymbolImpl(xsdInteger, true),
                new SumSPARQLFunctionSymbolImpl(true, abstractRDFType),
                // Distinct can be safely ignored
                new MinOrMaxOrSampleSPARQLFunctionSymbolImpl(typeFactory, MinOrMaxOrSampleSPARQLFunctionSymbolImpl.MinMaxSampleType.MIN),
                // Distinct can be safely ignored
                new MinOrMaxOrSampleSPARQLFunctionSymbolImpl(typeFactory, MinOrMaxOrSampleSPARQLFunctionSymbolImpl.MinMaxSampleType.MIN),
                // Distinct can be safely ignored
                new MinOrMaxOrSampleSPARQLFunctionSymbolImpl(typeFactory, MinOrMaxOrSampleSPARQLFunctionSymbolImpl.MinMaxSampleType.SAMPLE),
                new AvgSPARQLFunctionSymbolImpl(abstractRDFType, true),
                new StdevSPARQLFunctionSymbolImpl(abstractRDFType, true, true),
                new StdevSPARQLFunctionSymbolImpl(abstractRDFType, false, true),
                new StdevSPARQLFunctionSymbolImpl(abstractRDFType, true),
                new VarianceSPARQLFunctionSymbolImpl(abstractRDFType, true, true),
                new VarianceSPARQLFunctionSymbolImpl(abstractRDFType, false, true),
                new VarianceSPARQLFunctionSymbolImpl(abstractRDFType, true)
        );

        ImmutableTable.Builder<String, Integer, SPARQLFunctionSymbol> tableBuilder = ImmutableTable.builder();

        for(SPARQLFunctionSymbol functionSymbol : functionSymbols) {
            tableBuilder.put(functionSymbol.getOfficialName(), functionSymbol.getArity(), functionSymbol);
        }
        return tableBuilder.build();
    }


    @Override
    public RDFTermFunctionSymbol getRDFTermFunctionSymbol() {
        return rdfTermFunctionSymbol;
    }


    @Override
    public DBFunctionSymbolFactory getDBFunctionSymbolFactory() {
        return dbFunctionSymbolFactory;
    }

    @Override
    public BooleanFunctionSymbol getIsARDFTermTypeFunctionSymbol(RDFTermType rdfTermType) {
        return isAMap
                .computeIfAbsent(rdfTermType, t -> new IsARDFTermTypeFunctionSymbolImpl(metaRDFType, dbBooleanType, t));
    }

    @Override
    public BooleanFunctionSymbol getAreCompatibleRDFStringFunctionSymbol() {
        return areCompatibleRDFStringFunctionSymbol;
    }

    @Override
    public BooleanFunctionSymbol getLexicalNonStrictEqualityFunctionSymbol() {
        return lexicalNonStrictEqualityFunctionSymbol;
    }

    @Override
    public NotYetTypedEqualityFunctionSymbol getNotYetTypedEquality() {
        return notYetTypedEqualityFunctionSymbol;
    }

    @Override
    public BooleanFunctionSymbol getLexicalInequalityFunctionSymbol(InequalityLabel inequalityLabel) {
        return lexicalInequalityFunctionSymbolMap
                .computeIfAbsent(inequalityLabel, this::createLexicalInequalityFunctionSymbol);
    }

    @Override
    public BooleanFunctionSymbol getLexicalEBVFunctionSymbol() {
        return lexicalEBVFunctionSymbol;
    }

    protected BooleanFunctionSymbol createLexicalInequalityFunctionSymbol(InequalityLabel inequalityLabel) {
        return new LexicalInequalityFunctionSymbolImpl(inequalityLabel, metaRDFType,
                typeFactory.getXsdBooleanDatatype(), typeFactory.getXsdDatetimeDatatype(), typeFactory.getXsdStringDatatype(),
                dbStringType, dbBooleanType, typeFactory.getDatatype(XSD.DATETIMESTAMP), typeFactory.getDatatype(XSD.DATE));
    }


    @Override
    public BooleanFunctionSymbol getRDF2DBBooleanFunctionSymbol() {
        return rdf2DBBooleanFunctionSymbol;
    }

    @Override
    public RDFTermTypeFunctionSymbol getRDFTermTypeFunctionSymbol(TypeConstantDictionary dictionary,
                                                                  ImmutableSet<RDFTermTypeConstant> possibleConstants,
                                                                  boolean isSimplifiable) {
        ImmutableBiMap<DBConstant, RDFTermTypeConstant> conversionMap = dictionary.createConversionMap(possibleConstants);
        return new RDFTermTypeFunctionSymbolImpl(typeFactory, dictionary, conversionMap, isSimplifiable);
    }

    @Override
    public Optional<SPARQLFunctionSymbol> getSPARQLFunctionSymbol(String officialName, int arity) {
        switch (officialName) {
            case "http://www.w3.org/2005/xpath-functions#concat":
                return getSPARQLConcatFunctionSymbol(arity);
            case SPARQL.RAND:
                return Optional.of(createSPARQLRandFunctionSymbol());
            case SPARQL.UUID:
                return Optional.of(createSPARQLUUIDFunctionSymbol());
            case SPARQL.STRUUID:
                return Optional.of(createSPARQLStrUUIDFunctionSymbol());
            case SPARQL.COALESCE:
                return getSPARQLCoalesceFunctionSymbol(arity);
            case SPARQL.BNODE:
                if (arity == 0)
                    return Optional.of(createZeroArySPARQLBnodeFunctionSymbol());
                // Otherwise, default case
            default:
                return Optional.ofNullable(regularSparqlFunctionTable.get(officialName, arity));
        }
    }


    @Override
    public Optional<SPARQLFunctionSymbol> getSPARQLDistinctAggregateFunctionSymbol(String officialName, int arity) {
        return Optional.ofNullable(distinctSparqlAggregateFunctionTable.get(officialName, arity));
    }

    @Override
    public SPARQLAggregationFunctionSymbol getSPARQLGroupConcatFunctionSymbol(String separator, boolean isDistinct) {
        return isDistinct
                ? distinctSparqlGroupConcatMap.computeIfAbsent(separator, s -> createSPARQLGroupConcat(s, true))
                : nonDistinctSparqlGroupConcatMap.computeIfAbsent(separator, s -> createSPARQLGroupConcat(s, false));
    }

    @Override
    public synchronized SPARQLFunctionSymbol getIRIFunctionSymbol(IRI baseIRI) {
        return sparqlIRIMap.computeIfAbsent(baseIRI, b -> new IriSPARQLFunctionSymbolImpl(b,
                typeFactory.getAbstractRDFTermType(), typeFactory.getXsdStringDatatype(),
                typeFactory.getIRITermType()));
    }

    @Override
    public SPARQLFunctionSymbol getIRIFunctionSymbol() {
        return iriNoBaseFunctionSymbol;
    }

    protected SPARQLAggregationFunctionSymbol createSPARQLGroupConcat(String separator, boolean isDistinct) {
        return new GroupConcatSPARQLFunctionSymbolImpl(typeFactory.getXsdStringDatatype(), separator, isDistinct);
    }

    @Override
    public FunctionSymbol getSPARQLEffectiveBooleanValueFunctionSymbol() {
        return EBVSPARQLLikeFunctionSymbol;
    }

    /**
     * For smoother integration, return Optional.empty() for arity < 2
     */
    private Optional<SPARQLFunctionSymbol> getSPARQLConcatFunctionSymbol(int arity) {
        return arity < 2
                ? Optional.empty()
                : Optional.of(concatMap
                        .computeIfAbsent(arity, a -> new ConcatSPARQLFunctionSymbolImpl(a, typeFactory.getXsdStringDatatype())));
    }

    private Optional<SPARQLFunctionSymbol> getSPARQLCoalesceFunctionSymbol(int arity) {
        return arity < 1
                ? Optional.empty()
                : Optional.of(coalesceMap
                .computeIfAbsent(arity, a -> new CoalesceSPARQLFunctionSymbolImpl(a, typeFactory.getAbstractRDFTermType())));
    }

    private SPARQLFunctionSymbol createZeroArySPARQLBnodeFunctionSymbol() {
        return new ZeroAryBnodeSPARQLFunctionSymbolImpl(UUID.randomUUID(), typeFactory.getBlankNodeType());
    }

    /**
     * Freshly created on the fly with a UUID because RAND is non-deterministic.
     */
    protected SPARQLFunctionSymbol createSPARQLRandFunctionSymbol() {
        return new RandSPARQLFunctionSymbolImpl(UUID.randomUUID(), typeFactory.getXsdDoubleDatatype());
    }

    /**
     * Freshly created on the fly with a UUID because UUID is non-deterministic.
     */
    protected SPARQLFunctionSymbol createSPARQLUUIDFunctionSymbol() {
        return new UUIDSPARQLFunctionSymbolImpl(UUID.randomUUID(), typeFactory.getIRITermType());
    }

    /**
     * Freshly created on the fly with a UUID because STRUUID is non-deterministic.
     */
    protected SPARQLFunctionSymbol createSPARQLStrUUIDFunctionSymbol() {
        return new StrUUIDSPARQLFunctionSymbolImpl(UUID.randomUUID(), typeFactory.getXsdStringDatatype());
    }

    @Override
    public FunctionSymbol getCommonDenominatorFunctionSymbol(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Expected arity >= 2 for a common denominator");
        return commonDenominatorMap
                .computeIfAbsent(arity, a -> new CommonDenominatorFunctionSymbolImpl(a, typeFactory.getMetaRDFTermType()));
    }

    @Override
    public FunctionSymbol getCommonPropagatedOrSubstitutedNumericTypeFunctionSymbol() {
        return commonNumericTypeFunctionSymbol;
    }

    @Override
    public FunctionSymbol getRDFDatatypeStringFunctionSymbol() {
        return rdfDatatypeFunctionSymbol;
    }

    @Override
    public FunctionSymbol getLangTagFunctionSymbol() {
        return langTypeFunctionSymbol;
    }

    @Override
    public BooleanFunctionSymbol getLexicalLangMatches() {
        return lexicalLangMatchesFunctionSymbol;
    }

    @Override
    public FunctionSymbol getBinaryNumericLexicalFunctionSymbol(String dbNumericOperationName) {
        return new BinaryNumericLexicalFunctionSymbolImpl(dbNumericOperationName, dbStringType, metaRDFType);
    }

    @Override
    public FunctionSymbol getUnaryLatelyTypedFunctionSymbol(Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct,
                                                            DBTermType targetType) {
        return new UnaryLatelyTypedFunctionSymbolImpl(dbStringType, metaRDFType, targetType, dbFunctionSymbolFct);
    }

    @Override
    public FunctionSymbol getUnaryLexicalFunctionSymbol(Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        return new UnaryLexicalFunctionSymbolImpl(dbStringType, metaRDFType, dbFunctionSymbolFct);
    }

    @Override
    public FunctionSymbol getBinaryLatelyTypedFunctionSymbol(Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct,
                                                             DBTermType targetType) {
        return new BinaryLatelyTypedFunctionSymbolImpl(dbStringType, dbStringType, metaRDFType, metaRDFType, targetType,
                dbFunctionSymbolFct);
    }

    @Override
    public FunctionSymbol getExtractLexicalTermFromRDFTerm() {
        return extractLexicalTermFunctionSymbol;
    }

}
