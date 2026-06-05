package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.*;
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

        if (joinSubtree.getChildren().stream().anyMatch(child -> !(child.getRootNode() instanceof ExtensionalDataNode))) {
            return Optional.empty();
        }

        return mergeWithJoinCluster((JoinMappingEntryCluster) other);
    }

    private Optional<MappingEntryCluster> mergeWithJoinCluster(JoinMappingEntryCluster otherJoinCluster) {
        variableGenerator.registerAdditionalVariables(otherJoinCluster.variableGenerator.getKnownVariables());
        JoinMappingEntryCluster otherJoinClusterRenamed = (JoinMappingEntryCluster) otherJoinCluster
                .renameConflictingVariables(variableGenerator);

        var groupedDataNodes = groupDataNodesByRelation(otherJoinClusterRenamed);

        if (!areJoinClustersCompatible(groupedDataNodes, otherJoinClusterRenamed)) {
            return Optional.empty();
        }

        ImmutableList<IQTree> mergedJoinSubtrees = groupedDataNodes.stream()
                .map(extensionalDataNodes -> {
                    ExtensionalDataNode node1 = extensionalDataNodes.get(0);
                    ExtensionalDataNode node2 = extensionalDataNodes.get(1);

                    return iqFactory.createUnaryIQTree(unify(node1, node2),
                            mergeDataNodes(node1, node2));
                })
                .collect(ImmutableCollectors.toList());

        ConstructionNode topConstructionNode = createMergedTopConstructionNode((ConstructionNode) tree.getRootNode(),
                (ConstructionNode) otherJoinClusterRenamed.getIQTree().getRootNode());

        IQTree joinTree = iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(), mergedJoinSubtrees);
        IQTree mappingTree = iqFactory.createUnaryIQTree(topConstructionNode, joinTree)
                .normalizeForOptimization(variableGenerator);

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherJoinClusterRenamed.getRDFFactTemplates());

        return Optional.of(compressCluster(mappingTree, mergedRDFTemplates));
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

    private boolean areJoinClustersCompatible(ImmutableList<ImmutableList<ExtensionalDataNode>> groupedDataNodes,
                                              JoinMappingEntryCluster otherJoinCluster) {
        boolean areAllAttributesVars = groupedDataNodes.stream()
                .flatMap(Collection::stream)
                .map(ExtensionalDataNode::getArgumentMap)
                .flatMap(map -> map.values().stream())
                .allMatch(value -> value instanceof Variable);

        // TODO: is this actually always just two nodes, what about self joins?
        boolean sameJoinChildren = groupedDataNodes.stream()
                .allMatch(nodes -> nodes.size() == 2);

        boolean areJoinConditionsExplicit =  ((InnerJoinNode) joinSubtree.getRootNode()).getOptionalFilterCondition().isPresent()
                || ((InnerJoinNode) otherJoinCluster.joinSubtree.getRootNode()).getOptionalFilterCondition().isPresent();

        if (!areAllAttributesVars || !sameJoinChildren || areJoinConditionsExplicit) {
            return false;
        }
        // the idea is that different join clusters can have different variables in the join condition for the same column,
        // but the underlying attribute in the extensional node they refer to stays the same
        return getImplicitJoinAttributes(dataNodes).equals(getImplicitJoinAttributes(otherJoinCluster.dataNodes));
    }

    private ImmutableList<ImmutableList<ExtensionalDataNode>> groupDataNodesByRelation(JoinMappingEntryCluster otherJoinClusterRenamed) {
        return Streams.concat(
                        joinSubtree.getChildren().stream(), otherJoinClusterRenamed.joinSubtree.getChildren().stream())
                .filter(child -> child.getRootNode() instanceof ExtensionalDataNode)
                .map(child -> (ExtensionalDataNode)child.getRootNode())
                .collect(ImmutableCollectors.toMultimap(
                        ExtensionalDataNode::getRelationDefinition,
                        node -> node
                )).asMap().values().stream()
                .map(ImmutableList::copyOf)
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableSet<Attribute> getImplicitJoinAttributes(ImmutableList<ExtensionalDataNode> extensionalNodes) {
        var sharedVariables = NaryIQTreeTools.coOccurringVariablesStream(extensionalNodes);

        return sharedVariables
                .flatMap(var -> findSharedAttributes(var, extensionalNodes).stream())
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSet<Attribute> findSharedAttributes(VariableOrGroundTerm sharedVar, Collection<ExtensionalDataNode> dataNodes) {
        return dataNodes.stream()
                .flatMap(node -> node.getArgumentMap().entrySet().stream()
                        .filter(e -> e.getValue().equals(sharedVar))
                        .map(Map.Entry::getKey)
                        .map(attrIndex -> node.getRelationDefinition().getAttributes().get(attrIndex)))
                .collect(ImmutableCollectors.toSet());
    }
}
