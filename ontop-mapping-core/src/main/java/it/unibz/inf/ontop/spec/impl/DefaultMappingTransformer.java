package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.spec.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.util.Optional;

@Singleton
public class DefaultMappingTransformer implements MappingTransformer {

    @Inject
    private DefaultMappingTransformer() {
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata,
                                       Optional<Ontology> optionalOntology) throws MappingException, OntologyException {
        throw new RuntimeException("TODO: implement DefaultMappingTransformer.transform()");
    }
}
