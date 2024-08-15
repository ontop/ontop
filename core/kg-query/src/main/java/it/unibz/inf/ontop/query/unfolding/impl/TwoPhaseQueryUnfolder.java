package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.Mapping.RDFAtomIndexPattern;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.spec.mapping.Mapping.RDFAtomIndexPattern.*;


/**
 * See {@link QueryUnfolder.Factory} for creating a new instance.
 */
public class TwoPhaseQueryUnfolder extends AbstractIntensionalQueryMerger implements QueryUnfolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwoPhaseQueryUnfolder.class);

    private final Mapping mapping;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final AtomFactory atomFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final CoreUtilsFactory coreUtilsFactory;
    private final TermFactory termFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final ImmutableSet<ObjectStringTemplateFunctionSymbol> objectTemplates;

    // TODO: replace it by a cache or drop it?
    private final Map<ObjectConstant, Optional<ObjectStringTemplateFunctionSymbol>> constantTemplateMap;

    /**
     * See {@link QueryUnfolder.Factory#create(Mapping)}
     */
    @AssistedInject
    private TwoPhaseQueryUnfolder(@Assisted Mapping mapping, IntermediateQueryFactory iqFactory,
                                  SubstitutionFactory substitutionFactory, QueryTransformerFactory transformerFactory,
                                  UnionBasedQueryMerger queryMerger, CoreUtilsFactory coreUtilsFactory,
                                  AtomFactory atomFactory, TermFactory termFactory, FunctionSymbolFactory functionSymbolFactory) {
        super(iqFactory);
        this.mapping = mapping;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
        this.queryMerger = queryMerger;
        this.coreUtilsFactory = coreUtilsFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.constantTemplateMap = new HashMap<>();
        this.objectTemplates = termFactory.getDBFunctionSymbolFactory().getObjectTemplates();
    }

    @Override
    protected IQTree optimize(IQTree tree) {
        long before = System.currentTimeMillis();
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(tree.getKnownVariables());
        FirstPhaseQueryTransformer firstPhaseTransformer = new FirstPhaseQueryTransformer(variableGenerator);
        // NB: no normalization at that point, because of limitation of getPossibleVariableDefinitions implementation (Problem with join strict equality and condition)
        IQTree partiallyUnfoldedIQ = tree.acceptTransformer(firstPhaseTransformer);
        LOGGER.debug("First phase query unfolding time: {}", System.currentTimeMillis() - before);

        if (!firstPhaseTransformer.areIntensionalNodesRemaining())
            return partiallyUnfoldedIQ;

        return executeSecondPhaseUnfolding(partiallyUnfoldedIQ, variableGenerator);
    }

    @Override
    protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
        throw new MinorOntopInternalBugException("This method should not be called");
    }


    protected IQTree executeSecondPhaseUnfolding(IQTree partiallyUnfoldedIQ, VariableGenerator variableGenerator){
        long before = System.currentTimeMillis();
        QueryMergingTransformer secondPhaseTransformer = new SecondPhaseQueryTransformer(
                partiallyUnfoldedIQ.getPossibleVariableDefinitions(), variableGenerator);
        IQTree unfoldedIQ = partiallyUnfoldedIQ.acceptTransformer(secondPhaseTransformer);
        LOGGER.debug("Second phase query unfolding time: {}", System.currentTimeMillis() - before);
        return unfoldedIQ;
    }

    private boolean isTemplateCompatibleWithConstant(ObjectStringTemplateFunctionSymbol template, ObjectConstant objectConstant,
                                                     VariableGenerator variableGenerator) {
        ImmutableExpression strictEquality = termFactory.getStrictEquality(
                objectConstant,
                termFactory.getRDFFunctionalTerm(
                        termFactory.getImmutableFunctionalTerm(
                                template,
                                IntStream.range(0, template.getArity())
                                        .mapToObj(i -> variableGenerator.generateNewVariable())
                                        .collect(ImmutableCollectors.toList())),
                        termFactory.getRDFTermTypeConstant(objectConstant.getType())));
        return strictEquality.evaluate2VL(termFactory.createDummyVariableNullability(strictEquality))
                .getValue()
                .filter(v -> v.equals(ImmutableExpression.Evaluation.BooleanValue.FALSE))
                .isEmpty();
    }

    private Optional<ObjectStringTemplateFunctionSymbol> selectCompatibleTemplateWithConstant(
            ObjectConstant objectConstant, VariableGenerator variableGenerator) {
        if (!constantTemplateMap.containsKey(objectConstant)) {
            Optional<ObjectStringTemplateFunctionSymbol> selectedTemplate = objectTemplates.stream()
                    .filter(t -> isTemplateCompatibleWithConstant(t, objectConstant, variableGenerator))
                    .findAny();
            constantTemplateMap.put(objectConstant, selectedTemplate);
        }
        return constantTemplateMap.get(objectConstant);
    }

    private Optional<IQ> getDefinitionCompatibleWithConstant(RDFAtomPredicate rdfAtomPredicate, RDFAtomIndexPattern indexPattern,
                                                             ObjectConstant objectConstant,
                                                             VariableGenerator variableGenerator) {
        Optional<ObjectStringTemplateFunctionSymbol> selectedTemplate = selectCompatibleTemplateWithConstant(objectConstant, variableGenerator);

        if (selectedTemplate.isPresent())
            return mapping.getCompatibleDefinitions(rdfAtomPredicate, indexPattern, selectedTemplate.get(), variableGenerator);

        return indexPattern == SUBJECT_OF_ALL_CLASSES
                    ? mapping.getMergedClassDefinitions(rdfAtomPredicate)
                    : mapping.getMergedDefinitions(rdfAtomPredicate);
    }

    /**
     * Second phase transformer
     */
    protected class SecondPhaseQueryTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

        /**
         * For each variable, a disjunction of IRI/Bnode selectors
         */
        private final ImmutableMap<Variable, ImmutableSet<RDFSelector>> constraintMap;
        private final VariableGenerator variableGenerator;

        protected SecondPhaseQueryTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> localVariableDefinitions,
                                              VariableGenerator variableGenerator) {
            super(variableGenerator, TwoPhaseQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            this.variableGenerator = variableGenerator;
            this.constraintMap = convertIntoConstraints(localVariableDefinitions);
        }

        protected SecondPhaseQueryTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> localVariableDefinitions,
                                              VariableGenerator variableGenerator, Map<Variable, ImmutableSet<RDFSelector>> parentConstraints) {
            super(variableGenerator, TwoPhaseQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            this.variableGenerator = variableGenerator;
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
            // Template case
            if ((simplifiedTerm instanceof ImmutableFunctionalTerm)
                    && (((ImmutableFunctionalTerm)simplifiedTerm).getFunctionSymbol() instanceof RDFTermFunctionSymbol)){
                ImmutableTerm lexicalTerm = ((ImmutableFunctionalTerm)simplifiedTerm).getTerm(0);
                if ((lexicalTerm instanceof ImmutableFunctionalTerm)
                        && ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol) {
                    return Optional.of(new RDFSelector((ObjectStringTemplateFunctionSymbol) ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol()));
                }

                ImmutableTerm typeTerm = ((ImmutableFunctionalTerm)simplifiedTerm).getTerm(1);
                if (typeTerm instanceof RDFTermTypeConstant
                        && (((RDFTermTypeConstant) typeTerm).getRDFTermType() instanceof RDFDatatype)) {
                    // Literal
                    return Optional.of(new RDFSelector());
                }
            }
            // Constant case
            else if (simplifiedTerm instanceof ObjectConstant){
                return Optional.of(new RDFSelector((ObjectConstant)simplifiedTerm));
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
            if (selector.getType() != RDFSelectorType.OBJECT_CONSTANT)
                return true;

            // Constants that don't match a template
            return templateSelectors.stream()
                    .noneMatch(t -> isTemplateCompatibleWithConstant(
                            t.getObjectTemplate()
                                    .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to be a template")),
                            selector.getObjectConstant()
                                    .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to be a constant")), variableGenerator));
        }

        /**
         * Filters using the prefix of the template
         */
        private IQTree filteredTreeToPreventInsecureUnion(IQTree currentIQTree, ImmutableTerm var, String prefix) {
            ImmutableFunctionalTerm sparqlSTRSTARTSFunctionWithParameters = termFactory.getImmutableFunctionalTerm(
                    functionSymbolFactory.getSPARQLFunctionSymbol(XPathFunction.STARTS_WITH.getIRIString(), 2).get(),
                    termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getSPARQLFunctionSymbol(SPARQL.STR, 1)
                            .orElseThrow(() -> new MinorOntopInternalBugException("STR function missing")), var),
                    termFactory.getRDFLiteralConstant(
                            prefix,
                            termFactory.getTypeFactory().getXsdStringDatatype()
                    )
            );
            ImmutableExpression filterCondition = termFactory.getRDF2DBBooleanFunctionalTerm(sparqlSTRSTARTSFunctionWithParameters);
            FilterNode filterNode = iqFactory.createFilterNode(filterCondition);
            IQTree iqTreeWithFilter = iqFactory.createUnaryIQTree(filterNode, currentIQTree);
            return iqTreeWithFilter;
        }

        private Optional<IQ> getFilteredMatchingDefinitionFromTemplate(RDFAtomPredicate rdfAtomPredicate,
                                                                       ObjectStringTemplateFunctionSymbol template, RDFAtomIndexPattern indexPattern){
            Optional<IQ> filteredDefinition = mapping.getCompatibleDefinitions(rdfAtomPredicate, indexPattern, template, variableGenerator);
            if (filteredDefinition.isEmpty())
                return Optional.empty();

            IQ definition = filteredDefinition.get();
            Variable var = definition.getProjectionAtom().getArguments().get(indexPattern.getPosition());

            // TODO: only apply filter when there are more than one possible constraint
            String templatePrefix = template.getTemplateComponents().get(0).getComponent();
            return Optional.of(
                    iqFactory.createIQ(definition.getProjectionAtom(),
                    filteredTreeToPreventInsecureUnion(definition.getTree(), var, templatePrefix)));
        }

        private Optional<IQ> getFilteredMatchingDefinitionFromConstant(RDFAtomPredicate rdfAtomPredicate, ObjectConstant objectConstant,
                                                                       RDFAtomIndexPattern indexPattern) {
            return getDefinitionCompatibleWithConstant(rdfAtomPredicate, indexPattern, objectConstant, variableGenerator)
                    .map(d -> {
                        DistinctVariableOnlyDataAtom projectionAtom = d.getProjectionAtom();
                        Variable indexVariable = projectionAtom.getArguments().get(indexPattern.getPosition());

                        ImmutableExpression filterCondition = termFactory.getStrictEquality(indexVariable, objectConstant);
                        IQTree filteredTree = iqFactory.createUnaryIQTree(
                                iqFactory.createFilterNode(filterCondition),
                                d.getTree());
                        return iqFactory.createIQ(projectionAtom, filteredTree.normalizeForOptimization(variableGenerator));
                    });

        }

        private Optional<IQ> getCompatibleDefinition(RDFAtomPredicate rdfAtomPredicate,
                                                     RDFAtomIndexPattern atomIndexPattern, Variable subjOrObj){

            Optional<ImmutableSet<RDFSelector>> optionalConstraints = Optional.ofNullable(constraintMap.get(subjOrObj))
                    .flatMap(selectors -> handleLiterals(selectors, atomIndexPattern));

            if (optionalConstraints.isEmpty())
                return Optional.empty();

            ImmutableSet<RDFSelector> constraints = optionalConstraints.get();

            if (canBeSeparatedByPrefix(constraints)) {
                return queryMerger.mergeDefinitions(
                        getMatchingDefinitionsForSafeConstraints(rdfAtomPredicate, constraints, atomIndexPattern));
            }
            return Optional.empty();
        }

        private Optional<ImmutableSet<RDFSelector>> handleLiterals(ImmutableSet<RDFSelector> selectors,
                                                                   RDFAtomIndexPattern atomIndexPattern) {
            if (!atomIndexPattern.canBeLiteral())
                // Literal constraints can be eliminated
                return Optional.of(selectors.stream()
                        .filter(s -> s.getType() != RDFSelectorType.LITERAL)
                        .collect(ImmutableSet.toImmutableSet()));

            // If a literal constraint is present, cannot use indexes (as they are assuming no literal)
            return selectors.stream().anyMatch(s -> s.getType() == RDFSelectorType.LITERAL)
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
                    .filter(components -> !components.isEmpty())
                    .map(components -> components.get(0))
                    .collect(ImmutableList.toImmutableList());

            return IntStream.range(0, templateFirstComponents.size())
                    .noneMatch(i -> IntStream.range(i+1, templateFirstComponents.size())
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

        private Collection<IQ> getMatchingDefinitionsForSafeConstraints(RDFAtomPredicate rdfAtomPredicate,
                                                                        ImmutableSet<RDFSelector> constraints, RDFAtomIndexPattern indexPattern) {
            return constraints.stream()
                    .flatMap(c -> c.getObjectTemplate()
                            .map(t -> getFilteredMatchingDefinitionFromTemplate(rdfAtomPredicate, t, indexPattern))
                            .orElseGet(() -> getFilteredMatchingDefinitionFromConstant(rdfAtomPredicate,
                                    c.getObjectConstant()
                                            .orElseThrow(() -> new MinorOntopInternalBugException("Should be a constant")),
                                    indexPattern))
                            .stream())
                    .collect(ImmutableCollectors.toList());
        }

        @Override
        public final IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children){
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(child -> transformChildWithNewTransformer(child))
                    .collect(ImmutableCollectors.toList());

            return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createNaryIQTree(rootNode, newChildren);
        }

        @Override
        public final IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild){
            IQTree newLeftChild = leftChild.acceptTransformer(this);
            IQTree newRightChild = transformChildWithNewTransformer(rightChild);
            return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            return transformUnaryTreeUsingLocalDefinitions(tree, rootNode, child);
        }

        public IQTree transformUnaryTreeUsingLocalDefinitions(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            IQTree newChild = transformChildWithNewTransformer(child);
            return newChild.equals(child)
                    ? tree
                    : iqFactory.createUnaryIQTree(rootNode, newChild);
        }

        private IQTree transformChildWithNewTransformer(IQTree child) {
            return child.acceptTransformer(
                    new SecondPhaseQueryTransformer(child.getPossibleVariableDefinitions(), variableGenerator, constraintMap));
        }

        @Override
        public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
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
                    .filter(i -> i.equals(RDF.TYPE))
                    .map(i -> getClassDefinition(predicate, arguments))
                    .orElseGet(() -> getAllDefinitions(predicate, arguments));
        }

        private Optional<IQ> getAllDefinitions(RDFAtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
            VariableOrGroundTerm subject = predicate.getSubject(arguments);
            if (subject instanceof Variable) {
                Optional<IQ> definition = getCompatibleDefinition(predicate, SUBJECT_OF_ALL_DEFINITIONS, (Variable) subject);
                if (definition.isPresent())
                    return definition;
            }

            VariableOrGroundTerm object = predicate.getObject(arguments);
            if (object instanceof Variable) {
                Optional<IQ> definition = getCompatibleDefinition(predicate, OBJECT_OF_ALL_DEFINITIONS, (Variable) object);
                if (definition.isPresent())
                    return definition;
            }
            return getStarDefinition(predicate);
        }

        private Optional<IQ> getClassDefinition(RDFAtomPredicate predicate,
                                                ImmutableList<? extends VariableOrGroundTerm> arguments){
            VariableOrGroundTerm subject = predicate.getSubject(arguments);
            if (subject instanceof Variable) {
                Optional<IQ> definition = getCompatibleDefinition(predicate, SUBJECT_OF_ALL_CLASSES, (Variable) subject);
                if (definition.isPresent())
                    return definition;
            }

            return getStarClassDefinition(predicate);
        }

        private Optional<IQ> getStarClassDefinition(RDFAtomPredicate predicate) {
            return mapping.getMergedClassDefinitions(predicate);
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate) {
            return mapping.getMergedDefinitions(predicate);
        }

        @Override
        protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
            return iqFactory.createEmptyNode(dataNode.getVariables());
        }
    }

    protected class FirstPhaseQueryTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {
        private final VariableGenerator variableGenerator;
        private boolean areIntensionalNodeRemaining; //(?s ?p ?o) or (?s ?p ?o ?g)

        protected FirstPhaseQueryTransformer(VariableGenerator variableGenerator) {
            super(variableGenerator, TwoPhaseQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            this.areIntensionalNodeRemaining = false;
            this.variableGenerator = variableGenerator;
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
                    .map(i -> i.equals(RDF.TYPE)
                            ? getRDFClassDefinition(predicate, arguments)
                            : mapping.getRDFPropertyDefinition(predicate, i))
                    .orElseGet(() -> getStarDefinition(predicate, arguments));
        }

        private Optional<IQ> getRDFClassDefinition(RDFAtomPredicate predicate,
                                                   ImmutableList<? extends VariableOrGroundTerm> arguments) {
            return predicate.getClassIRI(arguments)
                    .map(i -> mapping.getRDFClassDefinition(predicate, i))
                    .orElseGet(() -> getStarClassDefinition(predicate, arguments));
        }

        private Optional<IQ> getStarClassDefinition(RDFAtomPredicate predicate,
                                                    ImmutableList<? extends VariableOrGroundTerm> arguments) {
            VariableOrGroundTerm subject = predicate.getSubject(arguments);
            if (subject instanceof ObjectConstant) {
                Optional<IQ> definition = getDefinitionCompatibleWithConstant(predicate, SUBJECT_OF_ALL_CLASSES, (ObjectConstant) subject,
                        variableGenerator);
                if (definition.isPresent())
                    return definition;
                else
                    return mapping.getMergedClassDefinitions(predicate);
            }

            // Leave it for next phase
            return Optional.empty();
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
            VariableOrGroundTerm subject = predicate.getSubject(arguments);

            if (subject instanceof ObjectConstant) {
                return getDefinitionCompatibleWithConstant(predicate, SUBJECT_OF_ALL_DEFINITIONS, (ObjectConstant) subject,
                        variableGenerator);
            }

            VariableOrGroundTerm object = predicate.getObject(arguments);
            if (object instanceof ObjectConstant) {
                return getDefinitionCompatibleWithConstant(predicate, OBJECT_OF_ALL_DEFINITIONS, (ObjectConstant) object,
                        variableGenerator);
            }

            // Leave it for next phase
            return Optional.empty();
        }

        @Override
        protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
            DataAtom<AtomPredicate> projectionAtom = dataNode.getProjectionAtom();
            AtomPredicate atomPredicate = projectionAtom.getPredicate();

            if ((atomPredicate instanceof RDFAtomPredicate)
                    && ((RDFAtomPredicate) atomPredicate).getPredicateIRI(projectionAtom.getArguments()).isEmpty()) {
                areIntensionalNodeRemaining = true;
                return dataNode;
            }

            return iqFactory.createEmptyNode(dataNode.getVariables());
        }

        public boolean areIntensionalNodesRemaining() {
            return areIntensionalNodeRemaining;
        }
    }

    protected enum RDFSelectorType {
        OBJECT_TEMPLATE,
        OBJECT_CONSTANT,
        LITERAL
    }

    protected class RDFSelector {

        @Nullable
        private final ObjectStringTemplateFunctionSymbol objectTemplate;

        @Nullable
        private final ObjectConstant objectConstant;

        private final RDFSelectorType type;


        protected RDFSelector(@Nonnull ObjectStringTemplateFunctionSymbol objectTemplate) {
            this.objectTemplate = objectTemplate;
            this.objectConstant = null;
            this.type = RDFSelectorType.OBJECT_TEMPLATE;
        }

        protected RDFSelector(ObjectConstant objectConstant) {
            this.objectConstant = objectConstant;
            this.objectTemplate = null;
            this.type = RDFSelectorType.OBJECT_CONSTANT;
        }

        protected RDFSelector() {
            this.objectConstant = null;
            this.objectTemplate = null;
            this.type = RDFSelectorType.LITERAL;
        }

        public boolean isObjectTemplate(){
            return type == RDFSelectorType.OBJECT_TEMPLATE;
        }

        public RDFSelectorType getType() {
            return type;
        }

        public Optional<ObjectStringTemplateFunctionSymbol> getObjectTemplate() {
            return Optional.ofNullable(objectTemplate);
        }

        public Optional<ObjectConstant> getObjectConstant() {
            return Optional.ofNullable(objectConstant);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RDFSelector)) return false;
            RDFSelector that = (RDFSelector) o;
            return Objects.equals(objectTemplate, that.objectTemplate)
                    && Objects.equals(objectConstant, that.objectConstant) && type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(objectConstant, objectTemplate, type);
        }

        @Override
        public String toString() {
            switch (type) {
                case OBJECT_TEMPLATE:
                    return "RDFSelector{" + objectTemplate + "}";
                case OBJECT_CONSTANT:
                    return "RDFSelector{" + objectConstant + "}";
                default:
                    return "RDFSelector{LITERAL}";
            }
        }
    }
}
