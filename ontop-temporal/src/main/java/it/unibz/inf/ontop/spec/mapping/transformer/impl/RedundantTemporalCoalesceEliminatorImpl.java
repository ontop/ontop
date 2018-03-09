package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.transformer.RedundantTemporalCoalesceEliminator;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.iq.node.TemporalCoalesceNode;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

public class RedundantTemporalCoalesceEliminatorImpl implements RedundantTemporalCoalesceEliminator{

    private final TemporalIntermediateQueryFactory TIQFactory;

    @Inject
    public RedundantTemporalCoalesceEliminatorImpl(TemporalIntermediateQueryFactory tiqFactory) {
        TIQFactory = tiqFactory;
    }

    public IntermediateQuery removeRedundantTemporalCoalesces(IntermediateQuery intermediateQuery, DBMetadata temporalDBMetadata, ExecutorRegistry executorRegistry){

        TemporalIntermediateQueryBuilder TIQBuilder = TIQFactory.createTemporalIQBuilder(temporalDBMetadata, executorRegistry);
        TIQBuilder.init(intermediateQuery.getProjectionAtom(), intermediateQuery.getRootNode());
        TIQBuilder = removeCoalesces(TIQBuilder, intermediateQuery, intermediateQuery.getRootNode());

        return TIQBuilder.build();
    }

    private TemporalIntermediateQueryBuilder removeCoalesces(TemporalIntermediateQueryBuilder TIQBuilder, IntermediateQuery query,
                                                             QueryNode currentNode){
        if(currentNode instanceof TemporalCoalesceNode){
            QueryNode child = query.getFirstChild(currentNode).get();
            if(child instanceof FilterNode){
                QueryNode childOfChild = query.getFirstChild(child).get();
                if(childOfChild instanceof TemporalCoalesceNode){
                    QueryNode childOfChildOfChild = query.getFirstChild(childOfChild).get();
                    TIQBuilder.addChild(currentNode, child);
                    TIQBuilder.addChild(child, childOfChildOfChild);
                    removeCoalesces(TIQBuilder, query, childOfChildOfChild);
                }else{
                    TIQBuilder.addChild(currentNode, child);
                    TIQBuilder.addChild(child, childOfChild);
                    removeCoalesces(TIQBuilder, query, childOfChild);
                }
            }else{
                TIQBuilder.addChild(currentNode, child);
                removeCoalesces(TIQBuilder, query, child);
            }
        }else if (currentNode instanceof UnaryOperatorNode){
            QueryNode child = query.getFirstChild(currentNode).get();
            TIQBuilder.addChild(currentNode, child);
            removeCoalesces(TIQBuilder, query,child);

        }else if(currentNode instanceof BinaryNonCommutativeOperatorNode){
            QueryNode leftChild = query.getChild(currentNode, LEFT).get();
            QueryNode rightChild = query.getChild(currentNode, RIGHT).get();
            TIQBuilder.addChild(currentNode,leftChild,LEFT);
            TIQBuilder.addChild(currentNode,rightChild, RIGHT);
            removeCoalesces(TIQBuilder, query, leftChild);
            removeCoalesces(TIQBuilder, query, rightChild);
        }else if(currentNode instanceof NaryOperatorNode){
            query.getChildren(currentNode).forEach(c -> TIQBuilder.addChild(currentNode, c));
            query.getChildren(currentNode).forEach(c -> removeCoalesces(TIQBuilder,query,c));
        }
        return TIQBuilder;
    }
}
