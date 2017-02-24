package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopOBDAConfigurationImpl;
import it.unibz.inf.ontop.model.DBMetadata;

import javax.annotation.Nonnull;

public interface OntopOBDAConfiguration extends OntopModelConfiguration {

    @Override
    OntopOBDASettings getSettings();

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopOBDAConfigurationImpl.BuilderImpl<>();
    }

    interface OntopOBDABuilderFragment<B extends Builder<B>> {

        B sameAsMappings(boolean enable);
    }

    interface Builder<B extends Builder<B>> extends OntopOBDABuilderFragment<B>, OntopModelConfiguration.Builder<B> {

        @Override
        OntopOBDAConfiguration build();
    }

}
