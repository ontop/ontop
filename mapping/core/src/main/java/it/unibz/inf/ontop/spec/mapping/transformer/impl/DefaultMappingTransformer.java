package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.DisjunctionOfEqualitiesMergingSimplifier;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.impl.MappingImpl;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.spec.rule.RuleExecutor;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.injection.OntopModelSettings.CardinalityPreservationMode.LOOSE;

public class DefaultMappingTransformer implements MappingTransformer {

    private final MappingVariableNameNormalizer mappingVariableNameNormalizer;
    private final MappingSaturator mappingSaturator;
    private final FactIntoMappingConverter factConverter;
    private final OntopMappingSettings settings;
    private final MappingSameAsInverseRewriter sameAsInverseRewriter;
    private final SpecificationFactory specificationFactory;
    private final RDF rdfFactory;

    private final MappingDistinctTransformer mappingDistinctTransformer;
    private final MappingValuesWrapper mappingValuesWrapper;

    private final TermFactory termFactory;
    private final RuleExecutor ruleExecutor;

    private final DisjunctionOfEqualitiesMergingSimplifier disjunctionOfEqualitiesMergingSimplifier;

    private final UnionBasedQueryMerger queryMerger;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private DefaultMappingTransformer(MappingVariableNameNormalizer mappingVariableNameNormalizer,
                                      MappingSaturator mappingSaturator,
                                      FactIntoMappingConverter inserter,
                                      OntopMappingSettings settings,
                                      MappingSameAsInverseRewriter sameAsInverseRewriter,
                                      SpecificationFactory specificationFactory,
                                      RDF rdfFactory,
                                      MappingDistinctTransformer mappingDistinctTransformer,
                                      MappingValuesWrapper mappingValuesWrapper,
                                      DisjunctionOfEqualitiesMergingSimplifier disjunctionOfEqualitiesMergingSimplifier,
                                      TermFactory termFactory,
                                      RuleExecutor ruleExecutor,
                                      UnionBasedQueryMerger queryMerger,
                                      IntermediateQueryFactory iqFactory) {
        this.mappingVariableNameNormalizer = mappingVariableNameNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.specificationFactory = specificationFactory;
        this.rdfFactory = rdfFactory;
        this.mappingDistinctTransformer = mappingDistinctTransformer;
        this.mappingValuesWrapper = mappingValuesWrapper;
        this.disjunctionOfEqualitiesMergingSimplifier = disjunctionOfEqualitiesMergingSimplifier;
        this.termFactory = termFactory;
        this.ruleExecutor = ruleExecutor;
        this.queryMerger = queryMerger;
        this.iqFactory = iqFactory;
    }

    @Override
    public OBDASpecification transform(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters,
                                       Optional<Ontology> optionalOntology, ImmutableSet<RDFFact> facts,
                                       ImmutableList<IQ> rules) {

        ImmutableList<MappingAssertion> factsAsMapping = factConverter.convert(facts);
        ImmutableList<MappingAssertion> mappingWithFacts =
                Stream.concat(mapping.stream(), factsAsMapping.stream()).collect(ImmutableCollectors.toList());

        Ontology ontology = optionalOntology.orElseGet(() -> OntologyBuilderImpl.builder(rdfFactory, termFactory).build());

        ImmutableList<MappingAssertion> sameAsRewrittenMapping = sameAsInverseRewriter.rewrite(mappingWithFacts);
        ImmutableList<MappingAssertion> saturatedMapping = mappingSaturator.saturate(sameAsRewrittenMapping, ontology.tbox());

        ImmutableList<MappingAssertion> simplifiedBooleanExpressionsMapping = saturatedMapping.stream()
                .map(m -> m.copyOf(disjunctionOfEqualitiesMergingSimplifier.optimize(m.getQuery())))
                .collect(ImmutableCollectors.toList());

        ImmutableList<MappingAssertion> mappingAfterApplyingRules = ruleExecutor.apply(simplifiedBooleanExpressionsMapping, rules);
        ImmutableList<MappingAssertion> mappingWithNormalizedVarNames = mappingVariableNameNormalizer.normalize(mappingAfterApplyingRules);

        // Don't insert the distinct if the cardinality preservation is set to LOOSE
        ImmutableList<MappingAssertion> mappingWithRightCardinality = settings.getCardinalityPreservationMode() == LOOSE
                ? mappingWithNormalizedVarNames
                : mappingDistinctTransformer.addDistinct(mappingWithNormalizedVarNames);

        ImmutableList<MappingAssertion> finalMapping = mappingValuesWrapper.normalize(mappingWithRightCardinality, dbParameters);

        return specificationFactory.createSpecification(getMapping(finalMapping), dbParameters, ontology.tbox());
    }

    private Mapping getMapping(ImmutableList<MappingAssertion> assertions) {
        ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyDefinitions = assertions.stream()
                .filter(e -> !e.getIndex().isClass())
                .map(DefaultMappingTransformer::asCell)
                .collect(ImmutableCollectors.toTable());

        ImmutableTable<RDFAtomPredicate, IRI, IQ> classDefinitions = assertions.stream()
                .filter(e -> e.getIndex().isClass())
                .map(DefaultMappingTransformer::asCell)
                .collect(ImmutableCollectors.toTable());


        return new MappingImpl(propertyDefinitions, classDefinitions, this.termFactory.getDBFunctionSymbolFactory().getIriTemplateSet(), termFactory, queryMerger, iqFactory);
    }

    private static Table.Cell<RDFAtomPredicate, IRI, IQ> asCell(MappingAssertion assertion) {
        MappingAssertionIndex index = assertion.getIndex();
        return Tables.immutableCell(index.getPredicate(), index.getIri(), assertion.getQuery());
    }
}
