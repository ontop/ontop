package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.impl.AbstractTermTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * The CDataDynamoDB driver seems to be struggling with the boolean operators AND and OR.
 * However, converting them to the opposite operators using De Morgan's law seems to fix these issues.
 */
public class ReformulateConjunctionDisjunctionNormalizer implements DialectExtraNormalizer {

    private final CoreSingletons coreSingletons;
    private final Transformer transformer;

    @Inject
    protected ReformulateConjunctionDisjunctionNormalizer(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.transformer = new Transformer();
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
        protected boolean isFunctionSymbolToReplace(FunctionSymbol fs) {
            return (fs instanceof DBOrFunctionSymbol) || (fs instanceof DBAndFunctionSymbol);
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol fs, ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (fs instanceof DBOrFunctionSymbol) {
                return negate(termFactory.getImmutableFunctionalTerm(
                        termFactory.getDBFunctionSymbolFactory().getDBAnd(fs.getArity()),
                        newTerms));
            }
            if (fs instanceof DBAndFunctionSymbol) {
                return negate(termFactory.getImmutableFunctionalTerm(
                        termFactory.getDBFunctionSymbolFactory().getDBOr(fs.getArity()),
                        newTerms));
            }
            throw new MinorOntopInternalBugException("Unsupported function symbol: " + fs);
        }

        private ImmutableFunctionalTerm negate(ImmutableFunctionalTerm term) {
            return termFactory.getImmutableFunctionalTerm(
                    termFactory.getDBFunctionSymbolFactory().getDBNot(), term);
        }
    }
}
