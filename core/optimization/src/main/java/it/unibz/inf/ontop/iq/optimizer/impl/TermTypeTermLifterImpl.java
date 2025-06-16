package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
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
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

@Singleton
public class TermTypeTermLifterImpl extends AbstractIQOptimizer implements TermTypeTermLifter {

    private final OptimizerFactory transformerFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private TermTypeTermLifterImpl(OptimizerFactory transformerFactory,
                                   IntermediateQueryFactory iqFactory,
                                   TermFactory termFactory, IQTreeTools iqTreeTools) {
        super(iqFactory, NO_ACTION);
        this.transformerFactory = transformerFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        TermTypeTermLiftTransformer transformer = transformerFactory.createRDFTermTypeConstantTransformer(
                variableGenerator);
        IQTree transformedTree = transformer.transform(tree);

        return makeRDFTermTypeFunctionSymbolsSimplifiable(transformedTree, variableGenerator);
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
    private IQTree makeRDFTermTypeFunctionSymbolsSimplifiable(IQTree tree, VariableGenerator variableGenerator) {
        var construction = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
        return iqTreeTools.unaryIQTreeBuilder()
                .append(construction.getOptionalNode()
                        .map(cn -> iqTreeTools.replaceSubstitution(
                                cn,
                                s -> s.transform(this::makeRDFTermTypeFunctionSymbolsSimplifiable))))
                .build(construction.getTail())
                .normalizeForOptimization(variableGenerator);
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
