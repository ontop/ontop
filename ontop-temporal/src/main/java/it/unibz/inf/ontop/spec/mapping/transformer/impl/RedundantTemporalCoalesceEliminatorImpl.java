package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MissingTemporalIntermediateQueryNodeException;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.transformer.RedundantTemporalCoalesceEliminator;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.iq.node.TemporalCoalesceNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalJoinNode;

public class RedundantTemporalCoalesceEliminatorImpl implements RedundantTemporalCoalesceEliminator{

    private final TemporalIntermediateQueryFactory TIQFactory;

    @Inject
    public RedundantTemporalCoalesceEliminatorImpl(TemporalIntermediateQueryFactory tiqFactory) {
        TIQFactory = tiqFactory;
    }

    public IntermediateQuery removeRedundantTemporalCoalesces(IntermediateQuery intermediateQuery,
                                                              DBMetadata temporalDBMetadata,
                                                              ExecutorRegistry executorRegistry)
            throws MissingTemporalIntermediateQueryNodeException {

        TemporalIntermediateQueryBuilder TIQBuilder = TIQFactory.createTemporalIQBuilder(temporalDBMetadata, executorRegistry);
        TIQBuilder.init(intermediateQuery.getProjectionAtom(), intermediateQuery.getRootNode());
        TIQBuilder = removeCoalesces(TIQBuilder, intermediateQuery, intermediateQuery.getRootNode(), null);

        return TIQBuilder.build();
    }

    private TemporalIntermediateQueryBuilder removeCoalesces(TemporalIntermediateQueryBuilder TIQBuilder,
                                                             IntermediateQuery query,
                                                             QueryNode currentNode,
                                                             QueryNode parentNode)
            throws MissingTemporalIntermediateQueryNodeException {

        if(currentNode instanceof TemporalCoalesceNode){
            QueryNode child = query.getFirstChild(currentNode).orElseThrow(() ->
                    new MissingTemporalIntermediateQueryNodeException("child of temporal coalesce node is missing"));
            if(child instanceof FilterNode){
                QueryNode childOfChild = query.getFirstChild(child).orElseThrow(() ->
                        new MissingTemporalIntermediateQueryNodeException("child of filter node is missing"));
                if(childOfChild instanceof TemporalCoalesceNode){
                    QueryNode childOfChildOfChild = query.getFirstChild(childOfChild).orElseThrow(() ->
                            new MissingTemporalIntermediateQueryNodeException("child of temporal coalesce node is missing"));
                    TIQBuilder.addChild(parentNode, currentNode);
                    TIQBuilder.addChild(currentNode, child);
                    TIQBuilder.addChild(child, childOfChildOfChild);
                    removeCoalesces(TIQBuilder, query, childOfChildOfChild, child);
                }else{
                    TIQBuilder.addChild(parentNode, currentNode);
                    TIQBuilder.addChild(currentNode, child);
                    TIQBuilder.addChild(child, childOfChild);
                    removeCoalesces(TIQBuilder, query, childOfChild, child);
                }
            } else if(child instanceof TemporalJoinNode){
                TIQBuilder.addChild(parentNode, child);
                removeCoalesces(TIQBuilder, query, child, parentNode);
            } else if (child instanceof TemporalCoalesceNode){
                QueryNode childOfChild = query.getFirstChild(child).orElseThrow(() ->
                        new MissingTemporalIntermediateQueryNodeException("child of temporal coalesce node is missing"));
                TIQBuilder.addChild(parentNode, currentNode);
                TIQBuilder.addChild(currentNode, childOfChild);
                removeCoalesces(TIQBuilder, query, childOfChild, currentNode);

            } else{
                TIQBuilder.addChild(parentNode, currentNode);
                TIQBuilder.addChild(currentNode, child);
                removeCoalesces(TIQBuilder, query, child, currentNode);
            }
        }else if (!(currentNode instanceof DataNode)){
            query.getChildren(currentNode).stream()
                    .filter(c -> !(c instanceof TemporalCoalesceNode))
                    .forEach(c -> TIQBuilder.addChild(currentNode, c));
            query.getChildren(currentNode).forEach(c -> {
                try {
                    removeCoalesces(TIQBuilder,query, c, currentNode);
                } catch (MissingTemporalIntermediateQueryNodeException e) {
                    e.printStackTrace();
                }
            });
        }
        return TIQBuilder;
    }

}
