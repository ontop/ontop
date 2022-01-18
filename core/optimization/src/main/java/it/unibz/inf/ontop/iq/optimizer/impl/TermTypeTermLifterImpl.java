package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
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

import java.util.Optional;

@Singleton
public class TermTypeTermLifterImpl implements TermTypeTermLifter {

    private final OptimizerFactory transformerFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private TermTypeTermLifterImpl(OptimizerFactory transformerFactory, IntermediateQueryFactory iqFactory,
                                   SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.transformerFactory = transformerFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
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
        return Optional.of(tree.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .map(ConstructionNode::getSubstitution)
                .map(s -> s.transform(this::makeRDFTermTypeFunctionSymbolsSimplifiable))
                .map(s -> iqFactory.createConstructionNode(tree.getVariables(), s))
                .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, ((UnaryIQTree) tree).getChild()))
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
