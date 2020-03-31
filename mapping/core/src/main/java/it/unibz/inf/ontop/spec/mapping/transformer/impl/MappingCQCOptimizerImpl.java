package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final CoreSingletons coreSingletons;

    @Inject
    public MappingCQCOptimizerImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                   CoreSingletons coreSingletons) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(ImmutableCQContainmentCheck<RelationPredicate> cqContainmentCheck, IQ query) {

        IQTree tree0 = new FilterAbsorber(iqFactory, termFactory).apply(query.getTree());

        IQTree tree = new InnerJoinFlattener(iqFactory, termFactory).apply(tree0);

        if (tree.getRootNode() instanceof ConstructionNode && tree.getChildren().size() == 1) {
            ConstructionNode constructionNode = (ConstructionNode)tree.getRootNode();
            IQTree joinTree = tree.getChildren().get(0);
            Optional<ImmutableList<ExtensionalDataNode>> c = IQ2CQ.getExtensionalDataNodes(joinTree);
            if (c.isPresent() && c.get().size() > 1) {
                InnerJoinNode joinNode = (InnerJoinNode) joinTree.getRootNode();
                ImmutableList<Variable> answerVariables = Stream.concat(
                        constructionNode.getSubstitution().getImmutableMap().values().stream()
                                .flatMap(ImmutableTerm::getVariableStream),
                        joinNode.getOptionalFilterCondition()
                                .map(ImmutableTerm::getVariableStream).orElse(Stream.of()))
                        .distinct()
                        .collect(ImmutableCollectors.toList());

                ImmutableList<ExtensionalDataNode> children = c.get();
                int currentIndex = 0;
                while (currentIndex < children.size()) {
                    ImmutableList.Builder<ExtensionalDataNode> builder = ImmutableList.builder();
                    for (int i = 0; i < children.size(); i++)
                        if (i != currentIndex)
                            builder.add(children.get(i));
                    ImmutableList<ExtensionalDataNode> subChildren = builder.build();

                    if (subChildren.stream()
                            .flatMap(a -> a.getVariables().stream())
                            .collect(ImmutableCollectors.toSet())
                            .containsAll(answerVariables)) {

                        if (cqContainmentCheck.isContainedIn(new ImmutableCQ<>(answerVariables,
                                IQ2CQ.toDataAtoms(subChildren, coreSingletons)),
                                new ImmutableCQ<>(answerVariables, IQ2CQ.toDataAtoms(children, coreSingletons)))) {
                            children = subChildren;
                            if (children.size() < 2)
                                break;
                            currentIndex = 0; // reset
                        }
                        else
                            currentIndex++;
                    }
                    else
                        currentIndex++;
                }

                return iqFactory.createIQ(
                        query.getProjectionAtom(),
                        iqFactory.createUnaryIQTree(
                                (ConstructionNode)tree.getRootNode(),
                                IQ2CQ.toIQTree(children, joinNode.getOptionalFilterCondition(), iqFactory)));
            }
        }
        return iqFactory.createIQ(query.getProjectionAtom(), tree);
    }
}
