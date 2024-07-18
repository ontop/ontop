package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
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
    private final Table<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitionsFromSubjSAC;
    private final Table<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitionsFromSubjSPO;
    private final Table<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitionsFromObjSPO;
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
        this.compatibleDefinitionsFromSubjSPO = HashBasedTable.create();
        this.compatibleDefinitionsFromObjSPO = HashBasedTable.create();
        this.compatibleDefinitionsFromSubjSAC = HashBasedTable.create();
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
                                                 ObjectStringTemplateFunctionSymbol templateFunctionSymbol, VariableGenerator variableGenerator) {
        switch (indexPattern) {
            case SUBJECT_OF_ALL_DEFINITIONS:
                return getCompatibleDefinitions(rdfAtomPredicate, indexPattern, templateFunctionSymbol, variableGenerator,
                        compatibleDefinitionsFromSubjSPO, getMergedDefinitions(rdfAtomPredicate));
            case OBJECT_OF_ALL_DEFINITIONS:
                return getCompatibleDefinitions(rdfAtomPredicate, indexPattern, templateFunctionSymbol, variableGenerator,
                        compatibleDefinitionsFromObjSPO, getMergedDefinitions(rdfAtomPredicate));
            case SUBJECT_OF_ALL_CLASSES:
                return getCompatibleDefinitions(rdfAtomPredicate, indexPattern, templateFunctionSymbol, variableGenerator,
                        compatibleDefinitionsFromSubjSAC, getMergedClassDefinitions(rdfAtomPredicate));
            default:
                throw new MinorOntopInternalBugException("Unsupported RDFAtomIndexPattern: " + indexPattern);
        }
    }

    private Optional<IQ> getCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate, RDFAtomIndexPattern indexPattern,
                                                  ObjectStringTemplateFunctionSymbol templateFunctionSymbol, VariableGenerator variableGenerator,
                                                  Table<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitions,
                                                  Optional<IQ> mergedDefinition) {
        if (mergedDefinition.isEmpty())
            return Optional.empty();

        // Just an optimization to avoid parallel computations of the same thing
        synchronized (templateFunctionSymbol) {
            Optional<IQ> result = Optional.ofNullable(compatibleDefinitions.get(rdfAtomPredicate, templateFunctionSymbol));
            if (result.isPresent())
                return result;

            IQ definition = pruneMergedDefinitionWithTemplate(variableGenerator, mergedDefinition.get(), indexPattern, templateFunctionSymbol);
            compatibleDefinitions.put(rdfAtomPredicate, templateFunctionSymbol, definition);
            return Optional.of(definition);
        }
    }

    private IQ pruneMergedDefinitionWithTemplate(VariableGenerator variableGenerator, IQ mergedDefinition, RDFAtomIndexPattern indexPattern,
                                                 ObjectStringTemplateFunctionSymbol template) {
        DistinctVariableOnlyDataAtom projectionAtom = mergedDefinition.getProjectionAtom();
        Variable targetedVariable = projectionAtom.getArguments().get(indexPattern.getPosition());

        ImmutableExpression strictEquality = termFactory.getStrictEquality(
                targetedVariable,
                termFactory.getIRIFunctionalTerm(termFactory.getImmutableFunctionalTerm(
                        template,
                        IntStream.range(0, template.getArity())
                                .mapToObj(i -> variableGenerator.generateNewVariable())
                                .collect(ImmutableCollectors.toList()))));

        IQTree prunedIQTree = mergedDefinition.getTree().propagateDownConstraint(strictEquality, variableGenerator);
        return iqFactory.createIQ(projectionAtom, prunedIQTree)
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