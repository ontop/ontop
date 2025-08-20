package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeListContainmentCheck;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

import java.util.*;
import java.util.stream.Stream;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private static final Logger log = LoggerFactory.getLogger(MappingCQCOptimizerImpl.class);
    
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    public MappingCQCOptimizerImpl(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    @Override
    public IQ optimize(ExtensionalDataNodeListContainmentCheck cqContainmentCheck, IQ query) {

        IQTree tree = query.getTree();
        var construction = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);

        return iqFactory.createIQ(query.getProjectionAtom(),
                tree.acceptVisitor(new Transformer(
                        construction.getNode().getSubstitution().getRangeVariables(), cqContainmentCheck)));
    }

    // TODO: compare with RedundantJoinFKTransformer
    // used to use == for comparing the transformed children
    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final ImmutableSet<Variable> constructionTreeVariables;
        private final ExtensionalDataNodeListContainmentCheck cqContainmentCheck;

        Transformer(ImmutableSet<Variable> constructionTreeVariables, ExtensionalDataNodeListContainmentCheck cqContainmentCheck) {
            super(MappingCQCOptimizerImpl.this.iqFactory);
            this.cqContainmentCheck = cqContainmentCheck;
            this.constructionTreeVariables = constructionTreeVariables;
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode innerJoinNode, ImmutableList<IQTree> children0) {

            Optional<ImmutableExpression> joiningConditions = innerJoinNode.getOptionalFilterCondition();

            ImmutableList<ExtensionalDataNode> extensionalDataNodes = tree.getChildren().stream()
                    .filter(n -> n instanceof ExtensionalDataNode)
                    .map(n -> (ExtensionalDataNode)n)
                    .collect(ImmutableCollectors.toList());

            ImmutableList<Variable> answerVariables = Stream.concat(
                            constructionTreeVariables.stream(),
                            joiningConditions.stream().flatMap(ImmutableTerm::getVariableStream))
                    .distinct()
                    .collect(ImmutableCollectors.toList());

            int currentIndex = 0;
            while (currentIndex < extensionalDataNodes.size()) {
                ExtensionalDataNode current = extensionalDataNodes.get(currentIndex);
                ImmutableList<ExtensionalDataNode> extensionalDataNodesExceptCurrent = extensionalDataNodes.stream()
                        .filter(child -> child != current)
                        .collect(ImmutableCollectors.toList());

                if (extensionalDataNodesExceptCurrent.stream()
                        .flatMap(a -> a.getVariables().stream())
                        .collect(ImmutableCollectors.toSet())
                        .containsAll(answerVariables)) {

                    if (cqContainmentCheck.isContainedIn(
                            answerVariables, extensionalDataNodesExceptCurrent, ImmutableSet.of(), answerVariables, extensionalDataNodes)) {
                        //System.out.println("CQC-REMOVED: " + extensionalDataNodes.get(currentIndex) + " FROM " + extensionalDataNodes);
                        log.debug("CQC-REMOVED: " + extensionalDataNodes.get(currentIndex) + " FROM " + extensionalDataNodes);
                        extensionalDataNodes = extensionalDataNodesExceptCurrent;
                        if (extensionalDataNodes.size() < 2)
                            break;
                        currentIndex = 0; // reset
                    }
                    else
                        currentIndex++;
                }
                else
                    currentIndex++;
            }

            ImmutableList<IQTree> children = Stream.concat(
                            extensionalDataNodes.stream(),
                            tree.getChildren().stream()
                                    .filter(n -> !(n instanceof ExtensionalDataNode)))
                    .collect(ImmutableCollectors.toList());

            return iqTreeTools.createOptionalInnerJoinTree(joiningConditions, children)
                    .orElseGet(iqFactory::createTrueNode);
        }
    }
}
