package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.eclipse.rdf4j.model.IRI;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class JoinMappingEntryCluster implements MappingEntryCluster {
    private final IQTree tree;
    private final RDFFactTemplates rdfFactTemplates;
    private final IQTree joinSubtree;
    private final VariableGenerator variableGenerator;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutableList<ExtensionalDataNode> extensionalNodes;

    public JoinMappingEntryCluster(IQTree tree,
                                   RDFFactTemplates rdfFactTemplates,
                                   IQTree joinSubtree,
                                   VariableGenerator variableGenerator,
                                   IntermediateQueryFactory iqFactory,
                                   SubstitutionFactory substitutionFactory) {
        this.tree = tree;
        this.rdfFactTemplates = rdfFactTemplates;
        this.joinSubtree = joinSubtree;
        this.variableGenerator = variableGenerator;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;

        this.extensionalNodes = findExtensionalNodes(joinSubtree);
    }

    @Override
    public IQTree getIQTree() {
        return tree;
    }

    @Override
    public RDFFactTemplates getRDFFactTemplates() {
        return rdfFactTemplates;
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster other) {
        if ( !(other instanceof JoinMappingEntryCluster)) {
            return Optional.empty();
        }

        if (!areJoinChildrenExtensional(joinSubtree)) {
            return Optional.empty();
        }

        JoinMappingEntryCluster otherJoin = (JoinMappingEntryCluster) other;
        variableGenerator.registerAdditionalVariables(otherJoin.variableGenerator.getKnownVariables());
        JoinMappingEntryCluster otherJoinInfoRenamed = otherJoin.renameConflictingVariables(variableGenerator);

        ImmutableMap<RelationDefinition, ImmutableList<ExtensionalDataNode>> relationDefinitionNodesMap = Streams.concat(
                        joinSubtree.getChildren().stream(), otherJoinInfoRenamed.joinSubtree.getChildren().stream())
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
        if (areAllAttributesVars && sameJoinChildren && areJoinConditionsEqual(otherJoinInfoRenamed)) {
            return mergeJoinMappingAssertions(otherJoinInfoRenamed, relationDefinitionNodesMap);
        }
        return Optional.empty();
    }

    @Override
    public RDFFactTemplates restrict(ImmutableSet<IRI> predicates) {
        ImmutableCollection<ImmutableList<Variable>> filteredTemplates = rdfFactTemplates.getTriplesOrQuadsVariables().stream()
                .filter(tripleOrQuad -> {
                    Substitution<ImmutableTerm> topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
                    ImmutableTerm predicate = topConstructSubstitution.apply(tripleOrQuad.get(1));
                    return predicate instanceof IRI && predicates.contains(predicate);
                })
                .collect(ImmutableCollectors.toList());

        return new RDFFactTemplatesImpl(filteredTemplates);
    }

    @Override
    public ImmutableList<RelationDefinition> getRelationsDefinitions() {
        return extensionalNodes.stream()
                .map(ExtensionalDataNode::getRelationDefinition)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public JoinMappingEntryCluster renameConflictingVariables(VariableGenerator conflictingVariableGenerator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(conflictingVariableGenerator,
                tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);

        return new JoinMappingEntryCluster(
                renamedTree,
                rdfFactTemplates.apply(renamingSubstitution),
                joinSubtree.applyFreshRenaming(renamingSubstitution),
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }

    private Optional<MappingEntryCluster> mergeJoinMappingAssertions(JoinMappingEntryCluster otherJoinInfoRenamed,
                                                                     ImmutableMap<RelationDefinition, ImmutableList<ExtensionalDataNode>> relationDefinitionNodesMap) {
        ImmutableList<IQTree> mergedJoinSubtrees = relationDefinitionNodesMap.entrySet().stream()
                .map(nodes -> {
                    ExtensionalDataNode node1 = nodes.getValue().get(0);
                    ExtensionalDataNode node2 = nodes.getValue().get(1);
                    ImmutableMap<Integer, Variable> argumentMap = node1.getArgumentMap().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> (Variable) entry.getValue()
                            ));
                    ImmutableMap<Integer, Variable> otherArgumentMap = node2.getArgumentMap().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> (Variable) entry.getValue()
                            ));
                    ConstructionNode renamingNode = createOptionalRenamingNode(argumentMap, otherArgumentMap);
                    return (IQTree) iqFactory.createUnaryIQTree(renamingNode,
                            iqFactory.createExtensionalDataNode(nodes.getKey(), mergeRelationArguments(argumentMap, otherArgumentMap)));
                })
                .collect(ImmutableCollectors.toList());

        Substitution<ImmutableTerm> topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> otherTopConstructSubstitution = ((ConstructionNode) otherJoinInfoRenamed.tree.getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> RDFTermsConstructionSubstitution = topConstructSubstitution.compose(otherTopConstructSubstitution);
        ImmutableSet<Variable> termsVariables = ImmutableSet.<Variable>builder()
                .addAll(topConstructSubstitution.getDomain())
                .addAll(otherTopConstructSubstitution.getDomain())
                .build();
        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(termsVariables, RDFTermsConstructionSubstitution);
        IQTree joinTree = iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(), mergedJoinSubtrees);
        IQTree mappingTree = iqFactory.createUnaryIQTree(topConstructionNode, joinTree);

        RDFFactTemplates mergedRDFTemplates = rdfFactTemplates.merge(otherJoinInfoRenamed.getRDFFactTemplates());
        Map.Entry<IQTree, RDFFactTemplates> treeTemplatePair = compressMappingAssertion(mappingTree.normalizeForOptimization(variableGenerator), mergedRDFTemplates);
        IQTree finalTree = treeTemplatePair.getKey();
        return Optional.of(new JoinMappingEntryCluster(finalTree,
                treeTemplatePair.getValue(),
                finalTree.getChildren().get(0),
                variableGenerator,
                iqFactory,
                substitutionFactory));
    }

    private ImmutableMap<Integer, Variable> mergeRelationArguments(ImmutableMap <Integer, Variable > argumentMap,
                                                                   ImmutableMap <Integer, Variable > otherArgumentMap){
        ImmutableSet<Integer> keys = ImmutableSet.<Integer>builder()
                .addAll(argumentMap.keySet())
                .addAll(otherArgumentMap.keySet())
                .build();

        return keys.stream()
                .collect(ImmutableCollectors.toMap(
                        idx -> idx,
                        idx -> argumentMap.getOrDefault(idx, otherArgumentMap.get(idx))
                ));
    }

    private ConstructionNode createOptionalRenamingNode(ImmutableMap <Integer, Variable > argumentMap,
                                                        ImmutableMap<Integer, Variable> otherArgumentMap) {
        ImmutableSet<Integer> keys = ImmutableSet.<Integer>builder()
                .addAll(argumentMap.keySet())
                .addAll(otherArgumentMap.keySet())
                .build();
        Optional<Substitution<Variable>> mergedSubstitution  = substitutionFactory.onVariables().unifierBuilder()
                .unify(keys.stream(),
                        idx -> otherArgumentMap.getOrDefault(idx, argumentMap.get(idx)),
                        idx -> argumentMap.getOrDefault(idx, otherArgumentMap.get(idx)))
                .build();

        ConstructionNode optionalRenamingNode;
        if (mergedSubstitution.isPresent()) {
            ImmutableSet<Variable> originalRelationsVariables = Streams.concat(
                    argumentMap.values().stream(),
                    otherArgumentMap.values().stream(),
                    mergedSubstitution.get().getRangeVariables().stream()
            ).collect(ImmutableCollectors.toSet());
            optionalRenamingNode = iqFactory.createConstructionNode(originalRelationsVariables, mergedSubstitution.get());
        } else {
            ImmutableSet<Variable> originalRelationsVariables = Streams.concat(
                    argumentMap.values().stream(),
                    otherArgumentMap.values().stream()
            ).collect(ImmutableCollectors.toSet());
            optionalRenamingNode = iqFactory.createConstructionNode(originalRelationsVariables);
        }
        return optionalRenamingNode;
    }

    private Map.Entry<IQTree, RDFFactTemplates> compressMappingAssertion(IQTree normalizedTree, RDFFactTemplates mergedRDFTemplates) {
        Substitution<ImmutableTerm> normalizedSubstitution = ((ConstructionNode) normalizedTree.getRootNode()).getSubstitution();
        RDFFactTemplates compressedTemplates = mergedRDFTemplates.compress(normalizedSubstitution.inverseMap().values().stream()
                .filter(vs -> vs.size() > 1)
                .map(ImmutableList::copyOf)
                .collect(ImmutableCollectors.toSet()));

        ImmutableSet<Variable> compressedVariables = compressedTemplates.getVariables();
        Substitution<ImmutableTerm> compressedSubstitution = normalizedSubstitution.restrictDomainTo(compressedVariables);

        IQTree compressedTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(compressedVariables, compressedSubstitution),
                normalizedTree.getChildren().get(0));

        return Map.entry(compressedTree, compressedTemplates);
    }

    private boolean areJoinChildrenExtensional(IQTree joinSubtree) {
        return joinSubtree.getChildren().stream().allMatch(child -> child.getRootNode() instanceof ExtensionalDataNode);
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

    private boolean areJoinConditionsEqual(JoinMappingEntryCluster otherJoinInfoRenamed) {
        if (((InnerJoinNode) joinSubtree.getRootNode()).getOptionalFilterCondition().isPresent()
            || ((InnerJoinNode) otherJoinInfoRenamed.joinSubtree.getRootNode()).getOptionalFilterCondition().isPresent()) {
            return false;
        }
        // the idea is that while different join subtrees can have different variables in the join condition for the same attribute,
        // the underlying attribute in the extensional node stays the same
        return getSharedAttributesIndexesInJoinSubtrees(extensionalNodes).equals(getSharedAttributesIndexesInJoinSubtrees(otherJoinInfoRenamed.extensionalNodes));
    }

    private ImmutableMap<ImmutableSet<Attribute>, ImmutableSet<RelationDefinition>> getSharedAttributesIndexesInJoinSubtrees(ImmutableList<ExtensionalDataNode> extensionalNodes) {
        ImmutableMap<VariableOrGroundTerm, ImmutableList<ExtensionalDataNode>> sharedVarsInExtNodes = extensionalNodes.stream()
                .flatMap(node -> node.getArgumentMap().values().stream()
                        .map(var -> Map.entry(var, node)))
                .collect(ImmutableCollectors.toMultimap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )).asMap().entrySet().stream().collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        entry -> ImmutableList.copyOf(entry.getValue())
                ));

        ImmutableMap<ImmutableSet<Attribute>, ImmutableSet<RelationDefinition>> tmp = sharedVarsInExtNodes.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> {
                    var sharedVariable = entry.getKey();
                    ImmutableSet<Attribute> sharedAttributes = entry.getValue().stream()
                            .map(node -> {
                                Integer index = node.getArgumentMap().entrySet().stream()
                                        .filter(e -> e.getValue().equals(sharedVariable))
                                        .map(Map.Entry::getKey)
                                        .findFirst().orElseThrow(() -> new IllegalStateException("Common variable between extensional nodes not found in argument map"));
                                return node.getRelationDefinition().getAttributes().get(index);
                            })
                            .collect(ImmutableCollectors.toSet());
                    return Map.entry(sharedAttributes, entry.getValue().stream().map(ExtensionalDataNode::getRelationDefinition).collect(ImmutableCollectors.toSet()));
                })
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        return tmp;
    }
}
