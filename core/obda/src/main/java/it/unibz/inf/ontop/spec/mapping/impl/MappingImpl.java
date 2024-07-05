package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.spec.mapping.impl.MappingImpl.IndexType.*;


public class MappingImpl implements Mapping {

    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyDefinitions;
    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> classDefinitions;
    private final ImmutableSet<ObjectStringTemplateFunctionSymbol> iriTemplateSet;
    private Table<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitionsFromSubjSAC;
    private Table<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitionsFromSubjSPO;
    private Table<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitionsFromObjSPO;
    private boolean isIQAllDefComputed;
    private boolean isIQClassDefComputed;
    private final TermFactory termFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final IntermediateQueryFactory iqFactory;
    private Optional<IQ> optIQAllDef;
    private Optional<IQ> optIQClassDef;

    private final FunctionSymbolFactory functionSymbolFactory;

    public MappingImpl(ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable,
                       ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable,
                       ImmutableSet<ObjectStringTemplateFunctionSymbol> iriTemplateSet,
                       TermFactory termFactory,
                       UnionBasedQueryMerger queryMerger,
                       IntermediateQueryFactory iqFactory,
                       FunctionSymbolFactory functionSymbolFactory) {
        this.propertyDefinitions = propertyTable;
        this.classDefinitions = classTable;
        this.iriTemplateSet = iriTemplateSet;
        this.termFactory = termFactory;
        this.queryMerger = queryMerger;
        this.iqFactory = iqFactory;
        this.compatibleDefinitionsFromSubjSPO = HashBasedTable.create();
        this.compatibleDefinitionsFromObjSPO = HashBasedTable.create();
        this.compatibleDefinitionsFromSubjSAC = HashBasedTable.create();
        this.optIQAllDef = Optional.empty();
        this.isIQAllDefComputed = false;
        this.isIQClassDefComputed = false;
        this.functionSymbolFactory = functionSymbolFactory;
    }

    public enum IndexType{
        SPO_SUBJ_INDEX,
        SPO_OBJ_INDEX,
        SAC_SUBJ_INDEX,
    }

    @Override
    public ImmutableSet<ObjectStringTemplateFunctionSymbol> getIriTemplateSet() {
        return iriTemplateSet;
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
    public Optional<IQ> getCompatibleDefinitions(VariableGenerator variableGenerator, IndexType indexType, RDFAtomPredicate rdfAtomPredicate, ObjectStringTemplateFunctionSymbol template){
        Table<RDFAtomPredicate, ObjectStringTemplateFunctionSymbol, IQ> compatibleDefinitions = null;
        Optional<IQ> optIQConsideredDef = Optional.empty();
        int subjOrObjIndex = -1;
        switch (indexType){
            case SPO_SUBJ_INDEX:
                compatibleDefinitions = compatibleDefinitionsFromSubjSPO;
                subjOrObjIndex = 0;
                optIQConsideredDef = getOptIQAllDef(rdfAtomPredicate);;
                break;
            case SPO_OBJ_INDEX:
                compatibleDefinitions = compatibleDefinitionsFromObjSPO;
                subjOrObjIndex = 2;
                optIQConsideredDef = getOptIQAllDef(rdfAtomPredicate);
                break;
            case SAC_SUBJ_INDEX:
                compatibleDefinitions = compatibleDefinitionsFromSubjSAC;
                subjOrObjIndex = 0;
                optIQConsideredDef = getOptIQClassDef(rdfAtomPredicate);;
                break;
        }
        if (iriTemplateSet.contains(template)){
            Optional<IQ> result = Optional.ofNullable(compatibleDefinitions.get(rdfAtomPredicate, template));
            if (result.isPresent()){
                return result;
            }
            else{
                if (optIQConsideredDef.isPresent()){
                    IQ iqConsideredDef = optIQAllDef.get();
                    Variable var = iqConsideredDef.getProjectionAtom().getArguments().get(subjOrObjIndex);
                    ImmutableExpression strictEquality = termFactory.getStrictEquality(
                            var,
                            termFactory.getIRIFunctionalTerm(termFactory.getImmutableFunctionalTerm(
                                    template,
                                    IntStream.range(0, template.getArity())
                                            .mapToObj(i -> variableGenerator.generateNewVariable())
                                            .collect(ImmutableCollectors.toList()))));
                    IQTree prunedIQTree = iqConsideredDef.getTree().propagateDownConstraint(strictEquality, variableGenerator).normalizeForOptimization(variableGenerator);
                    IQ prunedIQ = iqFactory.createIQ(iqConsideredDef.getProjectionAtom(), prunedIQTree);
                    compatibleDefinitions.put(rdfAtomPredicate, template, prunedIQ);
                    return Optional.of(compatibleDefinitions.get(rdfAtomPredicate, template));
                }
                else {
                    return Optional.empty();
                }
            }
        }
        else{
            return Optional.empty();
        }
    }

    @Override
    public Optional<IQ> getOptIQAllDef(RDFAtomPredicate rdfAtomPredicate) {
        if (!isIQAllDefComputed) {
            ImmutableCollection<IQ> allDef = getQueries(rdfAtomPredicate);
            optIQAllDef = queryMerger.mergeDefinitions(allDef);
            isIQAllDefComputed = true;
        }
        return optIQAllDef;
    }

    @Override
    public Optional<IQ> getOptIQClassDef(RDFAtomPredicate rdfAtomPredicate) {
        if(!isIQClassDefComputed){
            optIQClassDef = queryMerger.mergeDefinitions(classDefinitions.row(rdfAtomPredicate).values().stream().collect(ImmutableCollectors.toList()));
            isIQClassDefComputed = true;
        }
        return optIQClassDef;
    }
}