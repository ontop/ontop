package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
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

/**
 * Its tree is composed of one construction node, one filter node and an extensional node.
 * If the filter node is a NOT NULL filter, it is removed from the tree because it doesn't affect the materialization result.
 */
public class FilterMappingEntryCluster extends AbstractMappingEntryCluster implements MappingEntryCluster {
    private final ExtensionalDataNode dataNode;
    private final Optional<ImmutableExpression> filterCondition;
    private final QueryTransformerFactory queryTransformerFactory;

    public FilterMappingEntryCluster(IQTree originalTree,
                                     RDFFactTemplates rdfTemplates,
                                     VariableGenerator variableGenerator,
                                     IntermediateQueryFactory iqFactory,
                                     TermFactory termFactory,
                                     SubstitutionFactory substitutionFactory,
                                     QueryTransformerFactory queryTransformerFactory) {
        super(originalTree, rdfTemplates, variableGenerator, iqFactory, substitutionFactory, termFactory);

        this.queryTransformerFactory = queryTransformerFactory;
        if (originalTree.getChildren().get(0).getRootNode() instanceof FilterNode) {
            IQTree filterSubtree = originalTree.getChildren().get(0);
            ImmutableExpression condition = ((FilterNode) filterSubtree.getRootNode()).getFilterCondition();

            IQTree simplifiedTree = simplifyExplicitNotNullFilter(originalTree,
                    filterSubtree,
                    ((ConstructionNode) originalTree.getRootNode()).getSubstitution());

            this.filterCondition = simplifiedTree.equals(originalTree)
                    ? Optional.of((condition))
                    : Optional.empty();

            this.dataNode = simplifiedTree.getChildren().get(0).getRootNode() instanceof ExtensionalDataNode
                    ? (ExtensionalDataNode) simplifiedTree.getChildren().get(0).getRootNode()
                    : (ExtensionalDataNode) simplifiedTree.getChildren().get(0).getChildren().get(0).getRootNode();

            ImmutableSet<Variable> nullableVariables = filterCondition.isEmpty()
                    ? Optional.of(condition).get().getVariables()
                    : ImmutableSet.of();
            ConstructionNode newConstructionNode = setNullRDFDatatypes((ConstructionNode) simplifiedTree.getRootNode(),
                    nullableVariables);
            this.tree = iqFactory.createUnaryIQTree(newConstructionNode, simplifiedTree.getChildren().get(0));
        } else {
            this.filterCondition = Optional.empty();
            this.dataNode = (ExtensionalDataNode) originalTree.getChildren().get(0).getRootNode();
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
        FilterMappingEntryCluster otherRenamed = (FilterMappingEntryCluster) otherFilterCluster.renameConflictingVariables(variableGenerator);

        if (!(dataNode.getArgumentMap().values().stream().allMatch(v -> v instanceof Variable))
                || !(otherRenamed.dataNode.getArgumentMap().values().stream().allMatch(v -> v instanceof Variable))) {
            return Optional.empty();
        }

        if (filterCondition.isEmpty() && otherRenamed.filterCondition.isEmpty()) {
            return asSimpleMappingEntryCluster().merge(otherRenamed.asSimpleMappingEntryCluster());
        } else if (filterCondition.isEmpty() || otherRenamed.filterCondition.isEmpty()) {
            // One of the clusters has a filter condition, the other does not
            return Optional.empty();
        }

        return mergeWithBothFilterConditions(filterCondition.get(), otherRenamed);
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


    private boolean haveSameFilterCondition(Substitution<ImmutableTerm> unificationSubstitution,
                                            ImmutableExpression filterCondition,
                                            FilterMappingEntryCluster otherFilterRenamed) {
                var renamingSubstitution = unificationSubstitution.stream()
                .filter(e -> e.getValue() instanceof Variable)
                .map(e -> Map.entry(e.getKey(), (Variable) e.getValue()))
                .collect(substitutionFactory.toSubstitution());

        IQTree renamedOtherFilter = queryTransformerFactory.createRenamer(renamingSubstitution.injective()).transform(otherFilterRenamed.tree);
        var tmpCluster = new FilterMappingEntryCluster(renamedOtherFilter,
                otherFilterRenamed.rdfTemplates,
                otherFilterRenamed.variableGenerator,
                otherFilterRenamed.iqFactory,
                otherFilterRenamed.termFactory,
                otherFilterRenamed.substitutionFactory,
                queryTransformerFactory);

        return tmpCluster.filterCondition.isPresent() && filterCondition.equals(tmpCluster.filterCondition.get());
    }


    private Optional<MappingEntryCluster> mergeWithBothFilterConditions(ImmutableExpression filterCondition,
                                                                        FilterMappingEntryCluster otherFilterRenamed) {
        ConstructionNode constructionNodeAfterUnification = unify(dataNode, otherFilterRenamed.dataNode);

        if (!haveSameFilterCondition(constructionNodeAfterUnification.getSubstitution(), filterCondition, otherFilterRenamed)){
            return Optional.empty();
        }

        ExtensionalDataNode mergedDataNode = mergeDataNodes(dataNode, otherFilterRenamed.dataNode);
        ConstructionNode topConstructionNode = createMergedTopConstructionNode(
                (ConstructionNode) tree.getRootNode(),
                (ConstructionNode) otherFilterRenamed.tree.getRootNode());

        IQTree childTree = iqFactory.createUnaryIQTree(
                constructionNodeAfterUnification,
                iqFactory.createUnaryIQTree(
                        iqFactory.createFilterNode(filterCondition),
                        mergedDataNode));

        IQTree newTree = iqFactory.createUnaryIQTree(
                topConstructionNode,
                childTree);

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherFilterRenamed.getRDFFactTemplates());

        return Optional.of(compressCluster(
                newTree.normalizeForOptimization(variableGenerator),
                mergedRDFTemplates));
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
                substitutionFactory,
                termFactory);
    }

    @Override
    protected MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates) {
        return new FilterMappingEntryCluster(compressedTree,
                compressedTemplates,
                variableGenerator,
                iqFactory,
                termFactory,
                substitutionFactory,
                queryTransformerFactory);
    }

}
