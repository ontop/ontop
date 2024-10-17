package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.materialization.MappingAssertionInformation;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class FilterMappingAssertionInfo implements MappingAssertionInformation {
    private final IQTree tree;
    private final RDFFactTemplates rdfFactTemplates;
    private final ExtensionalDataNode relationDefinitionNode;
    private final Optional<ImmutableExpression> optionalFilterCondition;
    private final VariableGenerator variableGenerator;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    public FilterMappingAssertionInfo(IQTree originalTree,
                                      RDFFactTemplates rdfTemplates,
                                      ExtensionalDataNode relationDefinitionNode,
                                      IQTree filterSubtree,
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

        Optional<IQTree> potentiallySimplifiedTree = simplifyExplicitNotNullFilter(originalTree,
                filterSubtree,
                ((ConstructionNode) originalTree.getRootNode()).getSubstitution());

        IQTree simplifiedTree = potentiallySimplifiedTree.orElse(originalTree);
        this.optionalFilterCondition = potentiallySimplifiedTree.isPresent()
                ? Optional.empty()
                : Optional.of(((FilterNode) filterSubtree.getRootNode()).getFilterCondition());
        ImmutableSet<Variable> nullableVariables = optionalFilterCondition.isPresent()
                ? optionalFilterCondition.get().getVariables()
                : ImmutableSet.of();

        this.tree = setPossiblyNullRDFDatatypes(simplifiedTree, nullableVariables);
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
    public Optional<MappingAssertionInformation> merge(MappingAssertionInformation other) {
        if (other instanceof ComplexMappingAssertionInfo) {
            return Optional.empty();
        }

        if (!(relationDefinitionNode.getArgumentMap().values().stream().allMatch(v -> v instanceof Variable))) {
            return Optional.empty();
        }
        ImmutableMap<Integer, Variable> argumentMap = (ImmutableMap<Integer, Variable>) relationDefinitionNode.getArgumentMap();

        if (other instanceof SimpleMappingAssertionInfo) {
            if (optionalFilterCondition.isEmpty()) {
                SimpleMappingAssertionInfo otherSimpleInfo = (SimpleMappingAssertionInfo) other;
                boolean sameRelation = otherSimpleInfo.getRelationsDefinitions().get(0).getAtomPredicate().getName()
                        .equals(getRelationsDefinitions().get(0).getAtomPredicate().getName());
                if (!sameRelation) {
                    return other.merge(new SimpleMappingAssertionInfo(
                            relationDefinitionNode.getRelationDefinition(),
                            argumentMap,
                            tree,
                            rdfFactTemplates,
                            variableGenerator,
                            iqFactory,
                            substitutionFactory));
                }
            }
            return Optional.empty();
        }

        // merge trees with same filter
        FilterMappingAssertionInfo otherFilter = (FilterMappingAssertionInfo) other;
        otherFilter.variableGenerator.registerAdditionalVariables(variableGenerator.getKnownVariables());
        FilterMappingAssertionInfo otherFilterInfo = otherFilter.renameConflictingVariables(otherFilter.variableGenerator);
        variableGenerator.registerAdditionalVariables(otherFilterInfo.variableGenerator.getKnownVariables());

        ImmutableMap<Integer, Variable> otherArgumentMap = (ImmutableMap<Integer, Variable>) otherFilterInfo.relationDefinitionNode.getArgumentMap();
        ImmutableExpression filterCondition = this.optionalFilterCondition.orElseThrow();
        ImmutableExpression otherFilterCondition = otherFilterInfo.getOptionalFilterCondition().orElseThrow();

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
        if (!sameOperation || !sameTerms) {
            return Optional.empty();
        }

        ImmutablePair<ImmutableMap<Integer, Variable>, Optional<Substitution<Variable>>> mergedPair = mergeRelationArguments(argumentMap, otherArgumentMap);
        ImmutableMap<Integer, Variable> mergedArgumentMap = mergedPair.left;
        Optional<Substitution<Variable>> mergeRenamingSubstitution = mergedPair.right;

        ExtensionalDataNode relationDefinitionNode = iqFactory.createExtensionalDataNode(
                this.relationDefinitionNode.getRelationDefinition(),
                mergedArgumentMap);

        ConstructionNode optionalRenamingNode;
        if (mergeRenamingSubstitution.isPresent()) {
            ImmutableSet<Variable> originalRelationsVariables = Streams.concat(
                    argumentMap.values().stream(),
                    otherArgumentMap.values().stream(),
                    mergeRenamingSubstitution.get().getRangeVariables().stream()
            ).collect(ImmutableCollectors.toSet());
            optionalRenamingNode = iqFactory.createConstructionNode(originalRelationsVariables, mergeRenamingSubstitution.get());
        } else {
            ImmutableSet<Variable> originalRelationsVariables = Streams.concat(
                    argumentMap.values().stream(),
                    otherArgumentMap.values().stream()
            ).collect(ImmutableCollectors.toSet());
            optionalRenamingNode = iqFactory.createConstructionNode(originalRelationsVariables);
        }
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

        return Optional.of(new FilterMappingAssertionInfo(
                compressedTree,
                compressedTemplates,
                relationDefinitionNode,
                filterTree,
                variableGenerator,
                iqFactory,
                termFactory,
                substitutionFactory)
        );

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

    public Optional<ImmutableExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
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

    private IQTree setPossiblyNullRDFDatatypes(IQTree tree, ImmutableSet<Variable> nullableVariables) {
        ConstructionNode originalConstructionNode = (ConstructionNode) tree.getRootNode();
        Substitution<ImmutableTerm> originalSubstitution = originalConstructionNode.getSubstitution();
        ImmutableMap<Variable, ImmutableTerm> substitutionMap = originalSubstitution.getDomain().stream()
                .map(v -> {
                    ImmutableTerm term = originalSubstitution.apply(v);
                    boolean nullableVariablesInTerm = term.getVariableStream().anyMatch(nullableVariables::contains);

                    if (nullableVariablesInTerm && isRDFFunctionalTerm(term)) {
                        ImmutableTerm rdfTerm = ((ImmutableFunctionalTerm) term).getTerms().get(0);
                        ImmutableTerm datatypeTerm = ((ImmutableFunctionalTerm) term).getTerms().get(1);
                        return ImmutablePair.of(v, termFactory.getRDFFunctionalTerm(rdfTerm,
                                    termFactory.getIfElseNull(termFactory.getDBIsNotNull(rdfTerm), datatypeTerm)));
                        }
                    return ImmutablePair.of(v, term);
                })
                .collect(ImmutableCollectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));

        Substitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(substitutionMap.keySet().asList(), substitutionMap.values().asList());

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(originalConstructionNode.getVariables(), newSubstitution);
        return iqFactory.createUnaryIQTree(newConstructionNode, tree.getChildren().get(0));
    }

    private FilterMappingAssertionInfo renameConflictingVariables(VariableGenerator generator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(generator, tree.getVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);

        return new FilterMappingAssertionInfo(
                renamedTree,
                rdfFactTemplates.apply(renamingSubstitution),
                (ExtensionalDataNode) relationDefinitionNode.applyFreshRenaming(renamingSubstitution).getRootNode(),
                renamedTree.getChildren().get(0),
                variableGenerator,
                iqFactory,
                termFactory,
                substitutionFactory
        );
    }

    private ImmutablePair<ImmutableMap<Integer, Variable>, Optional<Substitution<Variable>>> mergeRelationArguments(
            ImmutableMap <Integer, Variable > argumentMap,
            ImmutableMap <Integer, Variable > otherArgumentMap){
        ImmutableSet<Integer> keys = ImmutableSet.<Integer>builder()
                .addAll(argumentMap.keySet())
                .addAll(otherArgumentMap.keySet())
                .build();

        Optional<Substitution<Variable>> mergedSubstitution  = substitutionFactory.onVariables().unifierBuilder()
                .unify(keys.stream(),
                        idx -> otherArgumentMap.getOrDefault(idx, argumentMap.get(idx)),
                        idx -> argumentMap.getOrDefault(idx, otherArgumentMap.get(idx)))
                .build();
        ImmutableMap<Integer, Variable> mergedArgumentMap = keys.stream()
                .collect(ImmutableCollectors.toMap(
                        idx -> idx,
                        idx -> argumentMap.getOrDefault(idx, otherArgumentMap.get(idx))
                ));
        return ImmutablePair.of(mergedArgumentMap, mergedSubstitution);
    }

    private boolean isNotNullFilterCondition(ImmutableExpression filterCondition) {
        return filterCondition.getFunctionSymbol() instanceof DBIsNullOrNotFunctionSymbol &&
                !((DBIsNullOrNotFunctionSymbol) filterCondition.getFunctionSymbol()).isTrueWhenNull();
    }

    private boolean isRDFFunctionalTerm(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
    }
}
