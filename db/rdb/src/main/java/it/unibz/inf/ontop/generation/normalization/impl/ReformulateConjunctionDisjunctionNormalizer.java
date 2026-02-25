package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.type.impl.AbstractTermTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;

import java.util.Optional;

/**
 * The CDataDynamoDB driver seems to be struggling with the boolean operators AND and OR.
 * However, converting them to the opposite operators using De Morgan's law seems to fix these issues.
 */
public class ReformulateConjunctionDisjunctionNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected ReformulateConjunctionDisjunctionNormalizer(CoreSingletons coreSingletons) {
        super(new TermTransformer(coreSingletons).treeTransformer());
    }

    private static class TermTransformer extends AbstractTermTransformer {
        TermTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons.getIQFactory(), coreSingletons.getTermFactory());
        }

        @Override
        protected Optional<ImmutableFunctionalTerm> replaceFunctionSymbol(FunctionSymbol fs, ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (fs instanceof DBOrFunctionSymbol) {
                return Optional.of(negate(termFactory.getImmutableFunctionalTerm(
                        termFactory.getDBFunctionSymbolFactory().getDBAnd(fs.getArity()),
                        newTerms)));
            }
            if (fs instanceof DBAndFunctionSymbol) {
                return Optional.of(negate(termFactory.getImmutableFunctionalTerm(
                        termFactory.getDBFunctionSymbolFactory().getDBOr(fs.getArity()),
                        newTerms)));
            }
            return Optional.empty();
        }

        private ImmutableFunctionalTerm negate(ImmutableFunctionalTerm term) {
            return termFactory.getImmutableFunctionalTerm(
                    termFactory.getDBFunctionSymbolFactory().getDBNot(), term);
        }
    }
}
