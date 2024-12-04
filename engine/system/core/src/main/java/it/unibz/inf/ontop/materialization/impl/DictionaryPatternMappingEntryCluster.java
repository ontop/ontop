package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.Attribute;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DictionaryPatternMappingEntryCluster extends AbstractMappingEntryCluster implements MappingEntryCluster {
    private final ExtensionalDataNode dataNode;
    private final ImmutableMap<Integer, Attribute> constantAttributes;
    private final TermFactory termFactory;

    public DictionaryPatternMappingEntryCluster(IQTree tree,
                                                RDFFactTemplates rdfTemplates,
                                                ImmutableMap<Integer, Attribute> constantAttributes,
                                                ExtensionalDataNode dataNode,
                                                VariableGenerator variableGenerator,
                                                IntermediateQueryFactory iqFactory,
                                                SubstitutionFactory substitutionFactory,
                                                TermFactory termFactory) {
        super(tree, rdfTemplates, variableGenerator, iqFactory, substitutionFactory);
        this.constantAttributes = constantAttributes;
        this.termFactory = termFactory;

        this.tree = dataNode.getArgumentMap().values().stream()
                .anyMatch(t -> t instanceof DBConstant)
                ? makeEqualityConditionExplicit(tree, constantAttributes, dataNode)
                : tree;
        this.dataNode = (ExtensionalDataNode) this.getIQTree().getChildren().get(0);
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
        if (other instanceof SimpleMappingEntryCluster) {
            SimpleMappingEntryCluster otherSimpleCluster = ((SimpleMappingEntryCluster) other)
                    .renameConflictingVariables(variableGenerator);
            return Optional.of(mergeWithSimpleCluster(otherSimpleCluster));
        }

        DictionaryPatternMappingEntryCluster otherDictionaryCluster = ((DictionaryPatternMappingEntryCluster) other)
                .renameConflictingVariables(variableGenerator);
        return Optional.of(mergeWithDictionaryCluster(otherDictionaryCluster));

    }

    private IQTree makeEqualityConditionExplicit(IQTree tree,
                                                 ImmutableMap<Integer, Attribute> constantAttributes,
                                                 ExtensionalDataNode dataNode) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> originalArgumentMap = dataNode.getArgumentMap();
        ImmutableMap<Integer, Variable> constantTermsVariables = constantAttributes.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> variableGenerator.generateNewVariable(e.getValue().getID().getName())));

        ImmutableList<DBConstant> constantValues = constantAttributes.keySet().stream()
                .map(originalArgumentMap::get)
                .map(v -> (DBConstant) v)
                .collect(ImmutableCollectors.toList());

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = originalArgumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() instanceof DBConstant
                                ? constantTermsVariables.get(e.getKey())
                                : e.getValue()));

        ConstructionNode topNode = (ConstructionNode) tree.getRootNode();
        Substitution<ImmutableTerm> newTopSubstitution = setPossiblyNullRDFTerms(
                topNode.getSubstitution(), constantTermsVariables.values().asList(), constantValues);
        ConstructionNode newTopNode = iqFactory.createConstructionNode(topNode.getVariables(), newTopSubstitution);

        ExtensionalDataNode variablesOnlyDataNode = iqFactory.createExtensionalDataNode(
                dataNode.getRelationDefinition(), newArgumentMap);

        return iqFactory.createUnaryIQTree(newTopNode, variablesOnlyDataNode);
    }

    private Substitution<ImmutableTerm> setPossiblyNullRDFTerms(Substitution<ImmutableTerm> rdfTermConstructionSubstitution,
                                                                ImmutableList<Variable> constantVariables,
                                                                ImmutableList<DBConstant> constantValues) {
        ImmutableExpression equalityCondition;
        if (constantVariables.size() == 1) {
             equalityCondition = termFactory.getStrictEquality(constantVariables.get(0), constantValues.get(0));
        } else {
             equalityCondition = termFactory.getConjunction(
                    IntStream.range(0, constantValues.size())
                            .boxed()
                            .map(i -> termFactory.getStrictEquality(constantVariables.get(i),
                                    constantValues.get(i)))
                            .collect(ImmutableCollectors.toList()));
        }

        return rdfTermConstructionSubstitution.stream()
                .map(e -> Map.entry(
                        e.getKey(),
                        termFactory.getIfElseNull(equalityCondition, e.getValue())
                ))
                .collect(substitutionFactory.toSubstitution());
    }

    private DictionaryPatternMappingEntryCluster mergeWithDictionaryCluster(DictionaryPatternMappingEntryCluster otherDictionaryCluster) {
        ExtensionalDataNode mergedDataNode = mergeDataNodes(dataNode, otherDictionaryCluster.dataNode);

        IQTree newTree = createMergedIQTree(otherDictionaryCluster, mergedDataNode);

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherDictionaryCluster.getRDFFactTemplates());

        Substitution<ImmutableTerm> simplifiedSubstitution = compressIfElseNullTerms(
                ((ConstructionNode)newTree.getRootNode()).getSubstitution(), mergedRDFTemplates.getVariables());
        ConstructionNode simplifiedConstructionNode = iqFactory.createConstructionNode(
                simplifiedSubstitution.getDomain(),
                simplifiedSubstitution);
        IQTree simplifiedTree = iqFactory.createUnaryIQTree(simplifiedConstructionNode, newTree.getChildren().get(0));

        DictionaryPatternMappingEntryCluster compressedCluster =
                (DictionaryPatternMappingEntryCluster) compressCluster(simplifiedTree, mergedRDFTemplates);

        ImmutableMap<Integer, Attribute> mergedConstantAttributes = Streams.concat(
                constantAttributes.entrySet().stream(),
                otherDictionaryCluster.constantAttributes.entrySet().stream()
        ).collect(ImmutableCollectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1));

        return compressedCluster.updateConstantAttributes(mergedConstantAttributes);
    }

    private MappingEntryCluster mergeWithSimpleCluster(SimpleMappingEntryCluster otherSimpleCluster) {
        ExtensionalDataNode mergedDataNode = mergeDataNodes(dataNode, otherSimpleCluster.getDataNodes().get(0));

        IQTree mappingTree = createMergedIQTree(otherSimpleCluster, mergedDataNode);

        RDFFactTemplates mergedRDFTemplates = rdfTemplates.merge(otherSimpleCluster.getRDFFactTemplates());

        return compressCluster(mappingTree, mergedRDFTemplates);
    }

    private IQTree createMergedIQTree(MappingEntryCluster otherCluster, ExtensionalDataNode mergedDataNode){

        ConstructionNode optionalRenamingNode = unify(dataNode, otherCluster.getDataNodes().get(0));
        IQTree childTree = iqFactory.createUnaryIQTree(optionalRenamingNode, mergedDataNode);

        Substitution<ImmutableTerm> topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> otherTopConstructSubstitution = ((ConstructionNode) otherCluster.getIQTree().getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> mergedTopSubstitution = topConstructSubstitution.compose(
                otherTopConstructSubstitution);

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                Sets.union(tree.getVariables(), otherCluster.getIQTree().getVariables()).immutableCopy(),
                mergedTopSubstitution);
        return iqFactory.createUnaryIQTree(
                topConstructionNode,
                childTree).normalizeForOptimization(variableGenerator);
    }

    private Substitution<ImmutableTerm> compressIfElseNullTerms(Substitution<ImmutableTerm> rdfTermsConstructionSubstitution,
                                                                ImmutableSet<Variable> projectedVariables) {
        var rdfFunctionalTerms = rdfTermsConstructionSubstitution.stream()
                .filter(e -> projectedVariables.contains(e.getKey())
                        && e.getValue() instanceof NonGroundFunctionalTerm
                        && ((NonGroundFunctionalTerm) e.getValue()).getFunctionSymbol() instanceof RDFTermFunctionSymbol)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ((NonGroundFunctionalTerm) e.getValue())
                ));

        var rdfIfElseNullTerms = rdfFunctionalTerms.entrySet().stream()
                .filter(e -> e.getValue().getTerm(0) instanceof ImmutableFunctionalTerm
                        && ( (ImmutableFunctionalTerm)e.getValue().getTerm(0)).getFunctionSymbol() instanceof DBIfElseNullFunctionSymbol)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getTerms()
                ));

        var thenLexicalTerms = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ((ImmutableFunctionalTerm)e.getValue()
                                .get(0)).getTerm(1)
                ));

        var thenTermsDatatypes = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ((ImmutableFunctionalTerm)e.getValue()
                                .get(1)).getTerm(1)
                ));

        var equalityConditionsMap = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (ImmutableExpression) ((ImmutableFunctionalTerm)e.getValue()
                                .get(0)).getTerm(0)));

        Substitution<ImmutableTerm> ifElseNullDisjunctionSubstitution = createIfElseNullDisjunctionSubstitution(
                thenLexicalTerms, thenTermsDatatypes, equalityConditionsMap);

        Substitution<ImmutableTerm> notIfElseNullTerms = rdfTermsConstructionSubstitution.stream()
                .filter(e -> !thenLexicalTerms.containsKey(e.getKey()))
                .collect(substitutionFactory.toSubstitution());

        return notIfElseNullTerms.compose(ifElseNullDisjunctionSubstitution);
    }

    private Substitution<ImmutableTerm> createIfElseNullDisjunctionSubstitution(ImmutableMap<Variable, ImmutableTerm> thenLexicalTerms,
                                                                                ImmutableMap<Variable, ImmutableTerm> thenTermsDatatypes,
                                                                                ImmutableMap<Variable, ImmutableExpression> equalityConditionsMap) {
        var thenTerms2sparqlVars = thenLexicalTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMultimap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                )).asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ImmutableSet.copyOf(e.getValue())
                ));

        return thenTerms2sparqlVars.values().stream()
                .map(variables -> {
                    Stream<ImmutableExpression> equalityConditionsStream = variables.stream()
                            .map(equalityConditionsMap::get);
                    ImmutableExpression disjunctionEqualityConditions = termFactory.getDisjunction(equalityConditionsStream)
                            .orElseThrow(() -> new MinorOntopInternalBugException("The disjunction of equality conditions should not be empty"));
                    return variables.stream()
                            .map(v -> {
                                        ImmutableTerm thenTerm = thenLexicalTerms.get(v);
                                        ImmutableTerm datatype = thenTermsDatatypes.get(v);
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

    public DictionaryPatternMappingEntryCluster renameConflictingVariables(VariableGenerator generator) {
        var renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(generator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);

        return new DictionaryPatternMappingEntryCluster(
                renamedTree,
                rdfTemplates.apply(renamingSubstitution),
                constantAttributes,
                (ExtensionalDataNode) dataNode.applyFreshRenaming(renamingSubstitution).getRootNode(),
                generator,
                iqFactory,
                substitutionFactory,
                termFactory);
    }

    private DictionaryPatternMappingEntryCluster updateConstantAttributes(ImmutableMap<Integer, Attribute> constantAttributes) {
        return new DictionaryPatternMappingEntryCluster(
                tree,
                rdfTemplates,
                constantAttributes,
                dataNode,
                variableGenerator,
                iqFactory,
                substitutionFactory,
                termFactory);
    }

    @Override
    protected MappingEntryCluster buildCluster(IQTree compressedTree, RDFFactTemplates compressedTemplates) {
        return new DictionaryPatternMappingEntryCluster(
                compressedTree,
                compressedTemplates,
                constantAttributes,
                (ExtensionalDataNode) compressedTree.getChildren().get(0),
                variableGenerator,
                iqFactory,
                substitutionFactory,
                termFactory);
    }

}
