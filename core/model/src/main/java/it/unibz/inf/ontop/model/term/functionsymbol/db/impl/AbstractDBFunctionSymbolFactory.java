package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class AbstractDBFunctionSymbolFactory implements DBFunctionSymbolFactory {

    private static final String BNODE_PREFIX = "_:ontop-bnode-";
    private static final String PLACEHOLDER = "{}";

    private final TypeFactory typeFactory;
    private final DBTypeConversionFunctionSymbol temporaryToStringCastFunctionSymbol;
    private final DBBooleanFunctionSymbol dbStartsWithFunctionSymbol;
    private final DBBooleanFunctionSymbol dbEndsWithFunctionSymbol;
    private final DBBooleanFunctionSymbol dbLikeFunctionSymbol;
    private final DBFunctionSymbol ifElseNullFunctionSymbol;
    private final DBNotFunctionSymbol dbNotFunctionSymbol;

    // Lazy
    @Nullable
    private DBBooleanFunctionSymbol containsFunctionSymbol;
    // Lazy
    @Nullable
    private DBFunctionSymbol r2rmlIRISafeEncodeFunctionSymbol;
    // Lazy
    @Nullable
    private DBFunctionSymbol strBeforeFunctionSymbol;
    // Lazy
    @Nullable
    private DBFunctionSymbol strAfterFunctionSymbol;


    /**
     *  For conversion function symbols that are SIMPLE CASTs from an undetermined type (no normalization)
     */
    private final Map<DBTermType, DBTypeConversionFunctionSymbol> castMap;
    /**
     *  For conversion function symbols that implies a NORMALIZATION as RDF lexical term
     */
    private final ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable;

    /**
     *  For conversion function symbols that implies a DENORMALIZATION from RDF lexical term
     */
    private final ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> deNormalizationTable;


    /**
     *  For conversion function symbols that are SIMPLE CASTs from a determined type (no normalization)
     */
    private final Table<DBTermType, DBTermType, DBTypeConversionFunctionSymbol> castTable;

    /**
     * For the CASE functions
     */
    private final Map<Integer, DBFunctionSymbol> caseMap;

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

    private final DBTermType rootDBType;
    private final DBTermType dbStringType;
    private final DBTermType dbBooleanType;

    /**
     * Name (in the DB dialect), arity -> predefined DBFunctionSymbol
     */
    private final ImmutableTable<String, Integer, DBFunctionSymbol> predefinedFunctionTable;

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

    private final Map<String, IRIStringTemplateFunctionSymbol> iriTemplateMap;
    private final Map<String, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap;
    // NB: Multi-threading safety is NOT a concern here
    // (we don't create fresh bnode templates for a SPARQL query)
    private final AtomicInteger counter;

    protected AbstractDBFunctionSymbolFactory(ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable,
                                              ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> deNormalizationTable,
                                              ImmutableTable<String, Integer, DBFunctionSymbol> defaultRegularFunctionTable,
                                              TypeFactory typeFactory) {
        this.castMap = new HashMap<>();
        this.castTable = HashBasedTable.create();
        this.normalizationTable = normalizationTable;
        this.deNormalizationTable = deNormalizationTable;
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        this.dbStringType = dbTypeFactory.getDBStringType();
        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.temporaryToStringCastFunctionSymbol = new TemporaryDBTypeConversionToStringFunctionSymbolImpl(
                dbTypeFactory.getAbstractRootDBType(), dbStringType);
        this.predefinedFunctionTable = defaultRegularFunctionTable;
        this.untypedFunctionTable = HashBasedTable.create();
        this.notPredefinedBooleanFunctionTable = HashBasedTable.create();
        this.caseMap = new HashMap<>();
        this.strictEqMap = new HashMap<>();
        this.strictNEqMap = new HashMap<>();
        this.falseOrNullMap = new HashMap<>();
        this.trueOrNullMap = new HashMap<>();
        this.r2rmlIRISafeEncodeFunctionSymbol = null;
        this.strBeforeFunctionSymbol = null;
        this.strAfterFunctionSymbol = null;
        this.iriTemplateMap = new HashMap<>();
        this.bnodeTemplateMap = new HashMap<>();
        this.counter = new AtomicInteger();
        this.typeFactory = typeFactory;
        this.dbStartsWithFunctionSymbol = new DefaultDBStrStartsWithFunctionSymbol(
                dbTypeFactory.getAbstractRootDBType(), dbStringType);
        this.dbEndsWithFunctionSymbol = new DefaultDBStrEndsWithFunctionSymbol(
                dbTypeFactory.getAbstractRootDBType(), dbStringType);
        this.rootDBType = dbTypeFactory.getAbstractRootDBType();
        this.dbLikeFunctionSymbol = new DBLikeFunctionSymbolImpl(dbBooleanType, rootDBType);
        this.ifElseNullFunctionSymbol = new DefaultDBIfElseNullFunctionSymbol(dbBooleanType, rootDBType);
        this.dbNotFunctionSymbol = createDBNotFunctionSymbol(dbBooleanType);
    }

    @Override
    public IRIStringTemplateFunctionSymbol getIRIStringTemplateFunctionSymbol(String iriTemplate) {
        return iriTemplateMap
                .computeIfAbsent(iriTemplate,
                        t -> IRIStringTemplateFunctionSymbolImpl.createFunctionSymbol(t, typeFactory));
    }

    @Override
    public BnodeStringTemplateFunctionSymbol getBnodeStringTemplateFunctionSymbol(String bnodeTemplate) {
        return bnodeTemplateMap
                .computeIfAbsent(bnodeTemplate,
                        t -> BnodeStringTemplateFunctionSymbolImpl.createFunctionSymbol(t, typeFactory));
    }

    @Override
    public BnodeStringTemplateFunctionSymbol getFreshBnodeStringTemplateFunctionSymbol(int arity) {
        String bnodeTemplate = IntStream.range(0, arity)
                .boxed()
                .map(i -> PLACEHOLDER)
                .reduce(
                        BNODE_PREFIX + counter.incrementAndGet(),
                        (prefix, suffix) -> prefix + "/" + suffix);

        return getBnodeStringTemplateFunctionSymbol(bnodeTemplate);
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
        if (castTable.contains(inputType, targetType))
            return castTable.get(inputType, targetType);

        DBTypeConversionFunctionSymbol castFunctionSymbol = createSimpleCastFunctionSymbol(inputType, targetType);
        castTable.put(inputType, targetType, castFunctionSymbol);
        return castFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getRegularDBFunctionSymbol(String nameInDialect, int arity) {
        String canonicalName = canonicalizeRegularFunctionSymbolName(nameInDialect);

        Optional<DBFunctionSymbol> optionalSymbol = Optional.ofNullable(predefinedFunctionTable.get(canonicalName, arity))
                .map(Optional::of)
                .orElseGet(() -> Optional.ofNullable(untypedFunctionTable.get(canonicalName, arity)));

        // NB: we don't look inside notPredefinedBooleanFunctionTable to avoid enforcing the boolean type

        if (optionalSymbol.isPresent())
            return optionalSymbol.get();

        DBFunctionSymbol symbol = createRegularUntypedFunctionSymbol(canonicalName, arity);
        untypedFunctionTable.put(canonicalName, arity, symbol);
        return symbol;
    }

    @Override
    public DBBooleanFunctionSymbol getRegularDBBooleanFunctionSymbol(String nameInDialect, int arity) {
        String canonicalName = canonicalizeRegularFunctionSymbolName(nameInDialect);

        Optional<DBFunctionSymbol> optionalSymbol = Optional.ofNullable(predefinedFunctionTable.get(canonicalName, arity))
                .map(Optional::of)
                .orElseGet(() -> Optional.ofNullable(notPredefinedBooleanFunctionTable.get(canonicalName, arity)));

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

    @Override
    public DBFunctionSymbol getDBCase(int arity) {
        if ((arity < 3) || (arity % 2 == 0))
            throw new IllegalArgumentException("Arity of a CASE function symbol must be odd and >= 3");

        return caseMap
                .computeIfAbsent(arity, a -> createDBCase(arity));

    }

    @Override
    public DBFunctionSymbol getDBIfElseNull() {
        return ifElseNullFunctionSymbol;
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
    public DBBooleanFunctionSymbol getDBStartsWith() {
        return dbStartsWithFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBEndsWith() {
        return dbEndsWithFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getR2RMLIRISafeEncode() {
        if (r2rmlIRISafeEncodeFunctionSymbol == null)
            r2rmlIRISafeEncodeFunctionSymbol = createR2RMLIRISafeEncode();
        return r2rmlIRISafeEncodeFunctionSymbol;
    }

    @Override
    public DBNotFunctionSymbol getDBNot() {
        return dbNotFunctionSymbol;
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
        if (containsFunctionSymbol == null)
            containsFunctionSymbol = createContainsFunctionSymbol();
        return containsFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBLike() {
        return dbLikeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBStrBefore() {
        if (strBeforeFunctionSymbol == null)
            strBeforeFunctionSymbol = createStrBeforeFunctionSymbol();
        return strBeforeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBStrAfter() {
        if (strAfterFunctionSymbol == null)
            strAfterFunctionSymbol = createStrAfterFunctionSymbol();
        return strAfterFunctionSymbol;
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

    protected abstract DBFunctionSymbol createDBCase(int arity);

    protected abstract DBStrictEqFunctionSymbol createDBStrictEquality(int arity);

    protected abstract DBBooleanFunctionSymbol createDBStrictNEquality(int arity);

    protected abstract DBNotFunctionSymbol createDBNotFunctionSymbol(DBTermType dbBooleanType);

    protected abstract DBFunctionSymbol createR2RMLIRISafeEncode();

    protected abstract String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter,
                                     TermFactory termFactory);

    protected abstract String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                                 Function<ImmutableTerm, String> termConverter,
                                                 TermFactory termFactory);

    protected abstract String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                                 Function<ImmutableTerm, String> termConverter,
                                                 TermFactory termFactory);
    

    @Override
    public DBTypeConversionFunctionSymbol getConversion2RDFLexicalFunctionSymbol(DBTermType inputType, RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .flatMap(t -> Optional.ofNullable(normalizationTable.get(inputType, t)))
                // Fallback to simple cast
                .orElseGet(() -> getDBCastFunctionSymbol(inputType, dbStringType));
    }

    @Override
    public DBTypeConversionFunctionSymbol getConversionFromRDFLexical2DBFunctionSymbol(DBTermType targetDBType,
                                                                                       RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .flatMap(t -> Optional.ofNullable(deNormalizationTable.get(targetDBType, t)))
                // Fallback to simple cast
                .orElseGet(() -> getDBCastFunctionSymbol(dbStringType, targetDBType));
    }
}
