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
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.FactIntoMappingConverter;
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


public class ABoxFactIntoMappingConverterImpl implements FactIntoMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABoxFactIntoMappingConverterImpl.class);

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final VariableGenerator projectedVariableGenerator;
    private final TypeFactory typeFactory;
    private final DBTypeFactory dbTypeFactory;

    private final DistinctVariableOnlyDataAtom tripleAtom;
    private final RDFAtomPredicate tripleAtomPredicate;
    private final DistinctVariableOnlyDataAtom quadAtom;
    private final RDFAtomPredicate quadAtomPredicate;

    private final IRIConstant RDF_TYPE;
    private final RDFTermType RDFTERMTYPE_TEXT;

    @Inject
    protected ABoxFactIntoMappingConverterImpl(TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                               SubstitutionFactory substitutionFactory, AtomFactory atomFactory,
                                               CoreUtilsFactory coreUtilsFactory,
                                               TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.typeFactory = typeFactory;
        this.dbTypeFactory = typeFactory.getDBTypeFactory();

        RDF_TYPE = termFactory.getConstantIRI(RDF.TYPE);
        RDFTERMTYPE_TEXT = typeFactory.getXsdStringDatatype();

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
    public ImmutableList<MappingAssertion> convert(ImmutableSet<RDFFact> facts) {
        // Group facts by class name or property name (for properties != rdf:type), by isClass, by isQuad.
        ImmutableMap<CustomKey, ImmutableList<RDFFact>> dict = facts.stream()
                .collect(ImmutableCollectors.toMap(
                        fact -> new CustomKey(
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

    private MappingAssertionIndex getMappingAssertionIndex(Entry<CustomKey, ImmutableList<RDFFact>> entry) {
        CustomKey key = entry.getKey();
        if (key.graphOptional.isPresent()) {
            return key.isClass
                    ? MappingAssertionIndex.ofClass(quadAtomPredicate,
                    Optional.of(key.classOrProperty)
                            .filter(c -> c instanceof IRIConstant)
                            .map(c -> ((IRIConstant) c).getIRI())
                            .orElseThrow(() -> new RuntimeException(
                                    "TODO: support bnode for classes as mapping assertion index")))
                    : MappingAssertionIndex.ofProperty(quadAtomPredicate,
                    ((IRIConstant) key.classOrProperty).getIRI());
        } else {
            return key.isClass
                    ? MappingAssertionIndex.ofClass(tripleAtomPredicate,
                    Optional.of(key.classOrProperty)
                            .filter(c -> c instanceof IRIConstant)
                            .map(c -> ((IRIConstant) c).getIRI())
                            .orElseThrow(() -> new RuntimeException(
                                    "TODO: support bnode for classes as mapping assertion index")))
                    : MappingAssertionIndex.ofProperty(tripleAtomPredicate,
                    ((IRIConstant) key.classOrProperty).getIRI());
        }
    }

    private IQ createIQ (Entry < CustomKey, ImmutableList < RDFFact >> entry){
        return entry.getKey().isClass
                ? createClassIQ(entry)
                : createPropertyIQ(entry);
    }

    private IQ createClassIQ (Entry < CustomKey, ImmutableList < RDFFact >> entry){
        if (containsMultipleSubjectTypes(entry)) {
            LOGGER.debug("This should only be reached if blank nodes are accepted.");
            return createMultiTypedClassIQ(entry);
        }

        CustomKey customKey = entry.getKey();

        // We've already excluded multiple types
        ValuesNode valuesNode = createSingleTypeDBValuesNode(entry);
        DBTermType subjectDBType = entry.getValue().get(0).getSubject().getType().getClosestDBType(dbTypeFactory);
        RDFTermTypeConstant subjectRDFTypeConstant = termFactory.getRDFTermTypeConstant(entry.getValue().get(0).getSubject().getType());

        ConstructionNode topConstructionNode = customKey.graphOptional.map(
                graph -> iqFactory.createConstructionNode(
                            quadAtom.getVariables(), substitutionFactory.getSubstitution(
                                quadAtom.getTerm(0),
                                    termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversionFromRDFLexical2DB(
                                            subjectDBType,
                                            valuesNode.getOrderedVariables().get(0),
                                            subjectRDFTypeConstant.getRDFTermType()),
                                        subjectRDFTypeConstant),
                                quadAtom.getTerm(1), RDF_TYPE,
                                quadAtom.getTerm(2), customKey.classOrProperty,
                                quadAtom.getTerm(3), graph)))
                .orElseGet(() ->
                        iqFactory.createConstructionNode(
                            tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                                tripleAtom.getTerm(0),
                                    termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversionFromRDFLexical2DB(
                                            subjectDBType,
                                            valuesNode.getOrderedVariables().get(0),
                                            subjectRDFTypeConstant.getRDFTermType()),
                                        subjectRDFTypeConstant),
                                tripleAtom.getTerm(1), RDF_TYPE,
                                tripleAtom.getTerm(2), customKey.classOrProperty)));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }

    private IQ createPropertyIQ (Entry < CustomKey, ImmutableList < RDFFact >> entry){
        if (containsMultipleObjectTypes(entry) || containsMultipleSubjectTypes(entry)) {
            return createMultiTypedPropertyIQ(entry);
        }

        CustomKey customKey = entry.getKey();

        // We've already excluded multiple types
        ValuesNode valuesNode = createSingleTypeDBValuesNode(entry);
        DBTermType subjectDBType = entry.getValue().get(0).getSubject().getType().getClosestDBType(dbTypeFactory);
        RDFTermTypeConstant subjectRDFTypeConstant = termFactory.getRDFTermTypeConstant(entry.getValue().get(0).getSubject().getType());
        DBTermType objectDBType = entry.getValue().get(0).getObject().getType().getClosestDBType(dbTypeFactory);
        RDFTermTypeConstant objectRDFTypeConstant = termFactory.getRDFTermTypeConstant(entry.getValue().get(0).getObject().getType());

        ConstructionNode topConstructionNode = customKey.graphOptional.map(
                graph -> iqFactory.createConstructionNode(
                        quadAtom.getVariables(), substitutionFactory.getSubstitution(
                            quadAtom.getTerm(0),
                                termFactory.getRDFFunctionalTerm(
                                    termFactory.getConversionFromRDFLexical2DB(
                                        subjectDBType,
                                        valuesNode.getOrderedVariables().get(0),
                                        subjectRDFTypeConstant.getRDFTermType()),
                                    subjectRDFTypeConstant),
                            quadAtom.getTerm(1), customKey.classOrProperty,
                            quadAtom.getTerm(2),
                                termFactory.getRDFFunctionalTerm(
                                    termFactory.getConversionFromRDFLexical2DB(
                                        objectDBType,
                                        valuesNode.getOrderedVariables().get(1),
                                        objectRDFTypeConstant.getRDFTermType()),
                                    objectRDFTypeConstant),
                            quadAtom.getTerm(3), graph)))
                .orElseGet(() ->
                        iqFactory.createConstructionNode(
                        tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                            tripleAtom.getTerm(0),
                                termFactory.getRDFFunctionalTerm(
                                    termFactory.getConversionFromRDFLexical2DB(
                                        subjectDBType,
                                        valuesNode.getOrderedVariables().get(0),
                                        subjectRDFTypeConstant.getRDFTermType()),
                                    subjectRDFTypeConstant),
                            tripleAtom.getTerm(1), customKey.classOrProperty,
                            tripleAtom.getTerm(2),
                                termFactory.getRDFFunctionalTerm(
                                    termFactory.getConversionFromRDFLexical2DB(
                                        objectDBType,
                                        valuesNode.getOrderedVariables().get(1),
                                        objectRDFTypeConstant.getRDFTermType()),
                                    objectRDFTypeConstant))));


        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }

    /**
     * Returns true if the given list of RDFFacts contains multiple types of object.
     *
     * @param entry, our custom (key, ImList[RDFFact]) entry
     * @return a boolean
     */
    private boolean containsMultipleObjectTypes (Entry < CustomKey, ImmutableList < RDFFact >> entry){
        ImmutableList<RDFFact> rdfFacts = entry.getValue();
        RDFTermType canonicalRDFTermType = rdfFacts.get(0).getObject().getType();
        DBTermType canonicalDBTermType = canonicalRDFTermType.getClosestDBType(dbTypeFactory);

        return rdfFacts.stream()
                .anyMatch(rdfFact -> !(
                        rdfFact.getObject().getType().getClosestDBType(dbTypeFactory).equals(canonicalDBTermType) &&
                        rdfFact.getObject().getType().equals(canonicalRDFTermType)));
    }

    /**
     * Returns true if the given list of RDFFacts contains multiple types of subject.
     *
     * @param entry, our custom (key, ImList[RDFFact]) entry
     * @return a boolean
     */
    private boolean containsMultipleSubjectTypes (Entry < CustomKey, ImmutableList < RDFFact >> entry){
        ImmutableList<RDFFact> rdfFacts = entry.getValue();
        RDFTermType canonicalRDFTermType = rdfFacts.get(0).getSubject().getType();
        DBTermType canonicalDBTermType = canonicalRDFTermType.getClosestDBType(dbTypeFactory);

        return rdfFacts.stream()
                .anyMatch(rdfFact -> !(
                        rdfFact.getSubject().getType().getClosestDBType(dbTypeFactory).equals(canonicalDBTermType) &&
                        rdfFact.getSubject().getType().equals(canonicalRDFTermType)));
    }

    private IQ createMultiTypedClassIQ (Entry < CustomKey, ImmutableList < RDFFact >> entry){
        ValuesNode valuesNode = createMultiTypedDBValuesNode(entry);
        CustomKey customKey = entry.getKey();

        ConstructionNode topConstructionNode = customKey.graphOptional.map(
                graph -> iqFactory.createConstructionNode(
                            quadAtom.getVariables(), substitutionFactory.getSubstitution(
                                quadAtom.getTerm(0),
                                    termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversionFromRDFLexical2DB(
                                            valuesNode.getOrderedVariables().get(0),
                                            RDFTERMTYPE_TEXT),
                                        valuesNode.getOrderedVariables().get(1)),
                                quadAtom.getTerm(1), RDF_TYPE,
                                quadAtom.getTerm(2), customKey.classOrProperty,
                                quadAtom.getTerm(3), graph)))
                .orElseGet(() ->
                        iqFactory.createConstructionNode(
                            tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                                tripleAtom.getTerm(0),
                                    termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversionFromRDFLexical2DB(
                                            valuesNode.getOrderedVariables().get(0),
                                                RDFTERMTYPE_TEXT),
                                            valuesNode.getOrderedVariables().get(1)),
                                tripleAtom.getTerm(1), RDF_TYPE,
                                tripleAtom.getTerm(2), customKey.classOrProperty)));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }



    private IQ createMultiTypedPropertyIQ (Entry < CustomKey, ImmutableList < RDFFact >> entry){
        ValuesNode valuesNode = createMultiTypedDBValuesNode(entry);
        CustomKey customKey = entry.getKey();
        ConstructionNode topConstructionNode = customKey.graphOptional.map(
                graph -> iqFactory.createConstructionNode(
                        quadAtom.getVariables(), substitutionFactory.getSubstitution(
                                quadAtom.getTerm(0),
                                termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversionFromRDFLexical2DB(
                                                valuesNode.getOrderedVariables().get(0),
                                                RDFTERMTYPE_TEXT),
                                        valuesNode.getOrderedVariables().get(2)),
                                quadAtom.getTerm(1), customKey.classOrProperty,
                                quadAtom.getTerm(2),
                                termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversionFromRDFLexical2DB(
                                                valuesNode.getOrderedVariables().get(1),
                                                RDFTERMTYPE_TEXT),
                                        valuesNode.getOrderedVariables().get(3)),
                                quadAtom.getTerm(3), graph)))
                .orElseGet(() ->
                        iqFactory.createConstructionNode(
                                tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                                        tripleAtom.getTerm(0),
                                        termFactory.getRDFFunctionalTerm(
                                                termFactory.getConversionFromRDFLexical2DB(
                                                        valuesNode.getOrderedVariables().get(0),
                                                        RDFTERMTYPE_TEXT),
                                                valuesNode.getOrderedVariables().get(2)),
                                        tripleAtom.getTerm(1), customKey.classOrProperty,
                                        tripleAtom.getTerm(2),
                                        termFactory.getRDFFunctionalTerm(
                                                termFactory.getConversionFromRDFLexical2DB(
                                                        valuesNode.getOrderedVariables().get(1),
                                                        RDFTERMTYPE_TEXT),
                                                valuesNode.getOrderedVariables().get(3)))));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }

    private ValuesNode createSingleTypeDBValuesNode(Entry < CustomKey, ImmutableList < RDFFact >> entry){
        ImmutableList<RDFFact> rdfFacts = entry.getValue();
        // This method is used only in case of a single type existing so this is okay here
        DBTermType subjectDBTermType = rdfFacts.get(0).getSubject().getType().getClosestDBType(dbTypeFactory);
        DBTermType objectDBTermType = rdfFacts.get(0).getObject().getType().getClosestDBType(dbTypeFactory);
        // Two cases, class assertion or not
        return entry.getKey().isClass

                ? iqFactory.createValuesNode(
                ImmutableList.of(
                        projectedVariableGenerator.generateNewVariable()),
                rdfFacts.stream()
                        .map(rdfFact -> ImmutableList.of(
                                (Constant) termFactory.getDBConstant(rdfFact.getSubject().getValue(),
                                        subjectDBTermType)))
                        .collect(ImmutableCollectors.toList()))

                : iqFactory.createValuesNode(
                ImmutableList.of(
                        projectedVariableGenerator.generateNewVariable(),
                        projectedVariableGenerator.generateNewVariable()),
                rdfFacts.stream()
                        .map(rdfFact -> ImmutableList.of(
                                (Constant) termFactory.getDBConstant(rdfFact.getSubject().getValue(),
                                        subjectDBTermType),
                                termFactory.getDBConstant(rdfFact.getObject().getValue(),
                                        objectDBTermType)))
                        .collect(ImmutableCollectors.toList()));
    }

    private ValuesNode createMultiTypedDBValuesNode (Entry < CustomKey, ImmutableList < RDFFact >> entry){
        ImmutableList<RDFFact> rdfFacts = entry.getValue();
        // Two cases, class assertion or not
        return entry.getKey().isClass

                ? iqFactory.createValuesNode(
                ImmutableList.of(
                        projectedVariableGenerator.generateNewVariable(),
                        projectedVariableGenerator.generateNewVariable()),
                rdfFacts.stream()
                        .map(rdfFact -> ImmutableList.of(
                                (Constant) termFactory.getDBConstant(rdfFact.getSubject().getValue(),
                                        rdfFact.getSubject().getType().getClosestDBType(dbTypeFactory)),
                                termFactory.getRDFTermTypeConstant(rdfFact.getSubject().getType())))
                        .collect(ImmutableCollectors.toList()))

                : iqFactory.createValuesNode(
                ImmutableList.of(
                        projectedVariableGenerator.generateNewVariable(),
                        projectedVariableGenerator.generateNewVariable(),
                        projectedVariableGenerator.generateNewVariable(),
                        projectedVariableGenerator.generateNewVariable()),
                rdfFacts.stream()
                        .map(rdfFact -> ImmutableList.of(
                                (Constant) termFactory.getDBConstant(rdfFact.getSubject().getValue(),
                                        rdfFact.getSubject().getType().getClosestDBType(dbTypeFactory)),
                                termFactory.getDBConstant(rdfFact.getObject().getValue(),
                                        rdfFact.getObject().getType().getClosestDBType(dbTypeFactory)),
                                termFactory.getRDFTermTypeConstant(rdfFact.getSubject().getType()),
                                termFactory.getRDFTermTypeConstant(rdfFact.getObject().getType())))
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

    private static class CustomKey {
        public final ObjectConstant classOrProperty;
        public final boolean isClass;
        public final Optional<ObjectConstant> graphOptional;

        private CustomKey(ObjectConstant classOrProperty, boolean isClass, Optional<ObjectConstant> graphOptional) {
            this.classOrProperty = classOrProperty;
            this.isClass = isClass;
            this.graphOptional = graphOptional;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomKey customKey = (CustomKey) o;
            return isClass == customKey.isClass &&
                    Objects.equals(graphOptional, customKey.graphOptional) &&
                    Objects.equals(classOrProperty, customKey.classOrProperty);
        }

        @Override
        public int hashCode() {
            return Objects.hash(classOrProperty, isClass, graphOptional);
        }
    }
}
