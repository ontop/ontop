package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemSettings;

import java.util.Optional;
import java.util.Properties;

public abstract class DefaultOntopSystemBuilderFragment<B extends OntopSystemConfiguration.Builder<B>>
        implements OntopSystemConfiguration.OntopSystemBuilderFragment<B> {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Boolean> keepPermanentConnection;

    DefaultOntopSystemBuilderFragment() {
        this.keepPermanentConnection = Optional.empty();
    }

    protected abstract B self();

    @Override
    public B keepPermanentDBConnection(boolean keep) {
        this.keepPermanentConnection = Optional.of(keep);
        return self();
    }

    Properties generateProperties() {
        Properties properties = new Properties();
        keepPermanentConnection
                .ifPresent(v -> properties.put(OntopSystemSettings.PERMANENT_DB_CONNECTION, v));
        return properties;
    }
}
