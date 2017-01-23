package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.impl.OntopMappingConfigurationImpl;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

public interface OntopMappingConfiguration extends OntopOBDAConfiguration {

    Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader();

    @Override
    OntopMappingSettings getSettings();

    /**
     * TODO: is it necessary?
     *
     */
    Optional<OBDASpecification> loadSpecification() throws IOException, OBDASpecificationException;

    /**
     * Only call it if you are sure that mapping assertions have been provided
     */
    default OBDASpecification loadProvidedSpecification() throws IOException, OBDASpecificationException {
        return loadSpecification()
                .orElseThrow(() -> new IllegalStateException("No OBDA specification has been provided. " +
                        "Do not call this method unless you are sure of the specification provision."));
    }



    static Builder<? extends Builder> defaultBuilder() {
        return new OntopMappingConfigurationImpl.BuilderImpl<>();
    }


    interface OntopMappingBuilderFragment<B extends Builder<B>> {

        B obdaSpecification(@Nonnull OBDASpecification obdaSpecification);

        B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints);

        B enableFullMetadataExtraction(boolean obtainFullMetadata);

        B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology);

    }

    interface Builder<B extends Builder<B>> extends OntopMappingBuilderFragment<B>, OntopOBDAConfiguration.Builder<B> {

        @Override
        OntopMappingConfiguration build();
    }

}
