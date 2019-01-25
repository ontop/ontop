package it.unibz.inf.ontop.answering.reformulation.generation.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PostProcessingProjectionSplitterImpl implements PostProcessingProjectionSplitter {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private PostProcessingProjectionSplitterImpl(IntermediateQueryFactory iqFactory,
                                                 SubstitutionFactory substitutionFactory,
                                                 TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public PostProcessingSplit split(IQ initialIQ) {
        UnaryIQTree initialTree = Optional.of(initialIQ.getTree())
                .filter(t -> t.getRootNode() instanceof ConstructionNode)
                .map(t -> (UnaryIQTree) t)
                .orElseThrow(() -> new IllegalArgumentException(
                        "The iq to be split is not starting with a construction node.\n" + initialIQ));

        ConstructionNode initialRootNode = (ConstructionNode) initialTree.getRootNode();
        IQTree initialSubTree = initialTree.getChild();

        VariableGenerator variableGenerator = initialIQ.getVariableGenerator();

        ProjectionDecomposition decomposition = decomposeSubstitution(initialRootNode.getSubstitution(), variableGenerator);

        ConstructionNode postProcessingNode = iqFactory.createConstructionNode(initialRootNode.getVariables(),
                decomposition.postProcessingSubstitution);

        ImmutableSet<Variable> newSubTreeVariables = postProcessingNode.getChildVariables();

        /*
         * NB: the presence of a subSubstitution implies the need to project new variables.
         */
        IQTree newSubTree = initialSubTree.getVariables().equals(newSubTreeVariables)
                ? initialSubTree
                : iqFactory.createUnaryIQTree(
                    decomposition.subSubstitution
                        .map(s -> iqFactory.createConstructionNode(newSubTreeVariables, s))
                        .orElseGet(() -> iqFactory.createConstructionNode(newSubTreeVariables)),
                    initialSubTree);

        return new PostProcessingSplitImpl(postProcessingNode, newSubTree, variableGenerator);
    }

    private ProjectionDecomposition decomposeSubstitution(ImmutableSubstitution<ImmutableTerm> substitution,
                                                          VariableGenerator variableGenerator) {

        ImmutableMap<Variable, DefinitionDecomposition> decompositionMap = substitution.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> decomposeDefinition(e.getValue(), variableGenerator, Optional.of(e.getKey()))
                ));

        ImmutableSubstitution<ImmutableTerm> postProcessingSubstitution = substitutionFactory.getSubstitution(
                decompositionMap.entrySet().stream()
                        // To avoid entries like t/t
                        .filter(e -> !e.getKey().equals(e.getValue().term))
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().term
                )));

        Optional<ImmutableSubstitution<ImmutableTerm>> subSubstitution = combineSubstitutions(
                decompositionMap.values().stream()
                        .map(d -> d.substitution));

        return new ProjectionDecomposition(postProcessingSubstitution, subSubstitution);
    }

    /**
     * Recursive
     */
    private DefinitionDecomposition decomposeDefinition(ImmutableTerm term, VariableGenerator variableGenerator,
                                                        Optional<Variable> definedVariable) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            if (functionalTerm.canBePostProcessed()) {
                // Recursive
                ImmutableList<DefinitionDecomposition> childDecompositions = functionalTerm.getTerms().stream()
                        .map(t -> decomposeDefinition(t, variableGenerator, Optional.empty()))
                        .collect(ImmutableCollectors.toList());

                Optional<ImmutableSubstitution<ImmutableTerm>> subSubstitution = combineSubstitutions(
                        childDecompositions.stream()
                                .map(d -> d.substitution));

                ImmutableFunctionalTerm newFunctionalTerm = subSubstitution
                        .map(s -> childDecompositions.stream()
                                .map(d -> d.term)
                                .collect(ImmutableCollectors.toList()))
                        .map(children -> termFactory.getImmutableFunctionalTerm(
                                functionalTerm.getFunctionSymbol(), children))
                        .orElse(functionalTerm);

                return subSubstitution
                        .map(s -> new DefinitionDecomposition(newFunctionalTerm, s))
                        .orElse(new DefinitionDecomposition(functionalTerm));
            }
            else {
                Variable variable = definedVariable
                        .orElseGet(variableGenerator::generateNewVariable);

                // Wraps variables replacing an expression into an IS_TRUE functional term
                ImmutableTerm newTerm = ((!definedVariable.isPresent())
                        && (functionalTerm instanceof ImmutableExpression))
                        ? termFactory.getIsTrue(variable)
                        : variable;

                return new DefinitionDecomposition(newTerm,
                        substitutionFactory.getSubstitution(variable, functionalTerm));
            }
        }
        else
            return new DefinitionDecomposition(term);
    }

    private Optional<ImmutableSubstitution<ImmutableTerm>> combineSubstitutions(
            Stream<Optional<ImmutableSubstitution<ImmutableTerm>>> stream) {
        return stream
                .filter(Optional::isPresent)
                .map(Optional::get)
                // The composition here behaves like an union (independent fresh variables)
                .reduce(ImmutableSubstitution::composeWith);
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class ProjectionDecomposition {
        final ImmutableSubstitution<ImmutableTerm> postProcessingSubstitution;
        final Optional<ImmutableSubstitution<ImmutableTerm>> subSubstitution;

        private ProjectionDecomposition(ImmutableSubstitution<ImmutableTerm> postProcessingSubstitution,
                                        Optional<ImmutableSubstitution<ImmutableTerm>> subSubstitution) {
            this.postProcessingSubstitution = postProcessingSubstitution;
            this.subSubstitution = subSubstitution;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class DefinitionDecomposition {
        final ImmutableTerm term;
        final Optional<ImmutableSubstitution<ImmutableTerm>> substitution;

        private DefinitionDecomposition(ImmutableTerm term,ImmutableSubstitution<ImmutableTerm> substitution) {
            this.term = term;
            this.substitution = Optional.of(substitution);
        }

        private DefinitionDecomposition(ImmutableTerm term) {
            this.term = term;
            this.substitution = Optional.empty();
        }
    }
}
