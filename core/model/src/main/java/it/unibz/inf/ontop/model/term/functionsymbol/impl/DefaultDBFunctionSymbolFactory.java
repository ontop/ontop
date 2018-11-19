package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultDBFunctionSymbolFactory implements DBFunctionSymbolFactory {

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
    private final DBTypeFactory dbTypeFactory;

    @Inject
    private DefaultDBFunctionSymbolFactory(TypeFactory typeFactory) {
        this(createDefaultNormalizationTable(typeFactory), typeFactory);
    }

    protected DefaultDBFunctionSymbolFactory(ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable,
                                             TypeFactory typeFactory) {
        this.castMap = new HashMap<>();
        this.castTable = HashBasedTable.create();
        this.normalizationTable = normalizationTable;
        dbTypeFactory = typeFactory.getDBTypeFactory();
        this.temporaryToStringCastFunctionSymbol = new TemporaryDBTypeConversionToStringFunctionSymbolImpl(
                dbTypeFactory.getAbstractRootDBType(), dbTypeFactory.getDBStringType());
    }

    protected static ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createDefaultNormalizationTable(TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();
        // TODO: complete (timestamp + boolean)
        return builder.build();
    }

    @Override
    public DBTypeConversionFunctionSymbol getTemporaryConversionToDBStringFunctionSymbol() {
        return temporaryToStringCastFunctionSymbol;
    }

    @Override
    public DBTypeConversionFunctionSymbol getDBCastFunctionSymbol(DBTermType targetType) {
        return castMap
                .computeIfAbsent(targetType,
                        t -> new DBTypeConversionFunctionSymbolImpl(dbTypeFactory.getAbstractRootDBType(), t));
    }

    @Override
    public DBTypeConversionFunctionSymbol getDBCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        if (castTable.contains(inputType, targetType))
            return castTable.get(inputType, targetType);

        DBTypeConversionFunctionSymbol castFunctionSymbol = new DBTypeConversionFunctionSymbolImpl(inputType, targetType);
        castTable.put(inputType, targetType, castFunctionSymbol);
        return castFunctionSymbol;
    }

    @Override
    public DBTypeConversionFunctionSymbol getConversion2RDFLexicalFunctionSymbol(DBTermType inputType, RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .flatMap(t -> Optional.ofNullable(normalizationTable.get(inputType, t)))
                // Fallback to simple cast
                .orElseGet(() -> getDBCastFunctionSymbol(inputType, dbTypeFactory.getDBStringType()));
    }
}
