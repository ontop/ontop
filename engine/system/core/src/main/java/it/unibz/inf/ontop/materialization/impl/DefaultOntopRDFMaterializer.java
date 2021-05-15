package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.model.atom.QuadPredicate;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;


/**
 * Allows you to materialize the virtual RDF graph of an OBDA specification.
 *
 * @author Mariano Rodriguez Muro (initial version was called QuestMaterializer)
 */
public class DefaultOntopRDFMaterializer implements OntopRDFMaterializer {

    private final MaterializationParams params;
    private final InputQueryFactory inputQueryFactory;
    private final OntopQueryEngine queryEngine;

    private final ImmutableMap<IRI, VocabularyEntry> vocabulary;
    private final TermFactory termFactory;
    private final RDF rdfFactory;

    public DefaultOntopRDFMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
        Injector injector = configuration.getInjector();
        OntopSystemFactory engineFactory = injector.getInstance(OntopSystemFactory.class);

        OBDASpecification specification = configuration.loadSpecification();
        this.queryEngine = engineFactory.create(specification);
        this.inputQueryFactory = injector.getInstance(InputQueryFactory.class);
        this.termFactory = injector.getInstance(TermFactory.class);
        this.rdfFactory = injector.getInstance(RDF.class);
        this.vocabulary = extractVocabulary(specification.getSaturatedMapping());

        this.params = materializationParams;
    }

    @Override
    public MaterializedGraphResultSet materialize() {
        return new DefaultMaterializedGraphResultSet(vocabulary, params, queryEngine, inputQueryFactory, termFactory, rdfFactory);
    }

    @Override
    public MaterializedGraphResultSet materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary) {
        return new DefaultMaterializedGraphResultSet(filterVocabularyEntries(selectedVocabulary), params, queryEngine, inputQueryFactory, termFactory, rdfFactory);
    }

    private ImmutableMap<IRI, VocabularyEntry> filterVocabularyEntries(ImmutableSet<IRI> selectedVocabulary) {
        return vocabulary.entrySet().stream()
                .filter(e -> selectedVocabulary.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap());
    }

    @Override
    public ImmutableSet<IRI> getClasses() {
        return vocabulary.entrySet().stream()
                .filter(e -> e.getValue().arity == 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<IRI> getProperties() {
        return vocabulary.entrySet().stream()
                .filter(e -> e.getValue().arity == 2)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());
    }

    private static ImmutableMap<IRI, VocabularyEntry> extractVocabulary(@Nonnull Mapping mapping) {
        Map<IRI, VocabularyEntry> result = new HashMap<>();
        for (RDFAtomPredicate predicate : mapping.getRDFAtomPredicates()) {
            if (predicate instanceof TriplePredicate || predicate instanceof QuadPredicate)
                result.putAll(extractTripleVocabulary(mapping, predicate)
                        .collect(ImmutableCollectors.toMap(e -> e.name, e -> e)));
        }
        return ImmutableMap.copyOf(result);
    }

    private static Stream<VocabularyEntry> extractTripleVocabulary(Mapping mapping, RDFAtomPredicate tripleOrQuadPredicate) {
        Stream<VocabularyEntry> vocabularyPropertyStream = mapping.getRDFProperties(tripleOrQuadPredicate).stream()
                .map(p -> new VocabularyEntry(p, 2));

        Stream<VocabularyEntry> vocabularyClassStream = mapping.getRDFClasses(tripleOrQuadPredicate).stream()
                .map(p -> new VocabularyEntry(p, 1));
        return Stream.concat(vocabularyClassStream, vocabularyPropertyStream);
    }
}
