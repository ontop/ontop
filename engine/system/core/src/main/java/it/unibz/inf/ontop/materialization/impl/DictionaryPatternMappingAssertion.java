package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.materialization.MappingAssertionInformation;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class DictionaryPatternMappingAssertion implements MappingAssertionInformation {
    private final IQTree tree;
    private final RDFFactTemplates rdfFactTemplates;
    private final ExtensionalDataNode relationDefinitionNode;
    private final VariableGenerator variableGenerator;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    public DictionaryPatternMappingAssertion(IQTree tree,
                                             RDFFactTemplates rdfFactTemplates,
                                             ImmutableMap<Integer, Attribute> constantAttributes,
                                             ExtensionalDataNode relationDefinitionNode,
                                             VariableGenerator variableGenerator,
                                             IntermediateQueryFactory iqFactory,
                                             SubstitutionFactory substitutionFactory,
                                             TermFactory termFactory) {
        this.rdfFactTemplates = rdfFactTemplates;
        this.variableGenerator = variableGenerator;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;

        this.tree = makeEqualityConditionExplicit(tree, constantAttributes, relationDefinitionNode);
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
    public Optional<MappingAssertionInformation> merge(MappingAssertionInformation other) {
        if (other instanceof ComplexMappingAssertionInfo || other instanceof FilterMappingAssertionInfo) {
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

        SimpleMappingAssertionInfo asSimpleMappingInfo = new SimpleMappingAssertionInfo(
                relationDefinitionNode.getRelationDefinition(),
                argumentMap,
                tree,
                rdfFactTemplates,
                variableGenerator,
                iqFactory,
                substitutionFactory);

        if (other instanceof SimpleMappingAssertionInfo) {
            return other.merge(asSimpleMappingInfo);
        } else if (other instanceof DictionaryPatternMappingAssertion) {
            SimpleMappingAssertionInfo otherAsSimpleMappingInfo = new SimpleMappingAssertionInfo(
                    other.getRelationsDefinitions().get(0),
                    (ImmutableMap<Integer, Variable>) ((DictionaryPatternMappingAssertion) other).relationDefinitionNode.getArgumentMap(),
                    other.getIQTree(),
                    other.getRDFFactTemplates(),
                    variableGenerator,
                    iqFactory,
                    substitutionFactory);
            return asSimpleMappingInfo.merge(otherAsSimpleMappingInfo);
        } else {
            // Should not happen
            return Optional.empty();
        }
    }

    @Override
    public RDFFactTemplates restrict(ImmutableSet<IRI> predicates) {
        ImmutableCollection<ImmutableList<Variable>> filteredTemplates = rdfFactTemplates.getTriplesOrQuadsVariables().stream()
                .filter(tripleOrQuad -> {
                    ImmutableTerm predicate = ((ConstructionNode)tree.getRootNode()).getSubstitution().apply(tripleOrQuad.get(1));
                    return predicate instanceof IRI && predicates.contains(predicate);
                })
                .collect(ImmutableCollectors.toList());

        return new RDFFactTemplatesImpl(filteredTemplates);
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
        ImmutableExpression equalityCondition = termFactory.getConjunction(
                IntStream.range(0, constantValues.size())
                        .boxed()
                        .map(i -> termFactory.getStrictEquality(constantVariables.get(i), constantValues.get(i)))
                        .collect(ImmutableCollectors.toList()));
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
}
