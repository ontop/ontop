package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class MappingImpl implements Mapping {

    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyDefinitions;
    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> classDefinitions;
    private final Map<RDFAtomPredicate, Map<ObjectStringTemplateFunctionSymbol, IQ>> compatibleDefinitionsFromSubjSAC;
    private final Map<RDFAtomPredicate, Map<ObjectStringTemplateFunctionSymbol, IQ>> compatibleDefinitionsFromSubjSPO;
    private final Map<RDFAtomPredicate, Map<ObjectStringTemplateFunctionSymbol, IQ>> compatibleDefinitionsFromObjSPO;
    private final TermFactory termFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final IntermediateQueryFactory iqFactory;
    private final Map<RDFAtomPredicate, IQ> allDefinitionMergedMap;
    private final Map<RDFAtomPredicate, IQ> allClassMergedMap;

    public MappingImpl(ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable,
                       ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable,
                       TermFactory termFactory,
                       UnionBasedQueryMerger queryMerger,
                       IntermediateQueryFactory iqFactory) {
        this.propertyDefinitions = propertyTable;
        this.classDefinitions = classTable;
        this.termFactory = termFactory;
        this.queryMerger = queryMerger;
        this.iqFactory = iqFactory;
        this.compatibleDefinitionsFromSubjSPO = Maps.newConcurrentMap();
        this.compatibleDefinitionsFromObjSPO = Maps.newConcurrentMap();
        this.compatibleDefinitionsFromSubjSAC = Maps.newConcurrentMap();
        this.allDefinitionMergedMap = Maps.newHashMap();
        this.allClassMergedMap = Maps.newHashMap();
    }

    @Override
    public Optional<IQ> getRDFPropertyDefinition(RDFAtomPredicate rdfAtomPredicate, IRI propertyIRI) {
        return Optional.ofNullable(propertyDefinitions.get(rdfAtomPredicate, propertyIRI));
    }

    @Override
    public Optional<IQ> getRDFClassDefinition(RDFAtomPredicate rdfAtomPredicate, IRI classIRI) {
        return Optional.ofNullable(classDefinitions.get(rdfAtomPredicate, classIRI));
    }

    @Override
    public ImmutableSet<IRI> getRDFProperties(RDFAtomPredicate rdfAtomPredicate) {
        return Optional.ofNullable(propertyDefinitions.rowMap().get(rdfAtomPredicate))
                .map(m -> ImmutableSet.copyOf(m.keySet()))
                .orElseGet(ImmutableSet::of);
    }

    @Override
    public ImmutableSet<IRI> getRDFClasses(RDFAtomPredicate rdfAtomPredicate) {
        return Optional.ofNullable(classDefinitions.rowMap().get(rdfAtomPredicate))
                .map(m -> ImmutableSet.copyOf(m.keySet()))
                .orElseGet(ImmutableSet::of);
    }

    @Override
    public ImmutableSet<RDFAtomPredicate> getRDFAtomPredicates() {
        return Sets.union(propertyDefinitions.rowKeySet(), classDefinitions.rowKeySet())
                .immutableCopy();
    }

    @Override
    public Optional<IQ> getCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate, RDFAtomIndexPattern indexPattern,
                                                 ObjectStringTemplateFunctionSymbol templateFunctionSymbol,
                                                 VariableGenerator variableGenerator) {
        switch (indexPattern) {
            case SUBJECT_OF_ALL_DEFINITIONS:
                return getMergedDefinitions(rdfAtomPredicate)
                        .map(d -> getCompatibleDefinitions(indexPattern, templateFunctionSymbol, variableGenerator,
                                compatibleDefinitionsFromSubjSPO.computeIfAbsent(
                                        rdfAtomPredicate, p -> Maps.newConcurrentMap()), d));
            case OBJECT_OF_ALL_DEFINITIONS:
                return getMergedDefinitions(rdfAtomPredicate)
                        .map(d -> getCompatibleDefinitions(indexPattern, templateFunctionSymbol, variableGenerator,
                                compatibleDefinitionsFromObjSPO.computeIfAbsent(
                                        rdfAtomPredicate, p -> Maps.newConcurrentMap()), d));
            case SUBJECT_OF_ALL_CLASSES:
                return getMergedClassDefinitions(rdfAtomPredicate)
                        .map(d -> getCompatibleDefinitions(indexPattern, templateFunctionSymbol, variableGenerator,
                                compatibleDefinitionsFromSubjSAC.computeIfAbsent(
                                        rdfAtomPredicate, p -> Maps.newConcurrentMap()), d));
            default:
                throw new MinorOntopInternalBugException("Unsupported RDFAtomIndexPattern: " + indexPattern);
        }
    }

    private IQ getCompatibleDefinitions(RDFAtomIndexPattern indexPattern,
                                        ObjectStringTemplateFunctionSymbol templateFunctionSymbol, VariableGenerator variableGenerator,
                                        Map<ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitions,
                                        IQ mergedDefinition) {
        return compatibleDefinitions.computeIfAbsent(templateFunctionSymbol,
                    t -> pruneMergedDefinitionWithTemplate(variableGenerator, mergedDefinition, indexPattern, t));
    }

    private IQ pruneMergedDefinitionWithTemplate(VariableGenerator variableGenerator, IQ mergedDefinition, RDFAtomIndexPattern indexPattern,
                                                 ObjectStringTemplateFunctionSymbol template) {
        DistinctVariableOnlyDataAtom projectionAtom = mergedDefinition.getProjectionAtom();
        Variable targetedVariable = projectionAtom.getArguments().get(indexPattern.getPosition());

        ImmutableFunctionalTerm lexicalTerm = termFactory.getImmutableFunctionalTerm(
                template,
                IntStream.range(0, template.getArity())
                        .mapToObj(i -> variableGenerator.generateNewVariable())
                        .collect(ImmutableCollectors.toList()));

        ImmutableExpression strictEquality = termFactory.getStrictEquality(
                targetedVariable,
                (template instanceof IRIStringTemplateFunctionSymbol)
                        ? termFactory.getIRIFunctionalTerm(lexicalTerm)
                        : termFactory.getBnodeFunctionalTerm(lexicalTerm));

        IQTree mergedTree = mergedDefinition.getTree();
        DownPropagation dp = DownPropagation.of(Optional.of(strictEquality), mergedTree.getVariables(), variableGenerator, termFactory);
        IQTree prunedTree = dp.propagate(mergedTree);
        return iqFactory.createIQ(projectionAtom, prunedTree)
                .normalizeForOptimization();
    }

    @Override
    public synchronized Optional<IQ> getMergedDefinitions(RDFAtomPredicate rdfAtomPredicate) {
        if (!allDefinitionMergedMap.containsKey(rdfAtomPredicate)) {
            ImmutableCollection<IQ> definitions = Stream.concat(
                        classDefinitions.row(rdfAtomPredicate).values().stream(),
                        propertyDefinitions.row(rdfAtomPredicate).values().stream())
                    .collect(ImmutableCollectors.toList());
            queryMerger.mergeDefinitions(definitions)
                    .ifPresent(d -> allDefinitionMergedMap.put(rdfAtomPredicate, d));
        }
        return Optional.ofNullable(allDefinitionMergedMap.get(rdfAtomPredicate));
    }

    @Override
    public synchronized Optional<IQ> getMergedClassDefinitions(RDFAtomPredicate rdfAtomPredicate) {
        if(!allClassMergedMap.containsKey(rdfAtomPredicate)) {
            ImmutableCollection<IQ> definitions = classDefinitions.row(rdfAtomPredicate).values();
            queryMerger.mergeDefinitions(definitions)
                    .ifPresent(d -> allClassMergedMap.put(rdfAtomPredicate, d));;
        }
        return Optional.ofNullable(allClassMergedMap.get(rdfAtomPredicate));
    }
}