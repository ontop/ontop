package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.model.vocabulary.XSD;

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
    private final BooleanFunctionSymbol lexicalEBVFunctionSymbol;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;

    private final ImmutableTable<String, Integer, SPARQLFunctionSymbol> regularSparqlFunctionTable;
    private final Map<Integer, FunctionSymbol> commonDenominatorMap;
    private final Map<Integer, SPARQLFunctionSymbol> concatMap;
    private final Map<RDFTermType, BooleanFunctionSymbol> isAMap;
    private final Map<InequalityLabel, BooleanFunctionSymbol> lexicalInequalityFunctionSymbolMap;
    private final BooleanFunctionSymbol rdf2DBBooleanFunctionSymbol;
    private final FunctionSymbol langTypeFunctionSymbol;
    private final FunctionSymbol rdfDatatypeFunctionSymbol;
    private final BooleanFunctionSymbol lexicalLangMatchesFunctionSymbol;
    private final FunctionSymbol commonNumericTypeFunctionSymbol;
    private final FunctionSymbol EBVSPARQLLikeFunctionSymbol;

    private final MetaRDFTermType metaRDFType;
    private final DBTermType dbBooleanType;
    private final DBTermType dbStringType;

    @Inject
    private FunctionSymbolFactoryImpl(TypeFactory typeFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory) {
        this.typeFactory = typeFactory;
        this.rdfTermFunctionSymbol = new RDFTermFunctionSymbolImpl(
                typeFactory.getDBTypeFactory().getDBStringType(),
                typeFactory.getMetaRDFTermType());
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        this.dbStringType = dbTypeFactory.getDBStringType();
        DBTermType rootDBType = dbTypeFactory.getAbstractRootDBType();

        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.metaRDFType = typeFactory.getMetaRDFTermType();

        this.regularSparqlFunctionTable = createSPARQLFunctionSymbolTable(typeFactory, dbFunctionSymbolFactory);
        this.commonDenominatorMap = new ConcurrentHashMap<>();
        this.concatMap = new ConcurrentHashMap<>();
        this.isAMap = new ConcurrentHashMap<>();
        this.lexicalInequalityFunctionSymbolMap = new ConcurrentHashMap<>();
        this.areCompatibleRDFStringFunctionSymbol = new AreCompatibleRDFStringFunctionSymbolImpl(metaRDFType, dbBooleanType);
        rdf2DBBooleanFunctionSymbol = new RDF2DBBooleanFunctionSymbolImpl(typeFactory.getXsdBooleanDatatype(),
                dbBooleanType, dbStringType);
        this.lexicalNonStrictEqualityFunctionSymbol = new LexicalNonStrictEqualityFunctionSymbolImpl(metaRDFType,
                typeFactory.getXsdBooleanDatatype(), typeFactory.getXsdDatetimeDatatype(), typeFactory.getXsdStringDatatype(),
                dbStringType, dbBooleanType, typeFactory.getDatatype(XSD.DATETIMESTAMP));
        this.langTypeFunctionSymbol = new LangTagFunctionSymbolImpl(metaRDFType, dbStringType);
        this.rdfDatatypeFunctionSymbol = new RDFDatatypeStringFunctionSymbolImpl(metaRDFType, dbStringType);
        this.lexicalLangMatchesFunctionSymbol = new LexicalLangMatchesFunctionSymbolImpl(dbStringType, dbBooleanType);
        this.commonNumericTypeFunctionSymbol = new CommonPropagatedOrSubstitutedNumericTypeFunctionSymbolImpl(metaRDFType);
        this.EBVSPARQLLikeFunctionSymbol = new EBVSPARQLLikeFunctionSymbolImpl(typeFactory.getAbstractRDFSLiteral(), typeFactory.getXsdBooleanDatatype());
        this.lexicalEBVFunctionSymbol = new LexicalEBVFunctionSymbolImpl(dbStringType, metaRDFType, dbBooleanType);

    }

    protected static ImmutableTable<String, Integer, SPARQLFunctionSymbol> createSPARQLFunctionSymbolTable(
            TypeFactory typeFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory) {
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();
        RDFDatatype xsdDecimal = typeFactory.getXsdDecimalDatatype();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFDatatype rdfsLiteral = typeFactory.getAbstractRDFSLiteral();
        RDFTermType abstractRDFType = typeFactory.getAbstractRDFTermType();
        ObjectRDFType bnodeType = typeFactory.getBlankNodeType();
        ObjectRDFType iriType = typeFactory.getIRITermType();
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype abstractNumericType = typeFactory.getAbstractOntopNumericDatatype();

        DBTermType dbBoolean = typeFactory.getDBTypeFactory().getDBBooleanType();

        ImmutableSet<SPARQLFunctionSymbol> functionSymbols = ImmutableSet.of(
                new UcaseSPARQLFunctionSymbolImpl(xsdString),
                new LcaseSPARQLFunctionSymbolImpl(xsdString),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_ENCODE_FOR_URI", XPathFunction.ENCODE_FOR_URI,
                        xsdString, xsdString, true,
                        TermFactory::getR2RMLIRISafeEncodeFunctionalTerm),
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
                new Sha512SPARQLFunctionSymbolImpl(xsdString),
                new NumericBinarySPARQLFunctionSymbolImpl("SP_MULTIPLY", SPARQL.NUMERIC_MULTIPLY, abstractNumericType),
                new NumericBinarySPARQLFunctionSymbolImpl("SP_ADD", SPARQL.NUMERIC_ADD, abstractNumericType),
                new NumericBinarySPARQLFunctionSymbolImpl("SP_SUBSTRACT", SPARQL.NUMERIC_SUBSTRACT, abstractNumericType),
                new DivideSPARQLFunctionSymbolImpl(abstractNumericType, xsdDecimal),
                new NonStrictEqSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean, dbBoolean),
                new LessThanSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean, dbBoolean),
                new GreaterThanSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean, dbBoolean),
                new SameTermSPARQLFunctionSymbolImpl(abstractRDFType, xsdBoolean),
                new UnaryNumericSPARQLFunctionSymbolImpl("SP_ABS", XPathFunction.NUMERIC_ABS, abstractNumericType,
                        dbFunctionSymbolFactory::getAbs),
                new UnaryNumericSPARQLFunctionSymbolImpl("SP_CEIL", XPathFunction.NUMERIC_CEIL, abstractNumericType,
                        dbFunctionSymbolFactory::getCeil),
                new UnaryNumericSPARQLFunctionSymbolImpl("SP_FLOOR", XPathFunction.NUMERIC_FLOOR, abstractNumericType,
                        dbFunctionSymbolFactory::getFloor),
                new UnaryNumericSPARQLFunctionSymbolImpl("SP_ROUND", XPathFunction.NUMERIC_ROUND, abstractNumericType,
                        dbFunctionSymbolFactory::getRound),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_YEAR", XPathFunction.YEAR_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBYear),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_MONTH", XPathFunction.MONTH_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBMonth),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_DAY", XPathFunction.DAY_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBDay),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_HOURS", XPathFunction.HOURS_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBHours),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_MINUTES", XPathFunction.MINUTES_FROM_DATETIME,
                        xsdDatetime, xsdInteger, false, TermFactory::getDBMinutes),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_SECONDS", XPathFunction.SECONDS_FROM_DATETIME,
                        xsdDatetime, xsdDecimal, false, TermFactory::getDBSeconds),
                new SimpleUnarySPARQLFunctionSymbolImpl("SP_TZ", SPARQL.TZ,
                xsdDatetime, xsdString, false, TermFactory::getDBTz),
                new NowSPARQLFunctionSymbolImpl(xsdDatetime)
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
                dbStringType, dbBooleanType, typeFactory.getDatatype(XSD.DATETIMESTAMP));
    }


    @Override
    public BooleanFunctionSymbol getRDF2DBBooleanFunctionSymbol() {
        return rdf2DBBooleanFunctionSymbol;
    }

    @Override
    public RDFTermTypeFunctionSymbol getRDFTermTypeFunctionSymbol(TypeConstantDictionary dictionary,
                                                                  ImmutableSet<RDFTermTypeConstant> possibleConstants) {
        ImmutableBiMap<DBConstant, RDFTermTypeConstant> conversionMap = dictionary.createConversionMap(possibleConstants);
        return new RDFTermTypeFunctionSymbolImpl(typeFactory, dictionary, conversionMap);
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
            default:
                return Optional.ofNullable(regularSparqlFunctionTable.get(officialName, arity));
        }
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
    public FunctionSymbol getUnaryLexicalFunctionSymbol(Function<DBTermType, DBFunctionSymbol> dbFunctionSymbolFct) {
        return new UnaryLexicalFunctionSymbolImpl(dbStringType, metaRDFType, dbFunctionSymbolFct);
    }
}
