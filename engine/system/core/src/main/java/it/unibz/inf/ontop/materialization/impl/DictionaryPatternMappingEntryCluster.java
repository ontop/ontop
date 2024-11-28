package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DictionaryPatternMappingEntryCluster implements MappingEntryCluster {
    private final IQTree tree;
    private final RDFFactTemplates rdfFactTemplates;
    private final ExtensionalDataNode relationDefinitionNode;
    private final ImmutableMap<Integer, Attribute> constantAttributes;
    private final VariableGenerator variableGenerator;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final QueryTransformerFactory queryTransformerFactory;

    public DictionaryPatternMappingEntryCluster(IQTree tree,
                                                RDFFactTemplates rdfFactTemplates,
                                                ImmutableMap<Integer, Attribute> constantAttributes,
                                                ExtensionalDataNode relationDefinitionNode,
                                                VariableGenerator variableGenerator,
                                                IntermediateQueryFactory iqFactory,
                                                SubstitutionFactory substitutionFactory,
                                                TermFactory termFactory,
                                                QueryTransformerFactory queryTransformerFactory) {
        this.rdfFactTemplates = rdfFactTemplates;
        this.constantAttributes = constantAttributes;
        this.variableGenerator = variableGenerator;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.queryTransformerFactory = queryTransformerFactory;

        this.tree = relationDefinitionNode.getArgumentMap().values().stream().anyMatch(t -> t instanceof DBConstant)
                ? makeEqualityConditionExplicit(tree, constantAttributes, relationDefinitionNode)
                : tree;
        this.relationDefinitionNode = findExtensionalNode(this.tree);
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
    public Optional<MappingEntryCluster> merge(MappingEntryCluster other) {
        if (other instanceof ComplexMappingEntryCluster
                || other instanceof FilterMappingEntryCluster
                || other instanceof JoinMappingEntryCluster) {
            return other.merge(this);
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

        variableGenerator.registerAdditionalVariables(other.getIQTree().getKnownVariables());
        if (other instanceof SimpleMappingEntryCluster) {
            SimpleMappingEntryCluster otherSimpleInfo = ((SimpleMappingEntryCluster) other).renameConflictingVariables(variableGenerator);
            ImmutableMap<Integer, Variable> otherArgumentMap = (ImmutableMap<Integer, Variable>) findExtensionalNode(otherSimpleInfo.getIQTree()).getArgumentMap();
            return Optional.of(mergeOnSimpleMappingInfo(otherSimpleInfo, argumentMap, otherArgumentMap));
        }

        DictionaryPatternMappingEntryCluster otherDictionaryInfo = ((DictionaryPatternMappingEntryCluster) other).renameConflictingVariables(variableGenerator);
        ImmutableMap<Integer, Variable> otherArgumentMap = (ImmutableMap<Integer, Variable>) otherDictionaryInfo.relationDefinitionNode.getArgumentMap();
        return Optional.of(mergeOnBothDictionaryMappingInfos(otherDictionaryInfo, argumentMap, otherArgumentMap));

    }

    @Override
    public ImmutableList<RelationDefinition> getRelationsDefinitions() {
        return ImmutableList.of(relationDefinitionNode.getRelationDefinition());
    }

    private IQTree makeEqualityConditionExplicit(IQTree tree, ImmutableMap<Integer, Attribute> constantAttributes, ExtensionalDataNode relationDefinitionNode) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> originalArgumentMap = relationDefinitionNode.getArgumentMap();
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

        ExtensionalDataNode newRelationDefinitionNode = iqFactory.createExtensionalDataNode(relationDefinitionNode.getRelationDefinition(), newArgumentMap);
        ConstructionNode topNode = (ConstructionNode) tree.getRootNode();
        Substitution<ImmutableTerm> topSubstitution = topNode.getSubstitution();
        Substitution<ImmutableTerm> newTopSubstitution = setPossiblyNullRDFTerms(topSubstitution, constantTermsVariables.values().asList(), constantValues);
        ConstructionNode newTopNode = iqFactory.createConstructionNode(topNode.getVariables(), newTopSubstitution);

        return iqFactory.createUnaryIQTree(newTopNode, newRelationDefinitionNode);
    }

    private Substitution<ImmutableTerm> setPossiblyNullRDFTerms(Substitution<ImmutableTerm> substitution,
                                                                ImmutableList<Variable> constantVariables,
                                                                ImmutableList<DBConstant> constantValues) {
        ImmutableExpression equalityCondition;
        if (constantVariables.size() == 1) {
             equalityCondition = termFactory.getStrictEquality(constantVariables.get(0), constantValues.get(0));
        } else {
             equalityCondition = termFactory.getConjunction(
                    IntStream.range(0, constantValues.size())
                            .boxed()
                            .map(i -> termFactory.getStrictEquality(constantVariables.get(i), constantValues.get(i)))
                            .collect(ImmutableCollectors.toList()));
        }
        return substitution.stream()
                .map(e -> Map.entry(
                        e.getKey(),
                        termFactory.getIfElseNull(equalityCondition, e.getValue())
                ))
                .collect(substitutionFactory.toSubstitution());
    }

    private ExtensionalDataNode findExtensionalNode(IQTree tree) {
        if (tree.getChildren().isEmpty()) {
            if (tree.getRootNode() instanceof ExtensionalDataNode) {
                return (ExtensionalDataNode) tree.getRootNode();
            } else {
                throw new MinorOntopInternalBugException("The leaf node of a mapping assertion is expected to be an ExtensionalDataNode");
            }
        } else {
            return tree.getChildren().stream()
                    .map(this::findExtensionalNode)
                    .findAny().orElseThrow( () -> new MinorOntopInternalBugException("The leaf node of a mapping assertion is expected to be an ExtensionalDataNode"));
        }
    }

    private DictionaryPatternMappingEntryCluster mergeOnBothDictionaryMappingInfos(DictionaryPatternMappingEntryCluster otherDictionaryInfo,
                                                                                   ImmutableMap<Integer, Variable> argumentMap,
                                                                                   ImmutableMap<Integer, Variable> otherArgumentMap) {
        ImmutableMap<Integer, Variable> mergedArgumentMap = mergeRelationArguments(argumentMap, otherArgumentMap);

        ExtensionalDataNode relationDefinitionNode = iqFactory.createExtensionalDataNode(
                this.relationDefinitionNode.getRelationDefinition(),
                mergedArgumentMap);

        ConstructionNode optionalRenamingNode = createOptionalRenamingNode(argumentMap, otherArgumentMap);
        IQTree childTree = iqFactory.createUnaryIQTree(optionalRenamingNode, relationDefinitionNode);

        RDFFactTemplates mergedRDFTemplates = rdfFactTemplates.merge(otherDictionaryInfo.getRDFFactTemplates());

        Substitution<ImmutableTerm> topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> otherTopConstructSubstitution = ((ConstructionNode) otherDictionaryInfo.tree.getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> rdfTermsConstructionSubstitution = topConstructSubstitution.compose(otherTopConstructSubstitution);
        ImmutableSet<Variable> termsVariables = ImmutableSet.<Variable>builder()
                .addAll(topConstructSubstitution.getDomain())
                .addAll(otherTopConstructSubstitution.getDomain())
                .build();
        IQTree mappingTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(termsVariables, rdfTermsConstructionSubstitution),
                childTree).normalizeForOptimization(variableGenerator);

        Substitution<ImmutableTerm> simplifiedSubstitution = compressIfElseNullTerms(
                ((ConstructionNode)mappingTree.getRootNode()).getSubstitution(), mergedRDFTemplates.getVariables());

        ConstructionNode simplifiedConstructionNode = iqFactory.createConstructionNode(simplifiedSubstitution.getDomain(), simplifiedSubstitution);
        Map.Entry<IQTree, RDFFactTemplates> treeTemplatePair = compressMappingAssertion(
                iqFactory.createUnaryIQTree(simplifiedConstructionNode, mappingTree.getChildren().get(0)), mergedRDFTemplates);

        ImmutableMap<Integer, Attribute> mergedConstantAttributes = Streams.concat(
                constantAttributes.entrySet().stream(),
                otherDictionaryInfo.constantAttributes.entrySet().stream()
        ).collect(ImmutableCollectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1));

        return new DictionaryPatternMappingEntryCluster(
                treeTemplatePair.getKey(),
                treeTemplatePair.getValue(),
                mergedConstantAttributes,
                relationDefinitionNode,
                variableGenerator,
                iqFactory,
                substitutionFactory,
                termFactory,
                queryTransformerFactory);
    }

    private DictionaryPatternMappingEntryCluster mergeOnSimpleMappingInfo(SimpleMappingEntryCluster otherSimpleInfo,
                                                                          ImmutableMap<Integer, Variable> argumentMap,
                                                                          ImmutableMap<Integer, Variable> otherArgumentMap) {
        ImmutableMap<Integer, Variable> mergedArgumentMap = mergeRelationArguments(argumentMap, otherArgumentMap);

        ExtensionalDataNode relationDefinitionNode = iqFactory.createExtensionalDataNode(
                this.relationDefinitionNode.getRelationDefinition(),
                mergedArgumentMap);

        ConstructionNode optionalRenamingNode = createOptionalRenamingNode(argumentMap, otherArgumentMap);
        IQTree childTree = iqFactory.createUnaryIQTree(optionalRenamingNode, relationDefinitionNode);

        RDFFactTemplates mergedRDFTemplates = rdfFactTemplates.merge(otherSimpleInfo.getRDFFactTemplates());

        Substitution<ImmutableTerm> topConstructSubstitution = ((ConstructionNode) tree.getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> otherTopConstructSubstitution = ((ConstructionNode) otherSimpleInfo.getIQTree().getRootNode()).getSubstitution();
        Substitution<ImmutableTerm> RDFTermsConstructionSubstitution = topConstructSubstitution.compose(otherTopConstructSubstitution);
        ImmutableSet<Variable> termsVariables = ImmutableSet.<Variable>builder()
                .addAll(topConstructSubstitution.getDomain())
                .addAll(otherTopConstructSubstitution.getDomain())
                .build();
        IQTree mappingTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(termsVariables, RDFTermsConstructionSubstitution),
                childTree).normalizeForOptimization(variableGenerator);

        Map.Entry<IQTree, RDFFactTemplates> treeTemplatePair = compressMappingAssertion(mappingTree, mergedRDFTemplates);

        return new DictionaryPatternMappingEntryCluster(
                treeTemplatePair.getKey(),
                treeTemplatePair.getValue(),
                constantAttributes,
                relationDefinitionNode,
                variableGenerator,
                iqFactory,
                substitutionFactory,
                termFactory,
                queryTransformerFactory);
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
        Optional<Substitution<Variable>> mergedSubstitution = substitutionFactory.onVariables().unifierBuilder()
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

    private Substitution<ImmutableTerm> compressIfElseNullTerms(Substitution<ImmutableTerm> substitution,
                                                                ImmutableSet<Variable> projectedVariables) {
        ImmutableMap<Variable, NonGroundFunctionalTerm> rdfFunctionalTerms = substitution.stream()
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
                .map(e -> Map.entry(e.getKey(), e.getValue().getTerms()))
                .collect(ImmutableCollectors.toMap());

        ImmutableMap<Variable, ImmutableTerm> thenLexicalTerms = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ((ImmutableFunctionalTerm)e.getValue().get(0)).getTerm(1)
                ));

        ImmutableMap<Variable, ImmutableTerm> thenTermsDatatypes = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ((ImmutableFunctionalTerm)e.getValue().get(1)).getTerm(1)
                ));

        ImmutableMap<Variable, ImmutableExpression> equalityConditionsMap = rdfIfElseNullTerms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (ImmutableExpression) ((ImmutableFunctionalTerm)e.getValue().get(0)).getTerm(0)));

        Substitution<ImmutableTerm> ifElseNullDisjunctionSubstitution = createIfElseNullDisjunctionSubstitution(
                thenLexicalTerms, thenTermsDatatypes, equalityConditionsMap);

        Substitution<ImmutableTerm> notIfElseNullTerms = substitution.stream()
                .filter(e -> !thenLexicalTerms.containsKey(e.getKey()))
                .collect(substitutionFactory.toSubstitution());

        return notIfElseNullTerms.compose(ifElseNullDisjunctionSubstitution);
    }

    public DictionaryPatternMappingEntryCluster renameConflictingVariables(VariableGenerator generator) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(generator, tree.getKnownVariables());
        IQTree renamedTree = tree.applyFreshRenaming(renamingSubstitution);

        return new DictionaryPatternMappingEntryCluster(
                renamedTree,
                rdfFactTemplates.apply(renamingSubstitution),
                constantAttributes,
                (ExtensionalDataNode) relationDefinitionNode.applyFreshRenaming(renamingSubstitution).getRootNode(),
                generator,
                iqFactory,
                substitutionFactory,
                termFactory,
                queryTransformerFactory);
    }

    private Substitution<ImmutableTerm> createIfElseNullDisjunctionSubstitution(ImmutableMap<Variable, ImmutableTerm> thenLexicalTerms,
                                                                                ImmutableMap<Variable, ImmutableTerm> thenTermsDatatypes,
                                                                                ImmutableMap<Variable, ImmutableExpression> equalityConditionsMap) {
        var thenTerms2sparqlVars = thenLexicalTerms.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, ImmutableCollectors.toList())));

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

}
