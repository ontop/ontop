package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.*;

import java.util.function.Function;

public class DefaultSQLDBFunctionSymbolFactory extends AbstractSQLDBFunctionSymbolFactory {

    @Inject
    private DefaultSQLDBFunctionSymbolFactory(TypeFactory typeFactory) {
        super(createDefaultNormalizationTable(typeFactory), createDefaultRegularFunctionTable(typeFactory), typeFactory);
    }

    @Override
    protected String serializeContains(ImmutableList<? extends ImmutableTerm> immutableTerms, Function<ImmutableTerm, String> immutableTermStringFunction, TermFactory termFactory) {
        throw new UnsupportedOperationException(
                "Not supported in the Default SQL factory since no-one uses " +
                        "the old official standard function.\n" +
                        "Please specific it in your dialect factory");
    }

}
