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
import it.unibz.inf.ontop.model.term.impl.DBConstantImpl;
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
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // TODO: generalize to object constants (IRI and Bnode)
    // TODO: replace it by a cache or drop it?
    private final Map<IRIConstant, Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>>> iriTemplateSetMap;

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
        this.iriTemplateSetMap = new HashMap<>();
        this.objectTemplates = termFactory.getDBFunctionSymbolFactory().getObjectTemplates();
    }

    @Override
    protected IQTree optimize(IQTree tree) {
        long before = System.currentTimeMillis();
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(tree.getKnownVariables());
        FirstPhaseQueryTransformer firstPhaseTransformer = new FirstPhaseQueryTransformer(variableGenerator);
        IQTree partiallyUnfoldedIQ = tree.acceptTransformer(firstPhaseTransformer)
                        .normalizeForOptimization(variableGenerator);
        LOGGER.debug("First phase query unfolding time: {}", System.currentTimeMillis() - before);

        if (!firstPhaseTransformer.areIntensionalNodesRemaining())
            return partiallyUnfoldedIQ;

        return executeSecondPhaseUnfoldingIfNecessary(partiallyUnfoldedIQ, variableGenerator);
    }

    @Override
    protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
        throw new MinorOntopInternalBugException("This method should not be called");
    }


    public IQTree executeSecondPhaseUnfoldingIfNecessary(IQTree partiallyUnfoldedIQ, VariableGenerator variableGenerator){
        long before = System.currentTimeMillis();
        QueryMergingTransformer secondPhaseTransformer = createSecondPhaseTransformer(
                partiallyUnfoldedIQ.getPossibleVariableDefinitions(), variableGenerator);
        IQTree unfoldedIQ = partiallyUnfoldedIQ.acceptTransformer(secondPhaseTransformer);
        LOGGER.debug("Second phase query unfolding time: {}", System.currentTimeMillis() - before);
        return unfoldedIQ;
    }

    protected SecondPhaseQueryTransformer createSecondPhaseTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions, VariableGenerator variableGenerator) {
        return new SecondPhaseQueryTransformer(variableDefinitions, variableGenerator);
    }

    protected SecondPhaseQueryTransformer createSecondPhaseTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions, VariableGenerator variableGenerator, Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> subjTemplateListMap) {
        return new SecondPhaseQueryTransformer(variableDefinitions, variableGenerator, subjTemplateListMap);
    }

    private boolean isIriTemplateCompatibleWithConst(ObjectStringTemplateFunctionSymbol iriTemplate, IRIConstant iriConstant, VariableGenerator variableGenerator){
        ImmutableExpression strictEquality = termFactory.getStrictEquality(
                termFactory.getConstantIRI(iriConstant.getIRI()),
                termFactory.getIRIFunctionalTerm(termFactory.getImmutableFunctionalTerm(
                        iriTemplate,
                        IntStream.range(0, iriTemplate.getArity())
                                .mapToObj(i -> variableGenerator.generateNewVariable())
                                .collect(ImmutableCollectors.toList()))));
        return strictEquality.evaluate2VL(termFactory.createDummyVariableNullability(strictEquality))
                .getValue()
                .filter(v -> v.equals(ImmutableExpression.Evaluation.BooleanValue.FALSE))
                .isEmpty();
    }

    private Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> extractCompatibleTemplateFromIriConst(IRIConstant iriConstant) {
        Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> optionalCompatibleTemplate;
        if (iriTemplateSetMap.get(iriConstant) == null) {
            optionalCompatibleTemplate = Optional.ofNullable(objectTemplates.stream()
                    .filter(template -> isIriTemplateCompatibleWithConst(template, iriConstant))
                    .collect(ImmutableSet.toImmutableSet()));
            iriTemplateSetMap.put(iriConstant, optionalCompatibleTemplate);
        }
        else{
            optionalCompatibleTemplate = iriTemplateSetMap.get(iriConstant);
        }
        return optionalCompatibleTemplate.isPresent() ? optionalCompatibleTemplate : Optional.empty();
    }

    private Optional<IQ> getCompatibleDefinitionsForConstant(RDFAtomPredicate rdfAtomPredicate, Mapping.RDFAtomIndexPattern RDFAtomIndexPattern,
                                                             ObjectConstant objectConstant,
                                                             VariableGenerator variableGenerator){
        Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> optionalCompatibleTemplate = extractCompatibleTemplateFromIriConst(objectConstant);
        //is == 1 and not >= 1, because you have problem when you two iri template for a iri constant and there is no generic one
        if (optionalCompatibleTemplate.isPresent() && optionalCompatibleTemplate.get().size() == 1){
            Optional<IQ> optDef = mapping.getCompatibleDefinitions(rdfAtomPredicate, RDFAtomIndexPattern, optionalCompatibleTemplate.get().stream().findFirst().get(), variableGenerator);
            if (optDef.isPresent()) {
                IQ def = optDef.get();
                ImmutableTerm var;
                var = def.getProjectionAtom().getArguments().get(RDFAtomIndexPattern.getPosition());
                ImmutableExpression filterCondition = termFactory.getStrictEquality(var, termFactory.getConstantIRI(objectConstant.getIRI()));
                FilterNode filterNode = iqFactory.createFilterNode(filterCondition);
                IQTree iqTreeWithFilter = iqFactory.createUnaryIQTree(filterNode, def.getTree());
                return Optional.of(iqFactory.createIQ(def.getProjectionAtom(), iqTreeWithFilter.normalizeForOptimization(variableGenerator)));
            }
            else{
                return Optional.empty();
            }
        }
        else{
            return Optional.empty();
        }
    }

    protected class SecondPhaseQueryTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {
        private Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> varTemplateSetMap;

        protected SecondPhaseQueryTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions, VariableGenerator variableGenerator) {
            super(variableGenerator, InternshipQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            this.varTemplateSetMap = new HashMap<>();
            this.updateSubjTemplateMapping(variableDefinitions);
        }

        protected SecondPhaseQueryTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions, VariableGenerator variableGenerator, Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> existingSubjTemplateMap) {
            this(variableDefinitions, variableGenerator);
            copyVarTemplateSetMapFrom(existingSubjTemplateMap);
        }

        private void copyVarTemplateSetMapFrom(Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> existingMap) {
            existingMap.forEach((key, value) ->
                    this.varTemplateSetMap.compute(key, (k, v) ->
                            v == null || !v.equals(value) ? value : v
                    )
            );
        }

        //I decided not to put a filter but to remove unnecessary information later with this method, to make it easier to remove template-compatible constant iri if necessary
        private void removeUselessVarAndIRIConstantIfExistsCompatibleTemplate(Map<Variable, Set<IRIOrBNodeTemplateSelector>> subjTemplateSetMap) {
            subjTemplateSetMap.forEach((key, valueSet) -> {
                Set<IRIOrBNodeTemplateSelector> extractedTemplates = valueSet.stream()
                        .filter(IRIOrBNodeTemplateSelector::isIRITemplate)
                        .collect(Collectors.toSet());
                Set<IRIOrBNodeTemplateSelector> extractedIRIConstants = valueSet.stream()
                        .filter(IRIOrBNodeTemplateSelector::isIRIConstant)
                        .collect(Collectors.toSet());
                Set<IRIOrBNodeTemplateSelector> extractedUselessVars = valueSet.stream()
                        .filter(IRIOrBNodeTemplateSelector::isEmpty)
                        .collect(Collectors.toSet());

                extractedIRIConstants.forEach(iriConstant ->
                        extractedTemplates.stream()
                                .filter(template -> isIriTemplateCompatibleWithConst(template.getOptTemplate().orElse(null), iriConstant.getOptIRIConstant().orElse(null)))
                                .findFirst()
                                .ifPresent(template -> valueSet.remove(iriConstant))
                );

                valueSet.removeAll(extractedUselessVars);
            });
        }

        private void updateSubjTemplateMapping(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions) {
            Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> finalMap = getNewSubjTemplateMapping(variableDefinitions);
            copyVarTemplateSetMapFrom(finalMap);
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

        private Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> getNewSubjTemplateMapping(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions) {
            //searching for iri constant, iri template, etc., from each substitution
            Set<Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>>> setVarTemplateSetMap =
                    variableDefinitions.stream()
                            .map(this::findIRITemplateOfJustOneSubstitution)
                            .collect(Collectors.toSet());

            //combining all the keys of the set of maps into a single map
            Map<Variable, Set<IRIOrBNodeTemplateSelector>> mergedVarTemplateSets = mergeAllKeysOfVarTemplateSets(setVarTemplateSetMap);

            //removing keys that do not appear in all substitutions
            setVarTemplateSetMap.forEach(tmpMap -> mergedVarTemplateSets.keySet().retainAll(tmpMap.keySet()));

            removeUselessVarAndIRIConstantIfExistsCompatibleTemplate(mergedVarTemplateSets);

            //converting mutable map to immutable map
            return mergedVarTemplateSets.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> ImmutableSet.copyOf(entry.getValue())
                    ));
        }

        private IRIOrBNodeTemplateSelector extractIRITemplateOrIRIConstantFromOneTerm(ImmutableTerm term){
            IRIOrBNodeTemplateSelector elem;
            if (term instanceof ImmutableFunctionalTerm && ((ImmutableFunctionalTerm)term).getFunctionSymbol().getName().equals("RDF")){
                ImmutableTerm subTerm = ((ImmutableFunctionalTerm)term).getTerm(0);
                ImmutableTerm subTermType = ((ImmutableFunctionalTerm)term).getTerm(1);
                if (subTerm instanceof NonGroundFunctionalTerm && ((NonGroundFunctionalTerm) subTerm).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol) {
                    elem = new IRIOrBNodeTemplateSelector((ObjectStringTemplateFunctionSymbol) ((NonGroundFunctionalTermImpl) subTerm).getFunctionSymbol());
                }
                else if (subTerm instanceof NonGroundFunctionalTerm && subTermType.toString().equals("IRI")){
                    elem = new IRIOrBNodeTemplateSelector(subTerm);
                }
                else if (subTerm instanceof DBConstant && subTermType instanceof RDFTermTypeConstant && ((RDFTermTypeConstant)subTermType).getValue().equals("IRI")){
                    elem = new IRIOrBNodeTemplateSelector(termFactory.getConstantIRI(((DBConstantImpl)subTerm).getValue()));
                }
                else{
                    elem = new IRIOrBNodeTemplateSelector();
                }
            }
            else{
                elem = new IRIOrBNodeTemplateSelector();
            }
            return elem;
        }

        private Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> findIRITemplateOfJustOneSubstitution(Substitution<? extends ImmutableTerm> substitution){
            Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> subjTemplateForSubstitution = substitution.stream()
                    .flatMap(entry -> {
                        ImmutableTerm term = entry.getValue();
                        IRIOrBNodeTemplateSelector elem = extractIRITemplateOrIRIConstantFromOneTerm(term);
                        return Stream.of(Pair.of(entry.getKey(), elem));
                    })
                    .collect(Collectors.groupingBy(
                    Pair::getLeft,
                    Collectors.mapping(Pair::getRight, ImmutableSet.toImmutableSet())
            ));
            return subjTemplateForSubstitution;
        }

        public Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> getVarTemplateSetMap() {
            return varTemplateSetMap;
        }

        private boolean isIQDefinitionSafe(Variable var, IQTree iqTree, ObjectStringTemplateFunctionSymbol template){
            boolean result = false;
            ImmutableSet substitution = iqTree.getPossibleVariableDefinitions();
            Map<Variable, ImmutableSet<IRIOrBNodeTemplateSelector>> iriTemplateMap = getNewSubjTemplateMapping(substitution);
            if (iriTemplateMap.get(var) != null){
                ImmutableSet<IRIOrBNodeTemplateSelector> setOfTemplate = iriTemplateMap.get(var)
                        .stream()
                        .filter(elem -> elem.isIRITemplate() || elem.isIRIConstantFromDB())
                        .collect(ImmutableSet.toImmutableSet());
                boolean templateFromDefinitionAndFromIQAreTheSame =
                        setOfTemplate.stream().findFirst().get().isIRITemplate() &&
                        setOfTemplate.stream().findFirst().get().optIRITemplate.get().equals(template);
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
            return templateSet.stream().filter(elem -> elem.isIRIConstantFromDB()).findFirst().isPresent();
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

        private Optional<IQ> filteredDefFromIRIConst(RDFAtomPredicate rdfAtomPredicate, IRIConstant iriConstant, Mapping.RDFAtomIndexPattern RDFAtomIndexPattern){
            Optional<IQ> evaluatedIQ;
            evaluatedIQ = getCompatibleDefinitionsForConstant(rdfAtomPredicate, RDFAtomIndexPattern, iriConstant);
            if (evaluatedIQ.isEmpty()) {
                if (RDFAtomIndexPattern == SUBJECT_OF_ALL_CLASSES)
                    evaluatedIQ = mapping.getMergedClassDefinitions(rdfAtomPredicate);
                else
                    evaluatedIQ = mapping.getMergedDefinitions(rdfAtomPredicate);
                Variable var = evaluatedIQ.get().getProjectionAtom().getArguments().get(RDFAtomIndexPattern.getPosition());
                ImmutableExpression filterCondition = termFactory.getStrictEquality(var, termFactory.getConstantIRI(iriConstant.getIRI()));
                FilterNode filterNode = iqFactory.createFilterNode(filterCondition);
                IQTree iqTreeWithFilter = iqFactory.createUnaryIQTree(filterNode, evaluatedIQ.get().getTree());
                evaluatedIQ = Optional.of(iqFactory.createIQ(evaluatedIQ.get().getProjectionAtom(), iqTreeWithFilter.normalizeForOptimization(variableGenerator)));
            }
            return evaluatedIQ;
        }

        private Collection<IQ> fromTemplateSetReturnForestOfCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate,
                                                                                  ImmutableSet<IRIOrBNodeTemplateSelector> templateSet, Mapping.RDFAtomIndexPattern RDFAtomIndexPattern){
            Collection<IQ> IQForest = templateSet.stream()
                    .filter(elem -> (elem.isIRITemplate() || elem.isIRIConstant()))
                    .map(elem -> {
                        if (elem.isIRITemplate())
                            return filteredDefFromIRITemplate(rdfAtomPredicate, elem.getOptTemplate().get(), RDFAtomIndexPattern);
                        else
                            return filteredDefFromIRIConst(rdfAtomPredicate, elem.getOptIRIConstant().get(), RDFAtomIndexPattern);
                    })
                    .filter(elem -> elem.isPresent())
                    .map(elem -> elem.get())
                    .collect(Collectors.toList());
            return IQForest;
        }

        private Optional<IQ> getUnionOfCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate,
                                                             Mapping.RDFAtomIndexPattern RDFAtomIndexPattern, Variable subjOrObj){
            Optional<ImmutableSet<IRIOrBNodeTemplateSelector>> optionalTemplateSet;
            optionalTemplateSet = Optional.ofNullable(varTemplateSetMap.get(subjOrObj));
            if (optionalTemplateSet.isPresent()) {
                ImmutableSet<IRIOrBNodeTemplateSelector> templateSet = optionalTemplateSet.get();
                if (someDefComeFromDB(templateSet))
                    return Optional.empty();
                return queryMerger.mergeDefinitions(fromTemplateSetReturnForestOfCompatibleDefinitions(rdfAtomPredicate, templateSet, RDFAtomIndexPattern));
            }
            else
                return Optional.empty();
        }

        @Override
        public final IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children){
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(t ->{
                        SecondPhaseQueryTransformer newTransformer = createSecondPhaseTransformer(t.getPossibleVariableDefinitions(), variableGenerator, this.getVarTemplateSetMap());
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
            SecondPhaseQueryTransformer newTransformer = createSecondPhaseTransformer(rightChild.getPossibleVariableDefinitions(), variableGenerator, this.getVarTemplateSetMap());
            IQTree newRightChild = rightChild.acceptTransformer(newTransformer);
            return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            this.updateSubjTemplateMapping(child.getPossibleVariableDefinitions());
            return transformUnaryNode(tree, rootNode, child);
        }

        @Override
        public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
            this.updateSubjTemplateMapping(child.getPossibleVariableDefinitions());
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
                    .map(i -> {
                        if(i.equals(RDF.TYPE)) return getClassDefinition(predicate, arguments);
                        else return mapping.getRDFPropertyDefinition(predicate, i);
                    })
                    .orElseGet(() -> handleGenericSPOCase(predicate, arguments));
        }

        private Optional<IQ> getClassDefinition(RDFAtomPredicate predicate,
                                                ImmutableList<? extends VariableOrGroundTerm> arguments){
            return predicate.getClassIRI(arguments)
                    .map(i -> mapping.getRDFClassDefinition(predicate, i))
                    .orElseGet(() -> handleGenericSACCase(predicate, arguments));
        }

        private Optional<IQ> handleGenericSACCase(RDFAtomPredicate predicate,
                                                  ImmutableList<? extends VariableOrGroundTerm> arguments){
            if(arguments.get(0) instanceof Variable && arguments.get(1) instanceof IRIConstant && arguments.get(2) instanceof Variable){
                Variable var = (Variable) arguments.get(0);
                Optional<IQ> definitionVarSubj = getUnionOfCompatibleDefinitions(predicate, SUBJECT_OF_ALL_CLASSES, var);
                return definitionVarSubj.isPresent() ? definitionVarSubj : getStarClassDefinition(predicate);
            }
            else{
                return getStarClassDefinition(predicate);
            }
        }

        private Optional<IQ> handleGenericSPOCase(RDFAtomPredicate predicate,
                                                  ImmutableList<? extends VariableOrGroundTerm> arguments){
            if(isVarVarVar(arguments)) {
                Variable var = (Variable) arguments.get(0);
                Optional<IQ> definitionVarSubj = getUnionOfCompatibleDefinitions(predicate, SUBJECT_OF_ALL_DEFINITIONS, var);
                if (definitionVarSubj.isPresent()){
                    return definitionVarSubj;
                }
                else{
                    Optional<IQ> definitionVarObj = getUnionOfCompatibleDefinitions(predicate, OBJECT_OF_ALL_DEFINITIONS, var);
                    return definitionVarObj.isPresent() ? definitionVarObj : getStarDefinition(predicate);
                }
            }
            else {
                return getStarDefinition(predicate);
            }
        }

        private boolean isVarVarVar(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof Variable && arguments.get(1) instanceof Variable && arguments.get(2) instanceof Variable)
                return true;
            else
                return false;
        }

        private Optional<IQ> getStarClassDefinition(RDFAtomPredicate predicate) {
            return queryMerger.mergeDefinitions(mapping.getRDFClasses(predicate).stream()
                    .flatMap(i -> mapping.getRDFClassDefinition(predicate, i).stream())
                    .collect(ImmutableCollectors.toList()));
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
                return getCompatibleDefinitionsForConstant(predicate, SUBJECT_OF_ALL_CLASSES, (ObjectConstant) subject,
                        variableGenerator);
            }

            // Leave it for next phase
            return Optional.empty();
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
            VariableOrGroundTerm subject = predicate.getSubject(arguments);

            if (subject instanceof ObjectConstant) {
                Optional<IQ> definition = getCompatibleDefinitionsForConstant(predicate, SUBJECT_OF_ALL_DEFINITIONS, (ObjectConstant) subject,
                        variableGenerator);
                if (definition.isPresent())
                    return definition;
            }

            VariableOrGroundTerm object = predicate.getObject(arguments);
            if (object instanceof ObjectConstant) {
                return getCompatibleDefinitionsForConstant(predicate, OBJECT_OF_ALL_DEFINITIONS, (ObjectConstant) object,
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
        private Optional<ObjectStringTemplateFunctionSymbol> optIRITemplate;
        private Optional<ImmutableTerm> optIRI;

        public IRIOrBNodeTemplateSelector(ObjectStringTemplateFunctionSymbol template) {
            this.optIRITemplate = Optional.ofNullable(template);
            this.optIRI = Optional.empty();
        }

        public IRIOrBNodeTemplateSelector(ImmutableTerm iri) {
            this.optIRI = Optional.ofNullable(iri);
            this.optIRITemplate = Optional.empty();
        }

        public IRIOrBNodeTemplateSelector() {
            this.optIRITemplate = Optional.empty();
            this.optIRI = Optional.empty();
        }

        public boolean isIRITemplate(){
            return optIRITemplate.isPresent();
        }

        public boolean isIRIConstant(){
            if (optIRI.isPresent()){
                return optIRI.get() instanceof IRIConstant ? true : false;
            }
            else{
                return false;
            }
        }

        public boolean isIRIConstantFromDB(){
            if (optIRI.isPresent()){
                return optIRI.get() instanceof IRIConstant ? false : true;
            }
            else{
                return false;
            }
        }

        public boolean isEmpty(){
            return !isIRITemplate() && !isIRIConstant() && !isIRIConstantFromDB();
        }

        public Optional<ObjectStringTemplateFunctionSymbol> getOptTemplate() {
            return optIRITemplate;
        }

        public Optional<IRIConstant> getOptIRIConstant() {
            return Optional.of((IRIConstant) (optIRI.get()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IRIOrBNodeTemplateSelector that = (IRIOrBNodeTemplateSelector) o;
            if (this.isIRIConstant() && that.isIRIConstant()){
                return this.getOptIRIConstant().get().getIRI().getIRIString() == that.getOptIRIConstant().get().getIRI().getIRIString();
            }
            else if (this.isIRITemplate() && that.isIRITemplate()){
                return this.getOptTemplate().get().getTemplate() == that.getOptTemplate().get().getTemplate();
            }
            else {
                return true;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(optIRI, optIRITemplate);
        }

        @Override
        public String toString() {
            if (isIRITemplate()){
                return "IRIorBNodeSelector{" + optIRITemplate.get() + "}";
            }
            else if (isIRIConstant() || isIRIConstantFromDB()){
                return "IRIorBNodeSelector{" + optIRI.get() + "}";
            }
            else{
                return "IRIorBNodeSelector{EMPTY}";
            }
        }
    }
}
