package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import it.unibz.inf.ontop.model.term.functionsymbol.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractDBFunctionSymbolFactory implements DBFunctionSymbolFactory {

    private final DBTypeConversionFunctionSymbol temporaryToStringCastFunctionSymbol;
    /**
     *  For conversion function symbols that are SIMPLE CASTs from an undetermined type (no normalization)
     */
    private final Map<DBTermType, DBTypeConversionFunctionSymbol> castMap;
    /**
     *  For conversion function symbols that implies a NORMALIZATION as RDF lexical term
     */
    private final ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable;
    /**
     *  For conversion function symbols that are SIMPLE CASTs from a determined type (no normalization)
     */
    private final Table<DBTermType, DBTermType, DBTypeConversionFunctionSymbol> castTable;
    private final DBTermType dbStringType;

    /**
     * Name (in the DB dialect), arity -> regular DBFunctionSymbol
     */
    private final Table<String, Integer, DBFunctionSymbol> regularFunctionTable;

    protected AbstractDBFunctionSymbolFactory(ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable,
                                              ImmutableTable<String, Integer, DBFunctionSymbol> defaultRegularFunctionTable,
                                              TypeFactory typeFactory) {
        this.castMap = new HashMap<>();
        this.castTable = HashBasedTable.create();
        this.normalizationTable = normalizationTable;
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        this.dbStringType = dbTypeFactory.getDBStringType();
        this.temporaryToStringCastFunctionSymbol = new TemporaryDBTypeConversionToStringFunctionSymbolImpl(
                dbTypeFactory.getAbstractRootDBType(), dbStringType);
        this.regularFunctionTable = HashBasedTable.create(defaultRegularFunctionTable);
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

        Optional<DBFunctionSymbol> optionalSymbol = Optional.ofNullable(regularFunctionTable.get(canonicalName, arity));
        if (optionalSymbol.isPresent())
            return optionalSymbol.get();

        DBFunctionSymbol symbol = createRegularFunctionSymbol(canonicalName, arity);
        regularFunctionTable.put(canonicalName, arity, symbol);
        return symbol;
    }

    /**
     * Can be overridden
     */
    protected String canonicalizeRegularFunctionSymbolName(String nameInDialect) {
        return nameInDialect.toUpperCase();
    }

    protected abstract DBFunctionSymbol createRegularFunctionSymbol(String nameInDialect, int arity);

    protected abstract DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType);

    protected abstract DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType,
                                                                                     DBTermType targetType);

    @Override
    public DBTypeConversionFunctionSymbol getConversion2RDFLexicalFunctionSymbol(DBTermType inputType, RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .flatMap(t -> Optional.ofNullable(normalizationTable.get(inputType, t)))
                // Fallback to simple cast
                .orElseGet(() -> getDBCastFunctionSymbol(inputType, dbStringType));
    }
}
