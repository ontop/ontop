package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.materialization.MappingAssertionInformation;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.UnifierBuilder;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

public class SimpleMappingAssertionInfo implements MappingAssertionInformation {
    private final RelationDefinition relationDefinition;
    private final ImmutableMap<Integer, Variable> argumentMap;
    private final Substitution<ImmutableTerm> topConstructSubstitution;
    private final VariableGenerator variableGenerator;
    private final IQTree tree;
    private final RDFFactTemplates RDFTemplates;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    public SimpleMappingAssertionInfo(RelationDefinition relationDefinition,
                                      ImmutableMap<Integer, Variable> argumentMap,
                                      Substitution<ImmutableTerm> topConstructSubstitution,
                                      IQTree tree,
                                      RDFFactTemplates RDFTemplates,
                                      VariableGenerator variableGenerator,
                                      IntermediateQueryFactory iqFactory,
                                      SubstitutionFactory substitutionFactory) {
        this.relationDefinition = relationDefinition;
        this.argumentMap = argumentMap;
        this.topConstructSubstitution = topConstructSubstitution;
        this.tree = tree;
        this.variableGenerator = variableGenerator;
        this.RDFTemplates = RDFTemplates;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SimpleMappingAssertionInfo) {
            SimpleMappingAssertionInfo that = (SimpleMappingAssertionInfo) other;
            return this.tree.equals(that.tree);
        }
        return false;
    }

    public String getRelationName() {
        return relationDefinition.getAtomPredicate().getName();
    }

    @Override
    public IQTree getIQTree() {
        return tree;
    }

    @Override
    public RDFFactTemplates getRDFFactTemplates() {
        return RDFTemplates;
    }

    @Override

    public Optional<MappingAssertionInformation> merge(MappingAssertionInformation otherInfo) {
        if (!(otherInfo instanceof SimpleMappingAssertionInfo)
                || !relationDefinition.getAtomPredicate().getName().equals(((SimpleMappingAssertionInfo) otherInfo).getRelationName())) {
            return Optional.empty();
        }
        SimpleMappingAssertionInfo other = (SimpleMappingAssertionInfo) otherInfo;

        ImmutablePair<ImmutableMap<Integer, Variable>, Optional<Substitution<Variable>>> mergedPair = mergeRelationArguments(other);
        ImmutableMap<Integer, Variable> mergedArgumentMap = mergedPair.left;
        Optional<Substitution<Variable>> mergeRenamingSubstitution = mergedPair.right;

        ExtensionalDataNode relationDefinitionNode = iqFactory.createExtensionalDataNode(
                relationDefinition,
                mergedArgumentMap);

        ConstructionNode OptionalRenamingNode;
        if (mergeRenamingSubstitution.isPresent()) {
            ImmutableSet<Variable> originalRelationsVariables = Streams.concat(
                    argumentMap.values().stream(),
                    other.argumentMap.values().stream(),
                    mergeRenamingSubstitution.get().getRangeVariables().stream()
            ).collect(ImmutableCollectors.toSet());
            OptionalRenamingNode = iqFactory.createConstructionNode(originalRelationsVariables, mergeRenamingSubstitution.get());
        } else {
            ImmutableSet<Variable> originalRelationsVariables = Streams.concat(
                    argumentMap.values().stream(),
                    other.argumentMap.values().stream()
            ).collect(ImmutableCollectors.toSet());
            OptionalRenamingNode = iqFactory.createConstructionNode(originalRelationsVariables);
        }
        IQTree childTree = iqFactory.createUnaryIQTree(OptionalRenamingNode, relationDefinitionNode);

        RDFFactTemplates mergedRDFTemplates = RDFTemplates.merge(other.RDFTemplates);

        // TODO: additional check for same-name variables??
        Substitution<ImmutableTerm> termConstructionSubstitution = topConstructSubstitution.compose(other.topConstructSubstitution);
        ImmutableSet<Variable> termsVariables = ImmutableSet.<Variable>builder()
                .addAll(topConstructSubstitution.getDomain())
                .addAll(other.topConstructSubstitution.getDomain())
                .build();
        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(termsVariables, termConstructionSubstitution);
        IQTree mappingTree = iqFactory.createUnaryIQTree(topConstructionNode, childTree);

        variableGenerator.registerAdditionalVariables(other.variableGenerator.getKnownVariables());
        IQTree normalizedTree = mappingTree.normalizeForOptimization(variableGenerator);

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

        return Optional.of(new SimpleMappingAssertionInfo(relationDefinition,
                mergedArgumentMap,
                ((ConstructionNode) compressedTree.getRootNode()).getSubstitution(),
                compressedTree,
                compressedTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory));
    }

    private ImmutablePair<ImmutableMap<Integer, Variable>, Optional<Substitution<Variable>>> mergeRelationArguments(SimpleMappingAssertionInfo other) {
        ImmutableSet<Integer> keys = ImmutableSet.<Integer>builder()
                .addAll(argumentMap.keySet())
                .addAll(other.argumentMap.keySet())
                .build();

        UnifierBuilder<Variable> unifierBuilder = substitutionFactory.onVariables().unifierBuilder();
        unifierBuilder.unify(keys.stream(),
                idx -> other.argumentMap.getOrDefault(idx, argumentMap.get(idx)),
                idx -> argumentMap.getOrDefault(idx, other.argumentMap.get(idx)));
        Optional<Substitution<Variable>> mergedSubstitution = unifierBuilder.build();

        ImmutableMap<Integer, Variable> mergedArgumentMap = keys.stream()
                .collect(ImmutableCollectors.toMap(
                        idx -> idx,
                        idx -> argumentMap.getOrDefault(idx, other.argumentMap.get(idx))
                ));
        return ImmutablePair.of(mergedArgumentMap, mergedSubstitution);
    }

}
