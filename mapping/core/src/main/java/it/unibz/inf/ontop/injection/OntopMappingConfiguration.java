package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopMappingConfigurationImpl;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;

public interface OntopMappingConfiguration extends OntopKGQueryConfiguration {

    Optional<TMappingExclusionConfig> getTmappingExclusions();

    @Override
    OntopMappingSettings getSettings();



    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopMappingConfigurationImpl.BuilderImpl<>();
    }


    interface OntopMappingBuilderFragment<B extends Builder<B>> {

        B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config);

        B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology);

        B enableDefaultDatatypeInference(boolean inferDefaultDatatype);

        B sparqlRulesFile(@Nonnull File file);

        B sparqlRulesFile(@Nonnull String path);

        B sparqlRulesReader(@Nonnull Reader reader);

    }

    interface Builder<B extends Builder<B>> extends OntopMappingBuilderFragment<B>, OntopKGQueryConfiguration.Builder<B> {

        @Override
        OntopMappingConfiguration build();
    }

}
