package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class AbstractMappingEntryCluster implements MappingEntryCluster {

    protected IQTree tree;
    protected final VariableGenerator variableGenerator;
    protected final RDFFactTemplates rdfTemplates;
    protected final IntermediateQueryFactory iqFactory;
    protected final SubstitutionFactory substitutionFactory;
    protected final TermFactory termFactory;

    protected AbstractMappingEntryCluster(IQTree tree,
                                          RDFFactTemplates rdfTemplates,
                                          VariableGenerator variableGenerator,
                                          IntermediateQueryFactory iqFactory,
                                          SubstitutionFactory substitutionFactory,
                                          TermFactory termFactory) {
        this.tree = tree;
        this.rdfTemplates = rdfTemplates;
        this.variableGenerator = variableGenerator;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQTree getIQTree() {
        return tree;
    }

    @Override
    public RDFFactTemplates getRDFFactTemplates() {
        return rdfTemplates;
    }

    @Override
    public MappingEntryCluster renameConflictingVariables(VariableGenerator conflictingVariableGenerator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory
                .generateNotConflictingRenaming(conflictingVariableGenerator,
                        tree.getKnownVariables());

        return buildCluster(
                tree.applyFreshRenaming(renamingSubstitution),
                rdfTemplates.apply(renamingSubstitution));
    }

    protected ExtensionalDataNode mergeDataNodes(ExtensionalDataNode dataNode, ExtensionalDataNode otherDataNode){
        var argumentMap = dataNode.getArgumentMap();
        var otherArgumentMap = otherDataNode.getArgumentMap();

        var mergedArgumentMap = Sets.union(argumentMap.keySet(), otherArgumentMap.keySet()).stream()
                .collect(ImmutableCollectors.toMap(
                        idx -> idx,
                        idx -> Optional.<VariableOrGroundTerm>ofNullable(argumentMap.get(idx))
                                .orElseGet(() -> otherArgumentMap.get(idx))
                ));

        return iqFactory.createExtensionalDataNode(dataNode.getRelationDefinition(), mergedArgumentMap);
    }

    protected ConstructionNode unify(ExtensionalDataNode dataNode, ExtensionalDataNode otherDataNode) {
        var argumentMap = (ImmutableMap<Integer, Variable>) dataNode.getArgumentMap();
        var otherArgumentMap = (ImmutableMap<Integer, Variable>) otherDataNode.getArgumentMap();
        var indexes = Sets.union(argumentMap.keySet(), otherArgumentMap.keySet()).stream();

        var unifier = substitutionFactory.onVariables().unifierBuilder()
                .unify(indexes,
                        idx -> otherArgumentMap.getOrDefault(idx, argumentMap.get(idx)),
                        idx -> argumentMap.getOrDefault(idx, otherArgumentMap.get(idx)))
                .build();

        var allVariables = Sets.union(dataNode.getVariables(), otherDataNode.getVariables())
                .immutableCopy();

        return unifier
                .map(s -> iqFactory.createConstructionNode(allVariables, s))
                .orElseGet(() -> iqFactory.createConstructionNode(allVariables));
    }

    protected ConstructionNode createMergedTopConstructionNode(ConstructionNode topConstructionNode, ConstructionNode otherTopConstructionNode) {
        var topSubstitution = topConstructionNode.getSubstitution();
        var otherTopSubstitution = otherTopConstructionNode.getSubstitution();
        var mergedTopSubstitution = topSubstitution.compose(
                otherTopSubstitution);

        return iqFactory.createConstructionNode(
                Sets.union(topConstructionNode.getVariables(), otherTopConstructionNode.getVariables()).immutableCopy(),
                mergedTopSubstitution);
    }

    protected MappingEntryCluster compressCluster(IQTree normalizedTree, RDFFactTemplates mergedRDFTemplates) {
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

        return buildCluster(compressedTree, compressedTemplates);
    }

    protected abstract MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates);
}
