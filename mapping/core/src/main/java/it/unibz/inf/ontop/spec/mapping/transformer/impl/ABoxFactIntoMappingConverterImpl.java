package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.FactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class ABoxFactIntoMappingConverterImpl implements FactIntoMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABoxFactIntoMappingConverterImpl.class);

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final VariableGenerator projectedVariableGenerator;
    private final DBTypeFactory dbTypeFactory;

    private final FactType CLASS_IN_DEFAULT_GRAPH, PROPERTY_IN_DEFAULT_GRAPH, CLASS_IN_NON_DEFAULT_GRAPH, PROPERTY_IN_NON_DEFAULT_GRAPH;


    @Inject
    protected ABoxFactIntoMappingConverterImpl(TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                               SubstitutionFactory substitutionFactory, AtomFactory atomFactory,
                                               CoreUtilsFactory coreUtilsFactory,
                                               TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.dbTypeFactory = typeFactory.getDBTypeFactory();

        projectedVariableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());

        DistinctVariableOnlyDataAtom tripleAtom = atomFactory.getDistinctTripleAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());

        DistinctVariableOnlyDataAtom quadAtom = atomFactory.getDistinctQuadAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());

        IRIConstant RDF_TYPE = termFactory.getConstantIRI(RDF.TYPE);
        CLASS_IN_DEFAULT_GRAPH = new FactType(
                ImmutableList.of(RDFFact::getSubject),
                (terms, iriConstant) -> ImmutableList.of(terms.get(0), RDF_TYPE, iriConstant),
                tripleAtom);
        PROPERTY_IN_DEFAULT_GRAPH = new FactType(
                ImmutableList.of(RDFFact::getSubject, RDFFact::getObject),
                (terms, iriConstant) -> ImmutableList.of(terms.get(0), iriConstant, terms.get(1)),
                tripleAtom);
        CLASS_IN_NON_DEFAULT_GRAPH = new FactType(ImmutableList.of(
                RDFFact::getSubject, f -> f.getGraph().get()),
                (terms, iriConstant) -> ImmutableList.of(terms.get(0), RDF_TYPE, iriConstant, terms.get(1)),
                quadAtom);
        PROPERTY_IN_NON_DEFAULT_GRAPH = new FactType(
                ImmutableList.of(RDFFact::getSubject, RDFFact::getObject, f -> f.getGraph().get()),
                (terms, iriConstant) -> ImmutableList.of(terms.get(0), iriConstant, terms.get(1), terms.get(2)),
                quadAtom);
    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableSet<RDFFact> facts) {
        ImmutableMultimap<MappingAssertionIndex, RDFFact> dict = facts.stream()
                .collect(ImmutableCollectors.toMultimap(
                        this::getIndex, f -> f));

        ImmutableList<MappingAssertion> assertions = dict.asMap().entrySet().stream()
                .map(entry -> new MappingAssertion(
                        createIQ(entry.getKey(), entry.getValue()),
                        new PPMappingAssertionProvenance() {
                            private final String provenance = entry.getValue().toString();
                            @Override
                            public String getProvenanceInfo() {
                                return provenance;
                            }
                        }))
                .collect(ImmutableCollectors.toList());

        LOGGER.debug("Transformed {} rdfFacts into {} mappingAssertions", facts.size(), assertions.size());

        return assertions;
    }

    private MappingAssertionIndex getIndex(RDFFact fact) {
        RDFAtomPredicate predicate = (RDFAtomPredicate) fact.getGraph()
                .map(g -> CLASS_IN_NON_DEFAULT_GRAPH).orElse(CLASS_IN_DEFAULT_GRAPH)
                .atom
                .getPredicate();

        Optional<IRI> optionalIri = Optional.of(fact.getClassOrProperty())
                .filter(i -> i instanceof IRIConstant)
                .map(i -> (IRIConstant)i)
                .map(IRIConstant::getIRI);

        return fact.isClassAssertion()
                ? MappingAssertionIndex.ofClass(predicate,  optionalIri)
                : MappingAssertionIndex.ofProperty(predicate,  optionalIri);
    }


    private static class FactType {

        final ImmutableList<Function<RDFFact, RDFConstant>> termGetter;
        final BiFunction<ImmutableList<ImmutableFunctionalTerm>, IRIConstant, ImmutableList<ImmutableTerm>> termListConstructor;
        final DistinctVariableOnlyDataAtom atom;

        FactType(ImmutableList<Function<RDFFact, RDFConstant>> termGetter,
                 BiFunction<ImmutableList<ImmutableFunctionalTerm>, IRIConstant, ImmutableList<ImmutableTerm>> termListConstructor,
                 DistinctVariableOnlyDataAtom atom) {
            this.termGetter = termGetter;
            this.termListConstructor = termListConstructor;
            this.atom = atom;
        }
    }

    private FactType getFactType(MappingAssertionIndex index) {
        if (index.getPredicate().getArity() == 3) {
            if (index.isClass())
                return CLASS_IN_DEFAULT_GRAPH;
            else
                return PROPERTY_IN_DEFAULT_GRAPH;
        }
        else {
            if (index.isClass())
                return CLASS_IN_NON_DEFAULT_GRAPH;
            else
                return PROPERTY_IN_NON_DEFAULT_GRAPH;
        }
    }

    private IQ createIQ(MappingAssertionIndex index, Collection<RDFFact> facts) {
        FactType type = getFactType(index);

        final ValuesNode valuesNode;
        final ImmutableList<ImmutableFunctionalTerm> terms;
        if (type.termGetter.stream().anyMatch(e -> containsMultipleTypes(facts, e))) {
            valuesNode = iqFactory.createValuesNode(
                    Stream.concat(type.termGetter.stream(), type.termGetter.stream())
                            .map(e -> projectedVariableGenerator.generateNewVariable()).collect(ImmutableCollectors.toList()),
                    facts.stream()
                            .map(rdfFact -> Stream.concat(
                                            type.termGetter.stream().<Constant>map(e -> termFactory.getDBStringConstant(e.apply(rdfFact).getValue())),
                                            type.termGetter.stream().<Constant>map(e -> termFactory.getRDFTermTypeConstant(e.apply(rdfFact).getType())))
                                    .collect(ImmutableCollectors.toList()))
                            .collect(ImmutableCollectors.toList()));

            ImmutableList<Variable> orderedVariables = valuesNode.getOrderedVariables();
            terms = IntStream.range(0, type.termGetter.size())
                    .mapToObj(i -> termFactory.getRDFFunctionalTerm(orderedVariables.get(i), orderedVariables.get(type.termGetter.size() + i)))
                    .collect(ImmutableCollectors.toList());
        }
        else {
            // We've already excluded multiple types
            valuesNode = iqFactory.createValuesNode(
                    type.termGetter.stream().map(e -> projectedVariableGenerator.generateNewVariable()).collect(ImmutableCollectors.toList()),
                    facts.stream()
                            .map(rdfFact -> type.termGetter.stream()
                                    .map(e -> e.apply(rdfFact))
                                    .map(this::extractNaturalDBValue)
                                    .collect(ImmutableCollectors.toList()))
                            .collect(ImmutableCollectors.toList()));

            ImmutableList<Variable> orderedVariables = valuesNode.getOrderedVariables();
            RDFFact firstFact = facts.iterator().next();
            terms = IntStream.range(0, type.termGetter.size())
                    .mapToObj(i -> getTerm(type.termGetter.get(i).apply(firstFact), orderedVariables.get(i)))
                    .collect(ImmutableCollectors.toList());
        }

        IRIConstant iriConstant = termFactory.getConstantIRI(index.getIri());
        ImmutableList<ImmutableTerm> arguments = type.termListConstructor.apply(terms, iriConstant);

        Substitution<?> substitution = substitutionFactory.getSubstitution(type.atom.getArguments(), arguments);

        return iqFactory.createIQ(type.atom,
                iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(substitution.getDomain(), substitution), valuesNode));

    }


    private ImmutableFunctionalTerm getTerm(RDFConstant constant, Variable variable) {
        RDFTermType type = constant.getType();
        DBTermType dbType = type.getClosestDBType(dbTypeFactory);
        RDFTermTypeConstant rdfTypeConstant = termFactory.getRDFTermTypeConstant(type);
        return termFactory.getRDFFunctionalTerm(
                termFactory.getConversion2RDFLexical(dbType, variable, rdfTypeConstant.getRDFTermType()),
                rdfTypeConstant);
    }


    /**
     * Returns true if the given list of RDFFacts contains multiple types of subject.
     *
     * @return a boolean
     */
    private boolean containsMultipleTypes(Collection<RDFFact> facts, Function<RDFFact, RDFConstant> extractor) {
        RDFFact firstFact = facts.iterator().next();
        RDFTermType firstRDFTermType = extractor.apply(firstFact).getType();

        return facts.stream()
                .map(extractor)
                .anyMatch(c -> !(c.getType().equals(firstRDFTermType)));
    }



    private Constant extractNaturalDBValue(RDFConstant rdfConstant) {
        ImmutableFunctionalTerm functionalTerm = termFactory.getConversionFromRDFLexical2DB(
                termFactory.getDBStringConstant(rdfConstant.getValue()),
                rdfConstant.getType());
        return (Constant) functionalTerm.simplify();
    }
}
