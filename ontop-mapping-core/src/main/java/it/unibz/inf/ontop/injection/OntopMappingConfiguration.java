package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface OntopMappingConfiguration extends OntopOBDAConfiguration {


    Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader();

    @Override
    OntopMappingSettings getSettings();

    interface OntopMappingBuilderFragment<B extends Builder> {

        B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints);

        B enableFullMetadataExtraction(boolean obtainFullMetadata);

    }

    interface Builder<B extends Builder> extends OntopMappingBuilderFragment<B>, OntopOBDAConfiguration.Builder<B> {

        @Override
        OntopMappingConfiguration build();
    }

}
