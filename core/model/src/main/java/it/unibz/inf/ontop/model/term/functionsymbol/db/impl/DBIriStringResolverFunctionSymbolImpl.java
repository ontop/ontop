package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.apache.commons.rdf.api.IRI;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

public class DBIriStringResolverFunctionSymbolImpl extends AbstractTypedDBFunctionSymbol {

    protected final URI baseIRI;
    protected final String iriPrefixRegex;

    protected DBIriStringResolverFunctionSymbolImpl(IRI baseIRI, String iriPrefixRegex, DBTermType rootDBType, DBTermType dbStringType) {
        super("IRI_RESOLVE_" + baseIRI, ImmutableList.of(rootDBType), dbStringType);
        try {
            this.baseIRI = new URI(baseIRI.getIRIString());
        } catch (URISyntaxException e) {
            throw new MinorOntopInternalBugException("Was not expecting a invalid base IRI: " + baseIRI);
        }
        this.iriPrefixRegex = iriPrefixRegex;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);

        if (newTerm instanceof DBConstant) {
            // TODO: see if the standard URI resolution is robust or not for IRIs in general
            URI newIRI = baseIRI.resolve(((DBConstant) newTerm).getValue());
            return termFactory.getDBStringConstant(newIRI.toString());
        }
        else
            return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableTerm argLexical = terms.get(0);

        ImmutableFunctionalTerm functionalTerm = termFactory.getIfThenElse(
                termFactory.getDBRegexpMatches(ImmutableList.of(argLexical, termFactory.getDBStringConstant(iriPrefixRegex))),
                argLexical,
                termFactory.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(termFactory.getDBStringConstant(baseIRI.toString()), argLexical)));

        return termConverter.apply(functionalTerm.simplify());
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }
}
