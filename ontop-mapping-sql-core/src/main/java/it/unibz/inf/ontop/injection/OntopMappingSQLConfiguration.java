package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.impl.OntopMappingSQLConfigurationImpl;
import it.unibz.inf.ontop.model.OBDAModel;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

public interface OntopMappingSQLConfiguration extends OntopSQLConfiguration, OntopMappingConfiguration {

    @Override
    OntopMappingSQLSettings getSettings();

    /**
     * Default builder
     */
    static Builder<? extends Builder> defaultBuilder() {
        return new OntopMappingSQLConfigurationImpl.BuilderImpl<>();
    }

    Optional<OBDAModel> loadPPMapping() throws IOException, InvalidMappingException, DuplicateMappingException;

    /**
     * TODO: explain
     */
    interface OntopMappingSQLBuilderFragment<B extends Builder<B>> {

        B obdaModel(@Nonnull OBDAModel obdaModel);
    }

    interface Builder<B extends Builder<B>> extends OntopMappingSQLBuilderFragment<B>,
            OntopSQLConfiguration.Builder<B>, OntopMappingConfiguration.Builder<B> {

        @Override
        OntopMappingSQLConfiguration build();
    }
}


