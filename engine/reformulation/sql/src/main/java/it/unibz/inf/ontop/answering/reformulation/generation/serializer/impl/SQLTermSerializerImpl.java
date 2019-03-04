package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;

import java.util.Optional;

@Singleton
public class SQLTermSerializerImpl implements SQLTermSerializer {

    private final SQLDialectAdapter sqlAdapter;
    private final TermFactory termFactory;

    @Inject
    private SQLTermSerializerImpl(SQLDialectAdapter sqlAdapter, TermFactory termFactory) {
        this.sqlAdapter = sqlAdapter;
        this.termFactory = termFactory;
    }


    @Override
    public String serialize(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> var2ColumnMap)
            throws SQLSerializationException {
        if (term instanceof Constant) {
            return serializeConstant((Constant)term);
        }
        else if (term instanceof Variable) {
            return Optional.ofNullable(var2ColumnMap.get(term))
                    .map(QualifiedAttributeID::getSQLRendering)
                    .orElseThrow(() -> new SQLSerializationException(String.format(
                            "The variable %s does not appear in the var2ColumnMap", term)));
        }
        /*
         * ImmutableFunctionalTerm with a DBFunctionSymbol
         */
        else {
            return Optional.of(term)
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm) t)
                    .filter(t -> t.getFunctionSymbol() instanceof DBFunctionSymbol)
                    .map(t -> ((DBFunctionSymbol) t.getFunctionSymbol()).getNativeDBString(
                            t.getTerms(),
                            t2 -> serialize(t2, var2ColumnMap),
                            termFactory))
                    .orElseThrow(() -> new SQLSerializationException("Only DBFunctionSymbols must be provided " +
                            "to a SQLTermSerializer"));
        }
    }

    private String serializeConstant(Constant constant) {
        if (constant.isNull())
            return constant.getValue();
        if (!(constant instanceof DBConstant)) {
            throw new SQLSerializationException(
                    "Only DBConstants or NULLs are expected in sub-tree to be translated into SQL");
        }
        return sqlAdapter.render((DBConstant) constant);
    }
}
