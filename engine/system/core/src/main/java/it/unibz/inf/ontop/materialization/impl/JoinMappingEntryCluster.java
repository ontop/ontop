package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Its tree is composed of one construction node, a join node and a list of extensional data nodes as the children of the join node.
 * It is merged only with other JoinMappingEntryCluster.
 */
public class JoinMappingEntryCluster extends AbstractMappingEntryCluster implements MappingEntryCluster {
    private final IQTree joinSubtree;
    private final ImmutableList<ExtensionalDataNode> dataNodes;

    public JoinMappingEntryCluster(IQTree tree,
                                   RDFFactTemplates rdfFactTemplates,
                                   VariableGenerator variableGenerator,
                                   IntermediateQueryFactory iqFactory,
                                   SubstitutionFactory substitutionFactory,
                                   TermFactory termFactory) {
        super(tree, rdfFactTemplates, variableGenerator, iqFactory, substitutionFactory, termFactory);

        this.joinSubtree = tree.getChildren().get(0);
        this.dataNodes = findExtensionalNodes(joinSubtree);
    }

    @Override
    protected MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates) {
        return new JoinMappingEntryCluster(compressedTree,
                compressedTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory,
                termFactory);
    }

    @Override
    public ImmutableList<ExtensionalDataNode> getDataNodes() {
        return dataNodes;
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster other) {
        if ( !(other instanceof JoinMappingEntryCluster)) {
            return Optional.empty();
        }

        if (!areJoinChildrenExtensional(joinSubtree)) {
            return Optional.empty();
        }

        JoinMappingEntryCluster otherJoinCluster = (JoinMappingEntryCluster) other;
        variableGenerator.registerAdditionalVariables(otherJoinCluster.variableGenerator.getKnownVariables());
        JoinMappingEntryCluster otherJoinClusterRenamed = (JoinMappingEntryCluster) otherJoinCluster
                .renameConflictingVariables(variableGenerator);

        ImmutableMap<RelationDefinition, ImmutableList<ExtensionalDataNode>> dataNodesMap = Streams.concat(
                        joinSubtree.getChildren().stream(), otherJoinClusterRenamed.joinSubtree.getChildren().stream())
                .filter(child -> child.getRootNode() instanceof ExtensionalDataNode)
                .map(child -> (ExtensionalDataNode)child.getRootNode())
                .collect(ImmutableCollectors.toMultimap(
                        ExtensionalDataNode::getRelationDefinition,
                        node -> node
                )).asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        entry -> ImmutableList.copyOf(entry.getValue())
                ));
        boolean areAllAttributesVars = dataNodesMap.values().stream()
                .flatMap(Collection::stream)
                .map(ExtensionalDataNode::getArgumentMap)
                .flatMap(map -> map.values().stream())
                .allMatch(value -> value instanceof Variable);

        boolean sameJoinChildren = dataNodesMap.values().stream()
                .allMatch(nodes -> nodes.size() == 2);

        if (areAllAttributesVars && sameJoinChildren && areJoinConditionsEqual(otherJoinClusterRenamed)) {
            return mergeWithJoinCluster(otherJoinClusterRenamed, dataNodesMap);
        }
        return Optional.empty();
    }

    private Optional<MappingEntryCluster> mergeWithJoinCluster(JoinMappingEntryCluster otherCluster,
                                                               ImmutableMap<RelationDefinition, ImmutableList<ExtensionalDataNode>> relationDefinitionNodesMap) {
        ImmutableList<IQTree> mergedJoinSubtrees = relationDefinitionNodesMap.values().stream()
                .map(extensionalDataNodes -> {
                    ExtensionalDataNode node1 = extensionalDataNodes.get(0);
                    ExtensionalDataNode node2 = extensionalDataNodes.get(1);

                    return iqFactory.createUnaryIQTree(unify(node1, node2),
                            mergeDataNodes(node1, node2));
                })
                .collect(ImmutableCollectors.toList());

        ConstructionNode topConstructionNode = createMergedTopConstructionNode((ConstructionNode) tree.getRootNode(),
                (ConstructionNode) otherCluster.getIQTree().getRootNode());

        IQTree joinTree = iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(), mergedJoinSubtrees);
        IQTree mappingTree = iqFactory.createUnaryIQTree(topConstructionNode, joinTree)
                .normalizeForOptimization(variableGenerator);

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherCluster.getRDFFactTemplates());

        return Optional.of(compressCluster(mappingTree, mergedRDFTemplates));
    }

    private boolean areJoinChildrenExtensional(IQTree joinSubtree) {
        return joinSubtree.getChildren().stream()
                .allMatch(child -> child.getRootNode() instanceof ExtensionalDataNode);
    }

    private ImmutableList<ExtensionalDataNode> findExtensionalNodes(IQTree tree) {
        if (tree.getRootNode() instanceof ExtensionalDataNode) {
            return ImmutableList.of((ExtensionalDataNode) tree.getRootNode());
        } else {
            return tree.getChildren().stream()
                    .map(this::findExtensionalNodes)
                    .flatMap(ImmutableList::stream)
                    .collect(ImmutableCollectors.toList());
        }
    }

    private boolean areJoinConditionsEqual(JoinMappingEntryCluster otherJoinClusterRenamed) {
        if (((InnerJoinNode) joinSubtree.getRootNode()).getOptionalFilterCondition().isPresent()
            || ((InnerJoinNode) otherJoinClusterRenamed.joinSubtree.getRootNode()).getOptionalFilterCondition().isPresent()) {
            return false;
        }
        // the idea is that different join clusters can have different variables in the join condition for the same column,
        // but the underlying attribute in the extensional node they refer to stays the same
        return getImplicitJoinAttributes(dataNodes)
                .equals(getImplicitJoinAttributes(otherJoinClusterRenamed.dataNodes));
    }

    private ImmutableMap<ImmutableSet<Attribute>, ImmutableSet<RelationDefinition>> getImplicitJoinAttributes(
            ImmutableList<ExtensionalDataNode> extensionalNodes) {

        var implicitJoinVariables = extensionalNodes.stream()
                .flatMap(node -> node.getArgumentMap().values().stream()
                        .map(var -> Map.entry(var, node)))
                .collect(ImmutableCollectors.toMultimap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        return implicitJoinVariables.asMap().entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> {
                    var sharedVar = entry.getKey();
                    var sharedAttributes = findSharedAttributes(sharedVar, entry.getValue());
                    var sharedRelations = entry.getValue().stream()
                            .map(ExtensionalDataNode::getRelationDefinition)
                            .collect(ImmutableCollectors.toSet());
                    return Map.entry(sharedAttributes, sharedRelations);
                })
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private ImmutableSet<Attribute> findSharedAttributes(VariableOrGroundTerm sharedVar, Collection<ExtensionalDataNode> dataNodes) {
        return dataNodes.stream()
                .map(node -> {
                    Integer index = node.getArgumentMap().entrySet().stream()
                            .filter(e -> e.getValue().equals(sharedVar))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElseThrow(() -> new MinorOntopInternalBugException("Common variable between extensional nodes not found in argument map"));
                    return node.getRelationDefinition().getAttributes().get(index);
                })
                .collect(ImmutableCollectors.toSet());
    }
}
