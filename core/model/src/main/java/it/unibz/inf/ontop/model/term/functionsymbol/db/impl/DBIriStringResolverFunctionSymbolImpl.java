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

public class DBIriStringResolverFunctionSymbolImpl extends AbstractTypedDBFunctionSymbol {

    protected final IRI baseIRI;
    protected final String iriPrefixRegex;

    protected DBIriStringResolverFunctionSymbolImpl(IRI baseIRI, String iriPrefixRegex, DBTermType rootDBType, DBTermType dbStringType) {
        super("IRI_RESOLVE_" + baseIRI, ImmutableList.of(rootDBType), dbStringType);
        this.baseIRI = baseIRI;
        this.iriPrefixRegex = iriPrefixRegex;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);

        if (newTerm instanceof DBConstant) {
            String arg = ((DBConstant) newTerm).getValue();
            boolean isAbsoluteIRI;
            try {
                isAbsoluteIRI = ParsedIRI.create(arg).isAbsolute();
            } catch (IllegalArgumentException e) {
                isAbsoluteIRI = false;
            }
            return termFactory.getDBStringConstant(isAbsoluteIRI ? arg : baseIRI.getIRIString() + arg);
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
