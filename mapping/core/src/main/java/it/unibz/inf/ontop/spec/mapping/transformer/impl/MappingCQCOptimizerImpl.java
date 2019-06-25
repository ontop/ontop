package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.query.algebra.Var;

import java.util.stream.Stream;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    public MappingCQCOptimizerImpl(IntermediateQueryFactory iqFactory) {

        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
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
