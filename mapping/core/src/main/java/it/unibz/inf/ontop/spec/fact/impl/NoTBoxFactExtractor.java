package it.unibz.inf.ontop.spec.fact.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import java.util.stream.Stream;

/**
 * Does not extract any T-box axioms
 */
@Singleton
public class NoTBoxFactExtractor extends AbstractFactExtractor {

    @Inject
    protected NoTBoxFactExtractor(OntopMappingSettings settings) {
        super(settings);
    }

    @Override
    protected Stream<RDFFact> extractTBox(ClassifiedTBox tbox) {
        return Stream.empty();
    }
}
