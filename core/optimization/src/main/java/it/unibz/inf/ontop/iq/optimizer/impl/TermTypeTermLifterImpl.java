package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.optimizer.TermTypeTermLifter;
import it.unibz.inf.ontop.iq.transformer.TermTypeTermLiftTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

@Singleton
public class TermTypeTermLifterImpl implements TermTypeTermLifter {

    private final OptimizerFactory transformerFactory;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private TermTypeTermLifterImpl(OptimizerFactory transformerFactory,
                                   IntermediateQueryFactory iqFactory,
                                   TermFactory termFactory, IQTreeTools iqTreeTools) {
        this.transformerFactory = transformerFactory;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQ optimize(IQ query) {
        TermTypeTermLiftTransformer transformer = transformerFactory.createRDFTermTypeConstantTransformer(
                query.getVariableGenerator());
        IQTree transformedTree = transformer.transform(query.getTree());

        IQTree newTree = makeRDFTermTypeFunctionSymbolsSimplifiable(transformedTree);

        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    /**
     * ONLY in the root construction node (if there is such a node)
     *
     * Makes sure all the RDFTermTypeFunctionSymbol-s are simplifiable and therefore post-processable.
     *
     * Note that such function symbols are only expected at this stage in the root
     * (they cannot be processed by the DB engine).
     *
     */
    private IQTree makeRDFTermTypeFunctionSymbolsSimplifiable(IQTree tree) {
        return UnaryIQTreeDecomposition.of(tree, ConstructionNode.class)
                .<IQTree>map((cn, t) ->
                        iqFactory.createUnaryIQTree(
                                iqTreeTools.replaceSubstitution(cn,
                                        cn.getSubstitution()
                                                .transform(this::makeRDFTermTypeFunctionSymbolsSimplifiable)),
                                t))
                .orElse(tree);
    }

    /**
     * Recursive
     */
    private ImmutableTerm makeRDFTermTypeFunctionSymbolsSimplifiable(ImmutableTerm immutableTerm) {
        if (immutableTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) immutableTerm;
            ImmutableList<ImmutableTerm> newTerms = functionalTerm.getTerms().stream()
                    //Recursive
                    .map(this::makeRDFTermTypeFunctionSymbolsSimplifiable)
                    .collect(ImmutableCollectors.toList());

            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();
            FunctionSymbol newFunctionSymbol = (functionSymbol instanceof RDFTermTypeFunctionSymbol)
                    ? ((RDFTermTypeFunctionSymbol) functionSymbol).getSimplifiableVariant()
                    : functionSymbol;

            return termFactory.getImmutableFunctionalTerm(newFunctionSymbol, newTerms);
        }
        else
            return immutableTerm;
    }

}
