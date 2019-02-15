package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.IRIDictionary;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;

import javax.annotation.Nullable;
import java.util.Optional;

@Singleton
public class SQLTermSerializerImpl implements SQLTermSerializer {

    @Nullable
    private final IRIDictionary iriDictionary;
    private final SQLDialectAdapter sqlAdapter;
    private final TermFactory termFactory;

    @Inject
    private SQLTermSerializerImpl(@Nullable IRIDictionary iriDictionary,
                                  SQLDialectAdapter sqlAdapter, TermFactory termFactory) {
        this.iriDictionary = iriDictionary;
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
                    .orElseThrow(() -> new SQLSerializationException("Only DBFunctionSymbol must be provided " +
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
        DBConstant ct = (DBConstant) constant;
        if (iriDictionary != null) {
            // TODO: check this hack
            if (ct.getType().isString()) {
                int id = iriDictionary.getId(ct.getValue());
                if (id >= 0)
                    //return jdbcutil.getSQLLexicalForm(String.valueOf(id));
                    return String.valueOf(id);
            }
        }
        return sqlAdapter.render(ct);
    }
}
