package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.rdf4j.common.net.ParsedIRI;

import java.util.function.Function;

public class SQLServerDBIriStringResolverFunctionSymbolImpl extends DBIriStringResolverFunctionSymbolImpl {

    private final String noSpecialCharacter;

    protected SQLServerDBIriStringResolverFunctionSymbolImpl(IRI baseIRI, DBTermType rootDBType, DBTermType dbStringType) {
        super(baseIRI, "[a-zA-Z].*:", rootDBType, dbStringType);
        noSpecialCharacter = ".*[-_,.#/;:|<>\\^(){}\\[\\]!?0-9].*:";
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableTerm argLexical = terms.get(0);

        ImmutableFunctionalTerm functionalTerm = termFactory.getIfThenElse(
                termFactory.getConjunction(
                        termFactory.getDBRegexpMatches(ImmutableList.of(argLexical, termFactory.getDBStringConstant(iriPrefixRegex))),
                        termFactory.getDBNot(termFactory.getDBRegexpMatches(ImmutableList.of(
                                argLexical,
                                termFactory.getDBStringConstant(noSpecialCharacter)
                        )))
                ),
                argLexical,
                termFactory.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(termFactory.getDBStringConstant(baseIRI.toString()), argLexical)));

        return termConverter.apply(functionalTerm.simplify());
    }

}
