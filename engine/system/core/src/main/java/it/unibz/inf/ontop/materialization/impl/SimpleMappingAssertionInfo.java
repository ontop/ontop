package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.materialization.MappingAssertionInformation;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;
import java.util.Optional;

public class SimpleMappingAssertionInfo implements MappingAssertionInformation {
    private final RelationDefinition relationDefinition;
    private final ImmutableMap<Integer, Variable> argumentMap;
    private final Substitution<ImmutableTerm> topConstructSubstitution;
    private final IQTree tree;
    private final VariableGenerator variableGenerator;
    private final RDFFactTemplates rdfFactTemplates;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    public SimpleMappingAssertionInfo(RelationDefinition relationDefinition,
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
        if (other instanceof SimpleMappingAssertionInfo) {
            SimpleMappingAssertionInfo that = (SimpleMappingAssertionInfo) other;
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
    public RDFFactTemplates restrict(ImmutableSet<IRI> predicates) {
        ImmutableCollection<ImmutableList<Variable>> filteredTemplates = rdfFactTemplates.getTriplesOrQuadsVariables().stream()
                .filter(tripleOrQuad -> {
                    ImmutableTerm predicate = topConstructSubstitution.apply(tripleOrQuad.get(1));
                    return predicate instanceof IRI && predicates.contains(predicate);
                })
                .collect(ImmutableCollectors.toList());

        return new RDFFactTemplatesImpl(filteredTemplates);

    }

    @Override
    public ImmutableList<RelationDefinition> getRelationsDefinitions() {
        return ImmutableList.of(relationDefinition);
    }

    @Override
    public Optional<MappingAssertionInformation> merge(MappingAssertionInformation otherInfo) {
        if (otherInfo instanceof ComplexMappingAssertionInfo ||
                otherInfo instanceof FilterMappingAssertionInfo ||
                otherInfo instanceof DictionaryPatternMappingAssertion) {
            return otherInfo.merge(this);
        }

        SimpleMappingAssertionInfo otherSimpleAssertion = (SimpleMappingAssertionInfo) otherInfo;
        if (!relationDefinition.getAtomPredicate().getName()
                .equals(otherSimpleAssertion.getRelationsDefinitions().get(0).getAtomPredicate().getName())) {
            return Optional.empty();
        }
        variableGenerator.registerAdditionalVariables(otherSimpleAssertion.variableGenerator.getKnownVariables());
        SimpleMappingAssertionInfo other = otherSimpleAssertion.renameConflictingVariables(variableGenerator);

        ImmutableMap<Integer, Variable> mergedArgumentMap = mergeRelationArguments(other.argumentMap);

        ExtensionalDataNode relationDefinitionNode = iqFactory.createExtensionalDataNode(
                relationDefinition,
                mergedArgumentMap);

        ConstructionNode optionalRenamingNode = createOptionalRenamingNode(other.argumentMap);

        Substitution<ImmutableTerm> rdfTermsConstructionSubstitution = topConstructSubstitution.compose(other.topConstructSubstitution);
        ImmutableSet<Variable> termsVariables = ImmutableSet.<Variable>builder()
                .addAll(topConstructSubstitution.getDomain())
                .addAll(other.topConstructSubstitution.getDomain())
                .build();
        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(termsVariables, rdfTermsConstructionSubstitution);
        IQTree mappingTree = iqFactory.createUnaryIQTree(topConstructionNode,
                iqFactory.createUnaryIQTree(optionalRenamingNode, relationDefinitionNode));

        RDFFactTemplates mergedRDFTemplates = rdfFactTemplates.merge(other.rdfFactTemplates);
        var treeTemplatesPair = compressMappingAssertion(mappingTree.normalizeForOptimization(variableGenerator), mergedRDFTemplates);

        return Optional.of(new SimpleMappingAssertionInfo(relationDefinition,
                mergedArgumentMap,
                treeTemplatesPair.getKey(),
                treeTemplatesPair.getValue(),
                variableGenerator,
                iqFactory,
                substitutionFactory));
    }

    private SimpleMappingAssertionInfo renameConflictingVariables(VariableGenerator generator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(generator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);
        RDFFactTemplates renamedRDFTemplates = rdfFactTemplates.apply(renamingSubstitution);

        ImmutableMap<Integer, Variable> renamedArgumentMap = argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (Variable) renamingSubstitution.apply(e.getValue())
                ));
        variableGenerator.registerAdditionalVariables(generator.getKnownVariables());
        return new SimpleMappingAssertionInfo(relationDefinition,
                renamedArgumentMap,
                renamedTree,
                renamedRDFTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }

    private ImmutableMap<Integer, Variable> mergeRelationArguments(ImmutableMap <Integer, Variable > otherArgumentMap){
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

    private ConstructionNode createOptionalRenamingNode(ImmutableMap<Integer, Variable> otherArgumentMap) {
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
}
