package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeListContainmentCheck;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.LazyRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private static final Logger log = LoggerFactory.getLogger(MappingCQCOptimizerImpl.class);
    
    private final IntermediateQueryFactory iqFactory;

    @Inject
    public MappingCQCOptimizerImpl(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQ optimize(ExtensionalDataNodeListContainmentCheck cqContainmentCheck, IQ query) {

        IQTree tree = query.getTree();
        ConstructionNode constructionNode = (ConstructionNode) tree.getRootNode();

        return iqFactory.createIQ(query.getProjectionAtom(), tree.acceptTransformer(new LazyRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children0) {

                Optional<ImmutableExpression> joiningConditions = rootNode.getOptionalFilterCondition();

                ImmutableList<ExtensionalDataNode> extensionalDataNodes = tree.getChildren().stream()
                        .filter(n -> n instanceof ExtensionalDataNode)
                        .map(n -> (ExtensionalDataNode)n)
                        .collect(ImmutableCollectors.toList());

                ImmutableList<Variable> answerVariables = Stream.concat(
                                constructionNode.getSubstitution().getRangeVariables().stream(),
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
                                answerVariables, extensionalDataNodesExceptCurrent, answerVariables, extensionalDataNodes)) {
                            System.out.println("CQC-REMOVED: " + extensionalDataNodes.get(currentIndex) + " FROM " + extensionalDataNodes);
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
                                tree.getChildren().stream().filter(n -> !(n instanceof ExtensionalDataNode)))
                        .collect(ImmutableCollectors.toList());

                switch (children.size()) {
                    case 0:
                        return iqFactory.createTrueNode();
                    case 1:
                        return joiningConditions
                                .<IQTree>map(c -> iqFactory.createUnaryIQTree(iqFactory.createFilterNode(c), children.get(0)))
                                .orElseGet(() -> children.get(0));
                    default:
                        return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(joiningConditions), children);
                }
            }
        }));
    }
}
