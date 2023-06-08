package it.unibz.inf.ontop.spec.fact.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.fact.FactExtractor;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractFactExtractor implements FactExtractor {

    private final OntopMappingSettings settings;

    protected AbstractFactExtractor(OntopMappingSettings settings) {
        this.settings = settings;
    }

    @Override
    public ImmutableSet<RDFFact> extractAndSelect(Optional<Ontology> ontology) {
        // TODO: consider other facts
        return ontology
                .map(o -> Stream.concat(
                        selectABox(o, settings.isOntologyAnnotationQueryingEnabled()),
                        extractTBox(o.tbox()))
                        .collect(ImmutableCollectors.toSet()))
                .orElseGet(ImmutableSet::of);
    }

    protected abstract Stream<RDFFact> extractTBox(ClassifiedTBox tbox);

    protected Stream<RDFFact> selectABox(Ontology ontology, boolean queryAnnotation) {
        if (queryAnnotation)
            return ontology.abox().stream();

        OntologyVocabularyCategory<AnnotationProperty> annotationProperties = ontology.annotationProperties();
        return ontology.abox().stream()
                .filter(f -> !annotationProperties.contains(f.getProperty().getIRI()));
    }
}
