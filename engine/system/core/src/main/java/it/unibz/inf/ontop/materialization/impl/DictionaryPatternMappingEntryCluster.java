package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;

public class DictionaryPatternMappingEntryCluster extends AbstractMappingEntryCluster implements MappingEntryCluster {
    private final ExtensionalDataNode dataNode;

    public DictionaryPatternMappingEntryCluster(IQTree tree,
                                                RDFFactTemplates rdfTemplates,
                                                VariableGenerator variableGenerator,
                                                IntermediateQueryFactory iqFactory,
                                                SubstitutionFactory substitutionFactory,
                                                TermFactory termFactory) {
        super(tree, rdfTemplates, variableGenerator, iqFactory, substitutionFactory, termFactory);

        var originalDataNode = (ExtensionalDataNode) this.tree.getChildren().get(0);
        this.tree = originalDataNode.getArgumentMap().values().stream()
                .anyMatch(t -> t instanceof DBConstant)
                ? makeEqualityConditionExplicit(tree, originalDataNode)
                : tree;
        this.dataNode = (ExtensionalDataNode) this.tree.getChildren().get(0);
    }

    @Override
    public ImmutableList<ExtensionalDataNode> getDataNodes() {
        return ImmutableList.of(dataNode);
    }

    @Override
    public Optional<MappingEntryCluster> merge(MappingEntryCluster other) {
        if (other instanceof FilterMappingEntryCluster) {
            return other.merge(this);
        }

        if (!(other instanceof DictionaryPatternMappingEntryCluster
                || other instanceof SimpleMappingEntryCluster)) {
            return Optional.empty();
        }

        if (!(dataNode.getArgumentMap().values().stream().allMatch(v -> v instanceof Variable))) {
            return Optional.empty();
        }

        if (!dataNode.getRelationDefinition().equals(
                other.getDataNodes().get(0).getRelationDefinition())) {
            return Optional.empty();
        }

        variableGenerator.registerAdditionalVariables(other.getIQTree().getKnownVariables());
        MappingEntryCluster otherRenamed = other.renameConflictingVariables(variableGenerator);
        if (otherRenamed instanceof SimpleMappingEntryCluster) {
            return Optional.of(mergeWithSimpleCluster((SimpleMappingEntryCluster) otherRenamed));
        }

        return Optional.of(mergeWithDictionaryCluster((DictionaryPatternMappingEntryCluster) otherRenamed));

    }

    private IQTree makeEqualityConditionExplicit(IQTree tree, ExtensionalDataNode dataNode) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> originalArgumentMap = dataNode.getArgumentMap();

        ImmutableMap<Integer, QuotedID> constantAttributes = originalArgumentMap.entrySet().stream()
                .filter(e -> e.getValue() instanceof DBConstant)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toMap(
                        k -> k,
                        k -> dataNode.getRelationDefinition().getAttribute(k + 1).getID()));

        ImmutableMap<Integer, Variable> constantTermsVariables = constantAttributes.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> variableGenerator.generateNewVariable(e.getValue().getName())));

        ImmutableMap<Integer, DBConstant> constantValues = constantAttributes.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (DBConstant) originalArgumentMap.get(e.getKey())
                ));

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = originalArgumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() instanceof DBConstant
                                ? constantTermsVariables.get(e.getKey())
                                : e.getValue()));

        ConstructionNode topNode = (ConstructionNode) tree.getRootNode();
        Substitution<ImmutableTerm> newTopSubstitution = setPossiblyNullRDFTerms(
                topNode.getSubstitution(), constantTermsVariables, constantValues);
        ConstructionNode newTopNode = iqFactory.createConstructionNode(topNode.getVariables(), newTopSubstitution);

        ExtensionalDataNode variablesOnlyDataNode = iqFactory.createExtensionalDataNode(
                dataNode.getRelationDefinition(), newArgumentMap);

        return iqFactory.createUnaryIQTree(newTopNode, variablesOnlyDataNode);
    }

    private Substitution<ImmutableTerm> setPossiblyNullRDFTerms(Substitution<ImmutableTerm> rdfTermConstructionSubstitution,
                                                                ImmutableMap<Integer, Variable> constantVariables,
                                                                ImmutableMap<Integer, DBConstant> constantValues) {
        if (!constantVariables.keySet().equals(constantValues.keySet())) {
            throw new MinorOntopInternalBugException("The constant variables and values should have the same keys");
        }

        ImmutableExpression equalityCondition = termFactory.getConjunction(
                constantValues.entrySet().stream()
                        .map(e -> termFactory.getStrictEquality(constantVariables.get(e.getKey()),
                                e.getValue()))
                        .collect(ImmutableCollectors.toList()));

        return rdfTermConstructionSubstitution.stream()
                .map(e -> Map.entry(
                        e.getKey(),
                        termFactory.getIfElseNull(equalityCondition, e.getValue())
                ))
                .collect(substitutionFactory.toSubstitution());
    }

    private MappingEntryCluster mergeWithDictionaryCluster(DictionaryPatternMappingEntryCluster otherDictionaryCluster) {
        IQTree newTree = createMergedUnaryIQTree(otherDictionaryCluster);

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherDictionaryCluster.getRDFFactTemplates());

        Substitution<ImmutableTerm> simplifiedSubstitution = compressIfElseNullTerms(
                ((ConstructionNode)newTree.getRootNode()).getSubstitution(), mergedRDFTemplates.getVariables());
        ConstructionNode simplifiedConstructionNode = iqFactory.createConstructionNode(
                simplifiedSubstitution.getDomain(),
                simplifiedSubstitution);
        IQTree simplifiedTree = iqFactory.createUnaryIQTree(simplifiedConstructionNode, newTree.getChildren().get(0));

        return compressCluster(simplifiedTree, mergedRDFTemplates);
    }

    private MappingEntryCluster mergeWithSimpleCluster(SimpleMappingEntryCluster otherSimpleCluster) {
        IQTree mappingTree = createMergedUnaryIQTree(otherSimpleCluster);

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherSimpleCluster.getRDFFactTemplates());

        return compressCluster(mappingTree, mergedRDFTemplates);
    }

    private IQTree createMergedUnaryIQTree(MappingEntryCluster otherCluster){
        ExtensionalDataNode mergedDataNode = mergeDataNodes(dataNode, otherCluster.getDataNodes().get(0));

        ConstructionNode optionalRenamingNode = unify(dataNode, otherCluster.getDataNodes().get(0));

        IQTree childTree = iqFactory.createUnaryIQTree(optionalRenamingNode, mergedDataNode);

        ConstructionNode topConstructionNode = createMergedTopConstructionNode(
                (ConstructionNode) tree.getRootNode(),
                (ConstructionNode) otherCluster.getIQTree().getRootNode());

        return iqFactory.createUnaryIQTree(
                topConstructionNode,
                childTree).normalizeForOptimization(variableGenerator);
    }

    private Substitution<ImmutableTerm> compressIfElseNullTerms(Substitution<ImmutableTerm> rdfTermsConstructionSubstitution,
                                                                ImmutableSet<Variable> projectedVariables) {
        ImmutableMap<Variable, NonGroundFunctionalTerm> rdfFunctionalTerms = rdfTermsConstructionSubstitution.stream()
                .filter(e -> projectedVariables.contains(e.getKey())
                        && e.getValue() instanceof NonGroundFunctionalTerm
                        && ((NonGroundFunctionalTerm) e.getValue()).getFunctionSymbol() instanceof RDFTermFunctionSymbol)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ((NonGroundFunctionalTerm) e.getValue())
                ));

        ImmutableMap<Variable, ImmutableList<? extends ImmutableTerm>> rdfIfElseNullTerms = rdfFunctionalTerms.entrySet().stream()
                .filter(e -> e.getValue().getTerm(0) instanceof ImmutableFunctionalTerm
                        && ( (ImmutableFunctionalTerm)e.getValue().getTerm(0)).getFunctionSymbol() instanceof DBIfElseNullFunctionSymbol)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getTerms()
                ));

        Substitution<ImmutableTerm> ifElseNullDisjunctionSubstitution = createIfElseNullDisjunctionSubstitution(rdfIfElseNullTerms);

        Substitution<ImmutableTerm> notIfElseNullTerms = rdfTermsConstructionSubstitution.stream()
                .filter(e -> !rdfIfElseNullTerms.containsKey(e.getKey()))
                .collect(substitutionFactory.toSubstitution());

        return notIfElseNullTerms.compose(ifElseNullDisjunctionSubstitution);
    }

    private Substitution<ImmutableTerm> createIfElseNullDisjunctionSubstitution(ImmutableMap<Variable, ImmutableList<? extends ImmutableTerm>> rdfIfElseNullTerms) {
        ImmutableMap<Variable, ImmutableTerm> thenLexicalTerms = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ((ImmutableFunctionalTerm)e.getValue()
                                .get(0)).getTerm(1)
                ));

        ImmutableMap<Variable, ImmutableTerm> thenLexicalTermsDatatypes = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ((ImmutableFunctionalTerm)e.getValue()
                                .get(1)).getTerm(1)
                ));

        ImmutableMap<Variable, ImmutableExpression> ifConditionsMap = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (ImmutableExpression) ((ImmutableFunctionalTerm)e.getValue()
                                .get(0)).getTerm(0)));

        ImmutableSet<ImmutableSet<Variable>> projectedVarsPerLexicalTerm = thenLexicalTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMultimap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                )).asMap().values().stream()
                .map(ImmutableSet::copyOf)
                .collect(ImmutableCollectors.toSet());

        return projectedVarsPerLexicalTerm.stream()
                .map(variables -> {
                    ImmutableExpression disjunctionEqualityConditions = termFactory.getDisjunction(variables.stream()
                                    .map(ifConditionsMap::get))
                            .orElseThrow(() -> new MinorOntopInternalBugException("The disjunction of conditions should not be empty"));
                    return variables.stream()
                            .map(v -> {
                                        ImmutableTerm thenTerm = thenLexicalTerms.get(v);
                                        ImmutableTerm datatype = thenLexicalTermsDatatypes.get(v);
                                        ImmutableTerm rdfTerm = termFactory.getRDFFunctionalTerm(
                                                termFactory.getIfElseNull(disjunctionEqualityConditions, thenTerm),
                                                termFactory.getIfElseNull(disjunctionEqualityConditions, datatype));
                                        return Map.entry(v, rdfTerm);
                                    }
                            );
                })
                .flatMap(Streams::concat)
                .collect(substitutionFactory.toSubstitution());
    }

    @Override
    protected MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates) {
        return new DictionaryPatternMappingEntryCluster(
                compressedTree,
                compressedTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory,
                termFactory);
    }

}
