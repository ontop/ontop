package it.unibz.inf.ontop.query.unfolding.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.ConstructionNodeImpl;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.ObjectStringTemplateFunctionSymbolImpl;
import it.unibz.inf.ontop.model.term.impl.DBConstantImpl;
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
import it.unibz.inf.ontop.spec.mapping.impl.MappingImpl;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.rdf4j.query.algebra.Var;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.spec.mapping.impl.MappingImpl.IndexType.*;


/**
 * See {@link QueryUnfolder.Factory} for creating a new instance.
 */
public class InternshipQueryUnfolder extends AbstractIntensionalQueryMerger implements QueryUnfolder {

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
    private InternshipQueryUnfolder(@Assisted Mapping mapping, IntermediateQueryFactory iqFactory,
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
        variableGenerator = coreUtilsFactory.createVariableGenerator(tree.getKnownVariables());
        FirstPhaseQueryTrasformer firstPhaseTransformer = createFirstPhaseTransformer(variableGenerator);
        IQTree partialUnfoldedIQ = tree.acceptTransformer(firstPhaseTransformer);
        if (firstPhaseTransformer.existsIntensionalNode()){
            IQTree normalizedPartialUnfoldedIQ = partialUnfoldedIQ.normalizeForOptimization(variableGenerator);
            QueryMergingTransformer secondPhaseTransformer = createSecondPhaseTransformer(normalizedPartialUnfoldedIQ.getPossibleVariableDefinitions(), variableGenerator);
            return partialUnfoldedIQ.acceptTransformer(secondPhaseTransformer);
        }
        else{
            return partialUnfoldedIQ;
        }
    }

    private FirstPhaseQueryTrasformer createFirstPhaseTransformer(VariableGenerator variableGenerator){
        return new FirstPhaseQueryTrasformer(variableGenerator);
    }

    protected SecondPhaseQueryTrasformer createSecondPhaseTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions, VariableGenerator variableGenerator) {
        return new SecondPhaseQueryTrasformer(variableDefinitions, variableGenerator);
    }

    protected SecondPhaseQueryTrasformer createSecondPhaseTransformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions, VariableGenerator variableGenerator, Map<NonFunctionalTerm, ImmutableSet<Object>> subjTemplateListMap) {
        return new SecondPhaseQueryTrasformer(variableDefinitions, variableGenerator, subjTemplateListMap);
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

    private Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> extractCompatibleTemplateFromIriConst(IRIConstant iriConstant) {
        ImmutableSet<ObjectStringTemplateFunctionSymbol> iriTemplateSet = mapping.getIriTemplateSet();
        Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> optionalCompatibleTemplate = Optional.ofNullable(iriTemplateSet.stream()
                .filter(template -> isIriTemplateCompatibleWithConst(template, iriConstant))
                .collect(ImmutableSet.toImmutableSet()));
        return optionalCompatibleTemplate.isPresent() ? optionalCompatibleTemplate : Optional.empty();
    }

    private Optional<IQ> getCompatibleDefinitionsForIRI(MappingImpl.IndexType indexType, RDFAtomPredicate predicate, IRIConstant iriConstant){
        Optional<ImmutableSet<ObjectStringTemplateFunctionSymbol>> optionalCompatibleTemplate = extractCompatibleTemplateFromIriConst(iriConstant);
        if (optionalCompatibleTemplate.isPresent() && optionalCompatibleTemplate.get().size() == 1){
            Optional<IQ> optDef = mapping.getCompatibleDefinitions(variableGenerator, indexType, predicate, optionalCompatibleTemplate.get().stream().findFirst().get());
            if (optDef.isPresent()) {
                IQ def = optDef.get();
                ImmutableTerm var = null;
                if (indexType == SAC_SUBJ_INDEX || indexType == SPO_SUBJ_INDEX) {
                    var = def.getProjectionAtom().getArguments().get(0);
                } else {
                    var = def.getProjectionAtom().getArguments().get(2);
                }
                ImmutableExpression filterCondition = termFactory.getStrictEquality(var, termFactory.getConstantIRI(iriConstant.getIRI()));
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

    protected class SecondPhaseQueryTrasformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {
        //serve a mappare le variabili dei soggetti in liste di IRITemplate, serve a tenere traccia di come interrogare compatibleDefinitionsFromIRITemplate
        //credo che sarà da cambiare in un immutable map
        private Map<NonFunctionalTerm, ImmutableSet<Object>> varTemplateListMap;

        protected SecondPhaseQueryTrasformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions, VariableGenerator variableGenerator) {
            super(variableGenerator, InternshipQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            this.varTemplateListMap = new HashMap<>();
            this.updateSubjTemplateMapping(variableDefinitions);
        }

        protected SecondPhaseQueryTrasformer(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions, VariableGenerator variableGenerator, Map<NonFunctionalTerm, ImmutableSet<Object>> existingSubjTemplateMap) {
            this(variableDefinitions, variableGenerator);
            copySubjTemplateListMapFrom(existingSubjTemplateMap);
        }

        private void copySubjTemplateListMapFrom(Map<NonFunctionalTerm, ImmutableSet<Object>> existingMap){
            for (Map.Entry<NonFunctionalTerm, ImmutableSet<Object>> entry : existingMap.entrySet()) {
                NonFunctionalTerm key = entry.getKey();
                ImmutableSet<Object> value = entry.getValue();
                if (!this.varTemplateListMap.containsKey(key) || !this.varTemplateListMap.get(key).equals(value)) {
                    this.varTemplateListMap.put(key, value);
                }
            }
        }

        private void updateSubjTemplateMapping(ImmutableSet<? extends Substitution<? extends ImmutableTerm>> variableDefinitions){
            Set<Map<Variable, ImmutableSet<Object>>> setSubjTemplateListMap = new HashSet<>();
            variableDefinitions.stream().forEach(elem -> {
                setSubjTemplateListMap.add(findIRITemplateOfJustOneSubstitution(elem));
            });
            //prima unione di tutte le chiavi e valori
            Map<NonFunctionalTerm, ImmutableSet<Object>> tmpSubjTemplateListMap = setSubjTemplateListMap.stream()
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(
                                    Map.Entry::getValue,
                                    Collectors.collectingAndThen(
                                            Collectors.toList(),
                                            lists -> lists.stream().reduce(ImmutableSet.<Object>builder(),
                                                    (builder, set) -> builder.addAll(set),
                                                    (builder1, builder2) -> builder1.addAll(builder2.build())
                                            ).build()
                                    )
                            )
                    ));
            //poi intersezione
            for (Map<Variable, ImmutableSet<Object>> tmpMap : setSubjTemplateListMap){
                tmpSubjTemplateListMap.keySet().retainAll(tmpMap.keySet());
            }

            //aggiornamento di quella del transformer
            copySubjTemplateListMapFrom(tmpSubjTemplateListMap);
        }

        private Map<Variable, ImmutableSet<Object>> findIRITemplateOfJustOneSubstitution(Substitution<? extends ImmutableTerm> substitution){
            Map<Variable, ImmutableSet<Object>> subjTemplateForSubstitution = new HashMap<>();
            var extractedIRITemplateAndIRIConst = substitution.stream()
                    .flatMap(entry -> {
                        ImmutableTerm term = entry.getValue();
                        Object elem = null;
                        if (term instanceof ImmutableFunctionalTerm && ((ImmutableFunctionalTerm)term).getFunctionSymbol().getName().equals("RDF")){
                            ImmutableTerm subTerm = ((ImmutableFunctionalTerm)term).getTerm(0);
                            if (subTerm instanceof NonGroundFunctionalTerm && ((NonGroundFunctionalTerm) subTerm).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol) {
                                elem = ((NonGroundFunctionalTermImpl) subTerm).getFunctionSymbol();
                            }
                            else if (subTerm instanceof DBConstant){
                                elem = termFactory.getConstantIRI(((DBConstantImpl)subTerm).getValue());
                            }
                            else{
                                elem = "NULL".toString();
                            }
                        }
                        else{
                            if (term instanceof ImmutableFunctionalTerm && ((ImmutableFunctionalTerm)term).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol){
                                elem = ((ImmutableFunctionalTerm)term).getFunctionSymbol();
                            }
                            else if (term instanceof DBConstant){
                                elem = termFactory.getConstantIRI(((DBConstantImpl)term).getValue());
                            }
                            else{
                                elem = "NULL".toString();
                            }
                        }
                        return Stream.of(Pair.of(entry.getKey(), elem));
                    })
                    .collect(ImmutableList.toImmutableList());

            Map<Variable, Set<Object>> resultMap = extractedIRITemplateAndIRIConst.stream()
                    .collect(Collectors.groupingBy(
                            Pair::getLeft,
                            Collectors.mapping(Pair::getRight, Collectors.toSet())
                    ));

            resultMap.forEach((key, value) -> {
                    subjTemplateForSubstitution.put(key, ImmutableSet.copyOf(value));
            });
            return subjTemplateForSubstitution;
        }

        public Map<NonFunctionalTerm, ImmutableSet<Object>> getVarTemplateListMap() {
            return varTemplateListMap;
        }

        //data una tripla rappresentata da un ?s ?p ?o, viene preso il nome del soggetto e si restituiscono tutte le definizioni che si ottengono interrogando compatibleDefinitionsFromIRITemplate
        //unite da un DISTINCT UNION.
        private Optional<IQ> getUnionOfCompatibleDefinitions(MappingImpl.IndexType indexType, RDFAtomPredicate rdfAtomPredicate, NonFunctionalTerm subj){
            Optional<ImmutableSet<Object>> optionalTemplateSet;
            int subjOrObjIndex = -1;
            switch (indexType){
                case SPO_SUBJ_INDEX: subjOrObjIndex = 0; break;
                case SPO_OBJ_INDEX: subjOrObjIndex = 2; break;
                case SAC_SUBJ_INDEX: subjOrObjIndex = 0; break;
            }
            optionalTemplateSet = Optional.ofNullable(varTemplateListMap.get(subj));
            Collection<IQ> IQForest = new ArrayList<>();
            if (optionalTemplateSet.isPresent()) {
                ImmutableSet<Object> templateSet = optionalTemplateSet.get();
                for (Object elem : templateSet) {
                    Optional<IQ> optionalSingleIQDef = Optional.empty();
                    if (elem instanceof ObjectStringTemplateFunctionSymbol) {
                        optionalSingleIQDef = mapping.getCompatibleDefinitions(variableGenerator, indexType, rdfAtomPredicate, (ObjectStringTemplateFunctionSymbol) elem);
                    }
                    else if (elem instanceof IRIConstant){ //ho un iri constant, provo ad ottenere le definizioni dall'index se ci riesco filtro quelle altrimenti prendo tutte le definizioni e filtro quelle
                        Optional<IQ> tmpOptionalIQ = getCompatibleDefinitionsForIRI(indexType, rdfAtomPredicate, (IRIConstant) elem);
                        if (tmpOptionalIQ.isEmpty()){
                            ImmutableTerm var = null;
                            if (indexType == SAC_SUBJ_INDEX){
                                tmpOptionalIQ = mapping.getOptIQClassDef(rdfAtomPredicate);
                                var = tmpOptionalIQ.get().getProjectionAtom().getArguments().get(0);
                            }
                            else if (indexType == SPO_SUBJ_INDEX){
                                tmpOptionalIQ = mapping.getOptIQAllDef(rdfAtomPredicate);
                                var = tmpOptionalIQ.get().getProjectionAtom().getArguments().get(0);
                            }
                            else{
                                tmpOptionalIQ = mapping.getOptIQAllDef(rdfAtomPredicate);
                                var = tmpOptionalIQ.get().getProjectionAtom().getArguments().get(2);
                            }

                            ImmutableExpression filterCondition = termFactory.getStrictEquality(var, termFactory.getConstantIRI(((IRIConstant) elem).getIRI()));
                            FilterNode filterNode = iqFactory.createFilterNode(filterCondition);
                            IQTree iqTreeWithFilter = iqFactory.createUnaryIQTree(filterNode, tmpOptionalIQ.get().getTree());
                            optionalSingleIQDef = Optional.of(iqFactory.createIQ(tmpOptionalIQ.get().getProjectionAtom(), iqTreeWithFilter.normalizeForOptimization(variableGenerator)));
                        }
                        else{
                            optionalSingleIQDef = tmpOptionalIQ;
                        }
                    }
                    //TODO il fatto che l'union è safe è momentaneamente sospesa
                    optionalSingleIQDef.ifPresent(IQForest::add);
                }
                Optional<IQ> optionalMergedIQ = queryMerger.mergeDefinitions(IQForest);
                return optionalMergedIQ;
            }
            return Optional.empty();
        }

        private boolean isIQDefinitionSafe(Variable defSubj, IQTree iqTree, ObjectStringTemplateFunctionSymbol template){
            boolean result = false;
            Substitution<ImmutableTerm> substitution = ((ConstructionNodeImpl)iqTree.getRootNode()).getSubstitution();
            Map<Variable, ImmutableSet<Object>> iriTemplateMap = findIRITemplateOfJustOneSubstitution(substitution);
            if (iriTemplateMap != null && iriTemplateMap.get(defSubj) != null){
                for (Object elem : iriTemplateMap.get(defSubj)){
                    template = (ObjectStringTemplateFunctionSymbol) elem;
                    if (template.getTemplate().equals(template.getTemplate())) {
                        result = true;
                    }
                    else{
                        break;
                    }
                }
            }
            return result;
        }

        @Override
        public final IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children){
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(t ->{
                        SecondPhaseQueryTrasformer newTransformer = createSecondPhaseTransformer(t.getPossibleVariableDefinitions(), variableGenerator, this.getVarTemplateListMap());
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
            SecondPhaseQueryTrasformer newTransformer = createSecondPhaseTransformer(rightChild.getPossibleVariableDefinitions(), variableGenerator, this.getVarTemplateListMap());
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
                    //dalla tupla si cerca di prendere l'IRI del predicato
                    .map(i -> { //se l'IRI è presente
                        if(i.equals(RDF.TYPE)) return getClassDefinition(predicate, arguments);
                        else return mapping.getRDFPropertyDefinition(predicate, i);
                    })
                    //altrimenti restituisci le definizioni delle proprietà se la proprietà è una variabile e non una costante
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
                Optional<IQ> definitionVarSubj = getUnionOfCompatibleDefinitions(SAC_SUBJ_INDEX, predicate, var);
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
                Optional<IQ> definitionVarSubj = getUnionOfCompatibleDefinitions(SPO_SUBJ_INDEX, predicate, var);
                if (definitionVarSubj.isPresent()){
                    return definitionVarSubj;
                }
                else{
                    Optional<IQ> definitionVarObj = getUnionOfCompatibleDefinitions(SPO_OBJ_INDEX, predicate, var);
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
        private boolean foundGenericSPO; //(?s ?p ?o)
        private boolean foundClassSPO; //(?s a ?c)

        protected FirstPhaseQueryTrasformer(VariableGenerator variableGenerator) {
            super(variableGenerator, InternshipQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
        }

        private boolean isGenericSPO(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof Variable && arguments.get(1) instanceof Variable && arguments.get(2) instanceof Variable ){
                foundGenericSPO = true;
                return true;
            }
            else
                return false;
        }

        private boolean isClassSPO(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof Variable && arguments.get(1) instanceof IRIConstant && arguments.get(2) instanceof Variable) {
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
                    .map(i -> handleGenericSACCase(i, predicate, arguments))
                    .orElseGet(() -> handleGenericSPOCase(predicate, arguments));
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

        private boolean isIRIVarVar(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof IRIConstant && arguments.get(1) instanceof Variable && arguments.get(2) instanceof Variable)
                return true;
            else
                return false;
        }
        private boolean isVarVarIRI(ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (arguments.get(0) instanceof Variable && arguments.get(1) instanceof Variable && arguments.get(2) instanceof IRIConstant)
                return true;
            else
                return false;
        }

        private Optional<IQ> handleGenericSPOCase(RDFAtomPredicate predicate,
                                                  ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (!isGenericSPO(arguments)){
                if (isIRIVarVar(arguments)){
                    IRIConstant subj = (IRIConstant) arguments.get(0);
                    Optional<IQ> subjDef = getCompatibleDefinitionsForIRI(SPO_SUBJ_INDEX, predicate, subj);
                    return subjDef.isPresent() ? subjDef : getStarDefinition(predicate);
                }
                else if (isVarVarIRI(arguments)){
                    IRIConstant obj = (IRIConstant) arguments.get(2);
                    Optional<IQ> objDef = getCompatibleDefinitionsForIRI(SPO_OBJ_INDEX, predicate, obj);
                    return objDef.isPresent() ? objDef : getStarDefinition(predicate);
                }
                else{
                    IRIConstant subj = (IRIConstant) arguments.get(0);
                    Optional<IQ> subjDef = getCompatibleDefinitionsForIRI(SPO_SUBJ_INDEX, predicate, subj);
                    if (subjDef.isPresent()){
                        return subjDef;
                    }
                    else{
                        IRIConstant obj = (IRIConstant) arguments.get(2);
                        Optional<IQ> objDef = getCompatibleDefinitionsForIRI(SPO_OBJ_INDEX, predicate, obj);
                        return objDef.isPresent() ? objDef : getStarDefinition(predicate);
                    }
                }
            }
            return Optional.<IQ>empty();
        }

        private Optional<IQ> handleGenericSACCase(IRI iri, RDFAtomPredicate predicate,
                                                  ImmutableList<? extends VariableOrGroundTerm> arguments){
            if (iri.equals(RDF.TYPE)) {
                if (isClassSPO(arguments)) //(?s a ?c)
                    return Optional.<IQ>empty();
                else if (arguments.get(0) instanceof IRIConstant && arguments.get(2) instanceof Variable){
                    IRIConstant subj = (IRIConstant) arguments.get(0);
                    Optional<IQ> subjDef = getCompatibleDefinitionsForIRI(SAC_SUBJ_INDEX, predicate, subj);
                    return subjDef.isPresent() ? subjDef : getRDFClassDefinition(predicate, arguments);
                }
                else return getRDFClassDefinition(predicate, arguments);
            }
            else {
                return mapping.getRDFPropertyDefinition(predicate, iri);
            }
        }
    }
}
