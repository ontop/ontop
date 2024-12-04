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
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class JoinMappingEntryCluster extends AbstractMappingEntryCluster implements MappingEntryCluster {
    private final IQTree joinSubtree;
    private final ImmutableList<ExtensionalDataNode> extensionalNodes;

    public JoinMappingEntryCluster(IQTree tree,
                                   RDFFactTemplates rdfFactTemplates,
                                   VariableGenerator variableGenerator,
                                   IntermediateQueryFactory iqFactory,
                                   SubstitutionFactory substitutionFactory) {
        super(tree, rdfFactTemplates, variableGenerator, iqFactory, substitutionFactory);

        this.joinSubtree = tree.getChildren().get(0);
        this.extensionalNodes = findExtensionalNodes(joinSubtree);
    }

    @Override
    protected MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates) {
        return new JoinMappingEntryCluster(compressedTree,
                compressedTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }

    @Override
    public ImmutableList<ExtensionalDataNode> getDataNodes() {
        return extensionalNodes;
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
        JoinMappingEntryCluster otherJoinClusterRenamed = otherJoinCluster.renameConflictingVariables(variableGenerator);

        ImmutableMap<RelationDefinition, ImmutableList<ExtensionalDataNode>> relationDefinitionNodesMap = Streams.concat(
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
        boolean areAllAttributesVars = relationDefinitionNodesMap.values().stream()
                .flatMap(Collection::stream)
                .map(ExtensionalDataNode::getArgumentMap)
                .flatMap(map -> map.values().stream())
                .allMatch(value -> value instanceof Variable);

        boolean sameJoinChildren = relationDefinitionNodesMap.values().stream()
                .allMatch(nodes -> nodes.size() == 2);

        if (areAllAttributesVars && sameJoinChildren && areJoinConditionsEqual(otherJoinClusterRenamed)) {
            return mergeJoinMappingAssertions(otherJoinClusterRenamed, relationDefinitionNodesMap);
        }
        return Optional.empty();
    }

    @Override
    public JoinMappingEntryCluster renameConflictingVariables(VariableGenerator conflictingVariableGenerator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory
                .generateNotConflictingRenaming(conflictingVariableGenerator,
                        tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);

        return new JoinMappingEntryCluster(
                renamedTree,
                rdfTemplates.apply(renamingSubstitution),
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }

    private Optional<MappingEntryCluster> mergeJoinMappingAssertions(JoinMappingEntryCluster otherCluster,
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
        // the idea is that different join clusters can have different variables in the join condition for the same attribute,
        // but the underlying attribute in the extensional node they refer to stays the same
        return getSharedAttributesIndexesInJoinSubtrees(extensionalNodes)
                .equals(getSharedAttributesIndexesInJoinSubtrees(otherJoinClusterRenamed.extensionalNodes));
    }

    private ImmutableMap<ImmutableSet<Attribute>, ImmutableSet<RelationDefinition>> getSharedAttributesIndexesInJoinSubtrees(
            ImmutableList<ExtensionalDataNode> extensionalNodes) {

        var sharedVarsInExtNodes = extensionalNodes.stream()
                .flatMap(node -> node.getArgumentMap().values().stream()
                        .map(var -> Map.entry(var, node)))
                .collect(ImmutableCollectors.toMultimap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )).asMap().entrySet().stream().collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        entry -> ImmutableList.copyOf(entry.getValue())
                ));

        return sharedVarsInExtNodes.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> {
                    var sharedVariable = entry.getKey();
                    ImmutableSet<Attribute> sharedAttributes = entry.getValue().stream()
                            .map(node -> {
                                Integer index = node.getArgumentMap().entrySet().stream()
                                        .filter(e -> e.getValue().equals(sharedVariable))
                                        .map(Map.Entry::getKey)
                                        .findFirst()
                                        .orElseThrow(() -> new MinorOntopInternalBugException("Common variable between extensional nodes not found in argument map"));
                                return node.getRelationDefinition().getAttributes().get(index);
                            })
                            .collect(ImmutableCollectors.toSet());
                    return Map.entry(
                            sharedAttributes,
                            entry.getValue().stream()
                                    .map(ExtensionalDataNode::getRelationDefinition)
                                    .collect(ImmutableCollectors.toSet()));
                })
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }
}
