package it.unibz.inf.ontop.answering.reformulation.generation.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer.ProjectionDecomposition;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class PostProcessingProjectionSplitterImpl implements PostProcessingProjectionSplitter {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ProjectionDecomposer avoidPostProcessingDecomposer;
    private final ProjectionDecomposer proPostProcessingDecomposer;
    private final DistinctNormalizer distinctNormalizer;

    @Inject
    private PostProcessingProjectionSplitterImpl(IntermediateQueryFactory iqFactory,
                                                 SubstitutionFactory substitutionFactory,
                                                 CoreUtilsFactory coreUtilsFactory,
                                                 DistinctNormalizer distinctNormalizer) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.avoidPostProcessingDecomposer = coreUtilsFactory.createProjectionDecomposer(
                PostProcessingProjectionSplitterImpl::hasFunctionalToBePostProcessed,
                t -> !(t.isNull() || (t instanceof Variable) || (t instanceof DBConstant)));
        this.proPostProcessingDecomposer = coreUtilsFactory.createProjectionDecomposer(
                ImmutableFunctionalTerm::canBePostProcessed, t -> true);
        this.distinctNormalizer = distinctNormalizer;
    }

    private static boolean hasFunctionalToBePostProcessed(ImmutableFunctionalTerm functionalTerm) {
        if (!functionalTerm.canBePostProcessed())
            return false;

        if (!(functionalTerm.getFunctionSymbol() instanceof DBFunctionSymbol))
            return true;

        return functionalTerm.getTerms().stream()
                .anyMatch(PostProcessingProjectionSplitterImpl::hasToBePostProcessed);
    }

    private static boolean hasToBePostProcessed(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm)
            return hasFunctionalToBePostProcessed((ImmutableFunctionalTerm) term);

        return !term.isNull() && (!(term instanceof DBConstant)) && (!(term instanceof Variable));
    }

    @Override
    public PostProcessingSplit split(IQ initialIQ, boolean avoidPostProcessing) {

        IQTree topTree = initialIQ.getTree();
        VariableGenerator variableGenerator = initialIQ.getVariableGenerator();

        return Optional.of(topTree)
                .filter(t -> t.getRootNode() instanceof ConstructionNode)
                .map(t -> (UnaryIQTree) t)
                .map(t -> split(t, variableGenerator, avoidPostProcessing))
                .orElseGet(() -> new PostProcessingSplitImpl(
                        // "Useless" construction node --> no post-processing
                        iqFactory.createConstructionNode(topTree.getVariables(), substitutionFactory.getSubstitution()),
                        topTree,
                        variableGenerator));
    }


    private PostProcessingSplit split(UnaryIQTree initialTree, VariableGenerator variableGenerator, boolean avoidPostProcessing) {

        ConstructionNode initialRootNode = (ConstructionNode) initialTree.getRootNode();
        IQTree initialSubTree = initialTree.getChild();

        ProjectionDecomposer decomposer = avoidPostProcessing ? avoidPostProcessingDecomposer : proPostProcessingDecomposer;

        ProjectionDecomposition decomposition = decomposer.decomposeSubstitution(initialRootNode.getSubstitution(), variableGenerator);

        ConstructionNode postProcessingNode = iqFactory.createConstructionNode(initialRootNode.getVariables(),
                decomposition.getTopSubstitution()
                .orElseGet(substitutionFactory::getSubstitution));

        ImmutableSet<Variable> newSubTreeVariables = postProcessingNode.getChildVariables();

        /*
         * NB: the presence of a subSubstitution implies the need to project new variables.
         */
        IQTree newSubTree = initialSubTree.getVariables().equals(newSubTreeVariables)
                ? initialSubTree
                : iqFactory.createUnaryIQTree(
                    decomposition.getSubSubstitution()
                        .map(s -> iqFactory.createConstructionNode(newSubTreeVariables, s))
                        .orElseGet(() -> iqFactory.createConstructionNode(newSubTreeVariables)),
                    initialSubTree);

        return new PostProcessingSplitImpl(postProcessingNode,
                normalizeNewSubTree(newSubTree, variableGenerator),
                variableGenerator);
    }

    /**
     * Tries to push down the possible root construction node under a SLICE and a DISTINCT
     *
     */
    private IQTree normalizeNewSubTree(IQTree newSubTree, VariableGenerator variableGenerator) {
        return Optional.of(newSubTree.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .map(c -> insertConstructionNode(((UnaryIQTree) newSubTree).getChild(), c, variableGenerator))
                .orElse(newSubTree);
    }

    /**
     * Recursive
     */
    private IQTree insertConstructionNode(IQTree tree, ConstructionNode constructionNode, VariableGenerator variableGenerator) {
        QueryNode rootNode = tree.getRootNode();
        if (rootNode instanceof SliceNode)
            return iqFactory.createUnaryIQTree(
                    (UnaryOperatorNode) rootNode,
                    // Recursive
                    insertConstructionNode(((UnaryIQTree)tree).getChild(), constructionNode, variableGenerator));
        /*
         * Distinct:
         * If
         *  1. the construction node could be fully lifted above the distinct,
         *  2. AND the distinct old projects the variables required by the construction node
         * THEN it can be pushed under the DISTINCT
         *
         * "Reverses" the binding lift operation except that here either all the bindings are pushed down or none.
         *
         * TODO: relax the condition 1?
         *
         */
        else if (rootNode instanceof DistinctNode) {
            IQTree childTree = ((UnaryIQTree) tree).getChild();

            // Stops if the child tree is projecting more variables than what the construction node expects
            if (!childTree.getVariables().equals(constructionNode.getChildVariables()))
                return iqFactory.createUnaryIQTree(
                        constructionNode,
                        tree
                );

            UnaryIQTree possibleChildTree = iqFactory.createUnaryIQTree(constructionNode, childTree);

            DistinctNode distinctNode = (DistinctNode) rootNode;

            IQTree liftedTree = distinctNormalizer.normalizeForOptimization(distinctNode, possibleChildTree, variableGenerator,
                    iqFactory.createIQTreeCache());

            return liftedTree.getRootNode().equals(constructionNode)
                    ? iqFactory.createUnaryIQTree(distinctNode, possibleChildTree)
                    : iqFactory.createUnaryIQTree(constructionNode, tree);
        }
        else
            return iqFactory.createUnaryIQTree(constructionNode, tree);
    }

    static class PostProcessingSplitImpl implements PostProcessingSplit {

        private final ConstructionNode postProcessingConstructionNode;
        private final IQTree subTree;
        private final VariableGenerator variableGenerator;

        PostProcessingSplitImpl(ConstructionNode postProcessingConstructionNode, IQTree subTree,
                                VariableGenerator variableGenerator) {
            this.postProcessingConstructionNode = postProcessingConstructionNode;
            this.subTree = subTree;
            this.variableGenerator = variableGenerator;
        }

        @Override
        public ConstructionNode getPostProcessingConstructionNode() {
            return postProcessingConstructionNode;
        }

        @Override
        public IQTree getSubTree() {
            return subTree;
        }

        @Override
        public VariableGenerator getVariableGenerator() {
            return variableGenerator;
        }
    }
}
