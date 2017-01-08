package it.unibz.inf.ontop.injection;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.impl.OntopModelConfigurationImpl;
import it.unibz.inf.ontop.pivotalrepr.OptimizationConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Properties;

/**
 * TODO: explain
 */
public interface OntopModelConfiguration {

    OntopModelProperties getProperties();

    OptimizationConfiguration getOptimizationConfiguration();

    Injector getInjector();

    void validate() throws InvalidOntopConfigurationException;

    /**
     * Default builder
     */
    static Builder<Builder> defaultBuilder() {
        return new OntopModelConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: explain
     */
    interface OntopModelBuilderFragment<B extends Builder> {

        B properties(@Nonnull Properties properties);
        B propertyFile(String propertyFilePath);
        B propertyFile(File propertyFile);
        B enableTestMode();

        // TODO: enable it later
        // B cardinalityPreservationMode(OntopModelProperties.CardinalityPreservationMode mode);
    }

    /**
     * TODO: explain
     */
    interface Builder<B extends Builder> extends OntopModelBuilderFragment<B> {

        OntopModelConfiguration build();
    }
}
