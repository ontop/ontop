package it.unibz.inf.ontop.iq.optimizer.splitter.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.optimizer.splitter.ProjectionSplitter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer.ProjectionDecomposition;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public abstract class ProjectionSplitterImpl implements ProjectionSplitter {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final DistinctNormalizer distinctNormalizer;

    protected ProjectionSplitterImpl(IntermediateQueryFactory iqFactory,
                                   SubstitutionFactory substitutionFactory,
                                   DistinctNormalizer distinctNormalizer) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.distinctNormalizer = distinctNormalizer;
    }

    protected ProjectionSplit split(IQTree topTree, VariableGenerator variableGenerator, ProjectionDecomposer decomposer) {

        var construction = UnaryIQTreeDecomposition.of(topTree, ConstructionNode.class);
        if (!construction.isPresent())
            return new ProjectionSplitImpl(
                // "Useless" construction node --> no post-processing
                iqFactory.createConstructionNode(topTree.getVariables()),
                topTree,
                ImmutableSet.of(),
                ImmutableSet.of());

        var initialRootNode = construction.getNode();
        var initialSubTree = construction.getChild();

        ProjectionDecomposition decomposition = decomposer.decomposeSubstitution(initialRootNode.getSubstitution(), variableGenerator);

        ConstructionNode postProcessingNode = iqFactory.createConstructionNode(initialRootNode.getVariables(),
                decomposition.getTopSubstitution()
                        .orElseGet(substitutionFactory::getSubstitution));

        ImmutableSet<Variable> newSubTreeVariables = postProcessingNode.getChildVariables();

        var subSubstitution = decomposition.getSubSubstitution()
                .orElseGet(substitutionFactory::getSubstitution);

        /*
         * NB: the presence of a subSubstitution implies the need to project new variables.
         */
        IQTree newSubTree = initialSubTree.getVariables().containsAll(newSubTreeVariables)
                ? initialSubTree
                : iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(
                        newSubTreeVariables,
                        subSubstitution),
                initialSubTree);

        return new ProjectionSplitImpl(postProcessingNode,
                normalizeNewSubTree(newSubTree, variableGenerator),
                subSubstitution.getRangeVariables(),
                subSubstitution.getRangeSet());
    }

    /**
     * Tries to push down the possible root construction node under a SLICE and a DISTINCT
     *
     */
    protected IQTree normalizeNewSubTree(IQTree newSubTree, VariableGenerator variableGenerator) {
        var slice = UnaryIQTreeDecomposition.of(newSubTree, ConstructionNode.class);
        return slice.isPresent()
                ? insertConstructionNode(slice.getChild(), slice.getNode(), variableGenerator)
                : newSubTree;
    }

    /**
     * Recursive
     * Overridden!
     */
    protected IQTree insertConstructionNode(IQTree tree, ConstructionNode constructionNode, VariableGenerator variableGenerator) {
        var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
        if (slice.isPresent())
            return iqFactory.createUnaryIQTree(
                    slice.getNode(),
                    // Recursive
                    insertConstructionNode(slice.getChild(), constructionNode, variableGenerator));
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
        var distinct = UnaryIQTreeDecomposition.of(tree, DistinctNode.class);
        if (distinct.isPresent()) {
            // Stops if the child tree is projecting more variables than what the construction node expects
            if (!distinct.getChild().getVariables().equals(constructionNode.getChildVariables()))
                return iqFactory.createUnaryIQTree(
                        constructionNode,
                        tree);

            UnaryIQTree possibleChildTree = iqFactory.createUnaryIQTree(constructionNode, distinct.getChild());

            IQTree liftedTree = distinctNormalizer.normalizeForOptimization(distinct.getNode(), possibleChildTree, variableGenerator,
                    iqFactory.createIQTreeCache(false));

            return liftedTree.getRootNode().equals(constructionNode)
                    ? iqFactory.createUnaryIQTree(distinct.getNode(), possibleChildTree)
                    : iqFactory.createUnaryIQTree(constructionNode, tree);
        }
        return iqFactory.createUnaryIQTree(constructionNode, tree);
    }

    static class ProjectionSplitImpl implements ProjectionSplit {

        private final ConstructionNode constructionNode;
        private final IQTree subTree;
        private final ImmutableSet<Variable> pushedVariables;
        private final ImmutableSet<ImmutableTerm> pushedTerms;

        ProjectionSplitImpl(ConstructionNode constructionNode,
                            IQTree subTree,
                            ImmutableSet<Variable> pushedVariables,
                            ImmutableSet<ImmutableTerm> pushedTerms) {
            this.constructionNode = constructionNode;
            this.subTree = subTree;
            this.pushedVariables = pushedVariables;
            this.pushedTerms = pushedTerms;
        }

        @Override
        public ConstructionNode getConstructionNode() {
            return constructionNode;
        }

        @Override
        public IQTree getSubTree() {
            return subTree;
        }

        @Override
        public ImmutableSet<Variable> getPushedVariables() {
            return pushedVariables;
        }

        @Override
        public ImmutableSet<ImmutableTerm> getPushedTerms() {
            return pushedTerms;
        }
    }
}
