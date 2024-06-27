package it.unibz.inf.ontop.query.unfolding.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.ObjectStringTemplateFunctionSymbolImpl;
import it.unibz.inf.ontop.model.term.impl.NonGroundFunctionalTermImpl;
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
import it.unibz.inf.ontop.utils.impl.VariableGeneratorImpl;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * See {@link QueryUnfolder.Factory} for creating a new instance.
 */
public class BasicQueryUnfolder extends AbstractIntensionalQueryMerger implements QueryUnfolder {

    private final Mapping mapping;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final AtomFactory atomFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final CoreUtilsFactory coreUtilsFactory;
    private final TermFactory termFactory;
    private VariableGenerator variableGenerator;

    /**
     * See {@link QueryUnfolder.Factory#create(Mapping)}
     */
    @AssistedInject
    private BasicQueryUnfolder(@Assisted Mapping mapping, IntermediateQueryFactory iqFactory,
                               SubstitutionFactory substitutionFactory, QueryTransformerFactory transformerFactory,
                               UnionBasedQueryMerger queryMerger, CoreUtilsFactory coreUtilsFactory,
                               AtomFactory atomFactory, TermFactory termFactory) {
        super(iqFactory);
        this.mapping = mapping;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
        this.queryMerger = queryMerger;
        this.coreUtilsFactory = coreUtilsFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
    }

    @Override
    protected IQTree optimize(IQTree tree) {
        FirstPhaseQueryTrasformer firstPhaseTransformer = createFirstPhaseTransformer(tree.getKnownVariables());
        IQTree partialUnfoldedIQ = tree.acceptTransformer(firstPhaseTransformer);
        if (firstPhaseTransformer.existsIntensionalNode()){
            IQTree normalizedPartialUnfoldedIQ = partialUnfoldedIQ.normalizeForOptimization(new VariableGeneratorImpl(partialUnfoldedIQ.getKnownVariables(), termFactory));
            variableGenerator = coreUtilsFactory.createVariableGenerator(normalizedPartialUnfoldedIQ.getKnownVariables());
            var tmp = normalizedPartialUnfoldedIQ.getPossibleVariableDefinitions();
            QueryMergingTransformer secondPhaseTransformer = createSecondPhaseTransformer(tmp, variableGenerator);
            return partialUnfoldedIQ.acceptTransformer(secondPhaseTransformer);
        }
        else{
            return partialUnfoldedIQ;
        }
    }

    private FirstPhaseQueryTrasformer createFirstPhaseTransformer(ImmutableSet<Variable> knownVariables){
        return new FirstPhaseQueryTrasformer(coreUtilsFactory.createVariableGenerator(knownVariables));
    }

    protected SecondPhaseQueryTrasformer createSecondPhaseTransformer(ImmutableSet<Substitution<NonVariableTerm>> variableDefinitions, VariableGenerator variableGenerator) {
        return new SecondPhaseQueryTrasformer(variableDefinitions, variableGenerator);
    }

    protected SecondPhaseQueryTrasformer createSecondPhaseTransformer(ImmutableSet<Substitution<NonVariableTerm>> variableDefinitions, VariableGenerator variableGenerator, Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> subjTemplateListMap) {
        return new SecondPhaseQueryTrasformer(variableDefinitions, variableGenerator, subjTemplateListMap);
    }

    protected class SecondPhaseQueryTrasformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {
        //serve a mappare le variabili dei soggetti in liste di IRITemplate, serve a tenere traccia di come interrogare compatibleDefinitionsFromIRITemplate
        //credo che sarà da cambiare in un immutable map
        private Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> subjTemplateListMap;

        protected SecondPhaseQueryTrasformer(ImmutableSet<Substitution<NonVariableTerm>> variableDefinitions, VariableGenerator variableGenerator) {
            super(variableGenerator, BasicQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            this.subjTemplateListMap = new HashMap<>();
            this.updateSubjTemplateMapping(variableDefinitions);
        }

        protected SecondPhaseQueryTrasformer(ImmutableSet<Substitution<NonVariableTerm>> variableDefinitions, VariableGenerator variableGenerator, Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> existingMap) {
            this(variableDefinitions, variableGenerator);
            copySubjTemplateListMapFrom(existingMap);
        }

        private void copySubjTemplateListMapFrom(Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> existingMap){
            for (Map.Entry<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> entry : existingMap.entrySet()) {
                Variable key = entry.getKey();
                ImmutableSet<ObjectStringTemplateFunctionSymbol> value = entry.getValue();
                if (!this.subjTemplateListMap.containsKey(key) || !this.subjTemplateListMap.get(key).equals(value)) {
                    this.subjTemplateListMap.put(key, value);
                }
            }
        }

        private void updateSubjTemplateMapping(ImmutableSet<Substitution<NonVariableTerm>> variableDefinitions){
            Set<Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>>> setSubjTemplateListMap = new HashSet<>();
            variableDefinitions.stream().forEach(elem -> {
                setSubjTemplateListMap.add(findIRITemplateOfJustOneSubstitution(elem));
            });
            //prima unione di tutte le chiavi e valori
            Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> tmpSubjTemplateListMap = setSubjTemplateListMap.stream()
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(
                                    Map.Entry::getValue,
                                    Collectors.collectingAndThen(
                                            Collectors.toList(),
                                            lists -> lists.stream().reduce(ImmutableSet.<ObjectStringTemplateFunctionSymbol>builder(),
                                                    (builder, set) -> builder.addAll(set),
                                                    (builder1, builder2) -> builder1.addAll(builder2.build())
                                            ).build()
                                    )
                            )
                    ));
            //poi intersezione
            for (Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> tmpMap : setSubjTemplateListMap){
                tmpSubjTemplateListMap.keySet().retainAll(tmpMap.keySet());
            }

            //aggiornamento di quella del transformer
            copySubjTemplateListMapFrom(tmpSubjTemplateListMap);
        }

        private Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> findIRITemplateOfJustOneSubstitution(Substitution<NonVariableTerm> substitution){
            Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> subjTemplateForSubstitution = new HashMap<>();
            ImmutableList<Map.Entry<Variable, NonVariableTerm>> dirtyListSubjTemplate = substitution.stream()
                    .filter(entry -> entry.getValue() instanceof NonGroundFunctionalTerm)
                    .filter(entry -> {
                        NonGroundFunctionalTerm term = (NonGroundFunctionalTerm) entry.getValue();
                        if (term.getFunctionSymbol().getName().equals("RDF")) {
                            NonVariableTerm subTerm = (NonVariableTerm)term.getTerm(0);
                            if (subTerm instanceof NonGroundFunctionalTerm && ((NonGroundFunctionalTerm) subTerm).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol) {
                                ObjectStringTemplateFunctionSymbol iriTemplate = (ObjectStringTemplateFunctionSymbol) ((NonGroundFunctionalTerm) subTerm).getFunctionSymbol();
                                return iriTemplate instanceof IRIStringTemplateFunctionSymbol;
                            } else {
                                return false;
                            }
                        } else {
                            return term.getFunctionSymbol() instanceof IRIStringTemplateFunctionSymbol;
                        }
                    })
                    .collect(ImmutableList.toImmutableList());

            ImmutableList<Pair<Variable, ObjectStringTemplateFunctionSymbol>> cleanedListSubjTemplate = dirtyListSubjTemplate.stream()
                    .map(entry -> {
                        NonGroundFunctionalTerm term = (NonGroundFunctionalTerm) entry.getValue();
                        ObjectStringTemplateFunctionSymbol iriTemplate = null;
                        if (term.getFunctionSymbol().getName().equals("RDF")){
                            ImmutableTerm immutableTerm = term.getTerm(0);
                            iriTemplate = (ObjectStringTemplateFunctionSymbolImpl)((NonGroundFunctionalTermImpl) immutableTerm).getFunctionSymbol();
                        }
                        else{
                            if (term.getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol){
                                iriTemplate = (ObjectStringTemplateFunctionSymbolImpl)term.getFunctionSymbol();
                            }
                        }
                        return Pair.of(entry.getKey(), iriTemplate);
                    })
                    .collect(ImmutableList.toImmutableList());

            Map<Variable, Set<ObjectStringTemplateFunctionSymbol>> resultMap = cleanedListSubjTemplate.stream()
                    .collect(Collectors.groupingBy(
                            Pair::getLeft,
                            Collectors.mapping(Pair::getRight, Collectors.toSet())
                    ));

            resultMap.forEach((key, value) -> subjTemplateForSubstitution.put(key, ImmutableSet.copyOf(value)));
            return subjTemplateForSubstitution;
        }

        public Map<Variable, ImmutableSet<ObjectStringTemplateFunctionSymbol>> getSubjTemplateListMap() {
            return subjTemplateListMap;
        }

        //data una tripla rappresentata da un ?s ?p ?o, viene preso il nome del soggetto e si restituiscono tutte le definizioni che si ottengono interrogando compatibleDefinitionsFromIRITemplate
        //unite da un DISTINCT UNION.
        private Optional<IQ> getCompatibleDefinitionsWithSubject(RDFAtomPredicate rdfAtomPredicate, Variable subj){
            ImmutableSet<ObjectStringTemplateFunctionSymbol> associatedTemplate = subjTemplateListMap.get(subj);
            return getCompatibleDefinitionsFromTemplateSet(rdfAtomPredicate, associatedTemplate);
        }

        private Optional<IQ> getCompatibleDefinitionsFromTemplateSet(RDFAtomPredicate rdfAtomPredicate, ImmutableSet<ObjectStringTemplateFunctionSymbol> templateSet){
            Collection<IQ> IQForest = new ArrayList<>();
            if (templateSet != null) {
                for (ObjectStringTemplateFunctionSymbol template : templateSet) {
                    Optional<IQ> optionalIQ = mapping.getCompatibleDefinitionsFromIRITemplate(rdfAtomPredicate, template);
                    optionalIQ.ifPresent(IQForest::add);
                }
                Optional<IQ> optionalMergedIQ = queryMerger.mergeDefinitions(IQForest);
                return optionalMergedIQ;
            }
            return Optional.empty();
        }

        @Override
        public final IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children){
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(t ->{
                        SecondPhaseQueryTrasformer newTransformer = createSecondPhaseTransformer(t.getPossibleVariableDefinitions(), variableGenerator, this.getSubjTemplateListMap());
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
            SecondPhaseQueryTrasformer newTransformer = createSecondPhaseTransformer(rightChild.getPossibleVariableDefinitions(), variableGenerator, this.getSubjTemplateListMap());
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
                    .flatMap(p -> {
                        mapping.computeCompatibleDefinitions(p);
                        return getDefinition(p, atom.getArguments());
                    });
        }

        private Optional<IQ> getDefinition(RDFAtomPredicate predicate,
                                           ImmutableList<? extends VariableOrGroundTerm> arguments) {
            return predicate.getPropertyIRI(arguments)
                    //dalla tupla si cerca di prendere l'IRI del predicato
                    .map(i -> { //se l'IRI è presente
                        if(i.equals(RDF.TYPE)) return handleClassSPOCase(predicate, arguments);
                        else return mapping.getRDFPropertyDefinition(predicate, i);
                    })
                    //altrimenti restituisci le definizioni delle proprietà se la proprietà è una variabile e non una costante
                    .orElseGet(() -> handleGenericSPOCase(predicate, arguments));
        }

        private Optional<IQ> handleClassSPOCase(RDFAtomPredicate predicate,
                                                ImmutableList<? extends VariableOrGroundTerm> arguments){
            if(isClassSPO(arguments)){
                if (arguments.get(0) instanceof Variable){ //(?s a ?c)
                    Optional<ObjectStringTemplateFunctionSymbol> chosenTemplate = chooseOneTemplateIfPossible(subjTemplateListMap.get((Variable) arguments.get(0)));
                    if (chosenTemplate.isPresent()){
                        Optional<IQ> definitionSubj = mapping.getCompatibleDefinitionsFromIRITemplate(predicate, chosenTemplate.get());
                        return definitionSubj.isPresent() ? definitionSubj : getStarDefinition(predicate);
                    }
                    else{
                        return getStarDefinition(predicate);
                    }
                }
                else if (arguments.get(0) instanceof IRIConstant){ //(iri a ?c)
                    Optional<ObjectStringTemplateFunctionSymbol> chosenTemplate = chooseOneTemplateIfPossible(extractCompatibleTemplateFromIriConst((IRIConstant) arguments.get(0)));
                    if (chosenTemplate.isPresent()){
                        Optional<IQ> iriDefinition = mapping.getCompatibleDefinitionsFromIRITemplate(predicate, chosenTemplate.get());
                        return iriDefinition.isPresent() ? iriDefinition : getRDFClassDefinition(predicate, arguments);
                    }
                    else{
                        return getRDFClassDefinition(predicate, arguments);
                    }
                }
                else{
                    return getRDFClassDefinition(predicate, arguments);
                }
            }
            else{
                return getRDFClassDefinition(predicate, arguments);
            }
        }

        private Optional<IQ> handleGenericSPOCase(RDFAtomPredicate predicate,
                                                  ImmutableList<? extends VariableOrGroundTerm> arguments){
            if(isSubPredObj(arguments)) {
                Variable subj = (Variable) arguments.get(0);
                ImmutableSet<ObjectStringTemplateFunctionSymbol> templateAssociatedWithSubj = subjTemplateListMap.containsKey(subj) ? subjTemplateListMap.get(subj) : ImmutableSet.of();
                Optional<ObjectStringTemplateFunctionSymbol> chosenTemplate = chooseOneTemplateIfPossible(templateAssociatedWithSubj);
                if (chosenTemplate.isPresent()){
                    Optional<IQ> definitionSubj = mapping.getCompatibleDefinitionsFromIRITemplate(predicate, chosenTemplate.get());
                    return definitionSubj.isPresent() ? definitionSubj : getStarDefinition(predicate);
                }
                else{
                    return getStarDefinition(predicate);
                }
            }
            else if(isIriPredObj(arguments)){
                Optional<ObjectStringTemplateFunctionSymbol> chosenTemplate = chooseOneTemplateIfPossible(extractCompatibleTemplateFromIriConst((IRIConstant) arguments.get(0)));
                if (chosenTemplate.isPresent()){
                    Optional<IQ> iriDefinition = mapping.getCompatibleDefinitionsFromIRITemplate(predicate, chosenTemplate.get());
                    return iriDefinition.isPresent() ? iriDefinition : getStarDefinition(predicate);
                }
                else{
                    return getStarDefinition(predicate);
                }
            }
            else if(isSubPredIri(arguments)){
                Variable subj = (Variable) arguments.get(0);
                ImmutableSet<ObjectStringTemplateFunctionSymbol> templateAssociatedWithSubj = subjTemplateListMap.containsKey(subj) ? subjTemplateListMap.get(subj) : ImmutableSet.of();
                Optional<ObjectStringTemplateFunctionSymbol> chosenTemplate = chooseOneTemplateIfPossible(templateAssociatedWithSubj);
                Optional<ObjectStringTemplateFunctionSymbol> firstCompatibleTemplate = chooseOneTemplateIfPossible(extractCompatibleTemplateFromIriConst((IRIConstant) arguments.get(2)));
                if (chosenTemplate.isPresent()){
                    Optional<IQ> definitionSubj = mapping.getCompatibleDefinitionsFromIRITemplate(predicate, chosenTemplate.get());
                    return definitionSubj.isPresent() ? definitionSubj : getStarDefinition(predicate);
                }
                else if (firstCompatibleTemplate.isPresent()){
                    Optional<IQ> iriDefinition = mapping.getCompatibleDefinitionsFromIRITemplate(predicate, firstCompatibleTemplate.get());
                    return iriDefinition.isPresent() ? iriDefinition : getStarDefinition(predicate);
                }
                else{
                    return getStarDefinition(predicate);
                }
            }
            else if(isIriPredIri(arguments)){
                Optional<ObjectStringTemplateFunctionSymbol> firstCompatibleTemplate = chooseOneTemplateIfPossible(extractCompatibleTemplateFromIriConst((IRIConstant) arguments.get(0)));
                Optional<ObjectStringTemplateFunctionSymbol> otherCompatibleTemplate = chooseOneTemplateIfPossible(extractCompatibleTemplateFromIriConst((IRIConstant) arguments.get(2)));
                if (firstCompatibleTemplate.isPresent()){
                    Optional<IQ> iriDefinition = mapping.getCompatibleDefinitionsFromIRITemplate(predicate, firstCompatibleTemplate.get());
                    return iriDefinition.isPresent() ? iriDefinition : getStarDefinition(predicate);
                }
                else if (otherCompatibleTemplate.isPresent()){
                    Optional<IQ> iriDefinition = mapping.getCompatibleDefinitionsFromIRITemplate(predicate, otherCompatibleTemplate.get());
                    return iriDefinition.isPresent() ? iriDefinition : getStarDefinition(predicate);
                }
                else{
                    return getStarDefinition(predicate);
                }
            }
            else {
                return getStarDefinition(predicate);
            }
        }

        private boolean isIriTemplateCompatibleWithConst(ObjectStringTemplateFunctionSymbol iriTemplate, IRIConstant iriConstant){
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

        private ImmutableSet<ObjectStringTemplateFunctionSymbol> extractCompatibleTemplateFromIriConst(IRIConstant iriConstant) {
            ImmutableSet<ObjectStringTemplateFunctionSymbol> iriTemplateSet = mapping.getIriTemplateSet();
            ImmutableSet<ObjectStringTemplateFunctionSymbol> compatibleTemplate = iriTemplateSet.stream()
                    .filter(elem -> isIriTemplateCompatibleWithConst(elem, iriConstant))
                    .collect(ImmutableSet.toImmutableSet());
            return compatibleTemplate;
        }

        private Optional<ObjectStringTemplateFunctionSymbol> chooseOneTemplateIfPossible(ImmutableSet<ObjectStringTemplateFunctionSymbol> compatibleTemplate){
            if (compatibleTemplate.size() <= 0){
                return Optional.empty();
            }
            else if (compatibleTemplate.size() == 1){
                return Optional.of(compatibleTemplate.iterator().next());
            }
            else{
                return Optional.empty();
            }
        }

        private boolean isSubPredObj(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof Variable && arguments.get(1) instanceof Variable && arguments.get(2) instanceof Variable)
                return true;
            else
                return false;
        }

        private boolean isSubPredIri(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof Variable && arguments.get(1) instanceof Variable && arguments.get(2) instanceof IRIConstant)
                return true;
            else
                return false;
        }

        private boolean isIriPredObj(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof IRIConstant && arguments.get(1) instanceof Variable && arguments.get(2) instanceof Variable)
                return true;
            else
                return false;
        }

        private boolean isIriPredIri(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof IRIConstant && arguments.get(1) instanceof Variable && arguments.get(2) instanceof IRIConstant)
                return true;
            else
                return false;
        }

        private boolean isClassSPO(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(1) instanceof Variable && arguments.get(2) instanceof Variable)
                return true;
            else
                return false;
        }

        private Optional<IQ> getRDFClassDefinition(RDFAtomPredicate predicate,
                                                   ImmutableList<? extends VariableOrGroundTerm> arguments) {
            return predicate.getClassIRI(arguments) //data la tupla, restituisce l'IRI del rdftype, es: Optional[http://schema.org/Hotel]
                    .map(i -> mapping.getRDFClassDefinition(predicate, i))
                    .orElseGet(() -> getStarClassDefinition(predicate));
        }

        private Optional<IQ> getStarClassDefinition(RDFAtomPredicate predicate) { //data una tupla restituisce tutte le definizioni
            return queryMerger.mergeDefinitions(mapping.getRDFClasses(predicate).stream()
                    .flatMap(i -> mapping.getRDFClassDefinition(predicate, i).stream())
                    .collect(ImmutableCollectors.toList()));
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate) {
            return queryMerger.mergeDefinitions(mapping.getQueries(predicate));
        }

        @Override
        protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
            return iqFactory.createEmptyNode(dataNode.getVariables());
        }

    }

    //Transformer che effettua l'unfolding di tutto eccetto della spo generica e quella con rdftype class
    protected class FirstPhaseQueryTrasformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {
        private boolean foundGenericSPO; //(?s ?p ?o), (iri ?p ?o), (?s ?p iri), (iri ?p iri)
        private boolean foundClassSPO; //(?s a ?c), (iri a ?c)

        protected FirstPhaseQueryTrasformer(VariableGenerator variableGenerator) {
            super(variableGenerator, BasicQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
        }

        private boolean isGenericSPO(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(1) instanceof Variable){
                foundGenericSPO = true;
                return true;
            }
            else
                return false;
        }

        private boolean isClassSPO(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(1) instanceof IRIConstant && arguments.get(2) instanceof Variable) {
                foundClassSPO = true;
                return true;
            }
            else
                return false;
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
                        if (i.equals(RDF.TYPE)) {
                            if(isClassSPO(arguments)) return Optional.<IQ>empty();
                            else return getRDFClassDefinition(predicate, arguments);
                        }
                        else {
                            return mapping.getRDFPropertyDefinition(predicate, i);
                        }
                    })
                    .orElseGet(() -> {
                        isGenericSPO(arguments);
                        return Optional.<IQ>empty();
                    });
        }

        private Optional<IQ> getRDFClassDefinition(RDFAtomPredicate predicate,
                                                   ImmutableList<? extends VariableOrGroundTerm> arguments) {
            return predicate.getClassIRI(arguments)
                    .map(i -> mapping.getRDFClassDefinition(predicate, i))
                    .orElseGet(() -> getStarClassDefinition(predicate));
        }

        private Optional<IQ> getStarClassDefinition(RDFAtomPredicate predicate) { //data una tupla restituisce tutte le definizioni
            return queryMerger.mergeDefinitions(mapping.getRDFClasses(predicate).stream()
                    .flatMap(i -> mapping.getRDFClassDefinition(predicate, i).stream())
                    .collect(ImmutableCollectors.toList()));
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate) {
            return queryMerger.mergeDefinitions(mapping.getQueries(predicate));
        }

        @Override
        protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
            ImmutableList arguments = dataNode.getProjectionAtom().getArguments();
            if(isGenericSPO(arguments) || isClassSPO(arguments))
                return dataNode.getRootNode();
            else{
                return iqFactory.createEmptyNode(dataNode.getVariables());
            }
        }

        public boolean existsIntensionalNode(){
            if (foundGenericSPO || foundClassSPO)
                return true;
            else
                return false;
        }
    }
}
