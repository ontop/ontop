package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopOBDAConfigurationImpl;
import it.unibz.inf.ontop.model.DBMetadata;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface OntopOBDAConfiguration extends OntopModelConfiguration {

    @Override
    OntopOBDASettings getSettings();

    @Deprecated
    Optional<DBMetadata> getDBMetadata();

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopOBDAConfigurationImpl.BuilderImpl<>();
    }

    interface OntopOBDABuilderFragment<B extends Builder<B>> {

        B dbMetadata(@Nonnull DBMetadata dbMetadata);
    }

    interface Builder<B extends Builder<B>> extends OntopOBDABuilderFragment<B>, OntopModelConfiguration.Builder<B> {

        @Override
        OntopOBDAConfiguration build();
    }

}
