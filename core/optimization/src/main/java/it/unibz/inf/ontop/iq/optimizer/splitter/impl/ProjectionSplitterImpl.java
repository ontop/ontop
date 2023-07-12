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
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

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

    @Override
    public ProjectionSplit split(IQ initialIQ) {
        return split(initialIQ.getTree(), initialIQ.getVariableGenerator());
    }

    protected ProjectionSplit split(IQ initialIQ, ProjectionDecomposer decomposer) {
        return split(initialIQ.getTree(), initialIQ.getVariableGenerator(), decomposer);
    }

    protected ProjectionSplit split(IQTree topTree, VariableGenerator variableGenerator, ProjectionDecomposer decomposer) {

        return Optional.of(topTree)
                .filter(t -> t.getRootNode() instanceof ConstructionNode)
                .map(t -> (UnaryIQTree) t)
                .map(t -> split(t, variableGenerator, decomposer))
                .orElseGet(() -> new ProjectionSplitImpl(
                        // "Useless" construction node --> no post-processing
                        iqFactory.createConstructionNode(topTree.getVariables(), substitutionFactory.getSubstitution()),
                        topTree,
                        variableGenerator,
                        ImmutableSet.of(),
                        ImmutableSet.of()));
    }


    private ProjectionSplit split(UnaryIQTree initialTree, VariableGenerator variableGenerator, ProjectionDecomposer decomposer) {

        ConstructionNode initialRootNode = (ConstructionNode) initialTree.getRootNode();
        IQTree initialSubTree = initialTree.getChild();

        ProjectionDecomposition decomposition = decomposer.decomposeSubstitution(initialRootNode.getSubstitution(), variableGenerator);

        ConstructionNode postProcessingNode = iqFactory.createConstructionNode(initialRootNode.getVariables(),
                decomposition.getTopSubstitution()
                .orElseGet(substitutionFactory::getSubstitution));

        ImmutableSet<Variable> newSubTreeVariables = postProcessingNode.getChildVariables();

        /*
         * NB: the presence of a subSubstitution implies the need to project new variables.
         */
        IQTree newSubTree = initialSubTree.getVariables().containsAll(newSubTreeVariables)
                ? initialSubTree
                : iqFactory.createUnaryIQTree(
                    decomposition.getSubSubstitution()
                        .map(s -> iqFactory.createConstructionNode(newSubTreeVariables, s))
                        .orElseGet(() -> iqFactory.createConstructionNode(newSubTreeVariables)),
                    initialSubTree);

        return new ProjectionSplitImpl(postProcessingNode,
                normalizeNewSubTree(newSubTree, variableGenerator),
                variableGenerator,
                decomposition.getSubSubstitution()
                        .map(Substitution::getRangeVariables)
                        .orElseGet(ImmutableSet::of),
                decomposition.getSubSubstitution()
                        .map(Substitution::getRangeSet)
                        .orElseGet(ImmutableSet::of));
    }

    /**
     * Tries to push down the possible root construction node under a SLICE and a DISTINCT
     *
     */
    protected IQTree normalizeNewSubTree(IQTree newSubTree, VariableGenerator variableGenerator) {
        return Optional.of(newSubTree.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .map(c -> insertConstructionNode(((UnaryIQTree) newSubTree).getChild(), c, variableGenerator))
                .orElse(newSubTree);
    }

    /**
     * Recursive
     */
    protected IQTree insertConstructionNode(IQTree tree, ConstructionNode constructionNode, VariableGenerator variableGenerator) {
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

    static class ProjectionSplitImpl implements ProjectionSplit {

        private final ConstructionNode constructionNode;
        private final IQTree subTree;
        private final VariableGenerator variableGenerator;
        private final ImmutableSet<Variable> pushedVariables;
        private final ImmutableSet<ImmutableTerm> pushedTerms;

        ProjectionSplitImpl(ConstructionNode constructionNode, IQTree subTree,
                                VariableGenerator variableGenerator, ImmutableSet<Variable> pushedVariables,
                            ImmutableSet<ImmutableTerm> pushedTerms) {
            this.constructionNode = constructionNode;
            this.subTree = subTree;
            this.variableGenerator = variableGenerator;
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
        public VariableGenerator getVariableGenerator() {
            return variableGenerator;
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
