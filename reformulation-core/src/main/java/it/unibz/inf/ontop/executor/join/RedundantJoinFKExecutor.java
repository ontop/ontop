package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.NaiveVariableOccurrenceAnalyzerImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.sql.DBMetadata;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.ForeignKeyConstraint;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Use foreign keys to remove some redundant inner joins
 *
 * Normalization assumption: variables are reused between data nodes (no explicit equality between variables)
 *
 */
public class RedundantJoinFKExecutor implements InnerJoinExecutor {

    @Override
    public NodeCentricOptimizationResults<InnerJoinNode> apply(InnerJoinOptimizationProposal proposal,
                                                               IntermediateQuery query,
                                                               QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        InnerJoinNode joinNode = proposal.getFocusNode();
        ImmutableMultimap<DatabaseRelationDefinition, DataNode> dataNodeMap = extractDataNodeMap(query, joinNode);

        ImmutableSet<DataNode> nodesToRemove = findRedundantNodes(query, joinNode, dataNodeMap);

        if (!nodesToRemove.isEmpty()) {
            throw new RuntimeException("TODO: remove the redundant nodes");
        }
        /**
         * No change
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, joinNode);
        }
    }

    /**
     * Predicates not having a DatabaseRelationDefinition are ignored
     */
    private ImmutableMultimap<DatabaseRelationDefinition, DataNode> extractDataNodeMap(IntermediateQuery query,
                                                                                       InnerJoinNode joinNode) {

        DBMetadata dbMetadata = query.getMetadata().getDBMetadata();

        return query.getChildren(joinNode).stream()
                .filter(c -> c instanceof DataNode)
                .map(c -> (DataNode) c)
                .map(c -> getDatabaseRelationByName(dbMetadata, c.getProjectionAtom().getPredicate().getName())
                        .map(r -> new SimpleEntry<DatabaseRelationDefinition, DataNode>(r, c)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toMultimap());
    }

    private ImmutableSet<DataNode> findRedundantNodes(IntermediateQuery query, InnerJoinNode joinNode,
                                                      ImmutableMultimap<DatabaseRelationDefinition, DataNode> dataNodeMap) {
        return dataNodeMap.keySet().stream()
                .flatMap(r -> r.getForeignKeys().stream()
                        .flatMap(c -> selectRedundantNodesForConstraint(r, c, query, joinNode, dataNodeMap)))
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * TODO: explain
     */
    private Stream<DataNode> selectRedundantNodesForConstraint(DatabaseRelationDefinition sourceRelation,
                                                               ForeignKeyConstraint constraint,
                                                               IntermediateQuery query,
                                                               InnerJoinNode joinNode,
                                                               ImmutableMultimap<DatabaseRelationDefinition, DataNode> dataNodeMap) {
        /**
         * "Target" data nodes === "referenced" data nodes
         */
        ImmutableCollection<DataNode> targetDataNodes = dataNodeMap.get(constraint.getReferencedRelation());

        /**
         * No optimization possible
         */
        if (targetDataNodes.isEmpty()) {
            return Stream.empty();
        }

        return dataNodeMap.get(sourceRelation).stream()
                .flatMap(s -> targetDataNodes.stream()
                        .filter(t -> areMatching(s,t,constraint)))
                .distinct()
                .filter(t -> areNonFKVariablesUnused(t, query, constraint));
    }

    /**
     *
     * TODO: explain
     */
    private boolean areMatching(DataNode sourceDataNode, DataNode targetDataNode, ForeignKeyConstraint constraint) {

        ImmutableList<? extends VariableOrGroundTerm> sourceArguments = sourceDataNode.getProjectionAtom().getArguments();
        ImmutableList<? extends VariableOrGroundTerm> targetArguments = targetDataNode.getProjectionAtom().getArguments();

        return constraint.getComponents().stream()
                .allMatch(c -> sourceArguments.get(c.getAttribute().getIndex() - 1)
                        .equals(targetArguments.get(c.getReference().getIndex() - 1)));
    }

    /**
     * TODO: find a better name
     *
     * TODO: explain
     */
    private boolean areNonFKVariablesUnused(DataNode targetDataNode, IntermediateQuery query,
                                            ForeignKeyConstraint constraint) {
        /**
         * TODO: use a more efficient implementation
         */
        VariableOccurrenceAnalyzer analyzer = new NaiveVariableOccurrenceAnalyzerImpl();

        ImmutableList<? extends VariableOrGroundTerm> targetArguments = targetDataNode.getProjectionAtom().getArguments();
        return constraint.getComponents().stream()
                .map(c -> targetArguments.get(c.getReference().getIndex() -1))
                .filter(t -> t instanceof Variable)
                .map(v -> (Variable) v)
                .allMatch(v -> analyzer.isVariableUsedSomewhereElse(query,targetDataNode,v));
    }

    private Optional<DatabaseRelationDefinition> getDatabaseRelationByName(DBMetadata dbMetadata, String name) {
        for(DatabaseRelationDefinition relation: dbMetadata.getDatabaseRelations()) {
            if(relation.getID().getTableName().equalsIgnoreCase(name)) {
                return Optional.of(relation);
            }
        }
        // throw new IllegalStateException("No relation found for " + name);
        return Optional.empty();
    }
}
