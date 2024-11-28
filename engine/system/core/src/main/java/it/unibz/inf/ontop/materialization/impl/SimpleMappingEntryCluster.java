package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;

public class SimpleMappingEntryCluster implements MappingEntryCluster {
    private final RelationDefinition relationDefinition;
    private final ImmutableMap<Integer, Variable> argumentMap;
    private final Substitution<ImmutableTerm> topConstructSubstitution;
    private final IQTree tree;
    private final VariableGenerator variableGenerator;
    private final RDFFactTemplates rdfFactTemplates;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    public SimpleMappingEntryCluster(RelationDefinition relationDefinition,
                                     ImmutableMap<Integer, Variable> argumentMap,
                                     IQTree tree,
                                     RDFFactTemplates RDFTemplates,
                                     VariableGenerator variableGenerator,
                                     IntermediateQueryFactory iqFactory,
                                     SubstitutionFactory substitutionFactory) {
        this.relationDefinition = relationDefinition;
        this.argumentMap = argumentMap;
        this.tree = tree;
        this.rdfFactTemplates = RDFTemplates;
        this.variableGenerator = variableGenerator;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;

        this.topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SimpleMappingEntryCluster) {
            SimpleMappingEntryCluster that = (SimpleMappingEntryCluster) other;
            return this.tree.equals(that.tree);
        }
        return false;
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
    public ImmutableList<RelationDefinition> getRelationsDefinitions() {
        return ImmutableList.of(relationDefinition);
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster otherCluster) {

        // Not simple but having a potential for merging implemented somewhere else
        if (otherCluster instanceof FilterMappingEntryCluster
                || otherCluster instanceof DictionaryPatternMappingEntryCluster) {
            return otherCluster.merge(this);
        }

        if (!(otherCluster instanceof SimpleMappingEntryCluster)) {
            return Optional.empty();
        }

        SimpleMappingEntryCluster otherSimpleCluster = (SimpleMappingEntryCluster) otherCluster;
        if (!relationDefinition.equals(otherSimpleCluster.relationDefinition)) {
            return Optional.empty();
        }
        variableGenerator.registerAdditionalVariables(otherSimpleCluster.variableGenerator.getKnownVariables());
        SimpleMappingEntryCluster otherRenamed = otherSimpleCluster.renameConflictingVariables(variableGenerator);

        ImmutableMap<Integer, Variable> mergedArgumentMap = mergeRelationArguments(otherRenamed.argumentMap);

        ExtensionalDataNode relationDefinitionNode = iqFactory.createExtensionalDataNode(
                relationDefinition,
                mergedArgumentMap);

        ConstructionNode optionalRenamingNode = createOptionalRenamingNode(otherRenamed.argumentMap);

        Substitution<ImmutableTerm> rdfTermsConstructionSubstitution = topConstructSubstitution.compose(
                otherRenamed.topConstructSubstitution);

        ImmutableSet<Variable> termsVariables = Sets.union(
                topConstructSubstitution.getDomain(),
                otherRenamed.topConstructSubstitution.getDomain()).immutableCopy();

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(termsVariables,
                rdfTermsConstructionSubstitution);

        IQTree mappingTree = iqFactory.createUnaryIQTree(topConstructionNode,
                iqFactory.createUnaryIQTree(optionalRenamingNode, relationDefinitionNode));

        RDFFactTemplates mergedRDFTemplates = rdfFactTemplates.merge(otherRenamed.rdfFactTemplates);

        var treeTemplatesPair = compressCluster(
                mappingTree.normalizeForOptimization(variableGenerator),
                mergedRDFTemplates);

        return Optional.of(new SimpleMappingEntryCluster(relationDefinition,
                mergedArgumentMap,
                treeTemplatesPair.getKey(),
                treeTemplatesPair.getValue(),
                variableGenerator,
                iqFactory,
                substitutionFactory));
    }

    public SimpleMappingEntryCluster renameConflictingVariables(VariableGenerator conflictingVariableGenerator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                conflictingVariableGenerator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);
        RDFFactTemplates renamedRDFTemplates = rdfFactTemplates.apply(renamingSubstitution);

        ImmutableMap<Integer, Variable> renamedArgumentMap = argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (Variable) renamingSubstitution.apply(e.getValue())
                ));
        return new SimpleMappingEntryCluster(relationDefinition,
                renamedArgumentMap,
                renamedTree,
                renamedRDFTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }

    private ImmutableMap<Integer, Variable> mergeRelationArguments(ImmutableMap <Integer, Variable > otherArgumentMap){

        return Sets.union(argumentMap.keySet(), otherArgumentMap.keySet()).stream()
                .collect(ImmutableCollectors.toMap(
                        idx -> idx,
                        idx -> argumentMap.getOrDefault(idx, otherArgumentMap.get(idx))
                ));
    }

    private ConstructionNode createOptionalRenamingNode(ImmutableMap<Integer, Variable> otherArgumentMap) {
        var keys = Sets.union(argumentMap.keySet(), otherArgumentMap.keySet()).stream();

        Optional<Substitution<Variable>> mergedSubstitution  = substitutionFactory.onVariables().unifierBuilder()
                .unify(keys,
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

    private Map.Entry<IQTree, RDFFactTemplates> compressCluster(IQTree normalizedTree, RDFFactTemplates mergedRDFTemplates) {
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
}
