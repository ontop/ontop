package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import it.unibz.inf.ontop.utils.impl.VariableGeneratorImpl;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.stream.Stream;


public class MappingImpl implements Mapping {

    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyDefinitions;
    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> classDefinitions;
    private final ImmutableSet<ObjectStringTemplateFunctionSymbol> iriTemplateSet;
    private ImmutableTable<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitionsFromIRITemplate;
    private boolean isCompatibleDefinitionComputed;
    private final TermFactory termFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final IntermediateQueryFactory iqFactory;

    public MappingImpl(ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable,
                       ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable,
                       ImmutableSet<ObjectStringTemplateFunctionSymbol> iriTemplateSet,
                       TermFactory termFactory,
                       UnionBasedQueryMerger queryMerger,
                       IntermediateQueryFactory iqFactory) {
        this.propertyDefinitions = propertyTable;
        this.classDefinitions = classTable;
        this.iriTemplateSet = iriTemplateSet;
        this.termFactory = termFactory;
        this.queryMerger = queryMerger;
        this.iqFactory = iqFactory;
        this.compatibleDefinitionsFromIRITemplate = ImmutableTable.of();
        this.isCompatibleDefinitionComputed = false;
    }

    @Override
    public void computeCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate){
        if (!isCompatibleDefinitionComputed) { //serve a computarlo solo la prima volta
            ImmutableTable.Builder<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> builder = ImmutableTable.builder();
            ImmutableCollection<IQ> allDef = getQueries(rdfAtomPredicate);
            Optional<IQ> optionalMergedIQ = queryMerger.mergeDefinitions(allDef);

            if (optionalMergedIQ.isPresent()) {
                IQ mergedIQ = optionalMergedIQ.get();
                iriTemplateSet.stream()
                        .forEach(iriTemplate -> {
                            FilterNode filterNode = iqFactory.createFilterNode(termFactory.getStrictEquality(termFactory.getDBBooleanConstant(true), termFactory.getDBBooleanConstant(true)));
                            IQTree mergedIQWithFilter = iqFactory.createUnaryIQTree(filterNode, mergedIQ.getTree());
                            IQTree optimizedMergedIQWithFilter = mergedIQWithFilter.normalizeForOptimization(new VariableGeneratorImpl(mergedIQWithFilter.getKnownVariables(), termFactory));
                            IQ newIQ = iqFactory.createIQ(mergedIQ.getProjectionAtom(), optimizedMergedIQWithFilter);
                            builder.put((RDFAtomPredicate) newIQ.getProjectionAtom().getPredicate(), iriTemplate, newIQ);
                        });
            }

            ImmutableTable<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitionsFromIRITemplate = builder.build();
            this.compatibleDefinitionsFromIRITemplate = compatibleDefinitionsFromIRITemplate;
            isCompatibleDefinitionComputed = true;
        }
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
    public ImmutableCollection<IQ> getQueries(RDFAtomPredicate rdfAtomPredicate) {
        return Stream.concat(classDefinitions.row(rdfAtomPredicate).values().stream(),
                    propertyDefinitions.row(rdfAtomPredicate).values().stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableSet<RDFAtomPredicate> getRDFAtomPredicates() {
        return Sets.union(propertyDefinitions.rowKeySet(), classDefinitions.rowKeySet())
                .immutableCopy();
    }

    @Override
    public Optional<IQ> getCompatibleDefinitionsFromIRITemplate(RDFAtomPredicate rdfAtomPredicate, ObjectStringTemplateFunctionSymbol template){
        return Optional.ofNullable(compatibleDefinitionsFromIRITemplate.get(rdfAtomPredicate, template));
    }
}