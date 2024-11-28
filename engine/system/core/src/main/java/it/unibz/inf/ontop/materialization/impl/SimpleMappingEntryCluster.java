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
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Its tree is composed of one construction node and one extensional node
 */
public class SimpleMappingEntryCluster implements MappingEntryCluster {
    private final ExtensionalDataNode dataNode;
    private final Substitution<ImmutableTerm> topConstructSubstitution;
    private final IQTree tree;
    private final VariableGenerator variableGenerator;
    private final RDFFactTemplates rdfFactTemplates;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    public SimpleMappingEntryCluster (IQTree tree,
                                     RDFFactTemplates RDFTemplates,
                                     VariableGenerator variableGenerator,
                                     IntermediateQueryFactory iqFactory,
                                     SubstitutionFactory substitutionFactory) {
        this.topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
        this.dataNode = (ExtensionalDataNode) tree.getChildren().get(0);
        this.tree = tree;
        this.rdfFactTemplates = RDFTemplates;
        this.variableGenerator = variableGenerator;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
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
        return ImmutableList.of(dataNode.getRelationDefinition());
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
        if (!dataNode.getRelationDefinition().equals(otherSimpleCluster.dataNode.getRelationDefinition())) {
            return Optional.empty();
        }

        return Optional.of(mergeWithSimpleCluster(otherSimpleCluster));
    }

    private SimpleMappingEntryCluster mergeWithSimpleCluster(SimpleMappingEntryCluster otherSimpleCluster) {
        variableGenerator.registerAdditionalVariables(otherSimpleCluster.variableGenerator.getKnownVariables());
        SimpleMappingEntryCluster otherRenamed = otherSimpleCluster.renameConflictingVariables(variableGenerator);

        ConstructionNode constructionNodeAfterUnification = unify(otherRenamed);

        ExtensionalDataNode mergedDataNode = mergeDataNodes(otherRenamed.dataNode);

        Substitution<ImmutableTerm> rdfTermsConstructionSubstitution = topConstructSubstitution.compose(
                otherRenamed.topConstructSubstitution);

        ImmutableSet<Variable> termsVariables = Sets.union(
                topConstructSubstitution.getDomain(),
                otherRenamed.topConstructSubstitution.getDomain()).immutableCopy();

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(termsVariables,
                rdfTermsConstructionSubstitution);

        IQTree mappingTree = iqFactory.createUnaryIQTree(topConstructionNode,
                iqFactory.createUnaryIQTree(constructionNodeAfterUnification, mergedDataNode));

        RDFFactTemplates mergedRDFTemplates = rdfFactTemplates.merge(otherRenamed.rdfFactTemplates);

        return compressCluster(
                mappingTree.normalizeForOptimization(variableGenerator),
                mergedRDFTemplates);
    }


    public SimpleMappingEntryCluster renameConflictingVariables(VariableGenerator conflictingVariableGenerator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                conflictingVariableGenerator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);
        RDFFactTemplates renamedRDFTemplates = rdfFactTemplates.apply(renamingSubstitution);

        return new SimpleMappingEntryCluster(
                renamedTree,
                renamedRDFTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }

    private ExtensionalDataNode mergeDataNodes(ExtensionalDataNode otherDataNode){
        var argumentMap = dataNode.getArgumentMap();
        var otherArgumentMap = otherDataNode.getArgumentMap();

        var mergedArgumentMap = Sets.union(argumentMap.keySet(), otherArgumentMap.keySet()).stream()
                .collect(ImmutableCollectors.toMap(
                        idx -> idx,
                        idx -> Optional.<VariableOrGroundTerm>ofNullable(argumentMap.get(idx))
                                .orElseGet(() -> otherArgumentMap.get(idx))
                ));

        return iqFactory.createExtensionalDataNode(
                dataNode.getRelationDefinition(),
                mergedArgumentMap);
    }

    private ConstructionNode unify(SimpleMappingEntryCluster renamedOtherCluster) {
        var argumentMap = (ImmutableMap<Integer, Variable>) dataNode.getArgumentMap();
        var otherArgumentMap = (ImmutableMap<Integer, Variable>) renamedOtherCluster.dataNode.getArgumentMap();
        var indexes = Sets.union(argumentMap.keySet(), otherArgumentMap.keySet()).stream();

        var unifier = substitutionFactory.onVariables().unifierBuilder()
                .unify(indexes,
                        idx -> otherArgumentMap.getOrDefault(idx, argumentMap.get(idx)),
                        idx -> argumentMap.getOrDefault(idx, otherArgumentMap.get(idx)))
                .build();

        var allVariables = Sets.union(dataNode.getVariables(), renamedOtherCluster.dataNode.getVariables())
                .immutableCopy();

        return unifier
                .map(s -> iqFactory.createConstructionNode(allVariables, s))
                .orElseGet(() -> iqFactory.createConstructionNode(allVariables));
    }

    private SimpleMappingEntryCluster compressCluster(IQTree normalizedTree, RDFFactTemplates mergedRDFTemplates) {
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

        return new SimpleMappingEntryCluster(
                compressedTree,
                compressedTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }
}
