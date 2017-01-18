package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.impl.OntopMappingConfigurationImpl;
import it.unibz.inf.ontop.mapping.extraction.DataSourceModel;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

public interface OntopMappingConfiguration extends OntopOBDAConfiguration {

    Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader();

    @Override
    OntopMappingSettings getSettings();

    /**
     * TODO: explain
     */
    Optional<DataSourceModel> loadDataSourceModel() throws IOException, InvalidMappingException;

    /**
     * Only call it if you are sure that mapping assertions have been provided
     */
    default DataSourceModel loadProvidedDataSourceModel() throws IOException, InvalidMappingException {
        return loadDataSourceModel()
                .orElseThrow(() -> new IllegalStateException("No mapping has been provided. " +
                        "Do not call this method unless you are sure of the mapping provision."));
    }



    static Builder<? extends Builder> defaultBuilder() {
        return new OntopMappingConfigurationImpl.BuilderImpl<>();
    }


    interface OntopMappingBuilderFragment<B extends Builder<B>> {

        B dataSourceModel(@Nonnull DataSourceModel dataSourceModel);

        B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints);

        B enableFullMetadataExtraction(boolean obtainFullMetadata);

    }

    interface Builder<B extends Builder<B>> extends OntopMappingBuilderFragment<B>, OntopOBDAConfiguration.Builder<B> {

        @Override
        OntopMappingConfiguration build();
    }

}
