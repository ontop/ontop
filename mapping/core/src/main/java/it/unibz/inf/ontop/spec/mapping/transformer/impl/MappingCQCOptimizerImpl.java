package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    public MappingCQCOptimizerImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(ImmutableCQContainmentCheck cqContainmentCheck, IQ query) {
        IQTree tree = query.getTree();
        if (tree.getRootNode() instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode)tree.getRootNode();
            if (tree.getChildren().size() == 1 && tree.getChildren().get(0).getRootNode() instanceof InnerJoinNode) {
                IQTree joinTree = tree.getChildren().get(0);
                InnerJoinNode joinNode = (InnerJoinNode) joinTree.getRootNode();
                ImmutableSet<Variable> answerVariables = Stream.concat(
                        constructionNode.getSubstitution().getImmutableMap().values().stream()
                                .flatMap(t -> t.getVariableStream()),
                        joinNode.getOptionalFilterCondition()
                                .map(f -> f.getVariableStream()).orElse(Stream.of()))
                            .collect(ImmutableCollectors.toSet());
                System.out.println("CQC " + query + " WITH " + answerVariables);
                if (joinTree.getChildren().stream().anyMatch(c -> !(c.getRootNode() instanceof DataNode))) {
                    System.out.println("CQC PANIC - NOT A JOIN OF DATA ATOMS");
                }
                else {
                    if (joinTree.getChildren().size() < 2) {
                        System.out.println("CQC: NOTING TO OPTIMIZE");
                        return query;
                    }

                    List<IQTree> children = new ArrayList<>(joinTree.getChildren());
                    for (int i = 0; i < children.size(); i++) {
                        IQTree toBeRemoved = children.get(i);

                        List<IQTree> toLeave = new ArrayList<>(children.size() - 1);
                        for (IQTree a: children)
                            if (a != toBeRemoved)
                                toLeave.add(a);

                        ImmutableSet<Variable> variablesInToLeave = toLeave.stream().flatMap(a -> a.getVariables().stream()).collect(ImmutableCollectors.toSet());
                        if (!variablesInToLeave.containsAll(answerVariables))
                            continue;

                        System.out.println("CHECK H: " + children + " TO " + toLeave);

                        ImmutableList<Variable> answerVariablesList = ImmutableList.copyOf(answerVariables);
                        ImmutableList<DataAtom<AtomPredicate>> from = children.stream()
                                .map(n -> ((DataNode<AtomPredicate>)n.getRootNode()).getProjectionAtom())
                                .collect(ImmutableCollectors.toList());
                        ImmutableList<DataAtom<AtomPredicate>> to = toLeave.stream()
                                .map(n -> ((DataNode<AtomPredicate>)n.getRootNode()).getProjectionAtom())
                                .collect(ImmutableCollectors.toList());
                        if (cqContainmentCheck.isContainedIn(
                                new ImmutableCQ<>(answerVariablesList, to),
                                new ImmutableCQ<>(answerVariablesList, from))) {
                            System.out.println("CQC: " + to + " IS CONTAINED IN " + from);
                            children.remove(toBeRemoved);
                            i--;
                        }
                    }

                    return iqFactory.createIQ(
                            query.getProjectionAtom(),
                            iqFactory.createUnaryIQTree(
                                (ConstructionNode)tree.getRootNode(),
                                    (children.size() < 2)
                                            ? children.get(0)
                                            : iqFactory.createNaryIQTree(joinNode, ImmutableList.copyOf(children))));
                }
            }

        }

        query.getTree().acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
                System.out.println("CQC Q:" + query + "\nCQC T: " + tree + "\nCQC N:" + rootNode);

                return iqFactory.createNaryIQTree(
                        rootNode,
                        children.stream()
                                .map(t -> t.acceptTransformer(this))
                                .collect(ImmutableCollectors.toList()));
            }
        });
        return query;
    }
}
