package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.impl.FullLinearInclusionDependenciesImpl;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Class provides the functionalities for the geosparql query rewrite extension
 */
public class GeoRewriter extends DummyRewriter {

    protected GeoRewriter(IntermediateQueryFactory iqFactory, AtomFactory atomFactory, TermFactory termFactory,
                          CoreUtilsFactory coreUtilsFactory, FullLinearInclusionDependenciesImpl<RDFAtomPredicate> sigma,
                          FunctionSymbolFactory functionSymbolFactory) {
        super(iqFactory, atomFactory, termFactory, coreUtilsFactory, functionSymbolFactory);
        this.sigma = sigma;
    }

    @Override
    public IQ rewrite(IQ query) throws EmptyQueryException {
        VariableGenerator variableGenerator = query.getVariableGenerator();

        // Update tree
        IQTree geoReWrittenTree = getGeoRewrittenTree(query.getTree(), variableGenerator);

        // Add additional construction node - necessary for Feature-Feature case
        ConstructionNode rootNode = iqFactory.createConstructionNode(query.getTree().getVariables());
        UnaryIQTree updatedTree = iqFactory.createUnaryIQTree(rootNode, geoReWrittenTree);

        return iqFactory.createIQ(query.getProjectionAtom(),
                updatedTree.acceptTransformer(new BasicGraphPatternTransformer(iqFactory) {
                    @Override
                    protected ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> bgp) {
                        return removeRedundantAtoms(bgp);
                    }
                }));
    }

    /**
     * The method expands the geosparql query rewrite extension intensional node into the respective triples
     * which generate the wkt literal.
     * @return IQTree with additional rdf triples that map wkt literal.
     */
    private IQTree getGeoRewrittenTree(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(new GeoQueryRewriterTransformer(iqFactory, atomFactory,
                variableGenerator,
                termFactory,
                functionSymbolFactory) {
            @Override
            protected ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> bgp) {
                return removeRedundantAtoms(bgp);
            }
        });
    }

}
