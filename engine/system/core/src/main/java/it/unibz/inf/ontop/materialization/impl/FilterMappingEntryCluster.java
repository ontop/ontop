package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsNullOrNotFunctionSymbol;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class FilterMappingEntryCluster implements MappingEntryCluster {
    private final IQTree tree;
    private final RDFFactTemplates rdfFactTemplates;
    private final ExtensionalDataNode relationDefinitionNode;
    private final Optional<ImmutableExpression> notSimplifiedFilterCondition;
    private final VariableGenerator variableGenerator;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    public FilterMappingEntryCluster(IQTree originalTree,
                                     RDFFactTemplates rdfTemplates,
                                     ExtensionalDataNode relationDefinitionNode,
                                     VariableGenerator variableGenerator,
                                     IntermediateQueryFactory iqFactory,
                                     TermFactory termFactory,
                                     SubstitutionFactory substitutionFactory) {
        this.rdfFactTemplates = rdfTemplates;
        this.relationDefinitionNode = relationDefinitionNode;
        this.variableGenerator = variableGenerator;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;

        Optional<IQTree> potentialFilterSubtree = findFilterSubtree(originalTree);
        if (potentialFilterSubtree.isEmpty()) {
            this.notSimplifiedFilterCondition = Optional.empty();
            this.tree = originalTree;
        } else {
            IQTree filterSubtree = potentialFilterSubtree.get();
            Optional<IQTree> simplifiedTree = simplifyExplicitNotNullFilter(originalTree,
                    filterSubtree,
                    ((ConstructionNode) originalTree.getRootNode()).getSubstitution());

            this.notSimplifiedFilterCondition = simplifiedTree.isPresent()
                    ? Optional.empty()
                    : Optional.of(((FilterNode) filterSubtree.getRootNode()).getFilterCondition());
            ImmutableSet<Variable> nullableVariables = notSimplifiedFilterCondition.isEmpty()
                    ? Optional.of(((FilterNode) filterSubtree.getRootNode()).getFilterCondition()).get().getVariables()
                    : ImmutableSet.of();
            this.tree = setNullRDFDatatypes(simplifiedTree.orElse(originalTree), nullableVariables);
        }
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
        return ImmutableList.of(relationDefinitionNode.getRelationDefinition());
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster other) {
        if (other instanceof ComplexMappingEntryCluster || other instanceof JoinMappingEntryCluster) {
            return Optional.empty();
        }

        if (!(relationDefinitionNode.getArgumentMap().values().stream().allMatch(v -> v instanceof Variable))) {
            return Optional.empty();
        }
        ImmutableMap<Integer, Variable> argumentMap = (ImmutableMap<Integer, Variable>) relationDefinitionNode.getArgumentMap();

        boolean sameRelation = other.getRelationsDefinitions().get(0).getAtomPredicate().getName()
                .equals(getRelationsDefinitions().get(0).getAtomPredicate().getName());
        if (!sameRelation) {
            return Optional.empty();
        }

        if (other instanceof SimpleMappingEntryCluster || other instanceof DictionaryPatternMappingEntryCluster) {
            return notSimplifiedFilterCondition.isEmpty()
                    ? other.merge(new SimpleMappingEntryCluster(
                        relationDefinitionNode.getRelationDefinition(),
                        argumentMap,
                        tree,
                        rdfFactTemplates,
                        variableGenerator,
                        iqFactory,
                        substitutionFactory))
                    : Optional.empty();
        }

        FilterMappingEntryCluster otherFilterInfo = (FilterMappingEntryCluster) other;
        variableGenerator.registerAdditionalVariables(otherFilterInfo.variableGenerator.getKnownVariables());
        FilterMappingEntryCluster otherFilterInfoRenamed = otherFilterInfo.renameConflictingVariables(variableGenerator);

        ImmutableMap<Integer, Variable> otherArgumentMap = (ImmutableMap<Integer, Variable>) otherFilterInfoRenamed.relationDefinitionNode.getArgumentMap();
        if (notSimplifiedFilterCondition.isEmpty() && otherFilterInfoRenamed.getNotSimplifiedFilterCondition().isEmpty()) {
            SimpleMappingEntryCluster simpleMappingEntryCluster = new SimpleMappingEntryCluster(
                    relationDefinitionNode.getRelationDefinition(),
                    argumentMap,
                    tree,
                    rdfFactTemplates,
                    variableGenerator,
                    iqFactory,
                    substitutionFactory);
            SimpleMappingEntryCluster otherSimpleMappingEntryCluster = new SimpleMappingEntryCluster(
                    otherFilterInfoRenamed.relationDefinitionNode.getRelationDefinition(),
                    otherArgumentMap,
                    otherFilterInfoRenamed.tree,
                    otherFilterInfoRenamed.rdfFactTemplates,
                    otherFilterInfoRenamed.variableGenerator,
                    otherFilterInfoRenamed.iqFactory,
                    otherFilterInfoRenamed.substitutionFactory);
            return simpleMappingEntryCluster.merge(otherSimpleMappingEntryCluster);
        } else if (notSimplifiedFilterCondition.isEmpty() || otherFilterInfoRenamed.getNotSimplifiedFilterCondition().isEmpty()) {
            return Optional.empty();
        }

        ImmutableExpression filterCondition = notSimplifiedFilterCondition.get();
        ImmutableExpression otherFilterCondition = otherFilterInfoRenamed.getNotSimplifiedFilterCondition().get();
        if (!haveSameFilterCondition(argumentMap, otherArgumentMap, filterCondition, otherFilterCondition)) {
            return Optional.empty();
        }
        return Optional.of(mergeOnSameFilterCondition(argumentMap, filterCondition, otherFilterInfoRenamed));
    }

    public Optional<ImmutableExpression> getNotSimplifiedFilterCondition() {
        return notSimplifiedFilterCondition;
    }

    private Optional<IQTree> simplifyExplicitNotNullFilter(IQTree tree, IQTree filterSubtree, Substitution<ImmutableTerm> rdfTermConstructionSubstitution) {
        FilterNode filterNode = (FilterNode) filterSubtree.getRootNode();

        ImmutableSet<Variable> filterVariables = filterNode.getFilterCondition().getVariables();
        boolean allFilterVariablesInRDFTerms = rdfTermConstructionSubstitution.getRangeVariables().containsAll(filterVariables);
        boolean isConjunctionNotNull = filterNode.getFilterCondition().getFunctionSymbol() instanceof DBAndFunctionSymbol &&
                filterNode.getFilterCondition().flattenAND().allMatch(this::isNotNullFilterCondition);
        if (allFilterVariablesInRDFTerms && isNotNullFilterCondition(filterNode.getFilterCondition())) {
            return Optional.ofNullable(tree.replaceSubTree(filterSubtree, filterSubtree.getChildren().get(0)));
        } else if (allFilterVariablesInRDFTerms && isConjunctionNotNull) {
            return Optional.ofNullable(tree.replaceSubTree(filterSubtree, filterSubtree.getChildren().get(0)));
        }

        return Optional.empty();
    }

    private IQTree setNullRDFDatatypes(IQTree tree, ImmutableSet<Variable> nullableVariables) {
        ConstructionNode originalConstructionNode = (ConstructionNode) tree.getRootNode();
        Substitution<ImmutableTerm> originalSubstitution = originalConstructionNode.getSubstitution();
        ImmutableMap<Variable, ImmutableTerm> substitutionMap = originalSubstitution.getDomain().stream()
                .collect(ImmutableCollectors.toMap( v -> v,
                        v -> {
                            ImmutableTerm term = originalSubstitution.apply(v);
                            boolean nullableVariablesInTerm = term.getVariableStream().anyMatch(nullableVariables::contains);

                            if (nullableVariablesInTerm && isRDFFunctionalTerm(term)) {
                                ImmutableTerm rdfTerm = ((ImmutableFunctionalTerm) term).getTerms().get(0);
                                ImmutableTerm datatypeTerm = ((ImmutableFunctionalTerm) term).getTerms().get(1);
                                return termFactory.getRDFFunctionalTerm(rdfTerm,
                                        termFactory.getIfElseNull(termFactory.getDBIsNotNull(rdfTerm), datatypeTerm));
                            }
                            return term;
                        }));

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(originalConstructionNode.getVariables(),
                substitutionMap.entrySet().stream().collect(substitutionFactory.toSubstitution()));
        return iqFactory.createUnaryIQTree(newConstructionNode, tree.getChildren().get(0));
    }

    public FilterMappingEntryCluster renameConflictingVariables(VariableGenerator generator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(generator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);

        return new FilterMappingEntryCluster(renamedTree,
                rdfFactTemplates.apply(renamingSubstitution),
                (ExtensionalDataNode) relationDefinitionNode.applyFreshRenaming(renamingSubstitution).getRootNode(),
                variableGenerator,
                iqFactory,
                termFactory,
                substitutionFactory
        );
    }

    private boolean haveSameFilterCondition(ImmutableMap<Integer, Variable> argumentMap,
                                            ImmutableMap<Integer, Variable> otherArgumentMap,
                                            ImmutableExpression filterCondition,
                                            ImmutableExpression otherFilterCondition) {
        boolean sameOperation = filterCondition.getFunctionSymbol().equals(otherFilterCondition.getFunctionSymbol());
        boolean sameTerms = IntStream.range(0, filterCondition.getTerms().size())
                .allMatch(i -> {
                    ImmutableTerm term = filterCondition.getTerms().get(i);
                    ImmutableTerm otherTerm = otherFilterCondition.getTerms().get(i);
                    if (term instanceof Variable && otherTerm instanceof Variable) {
                        Optional<Integer> attributeIndex = argumentMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(term))
                                .map(Map.Entry::getKey)
                                .findFirst();
                        Optional<Integer> otherAttributeIndex = otherArgumentMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(otherTerm))
                                .map(Map.Entry::getKey)
                                .findFirst();
                        return attributeIndex.isPresent() && otherAttributeIndex.isPresent() && attributeIndex.get().equals(otherAttributeIndex.get());
                    }
                    return term.equals(otherTerm);
                });
        return sameOperation && sameTerms;
    }

    private FilterMappingEntryCluster mergeOnSameFilterCondition(ImmutableMap<Integer, Variable> argumentMap,
                                                                 ImmutableExpression filterCondition,
                                                                 FilterMappingEntryCluster otherFilterInfo) {
        ImmutableMap<Integer, Variable> otherArgumentMap = (ImmutableMap<Integer, Variable>) otherFilterInfo.relationDefinitionNode.getArgumentMap();
        ImmutableMap<Integer, Variable> mergedArgumentMap = mergeRelationArguments(argumentMap, otherArgumentMap);

        ExtensionalDataNode relationDefinitionNode = iqFactory.createExtensionalDataNode(
                this.relationDefinitionNode.getRelationDefinition(),
                mergedArgumentMap);

        ConstructionNode optionalRenamingNode = createOptionalRenamingNode(argumentMap, otherArgumentMap);
        IQTree filterTree = iqFactory.createUnaryIQTree(iqFactory.createFilterNode(filterCondition), relationDefinitionNode);
        IQTree childTree = iqFactory.createUnaryIQTree(optionalRenamingNode, filterTree);

        RDFFactTemplates mergedRDFTemplates = rdfFactTemplates.merge(otherFilterInfo.getRDFFactTemplates());

        Substitution<ImmutableTerm> topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> otherTopConstructSubstitution = ((ConstructionNode) otherFilterInfo.tree.getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> RDFTermsConstructionSubstitution = topConstructSubstitution.compose(otherTopConstructSubstitution);
        ImmutableSet<Variable> termsVariables = ImmutableSet.<Variable>builder()
                .addAll(topConstructSubstitution.getDomain())
                .addAll(otherTopConstructSubstitution.getDomain())
                .build();
        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(termsVariables, RDFTermsConstructionSubstitution);
        IQTree mappingTree = iqFactory.createUnaryIQTree(topConstructionNode, childTree);

        Map.Entry<IQTree, RDFFactTemplates> treeTemplatePair = compressMappingAssertion(mappingTree.normalizeForOptimization(variableGenerator), mergedRDFTemplates);

        return new FilterMappingEntryCluster(
                treeTemplatePair.getKey(),
                treeTemplatePair.getValue(),
                relationDefinitionNode,
                variableGenerator,
                iqFactory,
                termFactory,
                substitutionFactory);
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

    private boolean isNotNullFilterCondition(ImmutableExpression filterCondition) {
        return filterCondition.getFunctionSymbol() instanceof DBIsNullOrNotFunctionSymbol &&
                !((DBIsNullOrNotFunctionSymbol) filterCondition.getFunctionSymbol()).isTrueWhenNull();
    }

    private boolean isRDFFunctionalTerm(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
    }

    private Optional<IQTree> findFilterSubtree(IQTree tree) {
        if (tree.getRootNode() instanceof FilterNode) {
            return Optional.of(tree);
        } else {
            return tree.getChildren().stream()
                    .map(this::findFilterSubtree)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny();
        }
    }

}
