package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.impl.AbstractTermTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * The CDataDynamoDB driver seems to be struggling with the boolean operators AND and OR.
 * However, converting them to the opposite operators using De Morgan's law seems to fix these issues.
 */
public class ReformulateConjunctionDisjunctionNormalizer implements DialectExtraNormalizer {

    private final CoreSingletons coreSingletons;
    private final IQVisitor<IQTree> transformer;

    @Inject
    protected ReformulateConjunctionDisjunctionNormalizer(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.transformer = new Transformer().treeTransformer();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends AbstractTermTransformer {
        Transformer() {
            super(coreSingletons);
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
