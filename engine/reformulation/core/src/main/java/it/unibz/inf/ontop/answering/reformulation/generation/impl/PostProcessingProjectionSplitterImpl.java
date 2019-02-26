package it.unibz.inf.ontop.answering.reformulation.generation.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer.ProjectionDecomposition;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class PostProcessingProjectionSplitterImpl implements PostProcessingProjectionSplitter {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ProjectionDecomposer decomposer;

    @Inject
    private PostProcessingProjectionSplitterImpl(IntermediateQueryFactory iqFactory,
                                                 SubstitutionFactory substitutionFactory,
                                                 CoreUtilsFactory coreUtilsFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.decomposer = coreUtilsFactory.createProjectionDecomposer(ImmutableFunctionalTerm::canBePostProcessed);
    }

    @Override
    public PostProcessingSplit split(IQ initialIQ) {

        IQTree topTree = initialIQ.getTree();
        VariableGenerator variableGenerator = initialIQ.getVariableGenerator();

        return Optional.of(topTree)
                .filter(t -> t.getRootNode() instanceof ConstructionNode)
                .map(t -> (UnaryIQTree) t)
                .map(t -> split(t, variableGenerator))
                .orElseGet(() -> new PostProcessingSplitImpl(
                        // "Useless" construction node --> no post-processing
                        iqFactory.createConstructionNode(topTree.getVariables(), substitutionFactory.getSubstitution()),
                        topTree,
                        variableGenerator));
    }


    private PostProcessingSplit split(UnaryIQTree initialTree, VariableGenerator variableGenerator) {

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
        IQTree newSubTree = initialSubTree.getVariables().equals(newSubTreeVariables)
                ? initialSubTree
                : iqFactory.createUnaryIQTree(
                    decomposition.getSubSubstitution()
                        .map(s -> iqFactory.createConstructionNode(newSubTreeVariables, s))
                        .orElseGet(() -> iqFactory.createConstructionNode(newSubTreeVariables)),
                    initialSubTree);

        return new PostProcessingSplitImpl(postProcessingNode, newSubTree, variableGenerator);
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
