package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.NonGroundFunctionalTermImpl;
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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.spec.mapping.Mapping.RDFAtomIndexPattern.*;


/**
 * See {@link QueryUnfolder.Factory} for creating a new instance.
 */
public class InternshipQueryUnfolder extends AbstractIntensionalQueryMerger implements QueryUnfolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternshipQueryUnfolder.class);

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
    private InternshipQueryUnfolder(@Assisted Mapping mapping, IntermediateQueryFactory iqFactory,
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
        //is == 1 and not >= 1, because you have problem when you two iri template for a iri constant and there is no generic one
        // TODO: revise that restriction
        if (optionalCompatibleTemplate.isPresent() && optionalCompatibleTemplate.get().size() == 1) {
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

        private final Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> constraints;
        private final VariableGenerator variableGenerator;

        protected SecondPhaseQueryTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> localVariableDefinitions, VariableGenerator variableGenerator) {
            super(variableGenerator, InternshipQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            this.constraints = new HashMap<>();
            this.updateWithDefinitions(localVariableDefinitions);
            this.variableGenerator = variableGenerator;
        }

        protected SecondPhaseQueryTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions,
                                              VariableGenerator variableGenerator,
                                              Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> parentConstraints) {
            this(variableDefinitions, variableGenerator);
            mergeWithParentContext(parentConstraints);
        }

        /**
         * Priority to the parent constraints
         *
         * TODO: put a better merging logic
         */
        private void mergeWithParentContext(Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> parentConstraints) {
            this.constraints.putAll(parentConstraints);
        }

        //I decided not to put a filter but to remove unnecessary information later with this method, to make it easier to remove template-compatible constant iri if necessary
        private void removeUselessVarAndObjectConstantIfExistsCompatibleTemplate(Map<Variable, Set<IRIOrBNodeTemplateSelector>> subjTemplateSetMap) {
            subjTemplateSetMap.forEach((key, valueSet) -> {
                Set<IRIOrBNodeTemplateSelector> extractedTemplateSelectors = valueSet.stream()
                        .filter(IRIOrBNodeTemplateSelector::isTemplate)
                        .collect(Collectors.toSet());
                Set<IRIOrBNodeTemplateSelector> extractedObjectConstants = valueSet.stream()
                        .filter(IRIOrBNodeTemplateSelector::isConstant)
                        .collect(Collectors.toSet());
                Set<IRIOrBNodeTemplateSelector> extractedUselessVars = valueSet.stream()
                        .filter(IRIOrBNodeTemplateSelector::isEmpty)
                        .collect(Collectors.toSet());

                extractedObjectConstants
                        .forEach(objectConstant ->
                                extractedTemplateSelectors.stream()
                                    .filter(selector -> isTemplateCompatibleWithConst(
                                                selector.getTemplate().get(),
                                                objectConstant.getObjectConstant().get(), variableGenerator))
                                    .findFirst()
                                    .ifPresent(template -> valueSet.remove(objectConstant))
                );

                valueSet.removeAll(extractedUselessVars);
            });
        }

        private void updateWithDefinitions(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions) {
            Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> finalMap = convertIntoConstraints(variableDefinitions);
            mergeWithParentContext(finalMap);
        }

        private Map<Variable, Set<IRIOrBNodeTemplateSelector>> mergeAllKeysOfVarTemplateSets(Set<Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>>> setVarTemplateSetMap) {
            return setVarTemplateSetMap.stream()
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(
                                    Map.Entry::getValue,
                                    Collectors.collectingAndThen(
                                            Collectors.toList(),
                                            lists -> lists.stream().flatMap(Set::stream).collect(Collectors.toSet())
                                    )
                            )
                    ));
        }

        private Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> convertIntoConstraints(
                ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions) {
            //searching for iri constant, iri template, etc., from each substitution
            Set<Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>>> setVarTemplateSetMap =
                    variableDefinitions.stream()
                            .map(this::findTemplateOfJustOneSubstitution)
                            .collect(Collectors.toSet());

            //combining all the keys of the set of maps into a single map
            Map<Variable, Set<IRIOrBNodeTemplateSelector>> mergedVarTemplateSets = mergeAllKeysOfVarTemplateSets(setVarTemplateSetMap);

            //removing keys that do not appear in all substitutions
            setVarTemplateSetMap.forEach(tmpMap -> mergedVarTemplateSets.keySet().retainAll(tmpMap.keySet()));

            removeUselessVarAndObjectConstantIfExistsCompatibleTemplate(mergedVarTemplateSets);

            // making the values immutable
            return mergedVarTemplateSets.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> ImmutableSet.copyOf(entry.getValue())
                    ));
        }

        private IRIOrBNodeTemplateSelector extractIRITemplateOrObjectConstantFromOneTerm(ImmutableTerm term){
            if (term instanceof ImmutableFunctionalTerm && ((ImmutableFunctionalTerm)term).getFunctionSymbol().getName().equals("RDF")){
                ImmutableTerm lexicalTerm = ((ImmutableFunctionalTerm)term).getTerm(0);
                if (lexicalTerm instanceof NonGroundFunctionalTerm && ((NonGroundFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol) {
                    return new IRIOrBNodeTemplateSelector((ObjectStringTemplateFunctionSymbol) ((NonGroundFunctionalTermImpl) lexicalTerm).getFunctionSymbol());
                }
                else if (lexicalTerm instanceof ObjectConstant){
                    return new IRIOrBNodeTemplateSelector((ObjectConstant)lexicalTerm);
                }
            }
            return new IRIOrBNodeTemplateSelector();
        }

        private Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> findTemplateOfJustOneSubstitution(Substitution<? extends ImmutableTerm> substitution){
            Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> subjTemplateForSubstitution = substitution.stream()
                    .flatMap(entry -> {
                        ImmutableTerm term = entry.getValue();
                        IRIOrBNodeTemplateSelector elem = extractIRITemplateOrObjectConstantFromOneTerm(term);
                        return Stream.of(Pair.of(entry.getKey(), elem));
                    })
                    .collect(Collectors.groupingBy(
                    Pair::getLeft,
                    Collectors.mapping(Pair::getRight, ImmutableSet.toImmutableSet())
            ));
            return subjTemplateForSubstitution;
        }

        private boolean isIQDefinitionSafe(Variable var, IQTree iqTree, ObjectStringTemplateFunctionSymbol template){
            boolean result = false;
            ImmutableSet substitution = iqTree.getPossibleVariableDefinitions();
            Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> iriTemplateMap = convertIntoConstraints(substitution);
            if (iriTemplateMap.get(var) != null){
                ImmutableSet<IRIOrBNodeTemplateSelector> setOfTemplate = iriTemplateMap.get(var)
                        .stream()
                        .filter(elem -> elem.isTemplate() || elem.isObjectConstantFromDB())
                        .collect(ImmutableSet.toImmutableSet());
                boolean templateFromDefinitionAndFromIQAreTheSame =
                        setOfTemplate.stream().findFirst().get().isTemplate() &&
                        setOfTemplate.stream().findFirst().get().template.equals(template);
                if (setOfTemplate.size() == 1 && templateFromDefinitionAndFromIQAreTheSame){
                    result = true;
                }
            }
            return result;
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

        private boolean someDefComeFromDB(ImmutableSet<IRIOrBNodeTemplateSelector> templateSet){
            return templateSet.stream().filter(elem -> elem.isObjectConstantFromDB()).findFirst().isPresent();
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
                                                                                  ImmutableSet<IRIOrBNodeTemplateSelector> templateSet, Mapping.RDFAtomIndexPattern RDFAtomIndexPattern){
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
            Optional<ImmutableSet<IRIOrBNodeTemplateSelector>> optionalTemplateSet;
            optionalTemplateSet = Optional.ofNullable(constraints.get(subjOrObj));
            if (optionalTemplateSet.isPresent()) {
                ImmutableSet<IRIOrBNodeTemplateSelector> templateSet = optionalTemplateSet.get();
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
            super(variableGenerator, InternshipQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
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
                return getDefinitionCompatibleWithConstant(predicate, SUBJECT_OF_ALL_CLASSES, (ObjectConstant) subject,
                        variableGenerator);
            }

            // Leave it for next phase
            return Optional.empty();
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
            VariableOrGroundTerm subject = predicate.getSubject(arguments);

            if (subject instanceof ObjectConstant) {
                Optional<IQ> definition = getDefinitionCompatibleWithConstant(predicate, SUBJECT_OF_ALL_DEFINITIONS, (ObjectConstant) subject,
                        variableGenerator);
                if (definition.isPresent())
                    return definition;
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

    protected class IRIOrBNodeTemplateSelector {

        @Nullable
        private final ObjectStringTemplateFunctionSymbol template;

        // TODO: Can we reduce it to ObjectConstant?
        @Nullable
        private final NonNullConstant constant;

        public IRIOrBNodeTemplateSelector(@Nonnull ObjectStringTemplateFunctionSymbol template) {
            this.template = template;
            this.constant = null;
        }

        public IRIOrBNodeTemplateSelector(NonNullConstant constant) {
            this.constant = constant;
            this.template = null;
        }

        public IRIOrBNodeTemplateSelector() {
            this.template = null;
            this.constant = null;
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

        public boolean isEmpty(){
            return !isTemplate() && !isConstant() && !isObjectConstantFromDB();
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
            IRIOrBNodeTemplateSelector that = (IRIOrBNodeTemplateSelector) o;
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
