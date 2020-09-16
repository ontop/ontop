package it.unibz.inf.ontop.spec.fact.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import java.util.stream.Stream;

@Singleton
public class FactExtractorWithSaturatedTBox extends AbstractFactExtractor {

    @Inject
    protected FactExtractorWithSaturatedTBox(OntopMappingSettings settings) {
        super(settings);
    }

    @Override
    protected Stream<RDFFact> extractTbox(ClassifiedTBox tbox) {
        throw new RuntimeException("TODO: implement it");
    }
}
