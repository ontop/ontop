package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.spec.mapping.Mapping.RDFAtomIndexPattern.*;

/**
 * Second phase transformer
 */
public class SecondPhaseQueryMergingTransformer extends AbstractMultiPhaseQueryMergingTransformer {

    /**
     * For each variable, a disjunction of IRI/Bnode selectors
     */
    private final ImmutableMap<Variable, ImmutableSet<RDFSelector>> constraintMap;
    private final UnionBasedQueryMerger queryMerger;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final CoreSingletons coreSingletons;

    protected SecondPhaseQueryMergingTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> localVariableDefinitions,
                                                 Mapping mapping, VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        super(mapping, variableGenerator, coreSingletons);
        this.functionSymbolFactory = coreSingletons.getFunctionSymbolFactory();
        this.queryMerger = coreSingletons.getUnionBasedQueryMerger();
        this.coreSingletons = coreSingletons;
        this.constraintMap = convertIntoConstraints(localVariableDefinitions);

    }

    protected SecondPhaseQueryMergingTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> localVariableDefinitions,
                                                 Mapping mapping, VariableGenerator variableGenerator,
                                                 Map<Variable, ImmutableSet<RDFSelector>> parentConstraints,
                                                 CoreSingletons coreSingletons) {
        super(mapping, variableGenerator, coreSingletons);
        this.functionSymbolFactory = coreSingletons.getFunctionSymbolFactory();
        this.queryMerger = coreSingletons.getUnionBasedQueryMerger();
        this.coreSingletons = coreSingletons;
        this.constraintMap = mergeConstraints(convertIntoConstraints(localVariableDefinitions), parentConstraints);
    }

    /**
     * Takes constraints from parent if variable not locally constrained
     */
    private ImmutableMap<Variable, ImmutableSet<RDFSelector>> mergeConstraints(
            Map<Variable, ImmutableSet<RDFSelector>> localConstraints,
            Map<Variable, ImmutableSet<RDFSelector>> parentConstraints) {
        Map<Variable, ImmutableSet<RDFSelector>> newConstraints = Maps.newHashMap(parentConstraints);
        newConstraints.putAll(localConstraints);
        return ImmutableMap.copyOf(newConstraints);
    }

    private ImmutableMap<Variable, ImmutableSet<RDFSelector>> convertIntoConstraints(
            ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions) {
        Set<Map<Variable, RDFSelector>> selectorMapFromSubstitutions =
                variableDefinitions.stream()
                        .map(this::extractSelectorsFromSubstitution)
                        .collect(Collectors.toSet());

        // Groups the selectors per variable
        ImmutableMap<Variable, Collection<RDFSelector>> groupedSelectors = selectorMapFromSubstitutions.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        var alwaysConstrainedVariables = selectorMapFromSubstitutions.stream()
                .map(Map::keySet)
                .reduce(Sets::intersection)
                .orElseGet(ImmutableSet::of);

        return groupedSelectors.entrySet().stream()
                .filter(e -> alwaysConstrainedVariables.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> filterRedundantSelectors(e.getValue())));
    }


    private Optional<RDFSelector> extractSelectorFromTerm(ImmutableTerm term) {
        ImmutableTerm simplifiedTerm = term.simplify();
        // RDF functional term case
        if ((simplifiedTerm instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) simplifiedTerm).getFunctionSymbol() instanceof RDFTermFunctionSymbol)) {
            ImmutableTerm lexicalTerm = ((ImmutableFunctionalTerm) simplifiedTerm).getTerm(0);
            if ((lexicalTerm instanceof ImmutableFunctionalTerm)
                    && ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol) {
                return Optional.of(new RDFSelector((ObjectStringTemplateFunctionSymbol) ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol()));
            }

            ImmutableTerm typeTerm = ((ImmutableFunctionalTerm) simplifiedTerm).getTerm(1);
            if (typeTerm instanceof RDFTermTypeConstant
                    && (((RDFTermTypeConstant) typeTerm).getRDFTermType() instanceof RDFDatatype)) {
                // Literal
                return Optional.of(new RDFSelector());
            }
        }
        // IRI/BNode constant case
        else if (simplifiedTerm instanceof ObjectConstant) {
            return Optional.of(new RDFSelector((ObjectConstant) simplifiedTerm));
        }
        // Literal constant case
        else if (simplifiedTerm instanceof RDFLiteralConstant) {
            return Optional.of(new RDFSelector());
        }

        // No constraint extracted (will be treated as unconstrained)
        return Optional.empty();
    }

    private Map<Variable, RDFSelector> extractSelectorsFromSubstitution(Substitution<? extends ImmutableTerm> substitution) {
        return substitution.stream()
                .flatMap(e -> extractSelectorFromTerm(e.getValue())
                        .map(s -> Maps.immutableEntry(e.getKey(), s))
                        .stream())
                .collect(ImmutableCollectors.toMap());
    }

    /**
     * Removes constants that are contained in templates.
     * Never causes the set to be empty (unless already empty)
     */
    private ImmutableSet<RDFSelector> filterRedundantSelectors(Collection<RDFSelector> selectors) {

        ImmutableSet<RDFSelector> templateSelectors = selectors.stream()
                .filter(RDFSelector::isObjectTemplate)
                .collect(ImmutableSet.toImmutableSet());

        return selectors.stream()
                .filter(s -> filterRedundantSelector(s, templateSelectors))
                .collect(ImmutableSet.toImmutableSet());
    }

    private boolean filterRedundantSelector(RDFSelector selector, ImmutableSet<RDFSelector> templateSelectors) {
        if (selector.getType() != RDFSelector.RDFSelectorType.OBJECT_CONSTANT)
            return true;

        // Constants that don't match a template
        return templateSelectors.stream()
                .noneMatch(t -> isTemplateCompatibleWithConstant(
                        t.getObjectTemplate()
                                .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to be a template")),
                        selector.getObjectConstant()
                                .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to be a constant"))));
    }

    @Override
    public final IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                this::transformChildWithNewTransformer);

        return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createNaryIQTree(rootNode, newChildren);
    }

    @Override
    public final IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = transform(leftChild);
        IQTree newRightChild = transformChildWithNewTransformer(rightChild);
        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }

    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryTreeUsingLocalDefinitions(tree, rootNode, child);
    }

    public IQTree transformUnaryTreeUsingLocalDefinitions(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        IQTree newChild = transformChildWithNewTransformer(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    private IQTree transformChildWithNewTransformer(IQTree child) {
        IQTreeVisitingTransformer transformer = new SecondPhaseQueryMergingTransformer(
                child.getPossibleVariableDefinitions(),
                mapping, variableGenerator, constraintMap, coreSingletons);
        return transformer.transform(child);
    }

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
        return transformUnaryTreeUsingLocalDefinitions(tree, rootNode, child);
    }

    @Override
    protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
        DataAtom<AtomPredicate> atom = dataNode.getProjectionAtom();
        return Optional.of(atom)
                .map(DataAtom::getPredicate)
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .flatMap(p -> getDefinition(p, atom.getArguments()));
    }

    private Optional<IQ> getDefinition(RDFAtomPredicate predicate,
                                       ImmutableList<? extends VariableOrGroundTerm> arguments) {
        return predicate.getPropertyIRI(arguments)
                // The other property IRIs should already have been handled by the first phase
                .filter(i -> i.equals(RDF.TYPE))
                .map(i -> getClassDefinition(predicate, arguments))
                .orElseGet(() -> getAllDefinitions(predicate, arguments));
    }

    private Optional<IQ> getAllDefinitions(RDFAtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
        VariableOrGroundTerm subject = predicate.getSubject(arguments);
        if (subject instanceof Variable) {
            Optional<IQ> definition = getCompatibleConstrainedDefinition(predicate, SUBJECT_OF_ALL_DEFINITIONS, (Variable) subject);
            if (definition.isPresent())
                return definition;
        }

        VariableOrGroundTerm object = predicate.getObject(arguments);
        if (object instanceof Variable) {
            Optional<IQ> definition = getCompatibleConstrainedDefinition(predicate, OBJECT_OF_ALL_DEFINITIONS, (Variable) object);
            if (definition.isPresent())
                return definition;
        }
        return mapping.getMergedDefinitions(predicate);
    }

    private Optional<IQ> getClassDefinition(RDFAtomPredicate predicate,
                                            ImmutableList<? extends VariableOrGroundTerm> arguments) {
        VariableOrGroundTerm subject = predicate.getSubject(arguments);
        if (subject instanceof Variable) {
            Optional<IQ> definition = getCompatibleConstrainedDefinition(predicate, SUBJECT_OF_ALL_CLASSES, (Variable) subject);
            if (definition.isPresent())
                return definition;
        }

        return mapping.getMergedClassDefinitions(predicate);
    }

    private Optional<IQ> getCompatibleConstrainedDefinition(RDFAtomPredicate rdfAtomPredicate,
                                                            Mapping.RDFAtomIndexPattern atomIndexPattern, Variable subjOrObj) {

        Optional<ImmutableSet<RDFSelector>> optionalConstraints = Optional.ofNullable(constraintMap.get(subjOrObj))
                .flatMap(selectors -> handleLiteralSelectors(selectors, atomIndexPattern));

        if (optionalConstraints.isEmpty())
            return Optional.empty();

        ImmutableSet<RDFSelector> constraints = optionalConstraints.get();

        if (canBeSeparatedByPrefix(constraints)) {
            return queryMerger.mergeDefinitions(
                    getMatchingDefinitionsSafely(rdfAtomPredicate, constraints, atomIndexPattern));
        }
        return Optional.empty();
    }

    private Optional<ImmutableSet<RDFSelector>> handleLiteralSelectors(ImmutableSet<RDFSelector> selectors,
                                                                       Mapping.RDFAtomIndexPattern atomIndexPattern) {
        if (!atomIndexPattern.canBeLiteral())
            // Literal constraints can be eliminated
            return Optional.of(selectors.stream()
                    .filter(s -> s.getType() != RDFSelector.RDFSelectorType.LITERAL)
                    .collect(ImmutableSet.toImmutableSet()));

        // If a literal constraint is present, cannot use indexes (as they are assuming no literal)
        return selectors.stream().anyMatch(s -> s.getType() == RDFSelector.RDFSelectorType.LITERAL)
                ? Optional.empty()
                : Optional.of(selectors);
    }

    private boolean canBeSeparatedByPrefix(ImmutableSet<RDFSelector> constraints) {
        if (constraints.size() < 2)
            return true;

        // NB: constant selectors are supposed at that stage to be incompatible with the templates
        ImmutableList<Template.Component> templateFirstComponents = constraints.stream()
                .filter(RDFSelector::isObjectTemplate)
                .map(s -> s.getObjectTemplate().orElseThrow().getTemplateComponents())
                .map(components -> components.stream()
                        .findFirst()
                        .orElseThrow(() -> new MinorOntopInternalBugException("A template should have at least one component")))
                .collect(ImmutableList.toImmutableList());

        return IntStream.range(0, templateFirstComponents.size())
                .noneMatch(i -> IntStream.range(i + 1, templateFirstComponents.size())
                        .anyMatch(j -> haveCompatiblePrefixes(templateFirstComponents.get(i), templateFirstComponents.get(j))));
    }

    private boolean haveCompatiblePrefixes(Template.Component firstComponentTemplate1, Template.Component firstComponentTemplate2) {
        if (firstComponentTemplate1.isColumn() || firstComponentTemplate2.isColumn())
            // Shall we do more checks based on lexical values?
            return true;

        String prefix1 = firstComponentTemplate1.getComponent();
        String prefix2 = firstComponentTemplate2.getComponent();

        return prefix1.startsWith(prefix2) || prefix2.startsWith(prefix1);
    }

    private Collection<IQ> getMatchingDefinitionsSafely(RDFAtomPredicate rdfAtomPredicate,
                                                        ImmutableSet<RDFSelector> constraints, Mapping.RDFAtomIndexPattern indexPattern) {
        var matchingDefinitions = constraints.stream()
                .flatMap(c -> c.getObjectTemplate()
                        .map(t -> mapping.getCompatibleDefinitions(rdfAtomPredicate, indexPattern, t, variableGenerator))
                        .orElseGet(() -> getDefinitionCompatibleWithConstant(rdfAtomPredicate, indexPattern, c.getObjectConstant()
                                .orElseThrow(() -> new MinorOntopInternalBugException("Should be a constant"))))
                        .map(iq -> Maps.immutableEntry(c, iq))
                        .stream())
                .collect(ImmutableCollectors.toMap());

        if (matchingDefinitions.size() < 2)
            return matchingDefinitions.values();

        return matchingDefinitions.entrySet().stream()
                .map(e -> e.getKey().getObjectTemplate()
                        .map(t -> filterDefinitionWithPrefix(e.getValue(), t, indexPattern))
                        .orElseGet(() -> filterDefinitionWithConstant(e.getValue(),
                                e.getKey().getObjectConstant()
                                        .orElseThrow(() -> new MinorOntopInternalBugException("Should be a constant")),
                                indexPattern)))
                .collect(ImmutableCollectors.toList());
    }

    private IQ filterDefinitionWithPrefix(IQ definition, ObjectStringTemplateFunctionSymbol template,
                                                    Mapping.RDFAtomIndexPattern indexPattern) {
        Variable var = definition.getProjectionAtom().getArguments().get(indexPattern.getPosition());

        String templatePrefix = template.getTemplateComponents().get(0).getComponent();
        return iqFactory.createIQ(definition.getProjectionAtom(),
                        filteredTreeToPreventInsecureUnion(definition.getTree(), var, templatePrefix));
    }

    /**
     * Filters using the prefix of the template
     */
    private IQTree filteredTreeToPreventInsecureUnion(IQTree currentIQTree, ImmutableTerm var, String prefix) {
        ImmutableFunctionalTerm sparqlSTRSTARTSFunctionWithParameters = termFactory.getImmutableFunctionalTerm(
                functionSymbolFactory.getSPARQLFunctionSymbol(XPathFunction.STARTS_WITH.getIRIString(), 2)
                        .orElseThrow(() -> new MinorOntopInternalBugException("SPARQL STARTS_WITH function missing")),
                termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getBNodeTolerantSPARQLStrFunctionSymbol(), var),
                termFactory.getRDFLiteralConstant(
                        prefix,
                        termFactory.getTypeFactory().getXsdStringDatatype()));

        return iqFactory.createUnaryIQTree(
                iqFactory.createFilterNode(termFactory.getRDF2DBBooleanFunctionalTerm(sparqlSTRSTARTSFunctionWithParameters)),
                currentIQTree);
    }

    private IQ filterDefinitionWithConstant(IQ definition, ObjectConstant objectConstant,
                                            Mapping.RDFAtomIndexPattern indexPattern) {
        DistinctVariableOnlyDataAtom projectionAtom = definition.getProjectionAtom();
        Variable indexVariable = projectionAtom.getArguments().get(indexPattern.getPosition());

        IQTree filteredTree = iqFactory.createUnaryIQTree(
                iqFactory.createFilterNode(termFactory.getStrictEquality(indexVariable, objectConstant)),
                definition.getTree());
        return iqFactory.createIQ(projectionAtom, filteredTree.normalizeForOptimization(variableGenerator));
    }


    @Override
    protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
        return iqFactory.createEmptyNode(dataNode.getVariables());
    }
}
