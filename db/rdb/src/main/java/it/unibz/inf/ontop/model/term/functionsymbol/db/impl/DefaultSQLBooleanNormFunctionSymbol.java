package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * SQL-specific
 */
public class DefaultSQLBooleanNormFunctionSymbol extends AbstractBooleanNormFunctionSymbol {

    protected DefaultSQLBooleanNormFunctionSymbol(DBTermType booleanType, DBTermType stringType) {
        super(booleanType, stringType);
    }
}
