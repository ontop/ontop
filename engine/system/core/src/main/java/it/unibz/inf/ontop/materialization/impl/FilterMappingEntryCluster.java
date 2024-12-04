package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
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
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Its tree is composed of one construction node, one filter node and an extensional node.
 * If the filter node is a NOT NULL filter, it is removed from the tree because it doesn't affect the materialization result.
 */
public class FilterMappingEntryCluster extends AbstractMappingEntryCluster implements MappingEntryCluster {
    private final ExtensionalDataNode dataNode;
    private final Optional<ImmutableExpression> filterCondition;
    private final TermFactory termFactory;

    public FilterMappingEntryCluster(IQTree originalTree,
                                     RDFFactTemplates rdfTemplates,
                                     ExtensionalDataNode dataNode,
                                     VariableGenerator variableGenerator,
                                     IntermediateQueryFactory iqFactory,
                                     TermFactory termFactory,
                                     SubstitutionFactory substitutionFactory) {
        super(originalTree, rdfTemplates, variableGenerator, iqFactory, substitutionFactory);
        this.dataNode = dataNode;
        this.termFactory = termFactory;

        if (originalTree.getChildren().get(0).getRootNode() instanceof FilterNode) {
            IQTree filterSubtree = originalTree.getChildren().get(0);
            ImmutableExpression condition = ((FilterNode) filterSubtree.getRootNode()).getFilterCondition();

            IQTree simplifiedTree = simplifyExplicitNotNullFilter(originalTree,
                    filterSubtree,
                    ((ConstructionNode) originalTree.getRootNode()).getSubstitution());

            this.filterCondition = simplifiedTree.equals(originalTree)
                    ? Optional.of((condition))
                    : Optional.empty();

            ImmutableSet<Variable> nullableVariables = filterCondition.isEmpty()
                    ? Optional.of(condition).get().getVariables()
                    : ImmutableSet.of();
            ConstructionNode newConstructionNode = setNullRDFDatatypes((ConstructionNode) simplifiedTree.getRootNode(),
                    nullableVariables);
            this.tree = iqFactory.createUnaryIQTree(newConstructionNode, simplifiedTree.getChildren().get(0));
        } else {
            this.filterCondition = Optional.empty();
            this.tree = originalTree;
        }
    }

    @Override
    public ImmutableList<ExtensionalDataNode> getDataNodes() {
        return ImmutableList.of(dataNode);
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster other) {
        if (other instanceof SimpleMappingEntryCluster
                || other instanceof DictionaryPatternMappingEntryCluster) {
            return filterCondition
                    .<Optional<MappingEntryCluster>>map(immutableExpression -> Optional.empty())
                    .orElseGet(() -> other.merge(asSimpleMappingEntryCluster()));
        }

        if (!(other instanceof FilterMappingEntryCluster)) {
            return Optional.empty();
        }

        FilterMappingEntryCluster otherFilterCluster = (FilterMappingEntryCluster) other;
        if (!dataNode.getRelationDefinition().equals(otherFilterCluster.dataNode.getRelationDefinition())) {
            return Optional.empty();
        }
        return mergeWithFilterCluster(otherFilterCluster);
    }

    private Optional<MappingEntryCluster> mergeWithFilterCluster(FilterMappingEntryCluster otherFilterCluster) {

        variableGenerator.registerAdditionalVariables(otherFilterCluster.variableGenerator.getKnownVariables());
        FilterMappingEntryCluster otherRenamed = otherFilterCluster.renameConflictingVariables(variableGenerator);

        if (!(dataNode.getArgumentMap().values().stream().allMatch(v -> v instanceof Variable))
                || !(otherRenamed.dataNode.getArgumentMap().values().stream().allMatch(v -> v instanceof Variable))) {
            return Optional.empty();
        }
        ImmutableMap<Integer, Variable> argumentMap = (ImmutableMap<Integer, Variable>) dataNode.getArgumentMap();
        ImmutableMap<Integer, Variable> otherArgumentMap = (ImmutableMap<Integer, Variable>) otherRenamed.dataNode.getArgumentMap();

        if (filterCondition.isEmpty() && otherRenamed.filterCondition.isEmpty()) {
            return asSimpleMappingEntryCluster().merge(otherRenamed.asSimpleMappingEntryCluster());
        } else if (filterCondition.isEmpty() || otherRenamed.filterCondition.isEmpty()) {
            // One of the clusters has a filter condition, the other does not
            return Optional.empty();
        }

        ImmutableExpression filterCondition = this.filterCondition.get();
        ImmutableExpression otherFilterCondition = otherRenamed.filterCondition.get();
        return haveSameFilterCondition(argumentMap, otherArgumentMap, filterCondition, otherFilterCondition)
                ? Optional.of(mergeOnSameFilterCondition(filterCondition, otherRenamed))
                : Optional.empty();
    }

    private IQTree simplifyExplicitNotNullFilter(IQTree tree, IQTree filterSubtree, Substitution<ImmutableTerm> rdfTermConstructionSubstitution) {
        FilterNode filterNode = (FilterNode) filterSubtree.getRootNode();
        ImmutableSet<Variable> filterVariables = filterNode.getFilterCondition().getVariables();

        boolean allFilterVariablesInRDFTerms = rdfTermConstructionSubstitution.getRangeVariables()
                .containsAll(filterVariables);
        boolean isConjunctionOfNotNull = filterNode.getFilterCondition().getFunctionSymbol() instanceof DBAndFunctionSymbol
                && filterNode.getFilterCondition().flattenAND().allMatch(this::isNotNullFilterCondition);
        if (allFilterVariablesInRDFTerms) {
            if (isNotNullFilterCondition(filterNode.getFilterCondition())
                    || isConjunctionOfNotNull) {
                return tree.replaceSubTree(filterSubtree, filterSubtree.getChildren().get(0));
            }
        }

        return tree;
    }

    private ConstructionNode setNullRDFDatatypes(ConstructionNode originalConstructionNode, ImmutableSet<Variable> nullableVariables) {
        var originalSubstitution = originalConstructionNode.getSubstitution();
        var newSubstitution = originalSubstitution.getDomain().stream()
                .map(v -> {
                            ImmutableTerm term = originalSubstitution.apply(v);
                            boolean nullableVariablesInTerm = term.getVariableStream()
                                    .anyMatch(nullableVariables::contains);

                            if (nullableVariablesInTerm && isRDFFunctionalTerm(term)) {
                                ImmutableTerm rdfTerm = ((ImmutableFunctionalTerm) term).getTerms().get(0);
                                ImmutableTerm datatypeTerm = ((ImmutableFunctionalTerm) term).getTerms().get(1);
                                term = termFactory.getRDFFunctionalTerm(rdfTerm,
                                        termFactory.getIfElseNull(termFactory.getDBIsNotNull(rdfTerm), datatypeTerm));
                            }
                            return Map.entry(v, term);
                        })
                .collect(substitutionFactory.toSubstitution());

        return iqFactory.createConstructionNode(
                originalConstructionNode.getVariables(),
                newSubstitution);
    }

    public FilterMappingEntryCluster renameConflictingVariables(VariableGenerator generator) {
        var renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(generator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);

        return new FilterMappingEntryCluster(renamedTree,
                rdfTemplates.apply(renamingSubstitution),
                (ExtensionalDataNode) dataNode.applyFreshRenaming(renamingSubstitution).getRootNode(),
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
                        return attributeIndex.isPresent()
                                && otherAttributeIndex.isPresent()
                                && attributeIndex.get().equals(otherAttributeIndex.get());
                    }
                    return term.equals(otherTerm);
                });
        return sameOperation && sameTerms;
    }

    private MappingEntryCluster mergeOnSameFilterCondition(ImmutableExpression filterCondition,
                                                                 FilterMappingEntryCluster otherFilterRenamed) {
        ConstructionNode constructionNodeAfterUnification = unify(dataNode, otherFilterRenamed.dataNode);

        ExtensionalDataNode mergedDataNode = mergeDataNodes(dataNode, otherFilterRenamed.dataNode);

        var topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
        var otherTopConstructSubstitution = ((ConstructionNode) otherFilterRenamed.tree.getRootNode()).getSubstitution();
        var mergedTopSubstitution = topConstructSubstitution.compose(
                otherTopConstructSubstitution);

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                Sets.union(tree.getVariables(), otherFilterRenamed.tree.getVariables()).immutableCopy(),
                mergedTopSubstitution);

        IQTree childTree = iqFactory.createUnaryIQTree(
                constructionNodeAfterUnification,
                iqFactory.createUnaryIQTree(
                        iqFactory.createFilterNode(filterCondition),
                        mergedDataNode));

        IQTree newTree = iqFactory.createUnaryIQTree(
                topConstructionNode,
                childTree);

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherFilterRenamed.getRDFFactTemplates());

        return compressCluster(
                newTree.normalizeForOptimization(variableGenerator),
                mergedRDFTemplates);
    }

    private boolean isNotNullFilterCondition(ImmutableExpression filterCondition) {
        return filterCondition.getFunctionSymbol() instanceof DBIsNullOrNotFunctionSymbol
                && !((DBIsNullOrNotFunctionSymbol) filterCondition.getFunctionSymbol()).isTrueWhenNull();
    }

    private boolean isRDFFunctionalTerm(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
    }

    private SimpleMappingEntryCluster asSimpleMappingEntryCluster() {
        return new SimpleMappingEntryCluster(
                tree,
                rdfTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);
    }

    @Override
    protected MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates) {
        return new FilterMappingEntryCluster(compressedTree,
                compressedTemplates,
                dataNode,
                variableGenerator,
                iqFactory,
                termFactory,
                substitutionFactory);
    }
}
