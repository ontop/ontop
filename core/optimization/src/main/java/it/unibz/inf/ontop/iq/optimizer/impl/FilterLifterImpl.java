package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.optimizer.FilterLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.stream.Stream;

public class FilterLifterImpl implements FilterLifter {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    @Inject
    private FilterLifterImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        TreeTransformer treeTransformer = new TreeTransformer(iqFactory);
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                query.getTree().acceptTransformer(treeTransformer)
        );
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        TreeTransformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {

            child = child.acceptTransformer(this);

            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, child);
            }

            QueryNode childRoot = child.getRootNode();
            if(childRoot instanceof FilterNode){
                ConstructionNode updatedCn = updateConstructionNode(
                        cn,
                        ((FilterNode) childRoot).getFilterCondition()
                );
                return iqFactory.createUnaryIQTree(
                        (UnaryOperatorNode) childRoot,
                        iqFactory.createUnaryIQTree(
                                updatedCn,
                                child
                ));
            }
            return iqFactory.createUnaryIQTree(
                    cn,
                    child
            );
        }


        private ConstructionNode updateConstructionNode(ConstructionNode cn, ImmutableExpression filteringCondition) {

                // add the variable that appear in the expression to ones projected by the cn
                ImmutableSet<Variable> projectedVars = ImmutableSet.<Variable>builder()
                        .addAll(filteringCondition.getVariables())
                        .addAll(cn.getVariables())
                        .build();

                return iqFactory.createConstructionNode(projectedVars, cn.getSubstitution());
            }
        }
    }
