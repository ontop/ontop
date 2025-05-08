package it.unibz.inf.ontop.iq.optimizer.splitter.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.optimizer.splitter.PreventDistinctProjectionSplitter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.IntStream;

public class PreventDistinctProjectionSplitterImpl extends ProjectionSplitterImpl implements PreventDistinctProjectionSplitter {

    private final ProjectionDecomposer decomposer;

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private PreventDistinctProjectionSplitterImpl(IntermediateQueryFactory iqFactory,
                                                  SubstitutionFactory substitutionFactory,
                                                  CoreUtilsFactory coreUtilsFactory,
                                                  DistinctNormalizer distinctNormalizer,
                                                  IQTreeTools iqTreeTools) {
        super(iqFactory, substitutionFactory, distinctNormalizer);
        this.decomposer = coreUtilsFactory.createProjectionDecomposer(
            t -> !shouldSplit(t),
            n -> true
        );
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public ProjectionSplit split(IQTree tree, VariableGenerator variableGenerator) {
        return split(tree, variableGenerator, decomposer);
    }

    private static boolean shouldSplit(ImmutableFunctionalTerm term) {
        return term.getTerms().stream().anyMatch(PreventDistinctProjectionSplitterImpl::shouldPreventDistinct)
                || IntStream.range(0, term.getArity())
                .mapToObj(i -> term.getFunctionSymbol().getExpectedBaseType(i))
                .filter(t -> t instanceof DBTermType)
                .anyMatch(t -> ((DBTermType) t).isPreventDistinctRecommended());
    }

    private static boolean shouldPreventDistinct(ImmutableTerm term) {
        var type = term.inferType();
        return type.flatMap(TermTypeInference::getTermType)
                .filter(t -> t instanceof DBTermType)
                .map(t -> (DBTermType) t)
                .map(DBTermType::isPreventDistinctRecommended)
                .orElse(false);
    }

    @Override
    protected IQTree insertConstructionNode(IQTree tree, ConstructionNode constructionNode, VariableGenerator variableGenerator) {
        var distinct = IQTreeTools.UnaryIQTreeDecomposition.of(tree, DistinctNode.class);
        if (distinct.isPresent()) {
            /* We can bypass the security check for pushing the CONSTRUCT into the DISTINCT used by the normal ProjectionSplitter,
             * as the general circumstances of this use case already revolve around that scenario.
             */
            return iqTreeTools.createUnaryIQTree(
                    distinct.getNode(),
                    constructionNode,
                    distinct.getChild());
        }
        return super.insertConstructionNode(tree, constructionNode, variableGenerator);
    }
}
