package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
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
    private final Map<ObjectConstant, Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>>> objectTemplateSetMap;

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
        this.objectTemplateSetMap = new HashMap<>();
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

    private boolean isTemplateCompatibleWithConst(ObjectStringTemplateFunctionSymbol template, ObjectConstant objectConstant, VariableGenerator variableGenerator) {
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

    private Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> extractCompatibleTemplateFromConst(
            ObjectConstant objectConstant, VariableGenerator variableGenerator) {
        Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> selectedTemplate;
        if (!objectTemplateSetMap.containsKey(objectConstant)) {
            selectedTemplate = Optional.ofNullable(objectTemplates.stream()
                    .filter(t -> isTemplateCompatibleWithConst(t, objectConstant, variableGenerator))
                    .collect(ImmutableSet.toImmutableSet()));
            objectTemplateSetMap.put(objectConstant, selectedTemplate);
        }
        else
            selectedTemplate = objectTemplateSetMap.get(objectConstant);
        return selectedTemplate;
    }

    private Optional<IQ> getDefinitionCompatibleWithConstant(RDFAtomPredicate rdfAtomPredicate, Mapping.RDFAtomIndexPattern indexPattern,
                                                             ObjectConstant objectConstant,
                                                             VariableGenerator variableGenerator) {
        Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> optionalCompatibleTemplate = extractCompatibleTemplateFromConst(objectConstant, variableGenerator);
        // TODO: revise that restriction
        if (optionalCompatibleTemplate.isPresent() && optionalCompatibleTemplate.get().size() >= 1) {
            ObjectStringTemplateFunctionSymbol template = optionalCompatibleTemplate.get().iterator().next();
            return mapping.getCompatibleDefinitions(rdfAtomPredicate, indexPattern, template, variableGenerator);
        }
        // TODO: handle the case of multiple templates
        return Optional.empty();
    }

    /**
     * Second phase transformer
     */
    protected class SecondPhaseQueryTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

        private final Map<Variable, ImmutableSet<IRIOrBNodeSelector>> constraints;
        private final VariableGenerator variableGenerator;

        protected SecondPhaseQueryTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> localVariableDefinitions,
                                              VariableGenerator variableGenerator) {
            super(variableGenerator, TwoPhaseQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            this.variableGenerator = variableGenerator;
            this.constraints = convertIntoConstraints(localVariableDefinitions);
        }

        protected SecondPhaseQueryTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> localVariableDefinitions,
                                              VariableGenerator variableGenerator, Map<Variable, ImmutableSet<IRIOrBNodeSelector>> parentConstraints) {
            this(localVariableDefinitions, variableGenerator);
            mergeWithParentContext(parentConstraints);
        }

        /**
         * Priority to the parent constraints
         *
         * TODO: put a better merging logic
         */
        private void mergeWithParentContext(Map<Variable, ImmutableSet<IRIOrBNodeSelector>> parentConstraints) {
            this.constraints.putAll(parentConstraints);
        }

        private void updateWithDefinitions(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions) {
            Map<Variable, ImmutableSet<IRIOrBNodeSelector>> finalMap = convertIntoConstraints(variableDefinitions);
            mergeWithParentContext(finalMap);
        }

        private Map<Variable, ImmutableSet<IRIOrBNodeSelector>> convertIntoConstraints(
                ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions) {
            Set<Map<Variable, Collection<IRIOrBNodeSelector>>> constraintMaps =
                    variableDefinitions.stream()
                            .map(this::extractSelectorsFromSubstitution)
                            .collect(Collectors.toSet());

            // Combining all the constraints per variable
            ImmutableMap<Variable, Collection<IRIOrBNodeSelector>> mergedConstraints = constraintMaps.stream()
                    .flatMap(m -> m.entrySet().stream()
                            .flatMap(e -> e.getValue().stream()
                                    .map(v -> Maps.immutableEntry(e.getKey(), v))))
                    .collect(ImmutableCollectors.toMultimap())
                    .asMap();

            var alwaysDefinedVariables = constraintMaps.stream()
                    .map(Map::keySet)
                    .reduce(Sets::intersection)
                    .orElseGet(ImmutableSet::of);

            return mergedConstraints.entrySet().stream()
                    .filter(e -> alwaysDefinedVariables.contains(e.getKey()))
                    // TODO: can we make it immutable?
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> filterSelectors(e.getValue())));
        }


        private Optional<IRIOrBNodeSelector> extractSelectorFromTerm(ImmutableTerm term) {
            ImmutableTerm simplifiedTerm = term.simplify();
            // Template case
            if ((simplifiedTerm instanceof ImmutableFunctionalTerm)
                    && (((ImmutableFunctionalTerm)simplifiedTerm).getFunctionSymbol() instanceof RDFTermFunctionSymbol)){
                ImmutableTerm lexicalTerm = ((ImmutableFunctionalTerm)simplifiedTerm).getTerm(0);
                if ((lexicalTerm instanceof ImmutableFunctionalTerm)
                        && ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol) {
                    return Optional.of(new IRIOrBNodeSelector((ObjectStringTemplateFunctionSymbol) ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol()));
                }
            }
            // Constant case
            else if (simplifiedTerm instanceof ObjectConstant){
                return Optional.of(new IRIOrBNodeSelector((ObjectConstant)simplifiedTerm));
            }
            // No constraint extracted (will be treated as unconstrained)
            return Optional.empty();
        }

        private Map<Variable, Collection<IRIOrBNodeSelector>> extractSelectorsFromSubstitution(Substitution<? extends ImmutableTerm> substitution) {
            return substitution.stream()
                    .flatMap(e -> extractSelectorFromTerm(e.getValue())
                            .map(s -> Maps.immutableEntry(e.getKey(), s))
                            .stream())
                    .collect(ImmutableCollectors.toMultimap())
                    .asMap();
        }

        /**
         * Removes constants that are contained in templates.
         * Never causes the set to be empty (unless already empty)
         */
        private ImmutableSet<IRIOrBNodeSelector> filterSelectors(Collection<IRIOrBNodeSelector> selectors) {

            ImmutableSet<IRIOrBNodeSelector> templateSelectors = selectors.stream()
                    .filter(IRIOrBNodeSelector::isTemplate)
                    .collect(ImmutableSet.toImmutableSet());

            return selectors.stream()
                    .filter(s -> filterSelector(s, templateSelectors))
                    .collect(ImmutableSet.toImmutableSet());
        }

        private boolean filterSelector(IRIOrBNodeSelector selector, ImmutableSet<IRIOrBNodeSelector> templateSelectors) {
            if (selector.isTemplate())
                return true;

            // Constants that don't match a template
            return templateSelectors.stream()
                    .noneMatch(t -> isTemplateCompatibleWithConst(
                            t.getTemplate()
                                    .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to be a template")),
                            selector.getObjectConstant()
                                    .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to be a constant")), variableGenerator));
        }

        private boolean isTermAConstantThatComesFromTable(ImmutableTerm term) {
            if (term instanceof ImmutableFunctionalTerm && ((ImmutableFunctionalTerm)term).getFunctionSymbol().getName().equals("RDF")) {
                ImmutableTerm lexicalTerm = ((ImmutableFunctionalTerm) term).getTerm(0);
                if (lexicalTerm instanceof NonGroundFunctionalTerm && ((NonGroundFunctionalTerm)lexicalTerm).getTerm(0) instanceof Variable)
                    return true;
            }
            return false;
        }

        private boolean doesSingleSubstitutionContainsConstantFromTable(Variable var, Substitution<? extends ImmutableTerm> substitution){
            return substitution.stream()
                    .map(entry -> {
                        if (entry.getKey().equals(var)) {
                            ImmutableTerm term = entry.getValue();
                            return isTermAConstantThatComesFromTable(term);
                        }
                        return false;
                    })
                    .anyMatch(result -> result);
        }

        private boolean doesConstantComeFromTableForVariable(Variable var, ImmutableSet substitutions){
            return substitutions.stream()
                    .anyMatch(entry -> doesSingleSubstitutionContainsConstantFromTable(var, (Substitution<? extends ImmutableTerm>) entry));
        }

        private boolean isIQDefinitionSafe(Variable var, IQTree iqTree, ObjectStringTemplateFunctionSymbol template){
            ImmutableSet substitution = iqTree.getPossibleVariableDefinitions();
            if (doesConstantComeFromTableForVariable(var, substitution)){
                return false;
            }
            Map<Variable, ImmutableSet<IRIOrBNodeSelector>> iriTemplateMap = convertIntoConstraints(substitution);
            if (iriTemplateMap.get(var) != null){
                ImmutableSet<IRIOrBNodeSelector> setOfTemplate = iriTemplateMap.get(var)
                        .stream()
                        .filter(elem -> elem.isTemplate() || elem.isObjectConstantFromDB())
                        .collect(ImmutableSet.toImmutableSet());
                boolean templateFromDefinitionAndFromIQAreTheSame =
                        setOfTemplate.stream().findFirst().get().isTemplate() &&
                        setOfTemplate.stream().findFirst().get().template.equals(template);
                if (setOfTemplate.size() == 1 && templateFromDefinitionAndFromIQAreTheSame){
                    return true;
                }
            }
            return false;
        }

        private IQTree filteredTreeToPreventInsecureUnion(IQTree currentIQTree, ImmutableTerm var, String prefix){
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

        private Optional<IQ> filteredDefFromIRITemplate(RDFAtomPredicate rdfAtomPredicate, ObjectStringTemplateFunctionSymbol iriTemplate, Mapping.RDFAtomIndexPattern RDFAtomIndexPattern){
            Optional<IQ> evaluatedIQ;
            evaluatedIQ = mapping.getCompatibleDefinitions(rdfAtomPredicate, RDFAtomIndexPattern, iriTemplate, variableGenerator);
            IQ singleIQDef = evaluatedIQ.get();
            Variable var = singleIQDef.getProjectionAtom().getArguments().get(RDFAtomIndexPattern.getPosition());
            if (!isIQDefinitionSafe(var, singleIQDef.getTree(), iriTemplate)) {
                String iriTemplatePrefix = iriTemplate.getTemplateComponents().get(0).toString();
                evaluatedIQ = Optional.ofNullable(iqFactory.createIQ(singleIQDef.getProjectionAtom(),
                        filteredTreeToPreventInsecureUnion(singleIQDef.getTree(), var, iriTemplatePrefix))
                );
            }
            return evaluatedIQ;
        }

        private Optional<IQ> filteredDefFromIRIConst(RDFAtomPredicate rdfAtomPredicate, ObjectConstant objectConstant, Mapping.RDFAtomIndexPattern RDFAtomIndexPattern) {
            Optional<IQ> evaluatedIQ;
            evaluatedIQ = getDefinitionCompatibleWithConstant(rdfAtomPredicate, RDFAtomIndexPattern, objectConstant, variableGenerator);
            if (evaluatedIQ.isEmpty()) {
                if (RDFAtomIndexPattern == SUBJECT_OF_ALL_CLASSES)
                    evaluatedIQ = mapping.getMergedClassDefinitions(rdfAtomPredicate);
                else
                    evaluatedIQ = mapping.getMergedDefinitions(rdfAtomPredicate);
                Variable var = evaluatedIQ.get().getProjectionAtom().getArguments().get(RDFAtomIndexPattern.getPosition());
                ImmutableExpression filterCondition = termFactory.getStrictEquality(var, objectConstant);
                FilterNode filterNode = iqFactory.createFilterNode(filterCondition);
                IQTree iqTreeWithFilter = iqFactory.createUnaryIQTree(filterNode, evaluatedIQ.get().getTree());
                evaluatedIQ = Optional.of(iqFactory.createIQ(evaluatedIQ.get().getProjectionAtom(), iqTreeWithFilter.normalizeForOptimization(variableGenerator)));
            }
            return evaluatedIQ;
        }

        private Collection<IQ> fromTemplateSetReturnForestOfCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate,
                                                                                  ImmutableSet<IRIOrBNodeSelector> templateSet, Mapping.RDFAtomIndexPattern RDFAtomIndexPattern){
            Collection<IQ> IQForest = templateSet.stream()
                    .filter(elem -> (elem.isTemplate() || elem.isConstant()))
                    .map(elem -> {
                        if (elem.isTemplate())
                            return filteredDefFromIRITemplate(rdfAtomPredicate, elem.getTemplate().get(), RDFAtomIndexPattern);
                        else
                            return filteredDefFromIRIConst(rdfAtomPredicate, elem.getObjectConstant().get(), RDFAtomIndexPattern);
                    })
                    .filter(elem -> elem.isPresent())
                    .map(elem -> elem.get())
                    .collect(Collectors.toList());
            return IQForest;
        }

        private Optional<IQ> getUnionOfCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate,
                                                             Mapping.RDFAtomIndexPattern RDFAtomIndexPattern, Variable subjOrObj){
            Optional<ImmutableSet<IRIOrBNodeSelector>> optionalTemplateSet;
            optionalTemplateSet = Optional.ofNullable(constraints.get(subjOrObj));
            if (optionalTemplateSet.isPresent()) {
                ImmutableSet<IRIOrBNodeSelector> templateSet = optionalTemplateSet.get();
                return queryMerger.mergeDefinitions(fromTemplateSetReturnForestOfCompatibleDefinitions(rdfAtomPredicate, templateSet, RDFAtomIndexPattern));
            }
            else
                return Optional.empty();
        }

        @Override
        public final IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children){
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(t ->{
                        SecondPhaseQueryTransformer newTransformer = new SecondPhaseQueryTransformer(t.getPossibleVariableDefinitions(), variableGenerator, constraints);
                        return t.acceptTransformer(newTransformer);
                    })
                    .collect(ImmutableCollectors.toList());

            return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createNaryIQTree(rootNode, newChildren);
        }

        @Override
        public final IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild){
            IQTree newLeftChild = leftChild.acceptTransformer(this);
            SecondPhaseQueryTransformer newTransformer = new SecondPhaseQueryTransformer(rightChild.getPossibleVariableDefinitions(), variableGenerator, constraints);
            IQTree newRightChild = rightChild.acceptTransformer(newTransformer);
            return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            this.updateWithDefinitions(child.getPossibleVariableDefinitions());
            return transformUnaryNode(tree, rootNode, child);
        }

        @Override
        public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
            this.updateWithDefinitions(child.getPossibleVariableDefinitions());
            return transformUnaryNode(tree, rootNode, child);
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
                Optional<IQ> definition = getUnionOfCompatibleDefinitions(predicate, SUBJECT_OF_ALL_DEFINITIONS, (Variable) subject);
                if (definition.isPresent())
                    return definition;
            }

            VariableOrGroundTerm object = predicate.getObject(arguments);
            if (object instanceof Variable) {
                Optional<IQ> definition = getUnionOfCompatibleDefinitions(predicate, OBJECT_OF_ALL_DEFINITIONS, (Variable) object);
                if (definition.isPresent())
                    return definition;
            }
            return getStarDefinition(predicate);
        }

        private Optional<IQ> getClassDefinition(RDFAtomPredicate predicate,
                                                ImmutableList<? extends VariableOrGroundTerm> arguments){
            VariableOrGroundTerm subject = predicate.getSubject(arguments);
            if (subject instanceof Variable) {
                Optional<IQ> definition = getUnionOfCompatibleDefinitions(predicate, SUBJECT_OF_ALL_CLASSES, (Variable) subject);
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

    protected class IRIOrBNodeSelector {

        @Nullable
        private final ObjectStringTemplateFunctionSymbol template;

        // TODO: Can we reduce it to ObjectConstant?
        @Nullable
        private final NonNullConstant constant;

        public IRIOrBNodeSelector(@Nonnull ObjectStringTemplateFunctionSymbol template) {
            this.template = template;
            this.constant = null;
        }

        public IRIOrBNodeSelector(NonNullConstant constant) {
            this.constant = constant;
            this.template = null;
        }

        public boolean isTemplate(){
            return template != null;
        }

        public boolean isConstant(){
            if (constant != null){
                return constant instanceof ObjectConstant;
            }
            else{
                return false;
            }
        }

        public boolean isObjectConstantFromDB(){
            if (constant != null){
                return !(constant instanceof ObjectConstant);
            }
            else{
                return false;
            }
        }

        public Optional<ObjectStringTemplateFunctionSymbol> getTemplate() {
            return Optional.ofNullable(template);
        }

        public Optional<ObjectConstant> getObjectConstant() {

            return Optional.ofNullable(constant)
                    .filter(c -> c instanceof ObjectConstant)
                    .map(c -> (ObjectConstant) c);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IRIOrBNodeSelector that = (IRIOrBNodeSelector) o;
            if (this.isConstant() && that.isConstant()){
                return this.constant.equals(that.constant);
            }
            else if (this.isTemplate() && that.isTemplate()){
                return this.template.equals(that.template);
            }
            else {
                return true;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(constant, template);
        }

        @Override
        public String toString() {
            if (isTemplate()){
                return "IRIorBNodeSelector{" + template + "}";
            }
            else if (isConstant() || isObjectConstantFromDB()){
                return "IRIorBNodeSelector{" + constant + "}";
            }
            else{
                return "IRIorBNodeSelector{EMPTY}";
            }
        }
    }
}
