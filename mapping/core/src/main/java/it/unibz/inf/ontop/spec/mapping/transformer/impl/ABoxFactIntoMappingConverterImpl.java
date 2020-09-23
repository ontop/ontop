package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


public class ABoxFactIntoMappingConverterImpl implements ABoxFactIntoMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABoxFactIntoMappingConverterImpl.class);

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final AtomFactory atomFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final VariableGenerator projectedVariableGenerator;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final DBTypeFactory dbTypeFactory;

    private final DistinctVariableOnlyDataAtom tripleAtom;
    private final RDFAtomPredicate tripleAtomPredicate;
    private final DistinctVariableOnlyDataAtom quadAtom;
    private final RDFAtomPredicate quadAtomPredicate;

    private final IRIConstant RDF_TYPE;
    private final DBTermType DB_TEXT_TYPE;

    @Inject
    protected ABoxFactIntoMappingConverterImpl(TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                               SubstitutionFactory substitutionFactory, AtomFactory atomFactory,
                                               CoreUtilsFactory coreUtilsFactory, FunctionSymbolFactory functionSymbolFactory,
                                               TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.atomFactory = atomFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.dbTypeFactory = typeFactory.getDBTypeFactory();

        RDF_TYPE = termFactory.getConstantIRI(RDF.TYPE);
        DB_TEXT_TYPE = dbTypeFactory.getDBStringType();

        projectedVariableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());

        tripleAtom = atomFactory.getDistinctTripleAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());
        tripleAtomPredicate = (RDFAtomPredicate) tripleAtom.getPredicate();

        quadAtom = atomFactory.getDistinctTripleAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());
        quadAtomPredicate = (RDFAtomPredicate) tripleAtom.getPredicate();
    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableSet<RDFFact> facts, boolean isOntologyAnnotationQueryingEnabled) {
        // Group facts by class name or property name (for properties != rdf:type), by isClass, by isQuad.
        ImmutableMap<OCKey, ImmutableList<RDFFact>> dict = facts.stream()
                .collect(ImmutableCollectors.toMap(
                        fact -> new OCKey(
                                        fact.getClassOrProperty(),
                                        fact.isClassAssertion(),
                                        fact.getGraph()),
                        ImmutableList::of,
                        (a, b) -> Stream.concat(a.stream(), b.stream()).collect(ImmutableCollectors.toList())));

        ImmutableList<MappingAssertion> assertions = dict.entrySet().stream()
                .map(entry -> new MappingAssertion(
                        getMappingAssertionIndex(entry),
                        createIQ(entry),
                        new ABoxFactProvenance(entry.getValue())))
                .collect(ImmutableCollectors.toList());

        LOGGER.debug("Transformed {} rdfFacts into {} mappingAssertions", facts.size(), assertions.size());

        return assertions;
    }

    private MappingAssertionIndex getMappingAssertionIndex(Entry<OCKey, ImmutableList<RDFFact>> entry) {
        if (entry.getKey().graphOptional.isPresent()) {
            return entry.getKey().isClass
                    ? MappingAssertionIndex.ofClass(quadAtomPredicate,
                        Optional.of(entry.getKey().classOrProperty)
                            .filter(c -> c instanceof IRIConstant)
                            .map(c -> ((IRIConstant) c).getIRI())
                            .orElseThrow(() -> new RuntimeException(
                                    "TODO: support bnode for classes as mapping assertion index")))
                    : MappingAssertionIndex.ofProperty(quadAtomPredicate,
                        ((IRIConstant) entry.getKey().classOrProperty).getIRI());    // Can properties also be blank nodes?
        }
        else {
            return entry.getKey().isClass
                    ? MappingAssertionIndex.ofClass(tripleAtomPredicate,
                    Optional.of(entry.getKey().classOrProperty)
                            .filter(c -> c instanceof IRIConstant)
                            .map(c -> ((IRIConstant) c).getIRI())
                            .orElseThrow(() -> new RuntimeException(
                                    "TODO: support bnode for classes as mapping assertion index")))
                    : MappingAssertionIndex.ofProperty(tripleAtomPredicate,
                        ((IRIConstant) entry.getKey().classOrProperty).getIRI());
        }
    }

    private IQ createIQ(Entry<OCKey, ImmutableList<RDFFact>> entry) {
        return entry.getKey().isClass
                    ? createClassIQ(entry)
                    : createPropertyIQ(entry);
    }

    private IQ createClassIQ(Entry<OCKey, ImmutableList<RDFFact>> entry) {
        ValuesNode valuesNode = createDBValuesNode(entry);
        OCKey ocKey = entry.getKey();

        ConstructionNode topConstructionNode = ocKey.graphOptional.map(
                graph -> iqFactory.createConstructionNode(
                            quadAtom.getVariables(), substitutionFactory.getSubstitution(
                                    quadAtom.getTerm(0),
                                        termFactory.getIRIFunctionalTerm(valuesNode.getOrderedVariables().get(0), false),
                                    quadAtom.getTerm(1), RDF_TYPE,
                                    quadAtom.getTerm(2), ocKey.classOrProperty,
                                    quadAtom.getTerm(3), graph)))
                .orElseGet( () ->
                        iqFactory.createConstructionNode(
                            tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                                    tripleAtom.getTerm(0),
                                        termFactory.getIRIFunctionalTerm(valuesNode.getOrderedVariables().get(0), false),
                                    tripleAtom.getTerm(1), RDF_TYPE,
                                    tripleAtom.getTerm(2), ocKey.classOrProperty)));


        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }

    private IQ createPropertyIQ(Entry<OCKey, ImmutableList<RDFFact>> entry) {
        ValuesNode valuesNode = createDBValuesNode(entry);
        OCKey ocKey = entry.getKey();

        DBandRDFType types = new DBandRDFType(entry);
        DBTermType objectDBType = types.getDbTermType();
        RDFTermTypeConstant objectRDFType = types.getRdfTermTypeConstant();

        ConstructionNode topConstructionNode = ocKey.graphOptional.map(
                graph -> iqFactory.createConstructionNode(
                        quadAtom.getVariables(), substitutionFactory.getSubstitution(
                                quadAtom.getTerm(0),
                                termFactory.getIRIFunctionalTerm(valuesNode.getOrderedVariables().get(0), false),
                                quadAtom.getTerm(1), ocKey.classOrProperty,
                                quadAtom.getTerm(2),
                                termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversion2RDFLexical(
                                                objectDBType,
                                                valuesNode.getOrderedVariables().get(1),
                                                objectRDFType.getRDFTermType()),
                                        objectRDFType),
                                quadAtom.getTerm(3), graph)))
                .orElseGet(() ->
                        iqFactory.createConstructionNode(
                                tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                                        tripleAtom.getTerm(0),
                                        termFactory.getIRIFunctionalTerm(valuesNode.getOrderedVariables().get(0), false),
                                        tripleAtom.getTerm(1), ocKey.classOrProperty,
                                        tripleAtom.getTerm(2),
                                        termFactory.getRDFFunctionalTerm(
                                                termFactory.getConversion2RDFLexical(
                                                        objectDBType,
                                                        valuesNode.getOrderedVariables().get(1),
                                                        objectRDFType.getRDFTermType()),
                                                objectRDFType))));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }

    private ValuesNode createDBValuesNode(Entry<OCKey, ImmutableList<RDFFact>> entry) {
        ImmutableList<RDFFact> rdfFacts = entry.getValue();
        // Two cases, class assertion or not
        return entry.getKey().isClass
                ?   iqFactory.createValuesNode(
                        ImmutableList.of(projectedVariableGenerator.generateNewVariable()),
                        rdfFacts.stream()
                            .map(rdfFact -> ImmutableList.of(
                                    (Constant) termFactory.getDBConstant(rdfFact.getSubject().getValue(),
                                            DB_TEXT_TYPE)))
                            .collect(ImmutableCollectors.toList()))
                :   iqFactory.createValuesNode(
                        ImmutableList.of(projectedVariableGenerator.generateNewVariable(),
                                projectedVariableGenerator.generateNewVariable()),
                        rdfFacts.stream()
                            .map(rdfFact -> ImmutableList.of(
                                    (Constant) termFactory.getDBConstant(rdfFact.getSubject().getValue(),
                                            DB_TEXT_TYPE),
                                    termFactory.getDBConstant(rdfFact.getObject().getValue(),
                                            rdfFact.getObject().getType().getClosestDBType(dbTypeFactory))))
                            .collect(ImmutableCollectors.toList()));
    }

    private static class ABoxFactProvenance implements PPMappingAssertionProvenance {
        private final String provenance;

        private ABoxFactProvenance(ImmutableList<RDFFact> rdfFacts) {
            provenance = rdfFacts.toString();
        }

        @Override
        public String getProvenanceInfo() {
            return provenance;
        }
    }

    private static class OCKey {
        public final ObjectConstant classOrProperty;
        public final boolean isClass;
        public final Optional<ObjectConstant> graphOptional;

        private OCKey(ObjectConstant classOrProperty, boolean isClass, Optional<ObjectConstant> graphOptional) {
            this.classOrProperty = classOrProperty;
            this.isClass = isClass;
            this.graphOptional = graphOptional;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OCKey ocKey = (OCKey) o;
            return isClass == ocKey.isClass &&
                    Objects.equals(graphOptional, ocKey.graphOptional) &&
                    Objects.equals(classOrProperty, ocKey.classOrProperty);
        }

        @Override
        public int hashCode() {
            return Objects.hash(classOrProperty, isClass, graphOptional);
        }
    }

    private class DBandRDFType {
        private final DBTermType dbTermType;
        private final RDFTermTypeConstant rdfTermTypeConstant;



        private DBandRDFType(Entry<OCKey, ImmutableList<RDFFact>> entry) {
            ImmutableList<RDFFact> rdfFacts = entry.getValue();
            dbTermType = rdfFacts.get(0).getObject().getType().getClosestDBType(dbTypeFactory);
            rdfTermTypeConstant = termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getObject().getType());
            if (rdfFacts.stream()   // Control that the dbTermType and rdfTermType is valid for each fact
                    .anyMatch(rdfFact -> !(rdfFact.getObject().getType().getClosestDBType(dbTypeFactory).equals(dbTermType) &&
                        termFactory.getRDFTermTypeConstant(rdfFact.getObject().getType()).equals(rdfTermTypeConstant)))) {
                throw new RuntimeException("TODO: Support multiple types in ValuesNode");
            }
        }

        public DBTermType getDbTermType() {
            return dbTermType;
        }

        public RDFTermTypeConstant getRdfTermTypeConstant() {
            return rdfTermTypeConstant;
        }
    }
}
